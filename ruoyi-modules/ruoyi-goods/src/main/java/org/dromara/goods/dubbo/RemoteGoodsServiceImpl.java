package org.dromara.goods.dubbo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.redis.utils.SequenceUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.goods.api.RemoteGoodsService;
import org.dromara.goods.api.domain.RemoteGoodsAttrBo;
import org.dromara.goods.api.domain.RemoteGoodsSkuSearchBo;
import org.dromara.goods.api.domain.RemoteGoodsSkuSearchResult;
import org.dromara.goods.api.domain.RemoteGoodsSkuView;
import org.dromara.goods.api.domain.RemoteMasterSkuBo;
import org.dromara.goods.constant.GoodsConstants;
import org.dromara.goods.constant.GoodsEsConstants;
import org.dromara.goods.domain.Attribute;
import org.dromara.goods.domain.AttributeOption;
import org.dromara.goods.domain.Image;
import org.dromara.goods.domain.Sku;
import org.dromara.goods.domain.SkuSpec;
import org.dromara.goods.domain.Spu;
import org.dromara.goods.domain.SpuAttributeValue;
import org.dromara.goods.domain.SpuParameterValue;
import org.dromara.goods.domain.dto.GoodsEsIndexDTO;
import org.dromara.goods.mapper.AttributeMapper;
import org.dromara.goods.mapper.AttributeOptionMapper;
import org.dromara.goods.mapper.ImageMapper;
import org.dromara.goods.mapper.SkuMapper;
import org.dromara.goods.mapper.SkuSpecMapper;
import org.dromara.goods.mapper.SpuAttributeValueMapper;
import org.dromara.goods.mapper.SpuMapper;
import org.dromara.goods.mapper.SpuParameterValueMapper;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 商品中心远程服务实现
 *
 * @author ljs
 * @date 2026-08-06
 */
@Slf4j
@RequiredArgsConstructor
@Service
@DubboService
public class RemoteGoodsServiceImpl implements RemoteGoodsService {

    private final SkuMapper skuMapper;
    private final SpuMapper spuMapper;
    private final ImageMapper imageMapper;
    private final SpuAttributeValueMapper spuAttributeValueMapper;
    private final SpuParameterValueMapper spuParameterValueMapper;
    private final SkuSpecMapper skuSpecMapper;
    private final AttributeMapper attributeMapper;
    private final AttributeOptionMapper attributeOptionMapper;
    private final ObjectProvider<RestHighLevelClient> restHighLevelClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<RemoteMasterSkuBo> selectSkuFullInfoBySpuId(String tenantId, Long spuId) {
        return TenantHelper.dynamic(tenantId, () -> {
            Spu spu = spuMapper.selectById(spuId);
            if (spu == null || !GoodsConstants.SOURCE_ROOT.equals(spu.getSourceSpuCode())) {
                return List.of();
            }
            return toFullBoList(skuMapper.selectList(Wrappers.lambdaQuery(Sku.class)
                .eq(Sku::getSpuId, spuId)
                .eq(Sku::getSourceSkuCode, GoodsConstants.SOURCE_ROOT)));
        });
    }

    @Override
    public Long selectMasterSpuSaleStatus(String tenantId, Long spuId) {
        return TenantHelper.dynamic(tenantId, () -> {
            Spu spu = spuMapper.selectById(spuId);
            if (spu == null || !GoodsConstants.SOURCE_ROOT.equals(spu.getSourceSpuCode())) {
                return null;
            }
            return spu.getSaleStatus();
        });
    }

