package org.dromara.order.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单营销台账枚举
 */
public final class OrderPromoEnums {

    private OrderPromoEnums() {
    }

    @Getter
    @AllArgsConstructor
    public enum SourceType {
        SHOP(1L, "店内促销"),
        CROSS(2L, "跨店优惠"),
        COUPON(3L, "优惠券"),
        EXCLUSIVE(4L, "单品专享");

        private final Long code;
        private final String desc;
    }

    @Getter
    @AllArgsConstructor
    public enum RecordType {
        FORWARD(1L, "正向分摊"),
        REVERSE(2L, "逆向冲正");

        private final Long code;
        private final String desc;
    }

    @Getter
    @AllArgsConstructor
    public enum PromoStatus {
        ACTIVE(1L, "已生效"),
        RELEASED(2L, "已释放");

        private final Long code;
        private final String desc;
    }
}
