package org.dromara.goods.mq;

import org.dromara.goods.api.mq.GoodsExclusivePromoEsMqConstants;

/**
 * 商品中心 RabbitMQ 常量
 *
 * @author ljs
 * @date 2026-07-28
 */
public final class GoodsMqConstants {

    private GoodsMqConstants() {
    }

    /** 延迟下架交换机（x-delayed-message） */
    public static final String EXCHANGE_GOODS_DELAY = "goods.delay.exchange";
    public static final String KEY_GOODS_AUTO_OFF_DELAY = "goods.auto.off.delay";
    public static final String QUEUE_GOODS_AUTO_OFF_DELAY = "goods.auto.off.delay.queue";

    /** ES 异步同步 */
    public static final String EXCHANGE_GOODS_DIRECT = "goods.direct.exchange";
    /** 可售写入/更新 ES，消息体 Long spuId */
    public static final String KEY_GOODS_SYNC_ES = "goods.sync.es";
    public static final String QUEUE_GOODS_SYNC_ES = "goods.sync.es.queue";
    /** 下架/删除清理 ES，消息体 Long[] skuIds */
    public static final String KEY_GOODS_ES_DELETE_SKU = "goods.es.delete.sku";
    public static final String QUEUE_GOODS_ES_DELETE_SKU = "goods.es.delete.sku.queue";

    /** 排他促销投影写入商品 ES（PM 生产，goods 消费） */
    public static final String KEY_EXCLUSIVE_PROMO_ES = GoodsExclusivePromoEsMqConstants.KEY_EXCLUSIVE_PROMO_ES;
    public static final String QUEUE_EXCLUSIVE_PROMO_ES = GoodsExclusivePromoEsMqConstants.QUEUE_EXCLUSIVE_PROMO_ES;
}
