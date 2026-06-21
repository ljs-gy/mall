package org.dromara.bidding.domain.vo;

import org.dromara.bidding.domain.Resource;
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
 * 竞价资源主视图对象 bid_resource
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Resource.class)
public class ResourceVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 全网唯一资源号/拼盘号
     */
    @ExcelProperty(value = "全网唯一资源号/拼盘号")
    private String resourceCode;

    /**
     * 商品标题/资源名称
     */
    @ExcelProperty(value = "商品标题/资源名称")
    private String title;

    /**
     * 商品分类ID
     */
    @ExcelProperty(value = "商品分类ID")
    private Long categoryId;

    /**
     * 牌号/规格
     */
    @ExcelProperty(value = "牌号/规格")
    private String brandSpec;

    /**
     * 物理仓库ID
     */
    @ExcelProperty(value = "物理仓库ID")
    private Long warehouseId;

    /**
     * 资源简要说明
     */
    @ExcelProperty(value = "资源简要说明")
    private String summary;

    /**
     * 实际存放提货地址
     */
    @ExcelProperty(value = "实际存放提货地址")
    private String address;

    /**
     * 底价
     */
    @ExcelProperty(value = "底价")
    private Long floorPrice;

    /**
     * 资源数量/可供重量
     */
    @ExcelProperty(value = "资源数量/可供重量")
    private Long quantity;

    /**
     * 计量单位
     */
    @ExcelProperty(value = "计量单位")
    private String unit;

    /**
     * 商品图片地址JSON数组
     */
    @ExcelProperty(value = "商品图片地址JSON数组")
    private String images;

    /**
     * 视频介绍地址
     */
    @ExcelProperty(value = "视频介绍地址")
    private String videoUrl;

    /**
     * 状态（0正常 1停用）
     */
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "0=正常,1=停用")
    private String status;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;


}
