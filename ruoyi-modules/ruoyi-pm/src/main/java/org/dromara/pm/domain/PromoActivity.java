package org.dromara.pm.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 营销活动主对象 pm_promo_activity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_promo_activity")
public class PromoActivity extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    private String title;

    /**
     * 渠道编码
     */
    private String channelCode;

    /**
     * 物权/归属编码
     */
    private String memberCode;

    /**
     * 物权/归属名称
     */
    private String memberName;

    /**
     * 促销类型: FULL_REDUCE/FULL_DISCOUNT/FULL_GIFT/SECKILL/...
     */
    private String promoType;

    /**
     * 计算维度: 1按金额, 2按件数
     */
    private Long calcType;

    private String imgUrl;

    private Date startTime;

    private Date endTime;

    private Long isCap;

    private Long scopeSupplierType;

    /**
     * 选择商品范围: 0全部, 1指定SKU, 2指定品类, 3指定品牌
     */
    private Long scopeItemType;

    /**
     * 商品参与方式: 1白名单, 2黑名单
     */
    private Long itemParticipateType;

    /**
     * 用户人群范围: 0全部, 1指定用户ID, 2指定会员等级
     */
    private Long userScopeType;

    /**
     * 用户参与方式: 1白名单, 2黑名单
     */
    private Long userParticipateType;

    private Long userLimitQty;

    private BigDecimal budgetTotal;

    private BigDecimal platformShareRate;

    private BigDecimal merchantShareRate;

    private Long isRefundCash;

    /**
     * 状态: 0待审核, 1未开始, 2进行中, 3已结束, 4已终止, 5已驳回
     */
    private Long status;

    private Long auditBy;

    private Date auditTime;

    private String auditRemark;

    private Long autoCancelMinutes;

    @TableLogic
    private String delFlag;

}