    @Override
    public List<RemoteGoodsAttrBo> selectAttrsBySpuId(String tenantId, Long spuId) {
        return TenantHelper.dynamic(tenantId, () -> {
            List<SpuAttributeValue> values = spuAttributeValueMapper.selectList(
                Wrappers.lambdaQuery(SpuAttributeValue.class).eq(SpuAttributeValue::getSpuId, spuId));
            if (CollUtil.isEmpty(values)) {
                return List.of();
            }
            Set<Long> attrIds = values.stream().map(SpuAttributeValue::getAttributeId).collect(Collectors.toSet());
            Set<Long> optionIds = values.stream().map(SpuAttributeValue::getAttributeOptionId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
            Map<Long, String> attrNameMap = attributeMapper.selectBatchIds(attrIds).stream()
                .collect(Collectors.toMap(Attribute::getId, Attribute::getAttributeName, (a, b) -> a));
            Map<Long, String> optionNameMap = CollUtil.isEmpty(optionIds) ? Map.of()
                : attributeOptionMapper.selectBatchIds(optionIds).stream()
                .collect(Collectors.toMap(AttributeOption::getId, AttributeOption::getAttributeOptionName, (a, b) -> a));
            List<RemoteGoodsAttrBo> result = new ArrayList<>(values.size());
            for (SpuAttributeValue value : values) {
                RemoteGoodsAttrBo attr = new RemoteGoodsAttrBo();
                attr.setAttrId(value.getAttributeId());
                attr.setAttrName(attrNameMap.get(value.getAttributeId()));
                attr.setOptionId(value.getAttributeOptionId());
                attr.setOptionValue(optionNameMap.get(value.getAttributeOptionId()));
                result.add(attr);
            }
            return result;
        });
    }

    @Override
    public List<RemoteMasterSkuBo> selectBySkuIds(String tenantId, Collection<String> skuIds) {
        return TenantHelper.dynamic(tenantId, () -> {
            if (CollUtil.isEmpty(skuIds)) {
                return List.of();
            }
            List<Long> ids = skuIds.stream().map(Long::valueOf).toList();
            return toFullBoList(skuMapper.selectList(Wrappers.lambdaQuery(Sku.class)
                .in(Sku::getId, ids)
                .eq(Sku::getChannelCode, LoginHelper.DEFAULT_CHANNEL_CODE)
                .eq(Sku::getSourceSkuCode, GoodsConstants.SOURCE_ROOT)));
        });
    }

    /**
     * 分销全量同步商品：按 sku_id 游标分页，一批批捞主站原品 SKU
     */
    @Override
    public List<RemoteMasterSkuBo> selectPageByCursor(String tenantId, String channelCode, Long lastSkuId, int limit) {
        return TenantHelper.dynamic(tenantId, () -> toFullBoList(skuMapper.selectList(Wrappers.lambdaQuery(Sku.class)
            .gt(Sku::getId, lastSkuId == null ? 0L : lastSkuId)
            .eq(Sku::getChannelCode, channelCode)
            .eq(Sku::getSourceSkuCode, GoodsConstants.SOURCE_ROOT)
            .orderByAsc(Sku::getId)
            .last("LIMIT " + limit))));
    }

    @Override
    public RemoteGoodsSkuSearchResult searchSkuForPromo(RemoteGoodsSkuSearchBo bo) {
        RestHighLevelClient client = restHighLevelClient.getIfAvailable();
        if (client == null) {
            throw new ServiceException("商品搜索服务未启用");
        }
        int pageNum = bo.getPageNum() == null ? 1 : bo.getPageNum();
        int pageSize = bo.getPageSize() == null ? 10 : bo.getPageSize();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
            .filter(QueryBuilders.termQuery("del_flag", 0))
            .filter(QueryBuilders.termQuery("sale_status", 1))
            .filter(QueryBuilders.termQuery("audit_status", 2))
            .filter(QueryBuilders.termQuery("sku_sale_status", 1));
        if (StrUtil.isNotBlank(bo.getTenantId())) {
            boolQuery.filter(QueryBuilders.termQuery("tenant_id", bo.getTenantId()));
        }
        applyTermsFilter(boolQuery, "sku_id", bo.getSkuIds(), false);
        applyTermsFilter(boolQuery, "sku_id", bo.getExcludeSkuIds(), true);
        applyTermsFilter(boolQuery, "category_backend_id", bo.getCategoryBackendIds(), false);
        applyTermsFilter(boolQuery, "category_backend_id", bo.getExcludeCategoryBackendIds(), true);
        applyTermsFilter(boolQuery, "brand_id", bo.getBrandIds(), false);
        applyTermsFilter(boolQuery, "brand_id", bo.getExcludeBrandIds(), true);
        if (CollUtil.isNotEmpty(bo.getMemberCcodes())) {
            boolQuery.filter(QueryBuilders.termsQuery("member_ccode", bo.getMemberCcodes()));
        }
        if (CollUtil.isNotEmpty(bo.getGoodsClasses())) {
            boolQuery.filter(QueryBuilders.termsQuery("goods_class", bo.getGoodsClasses()));
        }
        if (StrUtil.isNotBlank(bo.getMemberCode())) {
            boolQuery.filter(QueryBuilders.termQuery("member_code", bo.getMemberCode()));
        }
        if (StrUtil.isNotBlank(bo.getChannelCode())) {
            boolQuery.filter(QueryBuilders.termQuery("channel_code", bo.getChannelCode()));
        }
        boolean promoQuery = StrUtil.isNotBlank(bo.getPromoType())
            || bo.getPromoActivityId() != null
            || bo.getPromoStatus() != null;
        if (StrUtil.isNotBlank(bo.getPromoType())) {
            boolQuery.filter(QueryBuilders.termQuery("promo_type", bo.getPromoType()));
        }
        if (bo.getPromoActivityId() != null) {
            boolQuery.filter(QueryBuilders.termQuery("promo_activity_id", bo.getPromoActivityId()));
        }
        if (bo.getPromoStatus() != null) {
            boolQuery.filter(QueryBuilders.termQuery("promo_status", bo.getPromoStatus()));
        }
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
            .query(boolQuery)
            .from((pageNum - 1) * pageSize)
            .size(pageSize);
        String sortType = StrUtil.blankToDefault(bo.getSortType(), "default");
        String priceField = promoQuery ? "promo_price" : "price_sales";
        if ("price_asc".equalsIgnoreCase(sortType)) {
            sourceBuilder.sort(priceField, SortOrder.ASC);
        } else if ("price_desc".equalsIgnoreCase(sortType)) {
            sourceBuilder.sort(priceField, SortOrder.DESC);
        } else {
            sourceBuilder.sort("weight", SortOrder.DESC);
        }
        sourceBuilder.sort("sku_id", SortOrder.DESC);
        try {
            SearchResponse response = client.search(
                new SearchRequest(GoodsEsConstants.INDEX_GOODS_SKU).source(sourceBuilder),
                RequestOptions.DEFAULT);
            List<RemoteGoodsSkuView> rows = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                GoodsEsIndexDTO doc = objectMapper.readValue(hit.getSourceAsString(), GoodsEsIndexDTO.class);
                rows.add(BeanUtil.toBean(doc, RemoteGoodsSkuView.class));
            }
            RemoteGoodsSkuSearchResult result = new RemoteGoodsSkuSearchResult();
            result.setTotal(response.getHits().getTotalHits() == null ? 0L : response.getHits().getTotalHits().value);
            result.setRows(rows);
            return result;
        } catch (IOException e) {
            log.error("【营销圈货】ES 查询失败, bo={}", bo, e);
            RemoteGoodsSkuSearchResult empty = new RemoteGoodsSkuSearchResult();
            empty.setTotal(0L);
            empty.setRows(List.of());
            return empty;
        }
    }

