package org.dromara.bidding.domain.bo;

import org.dromara.bidding.domain.Resource;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 竞价资源主业务对象 bid_resource
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Resource.class, reverseConvertGenerate = false)
public class ResourceBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 全网唯一资源号/拼盘号
     */
    @NotBlank(message = "全网唯一资源号/拼盘号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String resourceCode;

    /**
     * 商品标题/资源名称
     */
    @NotBlank(message = "商品标题/资源名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String title;

    /**
     * 商品分类ID
     */
    @NotNull(message = "商品分类ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long categoryId;

    /**
     * 牌号/规格
     */
    private String brandSpec;

    /**
     * 物理仓库ID
     */
    private Long warehouseId;

    /**
     * 资源简要说明
     */
    private String summary;

    /**
     * 实际存放提货地址
     */
    private String address;

    /**
     * 底价
     */
    @NotNull(message = "底价不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long floorPrice;

    /**
     * 资源数量/可供重量
     */
    @NotNull(message = "资源数量/可供重量不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long quantity;

    /**
     * 计量单位
     */
    @NotBlank(message = "计量单位不能为空", groups = { AddGroup.class, EditGroup.class })
    private String unit;

    /**
     * 商品图片地址JSON数组
     */
    private String images;

    /**
     * 视频介绍地址
     */
    private String videoUrl;

    /**
     * 状态（0正常 1停用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;


}
