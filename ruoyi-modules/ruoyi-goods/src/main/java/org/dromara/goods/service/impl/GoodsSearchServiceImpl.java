package org.dromara.goods.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.goods.domain.CategoryFrontend;
import org.dromara.goods.domain.bo.GoodsSearchBo;
import org.dromara.goods.domain.dto.GoodsEsIndexDTO;
import org.dromara.goods.domain.vo.CategoryFilterVo;
import org.dromara.goods.domain.vo.GoodsSearchResultVo;
import org.dromara.goods.mapper.CategoryFrontendMapper;
import org.dromara.goods.constant.GoodsEsConstants;
import org.dromara.goods.service.IGoodsSearchService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * PC 商城商品列表搜索（RestHighLevelClient）
 * <p>
 * 查询字段使用索引 snake_case（与 GoodsEsIndexDTO @JsonProperty / mapping 一致）
 *
 * @author ljs
 * @date 2026-07-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsSearchServiceImpl implements IGoodsSearchService {

    private static final String AGG_CATEGORY_FRONTEND = "agg_category_frontend";

    private final ObjectProvider<RestHighLevelClient> restHighLevelClient;
    private final ObjectMapper objectMapper;
    private final CategoryFrontendMapper categoryFrontendMapper;

    @Override
    public GoodsSearchResultVo searchGoodsList(GoodsSearchBo bo) {
        RestHighLevelClient client = restHighLevelClient.getIfAvailable();
        if (client == null) {
            throw new ServiceException("商品搜索服务未启用");
        }

        SearchRequest searchRequest = new SearchRequest(GoodsEsConstants.INDEX_GOODS_SKU);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder baseQuery = QueryBuilders.boolQuery();

        // 基础逻辑删除与上下架状态过滤 (适配 Mapping 中的 byte 类型，传数值 0/1)
        baseQuery.filter(QueryBuilders.termQuery("del_flag", 0));
        baseQuery.filter(QueryBuilders.termQuery("sale_status", 1));
        baseQuery.filter(QueryBuilders.termQuery("audit_status", 2));
        baseQuery.filter(QueryBuilders.termQuery("sku_sale_status", 1));

        if (StrUtil.isNotBlank(bo.getKeyword())) {
            baseQuery.must(QueryBuilders.multiMatchQuery(
                bo.getKeyword(),
                "title", "spu_code", "sku_code", "brand_name"
            ));
        }


        if (bo.getBrandId() != null) {
            baseQuery.filter(QueryBuilders.termQuery("brand_id", bo.getBrandId()));
        }


        if (bo.getMinPrice() != null || bo.getMaxPrice() != null) {
            var rangeQuery = QueryBuilders.rangeQuery("price_sales");
            if (bo.getMinPrice() != null) {
                rangeQuery.gte(bo.getMinPrice());
            }
            if (bo.getMaxPrice() != null) {
                rangeQuery.lte(bo.getMaxPrice());
            }
            baseQuery.filter(rangeQuery);
        }

        if (Boolean.TRUE.equals(bo.getOnlyInStock())) {
            baseQuery.filter(QueryBuilders.termQuery("has_stock", 1));
        }

        sourceBuilder.query(baseQuery);


        if (bo.getCategoryFrontendId() != null) {
            sourceBuilder.postFilter(QueryBuilders.termQuery("category_frontend_id", bo.getCategoryFrontendId()));
        }

        sourceBuilder.sort("has_stock", SortOrder.DESC);

        SortOrder order = "asc".equalsIgnoreCase(bo.getSortOrder()) ? SortOrder.ASC : SortOrder.DESC;
        if ("sales".equalsIgnoreCase(bo.getSortField())) {
            sourceBuilder.sort("sales_volume", order);
            sourceBuilder.sort("create_time", SortOrder.DESC);
        } else if ("price".equalsIgnoreCase(bo.getSortField())) {
            sourceBuilder.sort("price_sales", order);
            sourceBuilder.sort("create_time", SortOrder.DESC);
        } else if ("time".equalsIgnoreCase(bo.getSortField())) {
            sourceBuilder.sort("create_time", order);
        } else {
            sourceBuilder.sort("_score", SortOrder.DESC);
            sourceBuilder.sort("create_time", SortOrder.DESC);
        }

        int pageNum = Math.max(1, bo.getPageNum() == null ? 1 : bo.getPageNum());
        int pageSize = Math.max(1, bo.getPageSize() == null ? 20 : bo.getPageSize());
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);

        TermsAggregationBuilder categoryAgg = AggregationBuilders
            .terms(AGG_CATEGORY_FRONTEND)
            .field("category_frontend_id")
            .size(1000);
        sourceBuilder.aggregation(categoryAgg);

        searchRequest.source(sourceBuilder);

        GoodsSearchResultVo resultVO = new GoodsSearchResultVo();
        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            long total = response.getHits().getTotalHits() != null ? response.getHits().getTotalHits().value : 0L;
            resultVO.setTotal(total);
            resultVO.setTotalPages((int) Math.ceil((double) total / pageSize));

            List<GoodsEsIndexDTO> goodsList = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                goodsList.add(objectMapper.readValue(hit.getSourceAsString(), GoodsEsIndexDTO.class));
            }
            resultVO.setGoodsList(goodsList);

            resultVO.setCategoryFilters(buildCategoryFilters(response));
        } catch (IOException e) {
            log.error("【商品搜索】ES 查询失败, bo={}", bo, e);
            resultVO.setTotal(0L);
            resultVO.setTotalPages(0);
            resultVO.setGoodsList(Collections.emptyList());
            resultVO.setCategoryFilters(Collections.emptyList());
        }
        return resultVO;
    }

    /**
     * 批量解析分类聚合，并在内存中完成名称映射
     */
    private List<CategoryFilterVo> buildCategoryFilters(SearchResponse response) {
        if (response.getAggregations() == null) {
            return Collections.emptyList();
        }
        ParsedLongTerms aggTerms = response.getAggregations().get(AGG_CATEGORY_FRONTEND);
        if (aggTerms == null || CollUtil.isEmpty(aggTerms.getBuckets())) {
            return Collections.emptyList();
        }

        List<Long> categoryIds = new ArrayList<>();
        Map<Long, Long> categoryCountMap = new HashMap<>();
        for (var bucket : aggTerms.getBuckets()) {
            Long catId = bucket.getKeyAsNumber().longValue();
            categoryIds.add(catId);
            categoryCountMap.put(catId, bucket.getDocCount());
        }

        List<CategoryFrontend> categoryList = categoryFrontendMapper.selectList(
            Wrappers.lambdaQuery(CategoryFrontend.class).in(CategoryFrontend::getId, categoryIds));
        Map<Long, String> catNameMap = CollUtil.isEmpty(categoryList) ? Collections.emptyMap()
            : categoryList.stream().collect(Collectors.toMap(
            CategoryFrontend::getId,
            CategoryFrontend::getFrontCategoryName,
            (a, b) -> a
        ));

        return categoryIds.stream()
            .filter(catNameMap::containsKey)
            .map(catId -> new CategoryFilterVo(catId, catNameMap.get(catId), categoryCountMap.get(catId)))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
