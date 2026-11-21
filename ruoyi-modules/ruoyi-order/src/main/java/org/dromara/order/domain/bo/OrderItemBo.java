package org.dromara.order.domain.bo;

import org.dromara.order.domain.OrderItem;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;

import java.math.BigDecimal;

/**
 * 订单明细业务对象 oc_order_item
 *
 * @author ljs
 * @date 2026-08-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = OrderItem.class, reverseConvertGenerate = false)
public class OrderItemBo extends BaseEntity {

    /**
     * 明细主键ID
     */
    @NotNull(message = "明细主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 关联订单号
     */
    @NotBlank(message = "关联订单号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orderNo;

    /**
     * 商品 SPU ID
     */
    @NotNull(message = "商品 SPU ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long spuId;

    /**
     * 商品 SKU ID
     */
    @NotNull(message = "商品 SKU ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long skuId;

    /**
     * 货品编号
     */
    @NotBlank(message = "货品编号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String skuCode;

    /**
     * 物权编码
     */
    @NotBlank(message = "物权编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String memberCode;

    /**
     * 商品标题
     */
    @NotBlank(message = "商品标题不能为空", groups = { AddGroup.class, EditGroup.class })
    private String goodsTitle;

    /**
     * 规格文本
     */
    @NotBlank(message = "规格文本不能为空", groups = { AddGroup.class, EditGroup.class })
    private String specText;

    /**
     * 主图
     */
    private String coverImage;

    /**
     * 成交时单价（元）
     */
    @NotNull(message = "成交时单价不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal priceSales;

    /**
     * 单件基准折后实付均价（real_pay_amount / quantity 四舍五入快照）
     */
    private BigDecimal sharePrice;

    /**
     * 购买数量
     */
    @NotNull(message = "购买数量不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long quantity;

    /**
     * 是否为赠品: 0否(主品), 1是(赠品)
     */
    @NotNull(message = "是否为赠品: 0否(主品), 1是(赠品)不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long isGift;

    /**
     * 触发赠品的阶梯门槛规则ID
     */
    private Long tierRuleId;

    /**
     * 小计金额（元）
     */
    @NotNull(message = "小计金额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal subTotal;

    /**
     * 当前明细行最终实付金额（元）
     */
    @NotNull(message = "当前明细行最终实付金额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal realPayAmount;

    /** 售后申请中数量 */
    private Integer refundingQty;

    /** 售后申请中金额 */
    private BigDecimal refundingAmount;

    /** 已退款成功数量 */
    private Integer refundedQty;

    /** 已退款成功金额 */
    private BigDecimal refundedAmount;

}
