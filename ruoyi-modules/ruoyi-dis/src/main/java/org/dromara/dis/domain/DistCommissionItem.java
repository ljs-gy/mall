package org.dromara.dis.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 分佣明细表 dist_commission_item
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dist_commission_item")
public class DistCommissionItem extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    /** 分佣头表ID */
    private Long commissionOrderId;

    /** 分佣单号 */
    private String commissionNo;

    /** 业务订单号 */
    private String orderNo;

    /** 受益分销员 userinfoId */
    private Long beneficiaryUserinfoId;

    /** 分佣层级：1/2/3 */
    private Integer level;

    /** 分佣比例快照 */
    private BigDecimal rate;

    /** 佣金金额 */
    private BigDecimal amount;

    /**
     * 状态：0待结算 1已结算 2已取消
     *
     * @see org.dromara.dis.enums.DistCommissionStatus
     */
    private Integer status;

    /** 结算入账时间 */
    private Date settleTime;

    /** 取消时间 */
    private Date cancelTime;

    /** 取消原因 */
    private String cancelReason;

    /** 入账流水号 um_account_journal.journal_no */
    private String accountJournalNo;

    @TableLogic
    private String delFlag;
}
