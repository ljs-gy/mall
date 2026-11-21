package org.dromara.dis.domain.vo;

import org.dromara.dis.domain.DistSetting;
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
 * 分销基础配置视图对象 dist_setting
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DistSetting.class)
public class DistSettingVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 基础一级分佣比例
     */
    @ExcelProperty(value = "基础一级分佣比例")
    private java.math.BigDecimal baseFirstRate;

    /**
     * 基础二级分佣比例
     */
    @ExcelProperty(value = "基础二级分佣比例")
    private java.math.BigDecimal baseSecondRate;

    /**
     * 基础三级分佣比例
     */
    @ExcelProperty(value = "基础三级分佣比例")
    private java.math.BigDecimal baseThirdRate;

    /**
     * 返佣资质：0-所有人均可获得返佣，1-仅分销员用户获得返佣
     */
    @ExcelProperty(value = "返佣资质：0-所有人均可获得返佣，1-仅分销员用户获得返佣")
    private Long qualifyType;

    /**
     * 分销员自己购买商品是否有佣金：0-无佣金，1-有佣金
     */
    @ExcelProperty(value = "分销员自己购买商品是否有佣金：0-无佣金，1-有佣金")
    private Long isBuyerHasCommission;

    /**
     * 加入分销页面背景图
     */
    @ExcelProperty(value = "加入分销页面背景图")
    private String joinBgUrl;

    /**
     * 邀请注册页面背景图
     */
    @ExcelProperty(value = "邀请注册页面背景图")
    private String inviteBgUrl;


}
