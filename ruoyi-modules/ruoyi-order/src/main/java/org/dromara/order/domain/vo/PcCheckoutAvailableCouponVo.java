package org.dromara.order.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 结算确认页 - 可用优惠券
 */
@Data
public class PcCheckoutAvailableCouponVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userCouponId;
    private Long templateId;
    private String title;
    private String couponType;
    private String grantType;
    private BigDecimal minSpendAmount;
    private BigDecimal benefitValue;
    private BigDecimal discountAmount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date useEndTime;

    /** 平台分摊比例（如 0.3000 = 30%） */
    private BigDecimal platformShareRate;
    /** 商家分摊比例 */
    private BigDecimal merchantShareRate;
}
