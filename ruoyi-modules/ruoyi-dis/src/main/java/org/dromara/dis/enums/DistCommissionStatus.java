package org.dromara.dis.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 分佣明细状态
 */
@Getter
@AllArgsConstructor
public enum DistCommissionStatus {

    PENDING(0, "待结算"),
    SETTLED(1, "已结算"),
    CANCELLED(2, "已取消");

    private final int code;
    private final String label;

}
