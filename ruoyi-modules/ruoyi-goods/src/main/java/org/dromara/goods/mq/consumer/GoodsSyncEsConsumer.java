package org.dromara.goods.mq.consumer;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.goods.constant.GoodsEsConstants;
import org.dromara.goods.domain.Image;
import org.dromara.goods.domain.Sku;
import org.dromara.goods.domain.Spu;
import org.dromara.goods.domain.SpuAttributeValue;
import org.dromara.goods.domain.dto.GoodsEsAttrItem;
import org.dromara.goods.domain.dto.GoodsEsIndexDTO;
import org.dromara.goods.mapper.ImageMapper;
import org.dromara.goods.mapper.SkuMapper;
import org.dromara.goods.mapper.SpuAttributeValueMapper;
import org.dromara.goods.mapper.SpuMapper;
import org.dromara.goods.mq.GoodsMqConstants;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class GoodsSyncEsConsumer {

    private final SpuMapper spuMapper;
    private final SkuMapper skuMapper;
    private final SpuAttributeValueMapper spuAttributeValueMapper;
    private final ImageMapper imageMapper;
    private final RestHighLevelClient restHighLevelClient;
    private final ObjectMapper objectMapper;


    /**
     * 同步es商品
     */
    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = GoodsMqConstants.QUEUE_GOODS_SYNC_ES, durable = "true"),
        exchange = @Exchange(value = GoodsMqConstants.EXCHANGE_GOODS_DIRECT, type = ExchangeTypes.DIRECT),
        key = GoodsMqConstants.KEY_GOODS_SYNC_ES
    ))
    public void handleSyncEs(Long spuId) {
        TenantHelper.ignore(() -> {
            try {
                syncBySpuId(spuId);
            } catch (Exception e) {
                log.error("【ES同步】写入失败, spuId={}", spuId, e);
            }
        });
    }


    /**
     * 同步删除es商品
     */
    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = GoodsMqConstants.QUEUE_GOODS_ES_DELETE_SKU, durable = "true"),
        exchange = @Exchange(value = GoodsMqConstants.EXCHANGE_GOODS_DIRECT, type = ExchangeTypes.DIRECT),
        key = GoodsMqConstants.KEY_GOODS_ES_DELETE_SKU
    ))
    public void handleDeleteSkuEs(Long[] skuIds) {
        TenantHelper.ignore(() -> {
            try {
                deleteBySkuIds(skuIds);
            } catch (Exception e) {
                log.error("【ES同步】按 sku 删除失败, skuIds={}", (Object) skuIds, e);
            }
        });
    }

    private void syncBySpuId(Long spuId) throws IOException {
        Spu spu = spuMapper.selectById(spuId);
        if (spu == null) {
            log.warn("【ES同步】SPU 不存在，spuId={}", spuId);
            return;
        }
        List<Sku> skuList = skuMapper.selectList(Wrappers.lambdaQuery(Sku.class).eq(Sku::getSpuId, spuId));
        if (CollUtil.isEmpty(skuList)) {
            return;
        }
        List<GoodsEsAttrItem> attrs = spuAttributeValueMapper.selectList(
                Wrappers.lambdaQuery(SpuAttributeValue.class).eq(SpuAttributeValue::getSpuId, spuId))
            .stream()
            .map(a -> new GoodsEsAttrItem(a.getAttributeId(), a.getAttributeOptionId()))
            .collect(Collectors.toList());
        Map<Long, String> skuCoverMap = imageMapper.selectList(Wrappers.lambdaQuery(Image.class)
                .eq(Image::getSpuId, spuId)
                .eq(Image::getMediaScene, Image.SCENE_SKU)
                .eq(Image::getMediaType, Image.MEDIA_IMAGE)
                .orderByAsc(Image::getSortOrder))
            .stream()
            .collect(Collectors.toMap(Image::getSkuId, Image::getImageUrl, (first, ignore) -> first));
        BulkRequest bulk = new BulkRequest();
        // 按 SPU 下每个 SKU 组一条 UpdateRequest，放进 BulkRequest，再 executeBulk 一次提交。
        for (Sku sku : skuList) {
            GoodsEsIndexDTO doc = buildDoc(spu, sku, skuCoverMap.get(sku.getId()), attrs);
            // bulk upsert有则更新，无则插入
            bulk.add(new UpdateRequest(GoodsEsConstants.INDEX_GOODS_SKU, String.valueOf(sku.getId()))
                .doc(objectMapper.writeValueAsString(doc), XContentType.JSON)
                .docAsUpsert(true));
        }
        executeBulk(bulk, spuId);
    }

    private void deleteBySkuIds(Long[] skuIds) throws IOException {
        if (skuIds == null || skuIds.length == 0) {
            return;
        }
        BulkRequest bulk = new BulkRequest();
        for (Long skuId : skuIds) {
            bulk.add(new DeleteRequest(GoodsEsConstants.INDEX_GOODS_SKU, String.valueOf(skuId)));
        }
        executeBulk(bulk, null);
    }

    private void executeBulk(BulkRequest bulk, Long spuId) throws IOException {
        if (bulk.numberOfActions() == 0) {
            return;
        }
        BulkResponse resp = restHighLevelClient.bulk(bulk, RequestOptions.DEFAULT);
        if (resp.hasFailures()) {
            log.error("【ES同步】bulk失败: {}", resp.buildFailureMessage());
        } else {
            log.info("【ES同步】bulk成功，actions={}, spuId={}", bulk.numberOfActions(), spuId);
        }
    }

    private GoodsEsIndexDTO buildDoc(Spu spu, Sku sku, String cover, List<GoodsEsAttrItem> attrs) {
        GoodsEsIndexDTO doc = new GoodsEsIndexDTO();
        doc.setSkuId(sku.getId());
        doc.setSpuId(spu.getId());
        doc.setTenantId(spu.getTenantId());
        doc.setSkuCode(sku.getSkuCode());
        doc.setSpuCode(spu.getSpuCode());
        doc.setTitle(spu.getTitle());
        doc.setSpecText(sku.getSpecText());
        doc.setCoverImage(cover);
        doc.setPriceSales(sku.getPriceSales());
        doc.setPriceMarket(sku.getPriceMarket());
        doc.setStock(sku.getStock());
        doc.setSalesVolume(sku.getSalesVolume());
        doc.setWeight(spu.getWeight());
        doc.setCreateTime(spu.getCreateTime());
        doc.setCategoryBackendId(spu.getCategoryBackendId());
        doc.setCategoryFrontendId(spu.getCategoryFrontendId());
        doc.setBrandId(spu.getBrandId());
        doc.setBrandName(spu.getBrandName());
        doc.setGoodsClass(spu.getGoodsClass());
        doc.setMemberCode(sku.getMemberCode());
        doc.setChannelCode(spu.getChannelCode());
        doc.setMemberCcode(sku.getMemberCcode());
        doc.setAttrs(attrs);
        doc.setSaleStatus(spu.getSaleStatus());
        doc.setSkuSaleStatus(sku.getSkuSaleStatus());
        doc.setAuditStatus(spu.getAuditStatus());
        doc.setDelFlag(sku.getDelFlag());
        return doc;
    }
}
