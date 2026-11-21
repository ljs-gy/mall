package org.dromara.pm.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 营销活动互斥组配置 pm_promo_mutex_group
 */
@Data
@TableName("pm_promo_mutex_group")
public class PromoMutexGroup implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    /**
     * 互斥组编码
     */
    private String groupCode;

    /**
     * 互斥组名称
     */
    private String groupName;

    /**
     * 活动类型编码
     */
    private String promoType;

    /**
     * 组内互斥规则: 1同组互斥择优, 2绝对超级排他
     */
    private Long mutexRule;

    /**
     * 优先级(越大越先算)
     */
    private Long priority;

    /**
     * 状态: 0禁用, 1启用
     */
    private Long status;

    private Date createTime;

    private Date updateTime;

}
