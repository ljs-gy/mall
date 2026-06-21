package org.dromara.bidding.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 清算申请对象 bid_financial_apply
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bid_financial_apply")
public class FinancialApply extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 审批单号
     */
    private String applyNo;

    /**
     * 关联的中标履约订单号
     */
    private String orderNo;

    /**
     * 目标买家用户ID
     */
    private Long userId;

    /**
     * 资金行为：1-收取佣金, 2-退还佣金, 3-释放保证金, 4-罚没保证金, 5-补款
     */
    private Integer applyType;

    /**
     * 申请金额
     */
    private BigDecimal applyAmount;

    /**
     * 申请原因
     */
    private String reason;

    /**
     * 交易凭证
     */
    private String proofImages;

    /**
     * 多级审批状态：0-待运营一审, 1-运营通过交由财务终审, 2-终审通过, 3-审批拒绝驳回
     */
    private Integer auditStatus;

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

    /**
     * 银行/第三方原路回扣状态：0-未执行, 1-成功, 2-失败
     */
    private Integer executePayStatus;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;

}