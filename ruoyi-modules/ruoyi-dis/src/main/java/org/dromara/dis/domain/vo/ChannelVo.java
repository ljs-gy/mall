package org.dromara.dis.domain.vo;

import org.dromara.dis.domain.Channel;
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
 * 渠道视图对象 dis_channel
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Channel.class)
public class ChannelVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 渠道唯一编码
     */
    @ExcelProperty(value = "渠道唯一编码")
    private String channelCode;

    /**
     * 渠道名称
     */
    @ExcelProperty(value = "渠道名称")
    private String channelName;

    /**
     * 渠道类型：1线上自营 2平台 3门店
     */
    @ExcelProperty(value = "渠道类型：1线上自营 2平台 3门店")
    private Long channelType;

    /**
     * 渠道大类归属：1销售平台 2直销平台 3供应商平台
     */
    @ExcelProperty(value = "渠道大类归属：1销售平台 2直销平台 3供应商平台")
    private Long channelSort;

    /**
     * 父渠道编码
     */
    @ExcelProperty(value = "父渠道编码")
    private String parentChannelCode;


}
