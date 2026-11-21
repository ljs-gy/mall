package org.dromara.dis.domain.bo;

import org.dromara.dis.domain.DistRelation;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 分销上下级关系绑定业务对象 dist_relation
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DistRelation.class, reverseConvertGenerate = false)
public class DistRelationBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 当前用户id
     */
    @NotNull(message = "当前用户id不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userinfoId;

    /**
     * 上级分销商的用户id
     */
    @NotNull(message = "上级分销商的用户id不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long parentUserinfoId;

    /**
     * 绑定关系时间
     */
    private Date bindTime;


}
