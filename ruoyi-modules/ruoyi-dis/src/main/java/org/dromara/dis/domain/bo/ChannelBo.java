package org.dromara.dis.domain.bo;

import org.dromara.dis.domain.Channel;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 渠道业务对象 dis_channel
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Channel.class, reverseConvertGenerate = false)
public class ChannelBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 渠道唯一编码
     */
    @NotBlank(message = "渠道唯一编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String channelCode;

    /**
     * 渠道名称
     */
    @NotBlank(message = "渠道名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String channelName;

    /**
     * 渠道类型：1线上自营 2平台 3门店
     */
    @NotNull(message = "渠道类型：1线上自营 2平台 3门店不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long channelType;

    /**
     * 渠道大类归属：1销售平台 2直销平台 3供应商平台
     */
    @NotNull(message = "渠道大类归属：1销售平台 2直销平台 3供应商平台不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long channelSort;

    /**
     * 父渠道编码
     */
    private String parentChannelCode;


}
