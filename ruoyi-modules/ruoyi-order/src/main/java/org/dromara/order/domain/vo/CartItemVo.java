package org.dromara.order.domain.vo;

import org.dromara.order.domain.CartItem;
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
 * 购物车明细视图对象 oc_cart_item
 *
 * @author ljs
 * @date 2026-08-20
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CartItem.class)
public class CartItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 买家ID
     */
    @ExcelProperty(value = "买家ID")
    private Long userinfoId;

    /**
     * 买家业务编码（= um_userinfo.userinfo_code）
     */
    @ExcelProperty(value = "买家业务编码")
    private String memberBcode;

    /**
     * 渠道编码
     */
    @ExcelProperty(value = "渠道编码")
    private String channelCode;

    /**
     * 物权编码
     */
    @ExcelProperty(value = "物权编码")
    private String memberCode;

    /**
     * 关联商品 SPU ID
     */
    @ExcelProperty(value = "关联商品 SPU ID")
    private Long spuId;

    /**
     * 关联商品 SKU ID
     */
    @ExcelProperty(value = "关联商品 SKU ID")
    private Long skuId;

    /**
     * 货品编号
     */
    @ExcelProperty(value = "货品编号")
    private String skuCode;

    /**
     * 加购数量
     */
    @ExcelProperty(value = "加购数量")
    private Long quantity;

    /**
     * 加购时SKU单价快照
     */
    @ExcelProperty(value = "加购时SKU单价快照")
    private Long addPrice;

    /**
     * 勾选状态：0未选中，1已选中
     */
    @ExcelProperty(value = "勾选状态：0未选中，1已选中")
    private Long isChecked;


}
