package org.dromara.goods.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.utils.IdGeneratorUtil;
import org.dromara.common.redis.utils.SequenceUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.dis.api.mq.DisGoodsMqConstants;
import org.dromara.goods.domain.CategoryBackend;
import org.dromara.goods.domain.Image;
import org.dromara.goods.domain.Sku;
import org.dromara.goods.domain.SkuSpec;
import org.dromara.goods.domain.SpecOption;
import org.dromara.goods.domain.Spu;
import org.dromara.goods.domain.SpuAttributeValue;
import org.dromara.goods.domain.SpuParameterValue;
import org.dromara.goods.domain.bo.AuditGoodsBo;
import org.dromara.goods.domain.bo.BatchSaleStatusBo;
import org.dromara.goods.domain.bo.ImageBo;
import org.dromara.goods.domain.bo.PublishGoodsBo;
import org.dromara.goods.domain.bo.SaleStatusBo;
import org.dromara.goods.domain.bo.SkuBo;
import org.dromara.goods.domain.bo.SpuAttributeValueBo;
import org.dromara.goods.domain.bo.SpuParameterValueBo;
import org.dromara.goods.mapper.CategoryBackendMapper;
import org.dromara.goods.mapper.ImageMapper;
import org.dromara.goods.mapper.SkuMapper;
import org.dromara.goods.mapper.SkuSpecMapper;
import org.dromara.goods.mapper.SpecOptionMapper;
import org.dromara.goods.mapper.SpuAttributeValueMapper;
import org.dromara.goods.mapper.SpuMapper;
import org.dromara.goods.mapper.SpuParameterValueMapper;
import org.dromara.goods.constant.GoodsConstants;
import org.dromara.goods.mq.GoodsMqConstants;
import org.dromara.goods.service.IGoodsPublishService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 商品发布/审核/上下架编排
 *
 * @author ljs
 * @date 2026-07-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class GoodsPublishServiceImpl implements IGoodsPublishService {

    private static final String GOODS_CLASS_PLAT = "plat";
    private static final String GOODS_CLASS_SUPPLIER = "supplier";
    private static final long AUDIT_PENDING = 1L;
    private static final long AUDIT_PASS = 2L;
    private static final long AUDIT_REJECT = 3L;
    private static final long SALE_OFF = 0L;
    private static final long SALE_ON = 1L;
    private static final long BIZ_PRE_SALE = 2L;

    private final SpuMapper spuMapper;
    private final SkuMapper skuMapper;
    private final ImageMapper imageMapper;
    private final SpuParameterValueMapper spuParameterValueMapper;
    private final SpuAttributeValueMapper spuAttributeValueMapper;
    private final CategoryBackendMapper categoryBackendMapper;
    private final SpecOptionMapper specOptionMapper;
    private final SkuSpecMapper skuSpecMapper;
    private final RabbitTemplate rabbitTemplate;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publish(PublishGoodsBo bo) {
        CategoryBackend backend = categoryBackendMapper.selectById(bo.getCategoryBackendId());
        if (backend == null) {
            throw new ServiceException("后台分类不存在");
        }
        if (backend.getTypeId() == null) {
            throw new ServiceException("后台分类未关联商品类型");
        }
        if (Objects.equals(bo.getBizGoodsType(), BIZ_PRE_SALE) && bo.getPreSaleDeliveryTime() == null) {
            throw new ServiceException("预售商品必须填写预计发货时间");
        }
        boolean isPlat = GOODS_CLASS_PLAT.equals(bo.getGoodsClass());
        long auditStatus = isPlat ? AUDIT_PASS : AUDIT_PENDING;
        // 只有运营品且前端勾了立即上架
        long saleStatus = (isPlat && Boolean.TRUE.equals(bo.getOnShelfNow())) ? SALE_ON : SALE_OFF;
        Long spuId = IdGeneratorUtil.nextLongId();
        Spu spu = new Spu();
        spu.setId(spuId);
        spu.setCategoryBackendId(bo.getCategoryBackendId());
        spu.setCategoryFrontendId(bo.getCategoryFrontendId());
        spu.setBrandId(bo.getBrandId());
        spu.setBrandName(bo.getBrandName());
        spu.setTitle(bo.getTitle());
        spu.setSpuCode(SequenceUtils.getDateTimeId("SPU", true));
        spu.setSourceSpuCode(GoodsConstants.SOURCE_ROOT);
        spu.setSpuNo(bo.getSpuNo());
        spu.setGoodsClass(bo.getGoodsClass());
        spu.setGoodsOrigin(bo.getGoodsOrigin());
        String channelCode = LoginHelper.getChannelCode();
        spu.setMemberCode(bo.getMemberCode());
        spu.setChannelCode(channelCode);
        spu.setMemberMcode(bo.getMemberMcode());
        spu.setMemberCcode(bo.getMemberCcode());
        spu.setPriceSales(bo.getPriceSales());
        spu.setPriceCost(bo.getPriceCost());
        spu.setPriceMarket(bo.getPriceMarket());
        spu.setPriceUnit(bo.getPriceUnit());
        spu.setBaseUnit(bo.getBaseUnit());
        spu.setUnitRatio(bo.getUnitRatio());
        spu.setSingleWeight(bo.getSingleWeight());
        spu.setWeight(bo.getWeight());
        spu.setMinOrderQuantity(bo.getMinOrderQuantity());
        spu.setBizGoodsType(bo.getBizGoodsType());
        spu.setPreSaleDeliveryTime(bo.getPreSaleDeliveryTime());
        spu.setSaleStatus(saleStatus);
        spu.setAuditStatus(auditStatus);
        spu.setAutoOffTime(bo.getAutoOffTime());
        spu.setDetailDesc(bo.getDetailDesc());
        spu.setFreightTemplateId(bo.getFreightTemplateId());
        spuMapper.insert(spu);
        saveSpuMedia(spuId, bo);
        saveParamValues(spuId, bo.getParamValues());
        saveAttrValues(spuId, bo.getAttrValues());
        saveSkus(spuId, bo.getMemberCode(), bo.getMemberMcode(), bo.getMemberCcode(),
            channelCode, saleStatus, bo.getSkus());
        afterCommit(() -> {
            if (isPlat && saleStatus == SALE_ON) {
                sendSyncEs(spuId);
                sendGoodsChange(spuId, channelCode);
                sendAutoOffDelay(spuId, bo.getAutoOffTime());
            }
        });
        return spuId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long edit(PublishGoodsBo bo) {
        Spu exist = spuMapper.selectById(bo.getSpuId());
        if (exist == null) {
            throw new ServiceException("商品不存在");
        }
        if (!Objects.equals(exist.getSaleStatus(), SALE_OFF)) {
            throw new ServiceException("仅下架商品可编辑");
        }
        if (Objects.equals(bo.getBizGoodsType(), BIZ_PRE_SALE) && bo.getPreSaleDeliveryTime() == null) {
            throw new ServiceException("预售商品必须填写预计发货时间");
        }
        boolean isPlat = GOODS_CLASS_PLAT.equals(bo.getGoodsClass());
        Long auditStatus = isPlat ? exist.getAuditStatus() : AUDIT_PENDING;
        long saleStatus = (isPlat
            && Boolean.TRUE.equals(bo.getOnShelfNow())
            && (bo.getAutoOffTime() == null || !bo.getAutoOffTime().before(new Date())))
            ? SALE_ON : SALE_OFF;
        Long spuId = exist.getId();
        Spu spu = new Spu();
        spu.setId(spuId);
        spu.setCategoryBackendId(bo.getCategoryBackendId());
        spu.setCategoryFrontendId(bo.getCategoryFrontendId());
        spu.setBrandId(bo.getBrandId());
        spu.setBrandName(bo.getBrandName());
        spu.setTitle(bo.getTitle());
        spu.setSpuNo(bo.getSpuNo());
        spu.setGoodsClass(bo.getGoodsClass());
        spu.setGoodsOrigin(bo.getGoodsOrigin());
        String channelCode = LoginHelper.getChannelCode();
        spu.setMemberCode(bo.getMemberCode());
        spu.setChannelCode(channelCode);
        spu.setMemberMcode(bo.getMemberMcode());
        spu.setMemberCcode(bo.getMemberCcode());
        spu.setPriceSales(bo.getPriceSales());
        spu.setPriceCost(bo.getPriceCost());
        spu.setPriceMarket(bo.getPriceMarket());
        spu.setPriceUnit(bo.getPriceUnit());
        spu.setBaseUnit(bo.getBaseUnit());
        spu.setUnitRatio(bo.getUnitRatio());
        spu.setSingleWeight(bo.getSingleWeight());
        spu.setWeight(bo.getWeight());
        spu.setMinOrderQuantity(bo.getMinOrderQuantity());
        spu.setBizGoodsType(bo.getBizGoodsType());
        spu.setPreSaleDeliveryTime(bo.getPreSaleDeliveryTime());
        spu.setSaleStatus(saleStatus);
        spu.setAuditStatus(auditStatus);
        spu.setAutoOffTime(bo.getAutoOffTime());
        spu.setDetailDesc(bo.getDetailDesc());
        spu.setFreightTemplateId(bo.getFreightTemplateId());
        spuMapper.updateById(spu);
        imageMapper.delete(Wrappers.lambdaQuery(Image.class)
            .eq(Image::getSpuId, spuId)
            .isNull(Image::getSkuId));
        spuParameterValueMapper.delete(Wrappers.lambdaQuery(SpuParameterValue.class).eq(SpuParameterValue::getSpuId, spuId));
        spuAttributeValueMapper.delete(Wrappers.lambdaQuery(SpuAttributeValue.class).eq(SpuAttributeValue::getSpuId, spuId));
        saveSpuMedia(spuId, bo);
        saveParamValues(spuId, bo.getParamValues());
        saveAttrValues(spuId, bo.getAttrValues());
        List<String> removedSkuCodes = upsertSkus(spuId, bo.getMemberCode(), bo.getMemberMcode(), bo.getMemberCcode(),
            channelCode, saleStatus, bo.getSkus());
        afterCommit(() -> {
            if (saleStatus == SALE_ON) {
                sendSyncEs(spuId);
            } else {
                sendOffEs(spuId);
            }
            if (isPlat) {
                if (CollUtil.isNotEmpty(removedSkuCodes)) {
                    sendChannelGoodsDelete(channelCode, removedSkuCodes);
                }
                sendGoodsChange(spuId, channelCode);
                if (saleStatus == SALE_ON) {
                    sendAutoOffDelay(spuId, bo.getAutoOffTime());
                }
            }
        });
        return spuId;
    }

    private List<Long> normalizeOptionIds(List<Long> optionIds) {
        if (CollUtil.isEmpty(optionIds)) {
            return List.of();
        }
        return optionIds.stream().filter(Objects::nonNull).distinct().sorted().toList();
    }

    private void saveSpuMedia(Long spuId, PublishGoodsBo bo) {
        saveMediaList(spuId, null, Image.SCENE_GALLERY, bo.getSpuImages());
        saveMediaList(spuId, null, Image.SCENE_QUALITY, bo.getQualityImages());
        saveMediaList(spuId, null, Image.SCENE_TRACE, bo.getTraceImages());
    }

    private void saveSkuImages(Long spuId, Long skuId, List<ImageBo> images) {
        saveMediaList(spuId, skuId, Image.SCENE_SKU, images);
    }

    private void saveMediaList(Long spuId, Long skuId, long scene, List<ImageBo> images) {
        if (CollUtil.isEmpty(images)) {
            return;
        }
        for (ImageBo item : images) {
            Image image = new Image();
            image.setId(IdGeneratorUtil.nextLongId());
            image.setSpuId(spuId);
            image.setSkuId(skuId);
            image.setMediaScene(scene);
            image.setMediaType(item.getMediaType());
            image.setImageUrl(item.getImageUrl());
            image.setSortOrder(item.getSortOrder());
            imageMapper.insert(image);
        }
    }

    private void saveParamValues(Long spuId, List<SpuParameterValueBo> items) {
        if (CollUtil.isEmpty(items)) {
            return;
        }
        for (SpuParameterValueBo item : items) {
            SpuParameterValue value = new SpuParameterValue();
            value.setId(IdGeneratorUtil.nextLongId());
            value.setSpuId(spuId);
            value.setParameterId(item.getParameterId());
            value.setParameterValueText(item.getParameterValueText());
            spuParameterValueMapper.insert(value);
        }
    }

    private void saveAttrValues(Long spuId, List<SpuAttributeValueBo> items) {
        if (CollUtil.isEmpty(items)) {
            return;
        }
        for (SpuAttributeValueBo item : items) {
            SpuAttributeValue value = new SpuAttributeValue();
            value.setId(IdGeneratorUtil.nextLongId());
            value.setSpuId(spuId);
            value.setAttributeId(item.getAttributeId());
            value.setAttributeOptionId(item.getAttributeOptionId());
            spuAttributeValueMapper.insert(value);
        }
    }

    private void saveSkus(Long spuId, String memberCode, String memberMcode, String memberCcode,
                          String channelCode, long saleStatus, List<SkuBo> skus) {
        Map<Long, SpecOption> optionMap = loadSpecOptions(skus);
        for (SkuBo item : skus) {
            List<Long> optionIds = normalizeOptionIds(item.getSpecOptionIds());
            if (optionIds.isEmpty()) {
                throw new ServiceException("SKU规格选项不能为空");
            }
            String skuNo = StrUtil.blankToDefault(item.getSkuNo(), item.getSkuCode());
            if (StrUtil.isBlank(skuNo)) {
                throw new ServiceException("SKU业务货号不能为空");
            }
            Long skuId = IdGeneratorUtil.nextLongId();
            Sku sku = new Sku();
            sku.setId(skuId);
            sku.setMemberCode(memberCode);
            sku.setMemberMcode(memberMcode);
            sku.setMemberCcode(memberCcode);
            sku.setChannelCode(channelCode);
            sku.setSpuId(spuId);
            sku.setSkuCode(SequenceUtils.getDateTimeId("SKU", true));
            sku.setSourceSkuCode(GoodsConstants.SOURCE_ROOT);
            sku.setSkuNo(skuNo);
            sku.setSpecText(item.getSpecText());
            sku.setPriceSales(item.getPriceSales());
            sku.setPriceMarket(item.getPriceMarket());
            sku.setPriceCost(item.getPriceCost());
            sku.setPriceUnit(item.getPriceUnit());
            sku.setBaseUnit(item.getBaseUnit());
            sku.setSingleWeight(item.getSingleWeight());
            sku.setMinOrderQuantity(item.getMinOrderQuantity());
            sku.setStock(item.getStock());
            sku.setLockStock(0L);
            sku.setIsMultiplierBuy(item.getIsMultiplierBuy());
            sku.setSkuSaleStatus(saleStatus);
            sku.setSalesVolume(0L);
            skuMapper.insert(sku);
            saveSkuSpecs(spuId, skuId, optionIds, optionMap);
            saveSkuImages(spuId, skuId, item.getSkuImages());
        }
    }

    private Map<Long, SpecOption> loadSpecOptions(List<SkuBo> skus) {
        Set<Long> optionIds = new HashSet<>();
        for (SkuBo sku : skus) {
            if (CollUtil.isNotEmpty(sku.getSpecOptionIds())) {
                optionIds.addAll(sku.getSpecOptionIds());
            }
        }
        if (optionIds.isEmpty()) {
            return Map.of();
        }
        List<SpecOption> options = specOptionMapper.selectBatchIds(optionIds);
        Map<Long, SpecOption> map = new HashMap<>(options.size());
        for (SpecOption option : options) {
            map.put(option.getId(), option);
        }
        return map;
    }

    private void saveSkuSpecs(Long spuId, Long skuId, List<Long> optionIds, Map<Long, SpecOption> optionMap) {
        skuSpecMapper.delete(Wrappers.lambdaQuery(SkuSpec.class).eq(SkuSpec::getSkuId, skuId));
        for (Long optionId : optionIds) {
            SpecOption option = optionMap.get(optionId);
            if (option == null || option.getSpecId() == null) {
                throw new ServiceException("规格选项不存在: " + optionId);
            }
            SkuSpec row = new SkuSpec();
            row.setId(IdGeneratorUtil.nextLongId());
            row.setSpuId(spuId);
            row.setSkuId(skuId);
            row.setSpecId(option.getSpecId());
            row.setSpecOptionId(optionId);
            skuSpecMapper.insert(row);
        }
    }



    private List<String> upsertSkus(Long spuId, String memberCode, String memberMcode, String memberCcode,
                                    String channelCode, long saleStatus, List<SkuBo> skus) {
        List<Sku> existSkus = skuMapper.selectList(Wrappers.lambdaQuery(Sku.class).eq(Sku::getSpuId, spuId));
        Map<Long, SpecOption> optionMap = loadSpecOptions(skus);
        Set<Long> keepIds = new HashSet<>();
        for (SkuBo item : skus) {
            if (CollUtil.isEmpty(item.getSpecOptionIds())) {
                throw new ServiceException("SKU规格选项不能为空");
            }
            boolean isNew = item.getId() == null;
            Long skuId = isNew ? IdGeneratorUtil.nextLongId() : item.getId();
            keepIds.add(skuId);
            Sku sku = new Sku();
            sku.setId(skuId);
            String skuNo = StrUtil.blankToDefault(item.getSkuNo(), item.getSkuCode());
            if (StrUtil.isBlank(skuNo)) {
                throw new ServiceException("SKU业务货号不能为空");
            }
            if (isNew) {
                sku.setSpuId(spuId);
                sku.setSkuCode(SequenceUtils.getDateTimeId("SKU", true));
                sku.setSourceSkuCode(GoodsConstants.SOURCE_ROOT);
                sku.setLockStock(0L);
                sku.setSalesVolume(0L);
            }
            sku.setSkuNo(skuNo);
            sku.setMemberCode(memberCode);
            sku.setMemberMcode(memberMcode);
            sku.setMemberCcode(memberCcode);
            sku.setChannelCode(channelCode);
            sku.setSpecText(item.getSpecText());
            sku.setPriceSales(item.getPriceSales());
            sku.setPriceMarket(item.getPriceMarket());
            sku.setPriceCost(item.getPriceCost());
            sku.setPriceUnit(item.getPriceUnit());
            sku.setBaseUnit(item.getBaseUnit());
            sku.setSingleWeight(item.getSingleWeight());
            sku.setMinOrderQuantity(item.getMinOrderQuantity());
            sku.setStock(item.getStock());
            sku.setIsMultiplierBuy(item.getIsMultiplierBuy());
            sku.setSkuSaleStatus(saleStatus);
            if (isNew) {
                skuMapper.insert(sku);
            } else {
                skuMapper.updateById(sku);
                imageMapper.delete(Wrappers.lambdaQuery(Image.class).eq(Image::getSkuId, skuId));
            }
            saveSkuSpecs(spuId, skuId, item.getSpecOptionIds(), optionMap);
            saveSkuImages(spuId, skuId, item.getSkuImages());
        }
        List<String> removedSkuCodes = new ArrayList<>();
        for (Sku oldSku : existSkus) {
            if (!keepIds.contains(oldSku.getId())) {
                skuMapper.update(null, Wrappers.lambdaUpdate(Sku.class)
                    .eq(Sku::getId, oldSku.getId())
                    .set(Sku::getSkuSaleStatus, SALE_OFF));
                if (StrUtil.isNotBlank(oldSku.getSkuCode())) {
                    removedSkuCodes.add(oldSku.getSkuCode());
                }
            }
        }
        return removedSkuCodes;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean audit(AuditGoodsBo bo) {
        Spu spu = spuMapper.selectById(bo.getSpuId());
        if (spu == null) {
            throw new ServiceException("商品不存在");
        }
        if (!Objects.equals(spu.getAuditStatus(), AUDIT_PENDING)) {
            throw new ServiceException("仅待审核商品可审核");
        }
        boolean pass = bo.getPass();
        if (!pass && StrUtil.isBlank(bo.getRejectReason())) {
            throw new ServiceException("驳回时必须填写原因");
        }
        boolean isSupplier = GOODS_CLASS_SUPPLIER.equals(spu.getGoodsClass());
        Spu update = new Spu();
        update.setId(spu.getId());
        if (pass) {
            update.setAuditStatus(AUDIT_PASS);
            update.setSaleStatus(SALE_ON);
        } else {
            update.setAuditStatus(AUDIT_REJECT);
            update.setSaleStatus(SALE_OFF);
        }
        Long platSpuId = (pass && isSupplier) ? createPlatGoods(spu) : null;
        spuMapper.updateById(update);
        skuMapper.update(null, Wrappers.lambdaUpdate(Sku.class)
            .eq(Sku::getSpuId, spu.getId())
            .set(Sku::getSkuSaleStatus, update.getSaleStatus()));
        afterCommit(() -> {
            if (!pass) {
                // 驳回清理ES商品
                sendOffEs(spu.getId());
                return;
            }
            if (isSupplier) {
                sendSyncEs(spu.getId());
                sendSyncEs(platSpuId);
                sendGoodsChange(platSpuId, spu.getChannelCode());
                sendAutoOffDelay(platSpuId, spu.getAutoOffTime());
            } else {
                sendSyncEs(spu.getId());
                sendAutoOffDelay(spu.getId(), spu.getAutoOffTime());
            }
        });
        return true;
    }


    private Long createPlatGoods(Spu supplierSpu) {
        String platMemberCode = LoginHelper.getUserinfoCode();
        if (StrUtil.isBlank(platMemberCode)) {
            throw new ServiceException("运营账号缺少 userinfoCode，无法生成运营商品");
        }
        String supplierCode = supplierSpu.getMemberCcode();
        if (StrUtil.isBlank(supplierCode)) {
            throw new ServiceException("供应商商品缺少供应商编码(memberCcode)");
        }
        Long platSpuId = IdGeneratorUtil.nextLongId();
        Spu plat = new Spu();
        plat.setId(platSpuId);
        plat.setCategoryBackendId(supplierSpu.getCategoryBackendId());
        plat.setCategoryFrontendId(supplierSpu.getCategoryFrontendId());
        plat.setBrandId(supplierSpu.getBrandId());
        plat.setBrandName(supplierSpu.getBrandName());
        plat.setTitle(supplierSpu.getTitle());
        plat.setSpuCode(SequenceUtils.getDateTimeId("SPU", true));
        plat.setSourceSpuCode(GoodsConstants.SOURCE_ROOT);
        plat.setSpuNo(supplierSpu.getSpuNo());
        plat.setGoodsClass(GOODS_CLASS_PLAT);
        plat.setGoodsOrigin(supplierSpu.getGoodsOrigin());
        String channelCode = LoginHelper.getChannelCode();
        plat.setMemberCode(platMemberCode);
        plat.setChannelCode(channelCode);
        plat.setMemberMcode(platMemberCode);
        plat.setMemberCcode(supplierCode);
        plat.setPriceSales(supplierSpu.getPriceSales());
        plat.setPriceCost(supplierSpu.getPriceCost());
        plat.setPriceMarket(supplierSpu.getPriceMarket());
        plat.setPriceUnit(supplierSpu.getPriceUnit());
        plat.setBaseUnit(supplierSpu.getBaseUnit());
        plat.setUnitRatio(supplierSpu.getUnitRatio());
        plat.setSingleWeight(supplierSpu.getSingleWeight());
        plat.setWeight(supplierSpu.getWeight());
        plat.setMinOrderQuantity(supplierSpu.getMinOrderQuantity());
        plat.setBizGoodsType(supplierSpu.getBizGoodsType());
        plat.setPreSaleDeliveryTime(supplierSpu.getPreSaleDeliveryTime());
        plat.setSaleStatus(SALE_ON);
        plat.setAuditStatus(AUDIT_PASS);
        plat.setAutoOffTime(supplierSpu.getAutoOffTime());
        plat.setDetailDesc(supplierSpu.getDetailDesc());
        plat.setFreightTemplateId(supplierSpu.getFreightTemplateId());
        spuMapper.insert(plat);
        copyParamValues(supplierSpu.getId(), platSpuId);
        copyAttrValues(supplierSpu.getId(), platSpuId);
        Map<Long, Long> skuIdMap = copySkus(supplierSpu.getId(), platSpuId, platMemberCode, supplierCode, channelCode);
        copyImages(supplierSpu.getId(), platSpuId, skuIdMap);
        return platSpuId;
    }

    private void copyParamValues(Long fromSpuId, Long toSpuId) {
        List<SpuParameterValue> list = spuParameterValueMapper.selectList(
            Wrappers.lambdaQuery(SpuParameterValue.class).eq(SpuParameterValue::getSpuId, fromSpuId));
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (SpuParameterValue src : list) {
            SpuParameterValue value = new SpuParameterValue();
            value.setId(IdGeneratorUtil.nextLongId());
            value.setSpuId(toSpuId);
            value.setParameterId(src.getParameterId());
            value.setParameterValueText(src.getParameterValueText());
            spuParameterValueMapper.insert(value);
        }
    }

    private void copyAttrValues(Long fromSpuId, Long toSpuId) {
        List<SpuAttributeValue> list = spuAttributeValueMapper.selectList(
            Wrappers.lambdaQuery(SpuAttributeValue.class).eq(SpuAttributeValue::getSpuId, fromSpuId));
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (SpuAttributeValue src : list) {
            SpuAttributeValue value = new SpuAttributeValue();
            value.setId(IdGeneratorUtil.nextLongId());
            value.setSpuId(toSpuId);
            value.setAttributeId(src.getAttributeId());
            value.setAttributeOptionId(src.getAttributeOptionId());
            spuAttributeValueMapper.insert(value);
        }
    }

    private Map<Long, Long> copySkus(Long fromSpuId, Long toSpuId, String platMemberCode, String supplierCode,
                                     String channelCode) {
        List<Sku> skus = skuMapper.selectList(Wrappers.lambdaQuery(Sku.class).eq(Sku::getSpuId, fromSpuId));
        Map<Long, Long> skuIdMap = new HashMap<>();
        if (CollUtil.isEmpty(skus)) {
            return skuIdMap;
        }
        for (Sku src : skus) {
            Long newSkuId = IdGeneratorUtil.nextLongId();
            Sku sku = new Sku();
            sku.setId(newSkuId);
            sku.setMemberCode(platMemberCode);
            sku.setMemberMcode(platMemberCode);
            sku.setMemberCcode(supplierCode);
            sku.setChannelCode(channelCode);
            sku.setSpuId(toSpuId);
            sku.setSkuCode(SequenceUtils.getDateTimeId("SKU", true));
            sku.setSourceSkuCode(GoodsConstants.SOURCE_ROOT);
            sku.setSkuNo(src.getSkuNo());
            sku.setSpecText(src.getSpecText());
            sku.setPriceSales(src.getPriceSales());
            sku.setPriceMarket(src.getPriceMarket());
            sku.setPriceCost(src.getPriceCost());
            sku.setPriceUnit(src.getPriceUnit());
            sku.setBaseUnit(src.getBaseUnit());
            sku.setSingleWeight(src.getSingleWeight());
            sku.setMinOrderQuantity(src.getMinOrderQuantity());
            sku.setStock(src.getStock());
            sku.setLockStock(0L);
            sku.setIsMultiplierBuy(src.getIsMultiplierBuy());
            sku.setSkuSaleStatus(SALE_ON);
            sku.setSalesVolume(0L);
            skuMapper.insert(sku);
            copySkuSpecs(src.getId(), toSpuId, newSkuId);
            skuIdMap.put(src.getId(), newSkuId);
        }
        return skuIdMap;
    }

    private void copySkuSpecs(Long fromSkuId, Long toSpuId, Long toSkuId) {
        List<SkuSpec> specs = skuSpecMapper.selectList(Wrappers.lambdaQuery(SkuSpec.class)
            .eq(SkuSpec::getSkuId, fromSkuId));
        if (CollUtil.isEmpty(specs)) {
            return;
        }
        for (SkuSpec src : specs) {
            SkuSpec row = new SkuSpec();
            row.setId(IdGeneratorUtil.nextLongId());
            row.setSpuId(toSpuId);
            row.setSkuId(toSkuId);
            row.setSpecId(src.getSpecId());
            row.setSpecOptionId(src.getSpecOptionId());
            skuSpecMapper.insert(row);
        }
    }

    private void copyImages(Long fromSpuId, Long toSpuId, Map<Long, Long> skuIdMap) {
        List<Image> images = imageMapper.selectList(Wrappers.lambdaQuery(Image.class).eq(Image::getSpuId, fromSpuId));
        if (CollUtil.isEmpty(images)) {
            return;
        }
        for (Image src : images) {
            Long newSkuId = null;
            if (src.getSkuId() != null) {
                newSkuId = skuIdMap.get(src.getSkuId());
                if (newSkuId == null) {
                    continue;
                }
            }
            Image image = new Image();
            image.setId(IdGeneratorUtil.nextLongId());
            image.setSpuId(toSpuId);
            image.setSkuId(newSkuId);
            image.setMediaType(src.getMediaType());
            image.setMediaScene(src.getMediaScene());
            image.setImageUrl(src.getImageUrl());
            image.setSortOrder(src.getSortOrder());
            imageMapper.insert(image);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateSaleStatus(SaleStatusBo bo) {
        Spu spu = spuMapper.selectById(bo.getSpuId());
        if (spu == null) {
            throw new ServiceException("商品不存在");
        }
        if (Objects.equals(bo.getSaleStatus(), SALE_ON) && !Objects.equals(spu.getAuditStatus(), AUDIT_PASS)) {
            throw new ServiceException("仅审核通过的商品可上架");
        }
        Spu update = new Spu();
        update.setId(spu.getId());
        update.setSaleStatus(bo.getSaleStatus());
        spuMapper.updateById(update);
        skuMapper.update(null, Wrappers.lambdaUpdate(Sku.class)
            .eq(Sku::getSpuId, spu.getId())
            .set(Sku::getSkuSaleStatus, bo.getSaleStatus()));
        afterCommit(() -> {
            if (Objects.equals(bo.getSaleStatus(), SALE_ON)) {
                sendSyncEs(spu.getId());
                sendAutoOffDelay(spu.getId(), spu.getAutoOffTime());
            } else {
                sendOffEs(spu.getId());
            }
            sendGoodsChange(spu.getId(), spu.getChannelCode());
        });
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchUpdateSaleStatus(BatchSaleStatusBo bo) {
        List<Spu> spuList = spuMapper.selectList(Wrappers.lambdaQuery(Spu.class).in(Spu::getId, bo.getSpuIds()));
        if (Objects.equals(bo.getSaleStatus(), SALE_ON)) {
            for (Spu spu : spuList) {
                if (!Objects.equals(spu.getAuditStatus(), AUDIT_PASS)) {
                    throw new ServiceException("仅审核通过的商品可上架，spuId=" + spu.getId());
                }
            }
        }

        spuMapper.update(null, Wrappers.lambdaUpdate(Spu.class)
            .in(Spu::getId, bo.getSpuIds())
            .set(Spu::getSaleStatus, bo.getSaleStatus()));
        skuMapper.update(null, Wrappers.lambdaUpdate(Sku.class)
            .in(Sku::getSpuId, bo.getSpuIds())
            .set(Sku::getSkuSaleStatus, bo.getSaleStatus()));

        boolean onShelf = Objects.equals(bo.getSaleStatus(), SALE_ON);
        afterCommit(() -> {
            for (Spu spu : spuList) {
                if (onShelf) {
                    sendSyncEs(spu.getId());
                    sendAutoOffDelay(spu.getId(), spu.getAutoOffTime());
                } else {
                    sendOffEs(spu.getId());
                }
                sendGoodsChange(spu.getId(), spu.getChannelCode());
            }
        });
        return true;
    }

    /**
     * 发送同步ES消息
     */
    private void sendSyncEs(Long spuId) {
        rabbitTemplate.convertAndSend(
            GoodsMqConstants.EXCHANGE_GOODS_DIRECT,
            GoodsMqConstants.KEY_GOODS_SYNC_ES,
            spuId
        );
        log.info("【事务已提交】发送商品 ES 同步消息，spuId={}", spuId);
    }


    private void sendOffEs(Long spuId) {
        Long[] skuIds = skuMapper.selectList(Wrappers.lambdaQuery(Sku.class)
                .select(Sku::getId)
                .eq(Sku::getSpuId, spuId))
            .stream().map(Sku::getId).toArray(Long[]::new);
        if (skuIds.length == 0) {
            return;
        }
        rabbitTemplate.convertAndSend(
            GoodsMqConstants.EXCHANGE_GOODS_DIRECT,
            GoodsMqConstants.KEY_GOODS_ES_DELETE_SKU,
            skuIds
        );
        log.info("【事务已提交】发送商品 ES 清理消息，spuId={}, skuCount={}", spuId, skuIds.length);
    }

    /**
     * 发送渠道同步商品消息
     */
    private void sendGoodsChange(Long spuId, String channelCode) {
        String tenantId = LoginHelper.getTenantId();
        rabbitTemplate.convertAndSend(
            DisGoodsMqConstants.EXCHANGE_GOODS_CHANGE,
            DisGoodsMqConstants.KEY_GOODS_CHANGE,
            spuId,
            message -> {
                message.getMessageProperties().setHeader(DisGoodsMqConstants.HEADER_TENANT_ID, tenantId);
                message.getMessageProperties().setHeader(DisGoodsMqConstants.HEADER_CHANNEL_CODE, channelCode);
                return message;
            }
        );
        log.info("【事务已提交】广播商品变更 MQ，spuId={}, tenantId={}, channelCode={}", spuId, tenantId, channelCode);
    }

    private void sendChannelGoodsDelete(String channelCode, List<String> sourceSkuCodes) {
        if (CollUtil.isEmpty(sourceSkuCodes) || StrUtil.isBlank(channelCode)) {
            return;
        }
        String tenantId = LoginHelper.getTenantId();
        rabbitTemplate.convertAndSend(
            DisGoodsMqConstants.EXCHANGE_GOODS_CHANGE,
            DisGoodsMqConstants.KEY_GOODS_DELETE,
            sourceSkuCodes.toArray(String[]::new),
            message -> {
                message.getMessageProperties().setHeader(DisGoodsMqConstants.HEADER_TENANT_ID, tenantId);
                message.getMessageProperties().setHeader(DisGoodsMqConstants.HEADER_CHANNEL_CODE, channelCode);
                return message;
            }
        );
        log.info("【事务已提交】广播渠道商品删除 MQ（编辑删规格），channelCode={}, count={}",
            channelCode, sourceSkuCodes.size());
    }

    /**
     * 发送延时下架消息
     */
    private void sendAutoOffDelay(Long spuId, Date autoOffTime) {
        if (autoOffTime == null) {
            return;
        }
        long delayMillis = Math.max(0, autoOffTime.getTime() - System.currentTimeMillis());
        rabbitTemplate.convertAndSend(
            GoodsMqConstants.EXCHANGE_GOODS_DELAY,
            GoodsMqConstants.KEY_GOODS_AUTO_OFF_DELAY,
            spuId,
            message -> {
                message.getMessageProperties().setDelayLong(delayMillis);
                return message;
            }
        );
        log.info("【事务已提交】发送定时下架延迟消息，spuId={}, delayMillis={}", spuId, delayMillis);
    }

    private void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}
