package org.dromara.order.domain.vo;

import org.dromara.order.domain.OrderTimeConfig;
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
 * 订单交易时效配置视图对象 oc_order_time_config
 *
 * @author ljs
 * @date 2026-08-20
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = OrderTimeConfig.class)
public class OrderTimeConfigVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置主键ID
     */
    @ExcelProperty(value = "配置主键ID")
    private Long id;

    /**
     * 渠道编码
     */
    @ExcelProperty(value = "渠道编码")
    private String channelCode;

    /**
     * 未支付超时取消时间(分钟)，默认30分钟
     */
    @ExcelProperty(value = "未支付超时取消时间(分钟)，默认30分钟")
    private Long unpaidCancelMinutes;

    /**
     * 发货后自动确认收货时间(天)，默认7天
     */
    @ExcelProperty(value = "发货后自动确认收货时间(天)，默认7天")
    private Long autoReceiveDays;

    /**
     * 确认收货后自动交易完成时间(天)，默认7天
     */
    @ExcelProperty(value = "确认收货后自动交易完成时间(天)，默认7天")
    private Long autoCompleteDays;

    /**
     * 状态：0停用，1启用
     */
    @ExcelProperty(value = "状态：0停用，1启用")
    private Long status;


}
