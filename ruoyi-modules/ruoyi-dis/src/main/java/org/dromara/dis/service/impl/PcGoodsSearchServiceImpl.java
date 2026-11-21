package org.dromara.dis.service.impl;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.dromara.dis.api.mq.DisGoodsMqConstants;
import org.dromara.dis.domain.bo.BuyerContextBo;
import org.dromara.dis.domain.dto.PcGoodsSearchParamDTO;
import org.dromara.dis.domain.vo.PcFilterAttrOptionVO;
import org.dromara.dis.domain.vo.PcFilterAttrVO;
import org.dromara.dis.domain.vo.PcFilterBrandVO;
import org.dromara.dis.domain.vo.PcGoodsSearchItem;
import org.dromara.dis.domain.vo.PcGoodsSearchResponseVO;
import org.dromara.dis.service.IBuyerAccessService;
import org.dromara.dis.service.IPcGoodsSearchService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * PC 渠道商品列表：dis_goods + 多渠道 IN + 品牌/属性聚合
 *
 * @author ljs
 * @date 2026-08-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PcGoodsSearchServiceImpl implements IPcGoodsSearchService {

    private final RestHighLevelClient restHighLevelClient;
    private final IBuyerAccessService buyerAccessService;
    private final ObjectMapper objectMapper;

    @Override
    public PcGoodsSearchResponseVO searchGoodsList(PcGoodsSearchParamDTO param) {
        int pageNum = Math.max(1, param.getPageNum() == null ? 1 : param.getPageNum());
        int pageSize = Math.min(100, Math.max(1, param.getPageSize() == null ? 20 : param.getPageSize()));
        if (pageNum > 100) {
            return emptyResult(pageNum, pageSize, Collections.singletonList(param.getChannelCode()));
        }

        List<String> actualChannels;
        if (param.getUserinfoId() != null) {
            BuyerContextBo buyerContext = buyerAccessService.getBuyerContext(param.getUserinfoId());
            actualChannels = buyerAccessService.getAccessibleChannels(
                param.getTenantId(), param.getChannelCode(), buyerContext);
        } else {
            actualChannels = Collections.singletonList(param.getChannelCode());
        }

        SearchRequest searchRequest = new SearchRequest(DisGoodsMqConstants.ES_INDEX_DIS_GOODS);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.timeout(new TimeValue(1500, TimeUnit.MILLISECONDS));

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.filter(QueryBuilders.termQuery("tenant_id", param.getTenantId()));
        boolQuery.filter(QueryBuilders.termsQuery("channel_code", actualChannels));
        boolQuery.filter(QueryBuilders.termQuery("sale_status", 1));
        boolQuery.filter(QueryBuilders.termQuery("sku_sale_status", 1));
        boolQuery.filter(QueryBuilders.termQuery("del_flag", 0));

        if (StrUtil.isNotBlank(param.getKeyword())) {
            boolQuery.must(QueryBuilders.multiMatchQuery(param.getKeyword(), "title", "title.pinyin"));
        }
        if (StrUtil.isNotBlank(param.getCategoryPath())) {
            boolQuery.filter(QueryBuilders.termQuery("category_backend_id", param.getCategoryPath()));
        }
        if (param.getBrandId() != null) {
            boolQuery.filter(QueryBuilders.termQuery("brand_id", param.getBrandId()));
        }
        if (param.getMinPrice() != null || param.getMaxPrice() != null) {
            var range = QueryBuilders.rangeQuery("price_sales");
            if (param.getMinPrice() != null) {
                range.gte(param.getMinPrice().doubleValue());
            }
            if (param.getMaxPrice() != null) {
                range.lte(param.getMaxPrice().doubleValue());
            }
            boolQuery.filter(range);
        }
        sourceBuilder.query(boolQuery);

        if (param.getAttrs() != null && !param.getAttrs().isEmpty()) {
            BoolQueryBuilder postFilterBool = QueryBuilders.boolQuery();
            for (String attrStr : param.getAttrs()) {
                if (StrUtil.isBlank(attrStr)) {
                    continue;
                }
                String[] split = attrStr.split("_");
                if (split.length != 2) {
                    continue;
                }
                try {
                    long attrId = Long.parseLong(split[0]);
                    long optionId = Long.parseLong(split[1]);
                    BoolQueryBuilder nestedBool = QueryBuilders.boolQuery();
                    nestedBool.must(QueryBuilders.termQuery("attrs.attr_id", attrId));
                    nestedBool.must(QueryBuilders.termQuery("attrs.option_id", optionId));
                    postFilterBool.filter(QueryBuilders.nestedQuery("attrs", nestedBool, ScoreMode.None));
                } catch (NumberFormatException ignored) {
                    // skip invalid attr filter
                }
            }
            sourceBuilder.postFilter(postFilterBool);
        }

        if ("price".equals(param.getSortField())) {
            SortOrder order = "asc".equalsIgnoreCase(param.getSortOrder()) ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort("price_sales", order);
        } else if ("sales".equals(param.getSortField())) {
            sourceBuilder.sort("sales_volume", SortOrder.DESC);
        } else if ("new".equals(param.getSortField())) {
            sourceBuilder.sort("create_time", SortOrder.DESC);
        } else {
            sourceBuilder.sort("weight", SortOrder.DESC);
            sourceBuilder.sort("sales_volume", SortOrder.DESC);
            sourceBuilder.sort("create_time", SortOrder.DESC);
        }

        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);

        if (param.getBrandId() == null) {
            sourceBuilder.aggregation(AggregationBuilders.terms("brand_agg").field("brand_id")
                .subAggregation(AggregationBuilders.terms("brand_name_agg").field("brand_name")));
        }
        sourceBuilder.aggregation(AggregationBuilders.nested("attrs_nested", "attrs")
            .subAggregation(AggregationBuilders.terms("attr_id_agg").field("attrs.attr_id")
                .subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attr_name"))
                .subAggregation(AggregationBuilders.terms("option_id_agg").field("attrs.option_id")
                    .subAggregation(AggregationBuilders.terms("option_val_agg").field("attrs.option_value")))));

        searchRequest.source(sourceBuilder);

        try {
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            PcGoodsSearchResponseVO vo = new PcGoodsSearchResponseVO();
            vo.setTotal(response.getHits().getTotalHits() == null ? 0L : response.getHits().getTotalHits().value);
            vo.setPageNum(pageNum);
            vo.setPageSize(pageSize);
            vo.setActualChannels(actualChannels);

            List<PcGoodsSearchItem> items = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                items.add(objectMapper.readValue(hit.getSourceAsString(), PcGoodsSearchItem.class));
            }
            vo.setList(items);

            if (response.getAggregations() != null) {
                parseAggregations(response, vo);
            } else {
                vo.setBrands(Collections.emptyList());
                vo.setAttributes(Collections.emptyList());
            }
            return vo;
        } catch (Exception e) {
            log.error("ES 检索异常降级, tenantId: {}, channels: {}", param.getTenantId(), actualChannels, e);
            return emptyResult(pageNum, pageSize, actualChannels);
        }
    }

    private void parseAggregations(SearchResponse response, PcGoodsSearchResponseVO vo) {
        ParsedLongTerms brandAgg = response.getAggregations().get("brand_agg");
        if (brandAgg != null) {
            List<PcFilterBrandVO> brandList = new ArrayList<>();
            for (Terms.Bucket bucket : brandAgg.getBuckets()) {
                if (bucket.getDocCount() <= 0) {
                    continue;
                }
                PcFilterBrandVO bVo = new PcFilterBrandVO();
                bVo.setBrandId(bucket.getKeyAsNumber().longValue());
                ParsedStringTerms brandNameAgg = bucket.getAggregations().get("brand_name_agg");
                if (brandNameAgg != null && !brandNameAgg.getBuckets().isEmpty()) {
                    bVo.setBrandName(brandNameAgg.getBuckets().get(0).getKeyAsString());
                }
                brandList.add(bVo);
            }
            vo.setBrands(brandList);
        } else {
            vo.setBrands(Collections.emptyList());
        }

        ParsedNested attrsNested = response.getAggregations().get("attrs_nested");
        if (attrsNested == null) {
            vo.setAttributes(Collections.emptyList());
            return;
        }
        ParsedLongTerms attrIdAgg = attrsNested.getAggregations().get("attr_id_agg");
        if (attrIdAgg == null) {
            vo.setAttributes(Collections.emptyList());
            return;
        }
        List<PcFilterAttrVO> attrList = new ArrayList<>();
        for (Terms.Bucket attrBucket : attrIdAgg.getBuckets()) {
            if (attrBucket.getDocCount() <= 0) {
                continue;
            }
            PcFilterAttrVO aVo = new PcFilterAttrVO();
            aVo.setAttrId(attrBucket.getKeyAsNumber().longValue());
            ParsedStringTerms attrNameAgg = attrBucket.getAggregations().get("attr_name_agg");
            if (attrNameAgg != null && !attrNameAgg.getBuckets().isEmpty()) {
                aVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());
            }
            ParsedLongTerms optionIdAgg = attrBucket.getAggregations().get("option_id_agg");
            List<PcFilterAttrOptionVO> optList = new ArrayList<>();
            if (optionIdAgg != null) {
                for (Terms.Bucket optBucket : optionIdAgg.getBuckets()) {
                    if (optBucket.getDocCount() <= 0) {
                        continue;
                    }
                    PcFilterAttrOptionVO oVo = new PcFilterAttrOptionVO();
                    oVo.setOptionId(optBucket.getKeyAsNumber().longValue());
                    ParsedStringTerms optValAgg = optBucket.getAggregations().get("option_val_agg");
                    if (optValAgg != null && !optValAgg.getBuckets().isEmpty()) {
                        oVo.setOptionValue(optValAgg.getBuckets().get(0).getKeyAsString());
                    }
                    optList.add(oVo);
                }
            }
            aVo.setOptions(optList);
            if (!optList.isEmpty()) {
                attrList.add(aVo);
            }
        }
        vo.setAttributes(attrList);
    }

    private PcGoodsSearchResponseVO emptyResult(int pageNum, int pageSize, List<String> channels) {
        PcGoodsSearchResponseVO vo = new PcGoodsSearchResponseVO();
        vo.setTotal(0L);
        vo.setPageNum(pageNum);
        vo.setPageSize(pageSize);
        vo.setActualChannels(channels);
        vo.setList(Collections.emptyList());
        vo.setBrands(Collections.emptyList());
        vo.setAttributes(Collections.emptyList());
        return vo;
    }
}
