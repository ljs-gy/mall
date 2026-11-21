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
import org.dromara.goods.domain.Sku;
import org.dromara.goods.domain.Spu;
import org.dromara.goods.domain.bo.SkuBo;
import org.dromara.goods.domain.vo.SkuVo;
import org.dromara.goods.mapper.SkuMapper;
import org.dromara.goods.mapper.SpuMapper;
import org.dromara.goods.mq.GoodsMqConstants;
import org.dromara.goods.service.ISkuService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 商品货品SKUService业务层处理
 *
 * @author ljs
 * @date 2026-07-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SkuServiceImpl implements ISkuService {

    private final SkuMapper baseMapper;
    private final SpuMapper spuMapper;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public SkuVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<SkuVo> queryPageList(SkuBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Sku> lqw = buildQueryWrapper(bo);
        Page<SkuVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<SkuVo> queryList(SkuBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<Sku> buildQueryWrapper(SkuBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Sku> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Sku::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getMemberCode()), Sku::getMemberCode, bo.getMemberCode());
        lqw.eq(StringUtils.isNotBlank(bo.getMemberMcode()), Sku::getMemberMcode, bo.getMemberMcode());
        lqw.eq(StringUtils.isNotBlank(bo.getMemberCcode()), Sku::getMemberCcode, bo.getMemberCcode());
        lqw.eq(bo.getSpuId() != null, Sku::getSpuId, bo.getSpuId());
        lqw.eq(StringUtils.isNotBlank(bo.getSkuCode()), Sku::getSkuCode, bo.getSkuCode());
        lqw.eq(StringUtils.isNotBlank(bo.getSpecText()), Sku::getSpecText, bo.getSpecText());
        lqw.eq(bo.getPriceSales() != null, Sku::getPriceSales, bo.getPriceSales());
        lqw.eq(bo.getPriceMarket() != null, Sku::getPriceMarket, bo.getPriceMarket());
        lqw.eq(bo.getPriceCost() != null, Sku::getPriceCost, bo.getPriceCost());
        lqw.eq(StringUtils.isNotBlank(bo.getPriceUnit()), Sku::getPriceUnit, bo.getPriceUnit());
        lqw.eq(StringUtils.isNotBlank(bo.getBaseUnit()), Sku::getBaseUnit, bo.getBaseUnit());
        lqw.eq(bo.getSingleWeight() != null, Sku::getSingleWeight, bo.getSingleWeight());
        lqw.eq(bo.getMinOrderQuantity() != null, Sku::getMinOrderQuantity, bo.getMinOrderQuantity());
        lqw.eq(bo.getStock() != null, Sku::getStock, bo.getStock());
        lqw.eq(bo.getLockStock() != null, Sku::getLockStock, bo.getLockStock());
        lqw.eq(bo.getIsMultiplierBuy() != null, Sku::getIsMultiplierBuy, bo.getIsMultiplierBuy());
        lqw.eq(bo.getSkuSaleStatus() != null, Sku::getSkuSaleStatus, bo.getSkuSaleStatus());
        return lqw;
    }

    @Override
    public Boolean insertByBo(SkuBo bo) {
        Sku add = MapstructUtils.convert(bo, Sku.class);
        // 主站 SKU 渠道强制跟登录态，禁止落库默认 default
        String channelCode = LoginHelper.getChannelCode();
        add.setChannelCode(channelCode);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
            afterCommit(() -> {
                sendEsBySpuSaleStatus(add.getSpuId());
                sendPriceStockChange(add.getSpuId(), channelCode);
            });
        }
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(SkuBo bo) {
        Sku update = MapstructUtils.convert(bo, Sku.class);
        String channelCode = LoginHelper.getChannelCode();
        update.setChannelCode(channelCode);
        boolean flag = baseMapper.updateById(update) > 0;
        if (flag) {
            afterCommit(() -> {
                Sku latest = baseMapper.selectById(bo.getId());
                Long spuId = latest != null ? latest.getSpuId() : bo.getSpuId();
                sendEsBySpuSaleStatus(spuId);
                sendPriceStockChange(spuId, channelCode);
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
        List<Sku> skus = baseMapper.selectBatchIds(ids);
        Map<String, List<String>> channelSourceCodes = new HashMap<>();
        for (Sku sku : skus) {
            if (!GoodsConstants.SOURCE_ROOT.equals(sku.getSourceSkuCode()) || StringUtils.isBlank(sku.getSkuCode())) {
                continue;
            }
            String channelCode = resolveChannelCode(sku);
            if (StringUtils.isBlank(channelCode)) {
                continue;
            }
            channelSourceCodes.computeIfAbsent(channelCode, k -> new ArrayList<>()).add(sku.getSkuCode());
        }
        String tenantId = LoginHelper.getTenantId();
        boolean flag = baseMapper.deleteByIds(ids) > 0;
        if (flag) {
            afterCommit(() -> {
                rabbitTemplate.convertAndSend(
                    GoodsMqConstants.EXCHANGE_GOODS_DIRECT,
                    GoodsMqConstants.KEY_GOODS_ES_DELETE_SKU,
                    ids.toArray(Long[]::new)
                );
                log.info("【事务已提交】发送 ES 按 sku 删除消息，skuIds={}", ids);
                channelSourceCodes.forEach((channelCode, codes) ->
                    sendChannelGoodsDelete(tenantId, channelCode, codes));
            });
        }
        return flag;
    }

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

    /** 按所属 SPU 上下架状态选择 sync / off */
    private void sendEsBySpuSaleStatus(Long spuId) {
        if (spuId == null) {
            return;
        }
        Spu spu = spuMapper.selectById(spuId);
        if (spu != null && Objects.equals(spu.getSaleStatus(), 1L)) {
            rabbitTemplate.convertAndSend(
                GoodsMqConstants.EXCHANGE_GOODS_DIRECT,
                GoodsMqConstants.KEY_GOODS_SYNC_ES,
                spuId
            );
            log.info("【事务已提交】发送商品 ES 同步消息，spuId={}", spuId);
        } else {
            Long[] skuIds = baseMapper.selectList(Wrappers.lambdaQuery(Sku.class)
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
    }

    /** 改价/改库存后通知渠道同步 */
    private void sendPriceStockChange(Long spuId, String channelCode) {
        if (spuId == null || StringUtils.isBlank(channelCode)) {
            log.warn("改价/改库存 MQ 跳过，spuId 或 channelCode 为空, spuId={}, channelCode={}", spuId, channelCode);
            return;
        }
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
        log.info("【事务已提交】广播改价/改库存 MQ，spuId={}, channelCode={}", spuId, channelCode);
    }

    private String resolveChannelCode(Sku sku) {
        if (sku == null) {
            return null;
        }
        if (StringUtils.isNotBlank(sku.getChannelCode())) {
            return sku.getChannelCode();
        }
        if (sku.getSpuId() == null) {
            return null;
        }
        Spu spu = spuMapper.selectById(sku.getSpuId());
        return spu == null ? null : spu.getChannelCode();
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
