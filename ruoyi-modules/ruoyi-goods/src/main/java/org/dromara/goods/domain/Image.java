package org.dromara.goods.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 商品媒体明细对象 goods_image
 * <p>spu_id 必填；sku_id 仅规格媒体有值。列表封面取 SKU 规格图（media_scene=SKU、media_type=图片、sort_order 最小）。</p>
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_image")
public class Image extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 媒体格式：图片 */
    public static final long MEDIA_IMAGE = 1L;
    /** 媒体格式：视频 */
    public static final long MEDIA_VIDEO = 2L;
    /** 媒体格式：文件（PDF 等） */
    public static final long MEDIA_FILE = 3L;

    /** 用途：SPU 主图/视频集 */
    public static final long SCENE_GALLERY = 1L;
    /** 用途：SKU 规格媒体 */
    public static final long SCENE_SKU = 2L;
    /** 用途：品质报告 */
    public static final long SCENE_QUALITY = 3L;
    /** 用途：溯源报告 */
    public static final long SCENE_TRACE = 4L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 归属 SPU ID（SKU 媒体也冗余）
     */
    private Long spuId;

    /**
     * 归属 SKU ID（仅规格媒体有值）
     */
    private Long skuId;

    /**
     * 媒体格式：1图片，2视频，3文件
     */
    private Long mediaType;

    /**
     * 媒体用途：1主图集，2SKU规格，3品质报告，4溯源报告
     */
    private Long mediaScene;

    /**
     * 媒体完整 URL 地址
     */
    private String imageUrl;

    /**
     * 排序值（数字越小越靠前；同 scene 内图片最小者作为封面）
     */
    private Long sortOrder;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;

}
