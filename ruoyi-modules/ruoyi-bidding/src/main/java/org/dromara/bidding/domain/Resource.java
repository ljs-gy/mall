package org.dromara.bidding.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 竞价资源主对象 bid_resource
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bid_resource")
public class Resource extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 全网唯一资源号/拼盘号
     */
    private String resourceCode;

    /**
     * 商品标题/资源名称
     */
    private String title;

    /**
     * 商品分类ID
     */
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
    private Long floorPrice;

    /**
     * 资源数量/可供重量
     */
    private Long quantity;

    /**
     * 计量单位
     */
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

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;


}
