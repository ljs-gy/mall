package org.dromara.order.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 订单明细对象 oc_order_item
 *
 * @author ljs
 * @date 2026-08-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oc_order_item")
public class OrderItem extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 明细主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 关联订单号
     */
    private String orderNo;

    /**
     * 商品 SPU ID
     */
    private Long spuId;

    /**
     * 商品 SKU ID
     */
    private Long skuId;

    /**
     * 货品编号
     */
    private String skuCode;

    /**
     * 物权编码
     */
    private String memberCode;

    /**
     * 商品标题
     */
    private String goodsTitle;

    /**
     * 规格文本
     */
    private String specText;

    /**
     * 主图
     */
    private String coverImage;

    /**
     * 成交时单价（元）
     */
    private BigDecimal priceSales;

    /**
     * 单件基准折后实付均价
     */
    private BigDecimal sharePrice;

    /**
     * 购买数量
     */
    private Long quantity;

    /**
     * 是否为赠品: 0否(主品), 1是(赠品)
     */
    private Long isGift;

    /**
     * 触发赠品的阶梯门槛规则ID
     */
    private Long tierRuleId;

    /**
     * 小计金额（元）
     */
    private BigDecimal subTotal;

    /**
     * 当前明细行最终实付金额（元）
     */
    private BigDecimal realPayAmount;

    /**
     * 售后申请中数量（在途占用）
     */
    private Integer refundingQty;

    /**
     * 售后申请中金额（在途占用）
     */
    private BigDecimal refundingAmount;

    /**
     * 已退款成功数量
     */
    private Integer refundedQty;

    /**
     * 已退款成功金额
     */
    private BigDecimal refundedAmount;

    /**
     * 删除标志：0存在，1删除
     */
    @TableLogic
    private String delFlag;

}
