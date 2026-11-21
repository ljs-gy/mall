package org.dromara.user.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 商家店铺对象 um_store
 *
 * @author ljs
 * @date 2026-07-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("um_store")
public class Store extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 店铺ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 所属用户信息ID
     */
    private Long userinfoId;

    /**
     * 店铺唯一编号
     */
    private String storeNo;

    /**
     * 店铺名称
     */
    private String storeName;

    /**
     * 店铺Logo
     */
    private String storeLogoUrl;

    /**
     * 店铺简介
     */
    private String storeIntro;

    /**
     * 店铺状态：1正常，0关闭
     */
    private Long status;

    /**
     * 删除标志
     */
    @TableLogic
    private String delFlag;


}
