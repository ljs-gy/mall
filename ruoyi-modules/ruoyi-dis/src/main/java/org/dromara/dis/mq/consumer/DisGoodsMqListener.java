package org.dromara.dis.mq.consumer;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dis.api.mq.DisGoodsMqConstants;
import org.dromara.dis.service.IDisGoodsSyncService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Arrays;


@Slf4j
@Component
@RequiredArgsConstructor
public class DisGoodsMqListener {

    private final IDisGoodsSyncService goodsSyncService;


    /**
     * 主站商品变更后，把该 SPU 自动同步到各渠道。
     */
    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = DisGoodsMqConstants.QUEUE_GOODS_CHANGE, durable = "true"),
        exchange = @Exchange(name = DisGoodsMqConstants.EXCHANGE_GOODS_CHANGE, type = ExchangeTypes.DIRECT),
        key = DisGoodsMqConstants.KEY_GOODS_CHANGE
    ))
    public void handleGoodsChange(Long spuId,
                                  @Header(name = DisGoodsMqConstants.HEADER_TENANT_ID, required = false) String tenantId,
                                  @Header(name = DisGoodsMqConstants.HEADER_CHANNEL_CODE, required = false) String channelCode) {
        if (spuId == null || StrUtil.isBlank(channelCode)) {
            log.warn("商品变更 MQ 缺少必要参数, spuId={}, channelCode={}", spuId, channelCode);
            return;
        }
        goodsSyncService.syncSingleSpuToChannels(spuId, tenantId, channelCode);
    }

    /**
     * 主站商品删除后，物理删除父渠道下各子渠道副本及 ES。
     */
    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = DisGoodsMqConstants.QUEUE_GOODS_DELETE, durable = "true"),
        exchange = @Exchange(name = DisGoodsMqConstants.EXCHANGE_GOODS_CHANGE, type = ExchangeTypes.DIRECT),
        key = DisGoodsMqConstants.KEY_GOODS_DELETE
    ))
    public void handleGoodsDelete(String[] sourceSkuCodes,
                                  @Header(name = DisGoodsMqConstants.HEADER_TENANT_ID, required = false) String tenantId,
                                  @Header(name = DisGoodsMqConstants.HEADER_CHANNEL_CODE, required = false) String channelCode) {
        if (sourceSkuCodes == null || sourceSkuCodes.length == 0 || StrUtil.isBlank(channelCode)) {
            log.warn("商品删除 MQ 缺少必要参数, channelCode={}, codes={}", channelCode,
                sourceSkuCodes == null ? null : sourceSkuCodes.length);
            return;
        }
        goodsSyncService.removeChannelGoodsBySourceSkuCodes(tenantId, channelCode, Arrays.asList(sourceSkuCodes));
    }
}
