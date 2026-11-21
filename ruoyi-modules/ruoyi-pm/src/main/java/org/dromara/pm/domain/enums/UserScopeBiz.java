package org.dromara.pm.domain.enums;

/**
 * 人群范围校验所属业务，决定读取哪套 Redis 用户/等级名单。
 */
public enum UserScopeBiz {

    /** 促销活动 */
    PROMO,
    /** 优惠券模板 */
    COUPON,
    /** 赠券规则 */
    GRANT
}
