package org.dromara.order.domain.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dromara.order.api.mq.SkuRollbackItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 它是「下单 MySQL 阶段一」的出参包裹——事务里写完订单后，把后面投延时关单消息要用的快照带出来，避免事务外再查库拼。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSubmitMysqlTxResult {

    /** 各子单超时关单载荷 */
    private List<SubOrderTimeoutPayload> subOrders = new ArrayList<>();

    /** 提交时已核销的用户券；未用券为 null */
    private Long usedUserCouponId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubOrderTimeoutPayload {
        private String orderNo;
        private List<SkuRollbackItem> rollbackItems;
        private Long activityId;
        private String exclusivePromoType;
    }
}
