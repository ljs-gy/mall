package org.dromara.goods.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.dis.api.mq.DisGoodsMqConstants;
import org.dromara.goods.constant.GoodsConstants;
import org.dromara.goods.domain.Brand;
import org.dromara.goods.domain.Sku;
import org.dromara.goods.domain.Spu;
import org.dromara.goods.domain.bo.SpuBo;
import org.dromara.goods.domain.vo.SpuVo;
import org.dromara.goods.mapper.BrandMapper;
import org.dromara.goods.mapper.SkuMapper;
import org.dromara.goods.mapper.SpuMapper;
import org.dromara.goods.mq.GoodsMqConstants;
import org.dromara.goods.service.ISpuService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 商品SPU主Service业务层处理
 *
 * @author ljs
 * @date 2026-07-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SpuServiceImpl implements ISpuService {

    private final SpuMapper baseMapper;
    private final SkuMapper skuMapper;
    private final BrandMapper brandMapper;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public SpuVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<SpuVo> queryPageList(SpuBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Spu> lqw = buildQueryWrapper(bo);
        Page<SpuVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<SpuVo> queryList(SpuBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<Spu> buildQueryWrapper(SpuBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Spu> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Spu::getId);
        lqw.eq(bo.getCategoryBackendId() != null, Spu::getCategoryBackendId, bo.getCategoryBackendId());
        lqw.eq(bo.getCategoryFrontendId() != null, Spu::getCategoryFrontendId, bo.getCategoryFrontendId());
        lqw.eq(bo.getBrandId() != null, Spu::getBrandId, bo.getBrandId());
        lqw.eq(StringUtils.isNotBlank(bo.getTitle()), Spu::getTitle, bo.getTitle());
        lqw.eq(StringUtils.isNotBlank(bo.getSpuCode()), Spu::getSpuCode, bo.getSpuCode());
        lqw.eq(StringUtils.isNotBlank(bo.getGoodsClass()), Spu::getGoodsClass, bo.getGoodsClass());
        lqw.eq(StringUtils.isNotBlank(bo.getGoodsOrigin()), Spu::getGoodsOrigin, bo.getGoodsOrigin());
        lqw.eq(StringUtils.isNotBlank(bo.getMemberCode()), Spu::getMemberCode, bo.getMemberCode());
        lqw.eq(StringUtils.isNotBlank(bo.getMemberMcode()), Spu::getMemberMcode, bo.getMemberMcode());
        lqw.eq(StringUtils.isNotBlank(bo.getMemberCcode()), Spu::getMemberCcode, bo.getMemberCcode());
        lqw.eq(bo.getPriceSales() != null, Spu::getPriceSales, bo.getPriceSales());
        lqw.eq(bo.getPriceCost() != null, Spu::getPriceCost, bo.getPriceCost());
        lqw.eq(bo.getPriceMarket() != null, Spu::getPriceMarket, bo.getPriceMarket());
        lqw.eq(StringUtils.isNotBlank(bo.getPriceUnit()), Spu::getPriceUnit, bo.getPriceUnit());
        lqw.eq(StringUtils.isNotBlank(bo.getBaseUnit()), Spu::getBaseUnit, bo.getBaseUnit());
        lqw.eq(bo.getUnitRatio() != null, Spu::getUnitRatio, bo.getUnitRatio());
        lqw.eq(bo.getSingleWeight() != null, Spu::getSingleWeight, bo.getSingleWeight());
        lqw.eq(bo.getWeight() != null, Spu::getWeight, bo.getWeight());
        lqw.eq(bo.getMinOrderQuantity() != null, Spu::getMinOrderQuantity, bo.getMinOrderQuantity());
        lqw.eq(bo.getBizGoodsType() != null, Spu::getBizGoodsType, bo.getBizGoodsType());
        lqw.eq(bo.getPreSaleDeliveryTime() != null, Spu::getPreSaleDeliveryTime, bo.getPreSaleDeliveryTime());
        lqw.eq(bo.getSaleStatus() != null, Spu::getSaleStatus, bo.getSaleStatus());
        lqw.eq(bo.getAuditStatus() != null, Spu::getAuditStatus, bo.getAuditStatus());
        lqw.eq(bo.getAutoOffTime() != null, Spu::getAutoOffTime, bo.getAutoOffTime());
        lqw.eq(StringUtils.isNotBlank(bo.getDetailDesc()), Spu::getDetailDesc, bo.getDetailDesc());
        lqw.eq(bo.getFreightTemplateId() != null, Spu::getFreightTemplateId, bo.getFreightTemplateId());
        return lqw;
    }

    @Override
    public Boolean insertByBo(SpuBo bo) {
        Spu add = MapstructUtils.convert(bo, Spu.class);
        fillBrandName(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(SpuBo bo) {
        Spu update = MapstructUtils.convert(bo, Spu.class);
        fillBrandName(update);
        boolean flag = baseMapper.updateById(update) > 0;
        if (flag) {
            afterCommit(() -> {
                Spu latest = baseMapper.selectById(bo.getId());
                if (latest != null && Objects.equals(latest.getSaleStatus(), 1L)) {
                    sendSyncEs(bo.getId());
                    sendAutoOffDelay(latest.getId(), latest.getAutoOffTime());
                } else {
                    sendOffEs(bo.getId());
                }
            });
        }
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (CollUtil.isEmpty(ids)) {
            return false;
        }
        Map<Long, Spu> spuMap = baseMapper.selectBatchIds(ids).stream()
            .collect(Collectors.toMap(Spu::getId, s -> s, (a, b) -> a));
        List<Sku> skus = skuMapper.selectList(Wrappers.lambdaQuery(Sku.class).in(Sku::getSpuId, ids));
        List<Long> skuIds = skus.stream().map(Sku::getId).toList();
        // 主站 SKU code → 按父渠道分组，供子渠道副本删除
        Map<String, List<String>> channelSourceCodes = new HashMap<>();
        for (Sku sku : skus) {
            Spu spu = spuMap.get(sku.getSpuId());
            if (spu == null
                || !GoodsConstants.SOURCE_ROOT.equals(spu.getSourceSpuCode())
                || !GoodsConstants.SOURCE_ROOT.equals(sku.getSourceSkuCode())
                || StringUtils.isBlank(spu.getChannelCode())
                || StringUtils.isBlank(sku.getSkuCode())) {
                continue;
            }
            channelSourceCodes.computeIfAbsent(spu.getChannelCode(), k -> new ArrayList<>()).add(sku.getSkuCode());
        }
        String tenantId = LoginHelper.getTenantId();
        boolean flag = baseMapper.deleteByIds(ids) > 0;
        if (flag) {
            afterCommit(() -> {
                if (!skuIds.isEmpty()) {
                    rabbitTemplate.convertAndSend(
                        GoodsMqConstants.EXCHANGE_GOODS_DIRECT,
                        GoodsMqConstants.KEY_GOODS_ES_DELETE_SKU,
                        skuIds.toArray(Long[]::new)
                    );
                    log.info("【事务已提交】发送 ES 按 sku 删除消息，skuIds={}", skuIds);
                }
                channelSourceCodes.forEach((channelCode, codes) ->
                    sendChannelGoodsDelete(tenantId, channelCode, codes));
            });
        }
        return flag;
    }

    /** 通知分销：删除父渠道下各子渠道中对应主站 SKU 的副本 */
    private void sendChannelGoodsDelete(String tenantId, String channelCode, List<String> sourceSkuCodes) {
        if (CollUtil.isEmpty(sourceSkuCodes) || StringUtils.isBlank(channelCode)) {
            return;
        }
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
        log.info("【事务已提交】广播渠道商品删除 MQ，channelCode={}, count={}", channelCode, sourceSkuCodes.size());
    }

    private void fillBrandName(Spu spu) {
        if (spu == null || spu.getBrandId() == null) {
            return;
        }
        Brand brand = brandMapper.selectById(spu.getBrandId());
        if (brand != null) {
            spu.setBrandName(brand.getBrandName());
        }
    }

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
