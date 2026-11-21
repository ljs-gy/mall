package org.dromara.goods.domain.vo;

import org.dromara.goods.domain.Sku;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;



/**
 * 商品货品SKU视图对象 goods_sku
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Sku.class)
public class SkuVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * SKU主键ID
     */
    @ExcelProperty(value = "SKU主键ID")
    private Long id;

    /**
     * 卖家代码
     */
    @ExcelProperty(value = "卖家代码")
    private String memberCode;

    /**
     * 物权/所属代码
     */
    @ExcelProperty(value = "物权/所属代码")
    private String memberMcode;

    /**
     * 供应商代码
     */
    @ExcelProperty(value = "供应商代码")
    private String memberCcode;

    /**
     * 销售渠道编码
     */
    @ExcelProperty(value = "销售渠道编码")
    private String channelCode;

    /**
     * 关联SPU ID
     */
    @ExcelProperty(value = "关联SPU ID")
    private Long spuId;

    /**
     * 货品编号
     */
    @ExcelProperty(value = "SKU身份证code")
    private String skuCode;

    @ExcelProperty(value = "上游SKU code")
    private String sourceSkuCode;

    @ExcelProperty(value = "业务货号")
    private String skuNo;

    /**
     * 规格文本
     */
    @ExcelProperty(value = "规格文本")
    private String specText;

    /**
     * SKU销售价
     */
    @ExcelProperty(value = "SKU销售价")
    private BigDecimal priceSales;

    /**
     * SKU建议零售价
     */
    @ExcelProperty(value = "SKU建议零售价")
    private BigDecimal priceMarket;

    /**
     * SKU成本价
     */
    @ExcelProperty(value = "SKU成本价")
    private BigDecimal priceCost;

    /**
     * 包装单位/计价单位
     */
    @ExcelProperty(value = "包装单位/计价单位")
    private String priceUnit;

    /**
     * 基础单位
     */
    @ExcelProperty(value = "基础单位")
    private String baseUnit;

    /**
     * 单重(kg)
     */
    @ExcelProperty(value = "单重(kg)")
    private BigDecimal singleWeight;

    /**
     * 该规格起订量
     */
    @ExcelProperty(value = "该规格起订量")
    private Long minOrderQuantity;

    /**
     * 当前物理库存
     */
    @ExcelProperty(value = "当前物理库存")
    private Long stock;

    /**
     * 锁定/冻结库存
     */
    @ExcelProperty(value = "锁定/冻结库存")
    private Long lockStock;

    /**
     * 是否倍数购买（0否，1是）
     */
    @ExcelProperty(value = "是否倍数购买", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "0=否，1是")
    private Long isMultiplierBuy;

    /**
     * 上下架状态：0下架，1上架
     */
    @ExcelProperty(value = "上下架状态：0下架，1上架")
    private Long skuSaleStatus;

    /**
     * 销量
     */
    @ExcelProperty(value = "销量")
    private Long salesVolume;

}
