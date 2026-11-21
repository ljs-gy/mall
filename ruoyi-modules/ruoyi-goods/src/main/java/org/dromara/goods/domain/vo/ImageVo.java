package org.dromara.goods.domain.vo;

import org.dromara.goods.domain.Image;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商品媒体明细视图对象 goods_image
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Image.class)
public class ImageVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "主键ID")
    private Long id;

    @ExcelProperty(value = "归属SPU ID")
    private Long spuId;

    @ExcelProperty(value = "归属SKU ID")
    private Long skuId;

    @ExcelProperty(value = "媒体格式", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "1=图片,2=视频,3=文件")
    private Long mediaType;

    @ExcelProperty(value = "媒体用途", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "1=主图集,2=SKU规格,3=品质报告,4=溯源报告")
    private Long mediaScene;

    @ExcelProperty(value = "媒体URL")
    private String imageUrl;

    @ExcelProperty(value = "排序值")
    private Long sortOrder;

}
