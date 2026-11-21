package org.dromara.dis.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 渠道对象 dis_channel
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dis_channel")
public class Channel extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 渠道唯一编码
     */
    private String channelCode;

    /**
     * 渠道名称
     */
    private String channelName;

    /**
     * 渠道类型：1线上自营 2平台 3门店
     */
    private Long channelType;

    /**
     * 渠道大类归属：1销售平台 2直销平台 3供应商平台
     */
    private Long channelSort;

    /**
     * 父渠道编码
     */
    private String parentChannelCode;

    /**
     * 
     */
    @TableLogic
    private String delFlag;


}
