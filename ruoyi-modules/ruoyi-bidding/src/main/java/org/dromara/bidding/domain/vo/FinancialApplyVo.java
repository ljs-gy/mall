package org.dromara.bidding.domain.vo;

import org.dromara.bidding.domain.FinancialApply;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 清算申请视图对象 bid_financial_apply
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = FinancialApply.class)
public class FinancialApplyVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 审批单号
     */
    @ExcelProperty(value = "审批单号")
    private String applyNo;

    /**
     * 关联的中标履约订单号
     */
    @ExcelProperty(value = "关联的中标履约订单号")
    private String orderNo;

    /**
     * 目标买家用户ID
     */
    @ExcelProperty(value = "目标买家用户ID")
    private Long userId;

    /**
     * 资金行为：1-收取佣金, 2-退还佣金, 3-释放保证金, 4-罚没保证金, 5-补款
     */
    @ExcelProperty(value = "资金行为：1-收取佣金, 2-退还佣金, 3-释放保证金, 4-罚没保证金, 5-补款")
    private Integer applyType;

    /**
     * 申请金额
     */
    @ExcelProperty(value = "申请金额")
    private BigDecimal applyAmount;

    /**
     * 申请原因
     */
    @ExcelProperty(value = "申请原因")
    private String reason;

    /**
     * 交易凭证
     */
    @ExcelProperty(value = "交易凭证")
    private String proofImages;

    /**
     * 多级审批状态：0-待运营一审, 1-运营通过交由财务终审, 2-终审通过, 3-审批拒绝驳回
     */
    @ExcelProperty(value = "多级审批状态：0-待运营一审, 1-运营通过交由财务终审, 2-终审通过, 3-审批拒绝驳回")
    private Integer auditStatus;

    /**
     * 申请人
     */
    @ExcelProperty(value = "申请人")
    private String proposerUser;

    /**
     * 审核人
     */
    @ExcelProperty(value = "审核人")
    private String auditorUser;

    /**
     * 审批意见
     */
    @ExcelProperty(value = "审批意见")
    private String auditOpinion;

    /**
     * 银行/第三方原路回扣状态：0-未执行, 1-成功, 2-失败
     */
    @ExcelProperty(value = "银行/第三方原路回扣状态：0-未执行, 1-成功, 2-失败")
    private Integer executePayStatus;

}