package org.dromara.user.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.dromara.user.domain.RechargeApply;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;



/**
 * 充值申请视图对象 um_recharge_apply
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = RechargeApply.class)
public class RechargeApplyVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 充值申请单号
     */
    @ExcelProperty(value = "充值申请单号")
    private String applyNo;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private Long userinfoId;

    /**
     * 用户编码
     */
    @ExcelProperty(value = "用户编码")
    private String userCode;

    /**
     * 用户名称
     */
    @ExcelProperty(value = "用户名称")
    private String userinfoName;

    /**
     * 充值入账的目标资金账户号
     */
    @ExcelProperty(value = "充值入账的目标资金账户号")
    private String accountNo;

    /**
     * 充值金额(元)
     */
    @ExcelProperty(value = "充值金额(元)")
    private BigDecimal rechargeAmount;

    /**
     * 凭证号码
     */
    @ExcelProperty(value = "凭证号码")
    private String voucherNo;

    /**
     * 合同号
     */
    @ExcelProperty(value = "合同号")
    private String contractNo;

    /**
     * 凭证图片URL
     */
    @ExcelProperty(value = "凭证图片URL")
    private String voucherUrls;

    /**
     * 摘要
     */
    @ExcelProperty(value = "摘要")
    private String summary;

    /**
     * 审核状态：0待审核，1审核通过，2审核驳回
     */
    @ExcelProperty(value = "审核状态：0待审核，1审核通过，2审核驳回")
    private Long auditStatus;

    /**
     * 审核完成时间
     */
    @ExcelProperty(value = "审核完成时间")
    private Date auditTime;

    /**
     * 审核操作人ID
     */
    @ExcelProperty(value = "审核操作人ID")
    private Long auditUserId;

    /**
     * 审核操作人姓名
     */
    @ExcelProperty(value = "审核操作人姓名")
    private String auditUserName;

    /**
     * 审核备注
     */
    @ExcelProperty(value = "审核备注")
    private String auditRemark;


}
