package org.dromara.user.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 充值申请对象 um_recharge_apply
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("um_recharge_apply")
public class RechargeApply extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    private String applyNo;

    private Long userinfoId;

    private String userCode;

    private String userinfoName;

    private String accountNo;

    private BigDecimal rechargeAmount;

    private String voucherNo;

    private String contractNo;

    private String voucherUrls;

    private String summary;

    private Long auditStatus;

    private Date auditTime;

    private Long auditUserId;

    private String auditUserName;

    private String auditRemark;

    @TableLogic
    private String delFlag;

}
