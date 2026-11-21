package org.dromara.goods.mq.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.goods.api.mq.ExclusivePromoEsChangeEvent;
import org.dromara.goods.constant.GoodsEsConstants;
import org.dromara.goods.mq.GoodsMqConstants;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 消费 PM 排他促销变更，partial update 商品 ES 的 promo_*（不碰商品主数据）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExclusivePromoEsConsumer {

    private static final String CLEAR_SCRIPT = """
        if (ctx._source.promo_activity_id != null
            && ctx._source.promo_activity_id == params.activityId) {
          ctx._source.remove('promo_type');
          ctx._source.remove('promo_activity_id');
          ctx._source.remove('promo_price');
          ctx._source.remove('promo_status');
          ctx._source.remove('promo_start_time');
          ctx._source.remove('promo_end_time');
        }
        """;

    private final ObjectProvider<RestHighLevelClient> restHighLevelClient;
    private final ObjectMapper objectMapper;

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = GoodsMqConstants.QUEUE_EXCLUSIVE_PROMO_ES, durable = "true"),
        exchange = @Exchange(value = GoodsMqConstants.EXCHANGE_GOODS_DIRECT, type = ExchangeTypes.DIRECT),
        key = GoodsMqConstants.KEY_EXCLUSIVE_PROMO_ES
    ))
    public void onExclusivePromoEsChange(ExclusivePromoEsChangeEvent event) {
        RestHighLevelClient client = restHighLevelClient.getIfAvailable();
        if (client == null) {
            log.warn("【排他促销ES】RestHighLevelClient 未启用，跳过");
            return;
        }
        if (event == null || CollUtil.isEmpty(event.getItems()) || event.getPromoActivityId() == null) {
            log.warn("【排他促销ES】无效消息: {}", event);
            return;
        }
        TenantHelper.ignore(() -> {
            try {
                if (ExclusivePromoEsChangeEvent.ACTION_CLEAR.equalsIgnoreCase(event.getAction())) {
                    clearPromo(client, event);
                } else {
                    upsertPromo(client, event);
                }
            } catch (Exception e) {
                log.error("【排他促销ES】处理失败 activityId={} action={}",
                    event.getPromoActivityId(), event.getAction(), e);
            }
        });
    }

    private void upsertPromo(RestHighLevelClient client, ExclusivePromoEsChangeEvent event) throws Exception {
        BulkRequest bulk = new BulkRequest();
        String start = event.getPromoStartTime() == null ? null : DateUtil.formatDateTime(event.getPromoStartTime());
        String end = event.getPromoEndTime() == null ? null : DateUtil.formatDateTime(event.getPromoEndTime());
        for (ExclusivePromoEsChangeEvent.Item item : event.getItems()) {
            if (item.getSkuId() == null) {
                continue;
            }
            Map<String, Object> doc = new LinkedHashMap<>();
            doc.put("promo_type", event.getPromoType());
            doc.put("promo_activity_id", event.getPromoActivityId());
            doc.put("promo_price", item.getPromoPrice());
            doc.put("promo_status", event.getPromoStatus());
            doc.put("promo_start_time", start);
            doc.put("promo_end_time", end);
            String json = objectMapper.writeValueAsString(doc);
            // 商品文档不存在则跳过（不 upsert 半截促销文档）
            bulk.add(new UpdateRequest(GoodsEsConstants.INDEX_GOODS_SKU, String.valueOf(item.getSkuId()))
                .doc(json, XContentType.JSON)
                .docAsUpsert(false)
                .retryOnConflict(3));
        }
        executeBulk(client, bulk, event, "UPSERT");
    }

    private void clearPromo(RestHighLevelClient client, ExclusivePromoEsChangeEvent event) throws Exception {
        BulkRequest bulk = new BulkRequest();
        Map<String, Object> params = new HashMap<>();
        params.put("activityId", event.getPromoActivityId());
        Script script = new Script(ScriptType.INLINE, "painless", CLEAR_SCRIPT, params);
        for (ExclusivePromoEsChangeEvent.Item item : event.getItems()) {
            if (item.getSkuId() == null) {
                continue;
            }
            bulk.add(new UpdateRequest(GoodsEsConstants.INDEX_GOODS_SKU, String.valueOf(item.getSkuId()))
                .script(script)
                .retryOnConflict(3));
        }
        executeBulk(client, bulk, event, "CLEAR");
    }

    private void executeBulk(RestHighLevelClient client, BulkRequest bulk,
                             ExclusivePromoEsChangeEvent event, String tag) throws Exception {
        if (bulk.numberOfActions() == 0) {
            return;
        }
        BulkResponse resp = client.bulk(bulk, RequestOptions.DEFAULT);
        if (resp.hasFailures()) {
            // document_missing_exception 常见于商品尚未同步 ES
            log.warn("【排他促销ES】{} 部分失败 activityId={} detail={}",
                tag, event.getPromoActivityId(), resp.buildFailureMessage());
        } else {
            log.info("【排他促销ES】{} 成功 activityId={} actions={} channel={}",
                tag, event.getPromoActivityId(), bulk.numberOfActions(),
                StrUtil.blankToDefault(event.getChannelCode(), ""));
        }
    }
}
