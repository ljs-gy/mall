package org.dromara.goods.domain.vo;

import java.util.Date;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.dromara.goods.domain.Spu;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商品SPU主视图对象 goods_spu
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Spu.class)
public class SpuVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * SPU主键ID
     */
    @ExcelProperty(value = "SPU主键ID")
    private Long id;

    /**
     * 关联后台三级分类ID
     */
    @ExcelProperty(value = "关联后台三级分类ID")
    private Long categoryBackendId;

    /**
     * 关联前台三级分类ID
     */
    @ExcelProperty(value = "关联前台三级分类ID")
    private Long categoryFrontendId;

    /**
     * 关联品牌ID
     */
    @ExcelProperty(value = "关联品牌ID")
    private Long brandId;

    /**
     * 品牌名称
     */
    @ExcelProperty(value = "品牌名称")
    private String brandName;

    /**
     * 商品标题
     */
    @ExcelProperty(value = "商品标题")
    private String title;

    /**
     * 商品编号/货号
     */
    @ExcelProperty(value = "SPU身份证code")
    private String spuCode;

    @ExcelProperty(value = "上游SPU code")
    private String sourceSpuCode;

    @ExcelProperty(value = "业务货号")
    private String spuNo;

    /**
     * 业务归属（plat运营  supplier供应商, merchant=商家）
     */
    @ExcelProperty(value = "业务归属", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "p=lat运营,s=upplier供应商,,m=erchant=商家")
    private String goodsClass;

    /**
     * 资源来源
     */
    @ExcelProperty(value = "资源来源")
    private String goodsOrigin;

    /**
     * 卖家代码
     */
    @ExcelProperty(value = "卖家代码")
    private String memberCode;

    /**
     * 销售渠道编码
     */
    @ExcelProperty(value = "销售渠道编码")
    private String channelCode;

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
     * 基础销售价
     */
    @ExcelProperty(value = "基础销售价")
    private BigDecimal priceSales;

    /**
     * 基础成本价
     */
    @ExcelProperty(value = "基础成本价")
    private BigDecimal priceCost;

    /**
     * 基础建议零售价
     */
    @ExcelProperty(value = "基础建议零售价")
    private BigDecimal priceMarket;

    /**
     * 计价单位
     */
    @ExcelProperty(value = "计价单位")
    private String priceUnit;

    /**
     * 基础单位
     */
    @ExcelProperty(value = "基础单位")
    private String baseUnit;

    /**
     * 单位换算比例
     */
    @ExcelProperty(value = "单位换算比例")
    private Long unitRatio;

    /**
     * 单重
     */
    @ExcelProperty(value = "单重")
    private BigDecimal singleWeight;

    /**
     * 排序权重
     */
    @ExcelProperty(value = "排序权重")
    private Long weight;

    /**
     * 起订量
     */
    @ExcelProperty(value = "起订量")
    private Long minOrderQuantity;

    /**
     * 商品业务类型：1普通商品，2预售商品
     */
    @ExcelProperty(value = "商品业务类型：1普通商品，2预售商品")
    private Long bizGoodsType;

    /**
     * 预售预计发货时间
     */
    @ExcelProperty(value = "预售预计发货时间")
    private Date preSaleDeliveryTime;

    /**
     * 上架状态0下架，1上架
     */
    @ExcelProperty(value = "上架状态0下架，1上架")
    private Long saleStatus;

    /**
     * 审核状态：0待提交，1待审核/审核中，2审核通过，3审核驳回
     */
    @ExcelProperty(value = "审核状态：0待提交，1待审核/审核中，2审核通过，3审核驳回")
    private Long auditStatus;

    /**
     * 定时下架时间
     */
    @ExcelProperty(value = "定时下架时间")
    private Date autoOffTime;

    /**
     * 商品详情富文本
     */
    @ExcelProperty(value = "商品详情富文本")
    private String detailDesc;

    /**
     * 运费模板ID（外系统，本库无模板表）
     */
    @ExcelProperty(value = "运费模板ID", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "外=系统，本库无模板表")
    private Long freightTemplateId;


}
