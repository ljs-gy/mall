package org.dromara.pm.domain.vo;

import org.dromara.pm.domain.CouponGiftGrantRule;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 满赠券发放规则视图对象 pm_coupon_gift_grant_rule
 *
 * @author ljs
 * @date 2026-07-21
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CouponGiftGrantRule.class)
public class CouponGiftGrantRuleVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "主键ID")
    private Long id;

    @ExcelProperty(value = "满赠券促销名称")
    private String title;

    @ExcelProperty(value = "渠道编码")
    private String channelCode;

    @ExcelProperty(value = "物权编码")
    private String memberCode;

    @ExcelProperty(value = "物权名称")
    private String memberName;

    @ExcelProperty(value = "发放门槛")
    private BigDecimal minOrderAmount;

    @ExcelProperty(value = "优惠券模板ID")
    private Long couponTemplateId;

    @ExcelProperty(value = "赠送张数")
    private Long giveCouponCount;

    @ExcelProperty(value = "用户人群范围")
    private Long userScopeType;

    @ExcelProperty(value = "用户参与方式")
    private Long userParticipateType;

    @ExcelProperty(value = "活动开始时间")
    private Date startTime;

    @ExcelProperty(value = "活动结束时间")
    private Date endTime;

    @ExcelProperty(value = "活动图片")
    private String imgUrl;

    @ExcelProperty(value = "状态")
    private Long status;

    @ExcelProperty(value = "审核人")
    private Long auditBy;

    @ExcelProperty(value = "审核时间")
    private Date auditTime;

    @ExcelProperty(value = "审核原因")
    private String auditRemark;

}
