package org.dromara.pm.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.dromara.pm.domain.CouponTemplate;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CouponTemplate.class)
public class CouponTemplateVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 优惠券模板ID主键
     */
    @ExcelProperty(value = "优惠券模板ID主键")
    private Long id;

    /**
     * 优惠券名称
     */
    @ExcelProperty(value = "优惠券名称")
    private String title;

    /**
     * 渠道编码
     */
    @ExcelProperty(value = "渠道编码")
    private String channelCode;

    /**
     * 优惠券类型: REDUCE(满减券), DISCOUNT(折扣券)
     */
    @ExcelProperty(value = "优惠券类型: REDUCE(满减券), DISCOUNT(折扣券)")
    private String couponType;

    /**
     * 发放方式: NORMAL(普通券), GIFT(满赠券), DIRECT(定向券)
     */
    @ExcelProperty(value = "发放方式: NORMAL(普通券), GIFT(满赠券), DIRECT(定向券)")
    private String grantType;

    /**
     * 活动图片URL
     */
    @ExcelProperty(value = "活动图片URL")
    private String imgUrl;

    /**
     * 使用门槛: 最低消费金额
     */
    @ExcelProperty(value = "使用门槛: 最低消费金额")
    private BigDecimal minSpendAmount;

    /**
     * 优惠强度: 满减券表示减免面额(元), 折扣券表示折扣率(如0.85表示85折)
     */
    @ExcelProperty(value = "优惠强度: 满减券表示减免面额(元), 折扣券表示折扣率(如0.85表示85折)")
    private BigDecimal benefitValue;

    /**
     * 优惠券总张数
     */
    @ExcelProperty(value = "优惠券总张数")
    private Long totalCount;

    /**
     * 每个会员ID可领取张数（-1表示不限制）
     */
    @ExcelProperty(value = "每个会员ID可领取张数（-1不限制）")
    private Long userLimitCount;

    /**
     * 领用开始时间
     */
    @ExcelProperty(value = "领用开始时间")
    private Date claimStartTime;

    /**
     * 领用结束时间
     */
    @ExcelProperty(value = "领用结束时间")
    private Date claimEndTime;

    /**
     * 使用开始时间（固定时段）
     */
    @ExcelProperty(value = "使用开始时间")
    private Date useStartTime;

    /**
     * 使用结束时间（固定时段）
     */
    @ExcelProperty(value = "使用结束时间")
    private Date useEndTime;

    /**
     * 相对有效分钟数（领取后有效）
     */
    @ExcelProperty(value = "相对有效分钟数")
    private Long validMinutes;

    /**
     * 定向券每个用户发放张数
     */
    @ExcelProperty(value = "定向券每个用户发放张数")
    private Long directPerUserCount;

    /**
     * 定向券发送时间类型: 1立即发送, 2定时发送
     */
    @ExcelProperty(value = "定向券发送时间类型: 1立即发送, 2定时发送")
    private Long sendType;

    /**
     * 定向券定时发送时间
     */
    @ExcelProperty(value = "定向券定时发送时间")
    private Date sendTime;

    /**
     * 选择供应商: 0全部供应商, 1指定供应商
     */
    @ExcelProperty(value = "选择供应商: 0全部供应商, 1指定供应商")
    private Long scopeSupplierType;

    /**
     * 选择商品范围: 0全部, 1SKU, 2品类, 3品牌
     */
    @ExcelProperty(value = "选择商品范围")
    private Long scopeItemType;

    /**
     * 商品参与方式: 1白名单, 2黑名单
     */
    @ExcelProperty(value = "商品参与方式")
    private Long itemParticipateType;

    /**
     * 用户人群范围: 0全部, 1用户ID, 2会员等级
     */
    @ExcelProperty(value = "用户人群范围")
    private Long userScopeType;

    /**
     * 用户参与方式: 1参与活动(白名单), 2不参与活动(黑名单)
     */
    @ExcelProperty(value = "用户参与方式: 1参与活动(白名单), 2不参与活动(黑名单)")
    private Long userParticipateType;

    /**
     * 平台分摊比例
     */
    @ExcelProperty(value = "平台分摊比例")
    private BigDecimal platformShareRate;

    /**
     * 商家分摊比例
     */
    @ExcelProperty(value = "商家分摊比例")
    private BigDecimal merchantShareRate;

    /**
     * 退款是否返回优惠券: 0否, 1是
     */
    @ExcelProperty(value = "退款是否返回优惠券: 0否, 1是")
    private Long isRefundCoupon;

    /**
     * 优惠券使用说明
     */
    @ExcelProperty(value = "优惠券使用说明")
    private String useInstructions;

    /**
     * 状态: 0待审核, 1未开始, 2进行中, 3已结束, 4已终止, 5审核失败
     */
    @ExcelProperty(value = "状态: 0待审核, 1未开始, 2进行中, 3已结束, 4已终止, 5审核失败")
    private Long status;

    /**
     * 乐观锁版本号
     */
    @ExcelProperty(value = "乐观锁版本号")
    private Integer version;

    /**
     * 审核人
     */
    @ExcelProperty(value = "审核人")
    private Long auditBy;

    /**
     * 审核时间
     */
    @ExcelProperty(value = "审核时间")
    private Date auditTime;

    /**
     * 审核原因
     */
    @ExcelProperty(value = "审核原因")
    private String auditRemark;

    /**
     * 剩余可领库存
     */
    private Long remainStock;

    /**
     * 当前用户已领取张数
     */
    private Long userClaimedQty;

}
