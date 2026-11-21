package org.dromara.order.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 订单营销参与汇总 oc_order_promo（主表只挂父单，子单归属见 oc_order_promo_item.order_no）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oc_order_promo")
public class OrderPromo extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    /** 结算父订单号 */
    private String parentOrderNo;
    private String channelCode;
    /** 1店内 2跨店 3券 4专享 */
    private Long promoSourceType;
    private String promoType;
    private Long activityId;
    /** 非券统一 0 */
    private Long userCouponId;
    private String activityTitle;
    private BigDecimal discountAmount;
    private BigDecimal platformShareAmount;
    private BigDecimal merchantShareAmount;
    /** 1已生效 2已释放(仅券/跨店 CAS；店内/专享恒为1) */
    private Long status;
}
