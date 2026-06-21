package org.dromara.bidding.domain.bo;

import org.dromara.bidding.domain.FinancialApply;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 清算申请业务对象 bid_financial_apply
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = FinancialApply.class, reverseConvertGenerate = false)
public class FinancialApplyBo extends BaseEntity {

    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    /**
     * 主键
     */
    private Long id;

    @NotBlank(message = "审批单号不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 审批单号
     */
    private String applyNo;

    @NotBlank(message = "关联的中标履约订单号不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 关联的中标履约订单号
     */
    private String orderNo;

    @NotNull(message = "目标买家用户ID不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 目标买家用户ID
     */
    private Long userId;

    @NotNull(message = "资金行为：1-收取佣金, 2-退还佣金, 3-释放保证金, 4-罚没保证金, 5-补款不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 资金行为：1-收取佣金, 2-退还佣金, 3-释放保证金, 4-罚没保证金, 5-补款
     */
    private Integer applyType;

    @NotNull(message = "申请金额不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 申请金额
     */
    private BigDecimal applyAmount;

    @NotBlank(message = "申请原因不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 申请原因
     */
    private String reason;

    /**
     * 交易凭证
     */
    private String proofImages;

    @NotNull(message = "多级审批状态：0-待运营一审, 1-运营通过交由财务终审, 2-终审通过, 3-审批拒绝驳回不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 多级审批状态：0-待运营一审, 1-运营通过交由财务终审, 2-终审通过, 3-审批拒绝驳回
     */
    private Integer auditStatus;

    @NotBlank(message = "申请人不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 申请人
     */
    private String proposerUser;

    /**
     * 审核人
     */
    private String auditorUser;

    /**
     * 审批意见
     */
    private String auditOpinion;

    @NotNull(message = "银行/第三方原路回扣状态：0-未执行, 1-成功, 2-失败不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 银行/第三方原路回扣状态：0-未执行, 1-成功, 2-失败
     */
    private Integer executePayStatus;

}