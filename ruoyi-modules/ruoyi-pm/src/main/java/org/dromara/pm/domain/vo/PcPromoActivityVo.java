package org.dromara.pm.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * PC 商城活动列表项（促销活动 + 满赠发券规则统一视图）
 */
@Data
public class PcPromoActivityVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 活动ID / 满赠发券规则ID
     */
    private Long id;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 渠道编码
     */
    private String channelCode;

    /**
     * 活动/规则名称
     */
    private String title;

    /**
     * 促销类型：FULL_REDUCE / FULL_DISCOUNT / FULL_GIFT / SECKILL /
     * SPECIAL_PRICE / CROSS_FULL_REDUCE / CROSS_FULL_DISCOUNT / COUPON_GIFT
     */
    private String promoType;

    /**
     * 计算维度：1按金额, 2按件数（满赠券固定为1）
     */
    private Long calcType;

    /**
     * 宣传图片 URL
     */
    private String imgUrl;

    /**
     * 物权编码
     */
    private String memberCode;

    /**
     * 物权/归属名称
     */
    private String memberName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /**
     * 商品圈选：0全部, 1指定SKU, 2品类, 3品牌（满赠券固定0）
     */
    private Long scopeItemType;

    /**
     * 活动状态：1未开始(预热), 2进行中
     */
    private Long status;

    /**
     * 是否上不封顶（仅促销活动）
     */
    private Long isCap;


    /**
     * 满赠发券门槛金额
     */
    private BigDecimal minOrderAmount;

    /**
     * 关联赠送优惠券模板ID
     */
    private Long couponTemplateId;

    /**
     * 赠送优惠券张数
     */
    private Long giveCouponCount;

    /**
     * 阶梯规则
     */
    private List<PromoTierRuleVo> tierRules = new ArrayList<>();
}
