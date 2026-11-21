package org.dromara.order.domain.vo;

import org.dromara.order.domain.AfterSaleItem;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 售后单明细视图对象 oc_after_sale_item
 *
 * @author ljs
 * @date 2026-08-20
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = AfterSaleItem.class)
public class AfterSaleItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "明细主键ID")
    private Long id;

    @ExcelProperty(value = "关联售后单号")
    private String afterSaleNo;

    @ExcelProperty(value = "关联店铺子订单号")
    private String orderNo;

    @ExcelProperty(value = "关联订单明细ID")
    private Long orderItemId;

    @ExcelProperty(value = "退款 SKU ID")
    private Long skuId;

    @ExcelProperty(value = "货品编号")
    private String skuCode;

    @ExcelProperty(value = "商品标题")
    private String goodsTitle;

    @ExcelProperty(value = "规格文本")
    private String specText;

    @ExcelProperty(value = "商品图片")
    private String coverImage;

    @ExcelProperty(value = "本次申请退货数量")
    private Integer returnQuantity;

    @ExcelProperty(value = "退款单价")
    private BigDecimal refundPrice;

    @ExcelProperty(value = "明细分摊退款金额")
    private BigDecimal refundAmount;


}
