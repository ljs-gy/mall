package org.dromara.pm.domain.vo;

import java.util.Date;
import org.dromara.pm.domain.CouponUserRel;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户优惠券领用记录视图对象 pm_coupon_user_rel
 *
 * @author ljs
 * @date 2026-07-21
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CouponUserRel.class)
public class CouponUserRelVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "用户优惠券ID主键")
    private Long id;

    @ExcelProperty(value = "用户ID")
    private Long userId;

    @ExcelProperty(value = "优惠券模板ID")
    private Long couponTemplateId;

    @ExcelProperty(value = "渠道编码")
    private String channelCode;

    @ExcelProperty(value = "券名称")
    private String title;

    @ExcelProperty(value = "优惠券类型")
    private String couponType;

    @ExcelProperty(value = "活动图片")
    private String imgUrl;

    @ExcelProperty(value = "用户券唯一流水码")
    private String userCouponCode;

    @ExcelProperty(value = "门槛快照: 最低消费金额")
    private java.math.BigDecimal minSpendAmount;

    @ExcelProperty(value = "面额/折扣快照")
    private java.math.BigDecimal benefitValue;

    @ExcelProperty(value = "有效期开始时间")
    private Date startTime;

    @ExcelProperty(value = "有效期结束时间")
    private Date endTime;

    /**
     * 使用状态: 0未使用, 1已使用, 2已过期, 3已作废
     * C 端列表：库中仍为 0 但已过 endTime 时，返回值会归一为 2
     */
    @ExcelProperty(value = "使用状态: 0未使用, 1已使用, 2已过期, 3已作废")
    private Long status;

}