    private void applyTermsFilter(BoolQueryBuilder boolQuery, String field, Collection<Long> ids, boolean exclude) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        if (exclude) {
            boolQuery.mustNot(QueryBuilders.termsQuery(field, ids));
        } else {
            boolQuery.filter(QueryBuilders.termsQuery(field, ids));
        }
    }

    private List<RemoteMasterSkuBo> toFullBoList(List<Sku> skus) {
        if (CollUtil.isEmpty(skus)) {
            return List.of();
        }
        Map<Long, Spu> spuMap = spuMapper.selectBatchIds(
                skus.stream().map(Sku::getSpuId).collect(Collectors.toSet()))
            .stream().collect(Collectors.toMap(Spu::getId, s -> s));
        List<RemoteMasterSkuBo> result = new ArrayList<>(skus.size());
        for (Sku sku : skus) {
            Spu spu = spuMap.get(sku.getSpuId());
            if (spu == null) {
                continue;
            }
            RemoteMasterSkuBo bo = BeanUtil.toBean(spu, RemoteMasterSkuBo.class);
            BeanUtil.copyProperties(sku, bo);
            bo.setSpuId(spu.getId());
            bo.setSkuId(sku.getId());
            bo.setOriginPriceSales(sku.getPriceSales());
            bo.setOriginPriceMarket(sku.getPriceMarket());
            bo.setOriginStock(sku.getStock().intValue());
            bo.setCoverImage(resolveSkuCover(sku.getId()));
            result.add(bo);
        }
        return result;
    }

    private String resolveSkuCover(Long skuId) {
        Image image = imageMapper.selectOne(Wrappers.lambdaQuery(Image.class)
            .eq(Image::getSkuId, skuId)
            .eq(Image::getMediaScene, Image.SCENE_SKU)
            .eq(Image::getMediaType, Image.MEDIA_IMAGE)
            .orderByAsc(Image::getSortOrder)
            .last("LIMIT 1"));
        return image == null ? null : image.getImageUrl();
    }

    @Override
    public boolean deductPhysicalStock(Long skuId, int qty) {
        return skuMapper.deductPhysicalStock(skuId, qty) > 0;
    }

    @Override
    public boolean confirmPhysicalStock(Long skuId, int qty) {
        return skuMapper.confirmPhysicalStock(skuId, qty) > 0;
    }

    @Override
    public boolean rollbackPhysicalStock(Long skuId, int qty) {
        return skuMapper.rollbackPhysicalStock(skuId, qty) > 0;
    }

    @Override
    public RemoteMasterSkuBo findChannelSku(String tenantId, String channelCode, String sourceSkuCode) {
        return TenantHelper.dynamic(tenantId, () -> {
            Sku sku = skuMapper.selectOne(Wrappers.lambdaQuery(Sku.class)
                .eq(Sku::getChannelCode, channelCode)
                .eq(Sku::getSourceSkuCode, sourceSkuCode)
                .last("LIMIT 1"));
            return sku == null ? null : CollUtil.getFirst(toFullBoList(List.of(sku)));
        });
    }

    @Override
    public List<RemoteMasterSkuBo> upsertChannelSpuGoods(String tenantId, String channelCode, List<RemoteMasterSkuBo> masters) {
        return TenantHelper.dynamic(tenantId, () -> doUpsertChannelSpuGoods(channelCode, masters));
    }

    /**
     * 把一条已算好价/库存/上下架的主站 SKU，落到指定渠道，做成渠道自己的商品副本（有则改、无则建），最后返回渠道 spuId/skuId 给上游写 ES。
     */
    private List<RemoteMasterSkuBo> doUpsertChannelSpuGoods(String channelCode, List<RemoteMasterSkuBo> masters) {
        Spu masterSpu = spuMapper.selectById(masters.get(0).getSpuId());
        boolean anyOnSale = masters.stream().anyMatch(m -> Integer.valueOf(1).equals(m.getSaleStatus()));
        Spu channelSpu = spuMapper.selectOne(Wrappers.lambdaQuery(Spu.class)
            .eq(Spu::getChannelCode, channelCode)
            .eq(Spu::getSourceSpuCode, masterSpu.getSpuCode())
            .last("LIMIT 1"));
        if (channelSpu == null) {
            channelSpu = new Spu();
            channelSpu.setSpuCode(SequenceUtils.getDateTimeId("SPU", true));
            channelSpu.setSourceSpuCode(masterSpu.getSpuCode());
            channelSpu.setChannelCode(channelCode);
            channelSpu.setAuditStatus(2L);
        }
        fillSpuContent(channelSpu, masterSpu);
        channelSpu.setSaleStatus(anyOnSale ? 1L : 0L);
        channelSpu.setPriceSales(masterSpu.getPriceSales());
        channelSpu.setPriceMarket(masterSpu.getPriceMarket());
        if (channelSpu.getId() == null) {
            spuMapper.insert(channelSpu);
        } else {
            spuMapper.updateById(channelSpu);
        }
        Long masterSpuId = masterSpu.getId();
        Long channelSpuId = channelSpu.getId();
        // 删渠道 SPU 的参数、属性、SPU 图
        spuParameterValueMapper.delete(Wrappers.lambdaQuery(SpuParameterValue.class)
            .eq(SpuParameterValue::getSpuId, channelSpuId));
        spuAttributeValueMapper.delete(Wrappers.lambdaQuery(SpuAttributeValue.class)
            .eq(SpuAttributeValue::getSpuId, channelSpuId));
        imageMapper.delete(Wrappers.lambdaQuery(Image.class)
            .eq(Image::getSpuId, channelSpuId)
            .isNull(Image::getSkuId));
        for (SpuParameterValue param : spuParameterValueMapper.selectList(
            Wrappers.lambdaQuery(SpuParameterValue.class).eq(SpuParameterValue::getSpuId, masterSpuId))) {
            SpuParameterValue copy = new SpuParameterValue();
            copy.setSpuId(channelSpuId);
            copy.setParameterId(param.getParameterId());
            copy.setParameterValueText(param.getParameterValueText());
            spuParameterValueMapper.insert(copy);
        }
        for (SpuAttributeValue attr : spuAttributeValueMapper.selectList(
            Wrappers.lambdaQuery(SpuAttributeValue.class).eq(SpuAttributeValue::getSpuId, masterSpuId))) {
            SpuAttributeValue copy = new SpuAttributeValue();
            copy.setSpuId(channelSpuId);
            copy.setAttributeId(attr.getAttributeId());
            copy.setAttributeOptionId(attr.getAttributeOptionId());
            spuAttributeValueMapper.insert(copy);
        }
        for (Image img : imageMapper.selectList(Wrappers.lambdaQuery(Image.class)
            .eq(Image::getSpuId, masterSpuId)
            .isNull(Image::getSkuId))) {
            imageMapper.insert(copyImage(img, channelSpuId, null));
        }
        Map<Long, Sku> masterSkuMap = skuMapper.selectBatchIds(
                masters.stream().map(RemoteMasterSkuBo::getSkuId).collect(Collectors.toSet()))
            .stream().collect(Collectors.toMap(Sku::getId, s -> s));
        Map<String, Sku> channelSkuMap = skuMapper.selectList(Wrappers.lambdaQuery(Sku.class)
                .eq(Sku::getChannelCode, channelCode)
                .in(Sku::getSourceSkuCode, masters.stream().map(RemoteMasterSkuBo::getSkuCode).toList()))
            .stream().collect(Collectors.toMap(Sku::getSourceSkuCode, s -> s));
        for (RemoteMasterSkuBo master : masters) {
            Sku masterSku = masterSkuMap.get(master.getSkuId());
            Sku channelSku = channelSkuMap.get(master.getSkuCode());
            if (channelSku == null) {
                channelSku = new Sku();
                channelSku.setChannelCode(channelCode);
                channelSku.setSkuCode(SequenceUtils.getDateTimeId("SKU", true));
                channelSku.setSourceSkuCode(master.getSkuCode());
                channelSku.setLockStock(0L);
                channelSku.setSalesVolume(0L);
            }
            channelSku.setSpuId(channelSpuId);
            fillSkuContent(channelSku, masterSku);
            channelSku.setPriceSales(master.getOriginPriceSales());
            channelSku.setPriceMarket(master.getOriginPriceMarket());
            channelSku.setStock(master.getOriginStock().longValue());
            channelSku.setSkuSaleStatus(master.getSaleStatus().longValue());
            if (channelSku.getId() == null) {
                skuMapper.insert(channelSku);
            } else {
                skuMapper.updateById(channelSku);
            }
            Long channelSkuId = channelSku.getId();
            skuSpecMapper.delete(Wrappers.lambdaQuery(SkuSpec.class).eq(SkuSpec::getSkuId, channelSkuId));
            imageMapper.delete(Wrappers.lambdaQuery(Image.class).eq(Image::getSkuId, channelSkuId));
            for (SkuSpec spec : skuSpecMapper.selectList(Wrappers.lambdaQuery(SkuSpec.class)
                .eq(SkuSpec::getSkuId, masterSku.getId()))) {
                SkuSpec copy = new SkuSpec();
                copy.setSpuId(channelSpuId);
                copy.setSkuId(channelSkuId);
                copy.setSpecId(spec.getSpecId());
                copy.setSpecOptionId(spec.getSpecOptionId());
                skuSpecMapper.insert(copy);
            }
            for (Image img : imageMapper.selectList(Wrappers.lambdaQuery(Image.class)
                .eq(Image::getSkuId, masterSku.getId()))) {
                imageMapper.insert(copyImage(img, channelSpuId, channelSkuId));
            }
            master.setSpuId(channelSpuId);
            master.setSkuId(channelSkuId);
        }
        return masters;
    }

    /** 同步商品资料（不含渠道身份、价/库存/上下架） */
    private void fillSpuContent(Spu target, Spu master) {
        target.setCategoryBackendId(master.getCategoryBackendId());
        target.setCategoryFrontendId(master.getCategoryFrontendId());
        target.setBrandId(master.getBrandId());
        target.setBrandName(master.getBrandName());
        target.setTitle(master.getTitle());
        target.setSpuNo(master.getSpuNo());
        target.setGoodsClass(master.getGoodsClass());
        target.setGoodsOrigin(master.getGoodsOrigin());
        target.setMemberCode(master.getMemberCode());
        target.setMemberMcode(master.getMemberMcode());
        target.setMemberCcode(master.getMemberCcode());
        target.setPriceCost(master.getPriceCost());
        target.setPriceUnit(master.getPriceUnit());
        target.setBaseUnit(master.getBaseUnit());
        target.setUnitRatio(master.getUnitRatio());
        target.setSingleWeight(master.getSingleWeight());
        target.setWeight(master.getWeight());
        target.setMinOrderQuantity(master.getMinOrderQuantity());
        target.setBizGoodsType(master.getBizGoodsType());
        target.setPreSaleDeliveryTime(master.getPreSaleDeliveryTime());
        target.setAutoOffTime(master.getAutoOffTime());
        target.setDetailDesc(master.getDetailDesc());
        target.setFreightTemplateId(master.getFreightTemplateId());
    }

    /** 同步 SKU 资料（不含渠道身份、价/库存/上下架、锁库存/销量） */
    private void fillSkuContent(Sku target, Sku master) {
        target.setMemberCode(master.getMemberCode());
        target.setMemberMcode(master.getMemberMcode());
        target.setMemberCcode(master.getMemberCcode());
        target.setSkuNo(master.getSkuNo());
        target.setSpecText(master.getSpecText());
        target.setPriceCost(master.getPriceCost());
        target.setPriceUnit(master.getPriceUnit());
        target.setBaseUnit(master.getBaseUnit());
        target.setSingleWeight(master.getSingleWeight());
        target.setMinOrderQuantity(master.getMinOrderQuantity());
        target.setIsMultiplierBuy(master.getIsMultiplierBuy());
    }


    private void refreshChannelSpuSaleStatus(Long spuId) {
        Spu spu = spuMapper.selectById(spuId);
        List<Sku> skus = skuMapper.selectList(Wrappers.lambdaQuery(Sku.class).eq(Sku::getSpuId, spuId));
        boolean anyOnSale = skus.stream().anyMatch(s -> Long.valueOf(1L).equals(s.getSkuSaleStatus()));
        spu.setSaleStatus(anyOnSale ? 1L : 0L);
        spuMapper.updateById(spu);
    }

    private Image copyImage(Image master, Long channelSpuId, Long channelSkuId) {
        Image copy = new Image();
        copy.setSpuId(channelSpuId);
        copy.setSkuId(channelSkuId);
        copy.setMediaType(master.getMediaType());
        copy.setMediaScene(master.getMediaScene());
        copy.setImageUrl(master.getImageUrl());
        copy.setSortOrder(master.getSortOrder());
        return copy;
    }

    @Override
    public List<RemoteMasterSkuBo> offSaleChannelBySourceSkuCodes(String tenantId, Collection<String> sourceSkuCodes,
                                                                  Collection<String> channelCodes) {
        return TenantHelper.dynamic(tenantId, () -> {
            if (CollUtil.isEmpty(sourceSkuCodes) || CollUtil.isEmpty(channelCodes)) {
                return List.of();
            }
            List<Sku> skus = skuMapper.selectList(Wrappers.lambdaQuery(Sku.class)
                .in(Sku::getSourceSkuCode, sourceSkuCodes)
                .in(Sku::getChannelCode, channelCodes)
                .ne(Sku::getSourceSkuCode, GoodsConstants.SOURCE_ROOT));
            if (CollUtil.isEmpty(skus)) {
                return List.of();
            }
            for (Sku sku : skus) {
                sku.setSkuSaleStatus(0L);
                skuMapper.updateById(sku);
            }
            skus.stream().map(Sku::getSpuId).collect(Collectors.toSet()).forEach(this::refreshChannelSpuSaleStatus);
            return toFullBoList(skus);
        });
    }

    @Override
    public List<RemoteMasterSkuBo> removeChannelGoodsBySourceSkuCodes(String tenantId, Collection<String> sourceSkuCodes,
                                                                      Collection<String> channelCodes) {
        return TenantHelper.dynamic(tenantId, () -> {
            if (CollUtil.isEmpty(sourceSkuCodes) || CollUtil.isEmpty(channelCodes)) {
                return List.of();
            }
            List<Sku> skus = skuMapper.selectList(Wrappers.lambdaQuery(Sku.class)
                .in(Sku::getSourceSkuCode, sourceSkuCodes)
                .in(Sku::getChannelCode, channelCodes)
                .ne(Sku::getSourceSkuCode, GoodsConstants.SOURCE_ROOT));
            if (CollUtil.isEmpty(skus)) {
                return List.of();
            }
            List<RemoteMasterSkuBo> snapshot = new ArrayList<>(skus.size());
            for (Sku sku : skus) {
                RemoteMasterSkuBo bo = new RemoteMasterSkuBo();
                bo.setChannelCode(sku.getChannelCode());
                bo.setSkuId(sku.getId());
                snapshot.add(bo);
            }
            List<Long> skuIds = skus.stream().map(Sku::getId).toList();
            Set<Long> spuIds = skus.stream().map(Sku::getSpuId).collect(Collectors.toSet());
            skuSpecMapper.delete(Wrappers.lambdaQuery(SkuSpec.class).in(SkuSpec::getSkuId, skuIds));
            imageMapper.delete(Wrappers.lambdaQuery(Image.class).in(Image::getSkuId, skuIds));
            skuMapper.delete(Wrappers.lambdaQuery(Sku.class).in(Sku::getId, skuIds));
            // 删完这批渠道 SKU 之后，看这些渠道 SPU 上还剩不剩别的 SKU
            Map<Long, List<Sku>> remainBySpu = skuMapper.selectList(Wrappers.lambdaQuery(Sku.class)
                    .in(Sku::getSpuId, spuIds))
                .stream().collect(Collectors.groupingBy(Sku::getSpuId));
            List<Long> emptySpuIds = new ArrayList<>();
            for (Long spuId : spuIds) {
                List<Sku> remain = remainBySpu.get(spuId);
                if (CollUtil.isEmpty(remain)) {
                    emptySpuIds.add(spuId);
                    continue;
                }
                boolean anyOnSale = remain.stream().anyMatch(s -> Long.valueOf(1L).equals(s.getSkuSaleStatus()));
                spuMapper.update(null, Wrappers.lambdaUpdate(Spu.class)
                    .set(Spu::getSaleStatus, anyOnSale ? 1L : 0L)
                    .eq(Spu::getId, spuId));
            }
            if (CollUtil.isNotEmpty(emptySpuIds)) {
                spuParameterValueMapper.delete(Wrappers.lambdaQuery(SpuParameterValue.class)
                    .in(SpuParameterValue::getSpuId, emptySpuIds));
                spuAttributeValueMapper.delete(Wrappers.lambdaQuery(SpuAttributeValue.class)
                    .in(SpuAttributeValue::getSpuId, emptySpuIds));
                imageMapper.delete(Wrappers.lambdaQuery(Image.class).in(Image::getSpuId, emptySpuIds));
                spuMapper.delete(Wrappers.lambdaQuery(Spu.class).in(Spu::getId, emptySpuIds));
            }
            return snapshot;
        });
    }

    @Override
    public List<Long> removeGoodsByChannel(String tenantId, String channelCode) {
        return TenantHelper.dynamic(tenantId, () -> {
            List<Sku> skus = skuMapper.selectList(Wrappers.lambdaQuery(Sku.class)
                .eq(Sku::getChannelCode, channelCode)
                .ne(Sku::getSourceSkuCode, GoodsConstants.SOURCE_ROOT));
            List<Long> skuIds = skus.stream().map(Sku::getId).toList();
            List<Long> spuIds = spuMapper.selectList(Wrappers.lambdaQuery(Spu.class)
                    .eq(Spu::getChannelCode, channelCode)
                    .ne(Spu::getSourceSpuCode, GoodsConstants.SOURCE_ROOT))
                .stream().map(Spu::getId).toList();
            if (CollUtil.isNotEmpty(skuIds)) {
                skuSpecMapper.delete(Wrappers.lambdaQuery(SkuSpec.class).in(SkuSpec::getSkuId, skuIds));
                skuMapper.delete(Wrappers.lambdaQuery(Sku.class).in(Sku::getId, skuIds));
            }
            if (CollUtil.isNotEmpty(spuIds)) {
                spuParameterValueMapper.delete(Wrappers.lambdaQuery(SpuParameterValue.class)
                    .in(SpuParameterValue::getSpuId, spuIds));
                spuAttributeValueMapper.delete(Wrappers.lambdaQuery(SpuAttributeValue.class)
                    .in(SpuAttributeValue::getSpuId, spuIds));
                imageMapper.delete(Wrappers.lambdaQuery(Image.class).in(Image::getSpuId, spuIds));
                spuMapper.delete(Wrappers.lambdaQuery(Spu.class).in(Spu::getId, spuIds));
            }
            return skuIds;
        });
    }
}
