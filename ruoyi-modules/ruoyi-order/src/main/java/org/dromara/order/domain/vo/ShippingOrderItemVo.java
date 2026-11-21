package org.dromara.order.domain.vo;

import org.dromara.order.domain.ShippingOrderItem;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * 发货单包裹明细视图对象 oc_shipping_order_item
 *
 * @author ljs
 * @date 2026-08-20
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = ShippingOrderItem.class)
public class ShippingOrderItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 明细主键ID
     */
    @ExcelProperty(value = "明细主键ID")
    private Long id;

    /**
     * 关联发货单号
     */
    @ExcelProperty(value = "关联发货单号")
    private String shippingNo;

    /**
     * 关联店铺子订单号
     */
    @ExcelProperty(value = "关联店铺子订单号")
    private String orderNo;

    /**
     * 关联订单明细ID
     */
    @ExcelProperty(value = "关联订单明细ID")
    private Long orderItemId;

    /**
     * SKU ID
     */
    @ExcelProperty(value = "SKU ID")
    private Long skuId;

    /**
     * 本包裹实际发货数量
     */
    @ExcelProperty(value = "本包裹实际发货数量")
    private Long shippingQuantity;


}
