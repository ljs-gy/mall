package org.dromara.goods.domain.bo;

import org.dromara.goods.domain.Image;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 商品媒体明细业务对象 goods_image
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Image.class, reverseConvertGenerate = false)
public class ImageBo extends BaseEntity {

    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    @NotNull(message = "归属 SPU ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long spuId;

    /** 归属 SKU ID（仅规格媒体） */
    private Long skuId;

    /** 媒体格式：1图片，2视频，3文件 */
    @NotNull(message = "媒体格式不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long mediaType;

    /** 媒体用途：1主图集，2SKU规格，3品质报告，4溯源报告 */
    @NotNull(message = "媒体用途不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long mediaScene;

    @NotBlank(message = "媒体 URL 地址不能为空", groups = { AddGroup.class, EditGroup.class })
    private String imageUrl;

    @NotNull(message = "排序值不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long sortOrder;

}
