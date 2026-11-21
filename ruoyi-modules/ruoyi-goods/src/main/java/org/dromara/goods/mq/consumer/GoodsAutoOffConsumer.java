package org.dromara.goods.mq.consumer;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.dis.api.mq.DisGoodsMqConstants;
import org.dromara.goods.domain.Sku;
import org.dromara.goods.domain.Spu;
import org.dromara.goods.mapper.SkuMapper;
import org.dromara.goods.mapper.SpuMapper;
import org.dromara.goods.mq.GoodsMqConstants;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Date;

/**
 * 商品定时下架延迟消费
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoodsAutoOffConsumer {

    private final SpuMapper spuMapper;
    private final SkuMapper skuMapper;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = GoodsMqConstants.QUEUE_GOODS_AUTO_OFF_DELAY, durable = "true"),
        exchange = @Exchange(name = GoodsMqConstants.EXCHANGE_GOODS_DELAY, delayed = "true", type = ExchangeTypes.DIRECT),
        key = GoodsMqConstants.KEY_GOODS_AUTO_OFF_DELAY
    ))
    @Transactional(rollbackFor = Exception.class)
    public void handleAutoOff(Long spuId) {
        TenantHelper.ignore(() -> doAutoOff(spuId));
    }

    private void doAutoOff(Long spuId) {
        Spu spu = spuMapper.selectById(spuId);
        if (spu == null || spu.getAutoOffTime() == null || spu.getAutoOffTime().after(new Date())) {
            return;
        }
        spuMapper.update(null, Wrappers.lambdaUpdate(Spu.class)
            .eq(Spu::getId, spuId)
            .set(Spu::getSaleStatus, 0L));
        skuMapper.update(null, Wrappers.lambdaUpdate(Sku.class)
            .eq(Sku::getSpuId, spuId)
            .set(Sku::getSkuSaleStatus, 0L));
        Long[] skuIds = skuMapper.selectList(Wrappers.lambdaQuery(Sku.class)
                .select(Sku::getId)
                .eq(Sku::getSpuId, spuId))
            .stream().map(Sku::getId).toArray(Long[]::new);
        String tenantId = spu.getTenantId();
        afterCommit(() -> {
            if (skuIds.length > 0) {
                rabbitTemplate.convertAndSend(
                    GoodsMqConstants.EXCHANGE_GOODS_DIRECT,
                    GoodsMqConstants.KEY_GOODS_ES_DELETE_SKU,
                    skuIds
                );
            }
            rabbitTemplate.convertAndSend(
                DisGoodsMqConstants.EXCHANGE_GOODS_CHANGE,
                DisGoodsMqConstants.KEY_GOODS_CHANGE,
                spuId,
                message -> {
                    message.getMessageProperties().setHeader(DisGoodsMqConstants.HEADER_TENANT_ID, tenantId);
                    message.getMessageProperties().setHeader(DisGoodsMqConstants.HEADER_CHANNEL_CODE, spu.getChannelCode());
                    return message;
                }
            );
            log.info("【定时下架】完成，spuId={}, channelCode={}", spuId, spu.getChannelCode());
        });
    }

    private void afterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }
}
