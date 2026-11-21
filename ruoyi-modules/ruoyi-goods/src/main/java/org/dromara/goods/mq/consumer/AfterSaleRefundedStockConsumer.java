package org.dromara.goods.mq.consumer;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.goods.mapper.SkuMapper;
import org.dromara.order.api.mq.AfterSaleRefundedEvent;
import org.dromara.order.api.mq.OrderMqConstants;
import org.dromara.order.api.mq.SkuRollbackItem;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Slf4j
@Component
@RequiredArgsConstructor
public class AfterSaleRefundedStockConsumer {

    private static final long STOCK_ROLLED_TTL_SECONDS = 7L * 24 * 3600;

    private final SkuMapper skuMapper;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 售后退款完成后加回物理库存
     */
    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = OrderMqConstants.QUEUE_AFTER_SALE_REFUNDED_GOODS, durable = "true"),
        exchange = @Exchange(name = OrderMqConstants.EXCHANGE_AFTER_SALE_REFUNDED, type = ExchangeTypes.FANOUT)
    ))
    public void onAfterSaleRefunded(AfterSaleRefundedEvent event) {
        if (event == null
            || StringUtils.isBlank(event.getTenantId())
            || StringUtils.isBlank(event.getAfterSaleNo())) {
            log.warn("售后退款库存回滚消息非法，丢弃 event={}", event);
            return;
        }
        if (CollUtil.isEmpty(event.getPhysicalItems())) {
            return;
        }
        TenantHelper.dynamic(event.getTenantId(), () -> rollbackPhysicalStock(event));
    }

    private void rollbackPhysicalStock(AfterSaleRefundedEvent event) {
        String afterSaleNo = event.getAfterSaleNo();
        String claimKey = "goods:after.sale.stock.rolled:" + afterSaleNo;
        Boolean claimed = stringRedisTemplate.opsForValue().setIfAbsent(claimKey, "1", STOCK_ROLLED_TTL_SECONDS, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(claimed)) {
            log.info("售后退款库存已回滚，跳过 afterSaleNo={}", afterSaleNo);
            return;
        }
        try {
            for (SkuRollbackItem item : event.getPhysicalItems()) {
                int rows = skuMapper.rollbackPhysicalStock(item.getSkuId(), item.getBuyCount());
                if (rows <= 0) {
                    throw new ServiceException("物理库存回滚失败 skuId=" + item.getSkuId()
                        + " qty=" + item.getBuyCount());
                }
            }
            log.info("售后退款库存回滚完成 afterSaleNo={} lines={}", afterSaleNo, event.getPhysicalItems().size());
        } catch (RuntimeException e) {
            stringRedisTemplate.delete(claimKey);
            throw e;
        }
    }
}
