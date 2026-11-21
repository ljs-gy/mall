package org.dromara.user.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 用户经营类目资质对象 um_user_category
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("um_user_category")
public class UserCategory extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 用户信息ID
     */
    private Long userinfoId;

    /**
     * 业务类型：merchant(商家售卖类目), supplier(供应商供货类目)
     */
    private String bizType;

    /**
     * 商品平台类目ID
     */
    private Long categoryId;

    /**
     * 审核状态：0待审核，1已通过，2已驳回
     */
    private Long auditStatus;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;

}
