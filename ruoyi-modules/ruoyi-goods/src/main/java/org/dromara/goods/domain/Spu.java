package org.dromara.goods.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serial;

/**
 * 商品SPU主对象 goods_spu
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_spu")
public class Spu extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * SPU主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 关联后台三级分类ID
     */
    private Long categoryBackendId;

    /**
     * 关联前台三级分类ID
     */
    private Long categoryFrontendId;

    /**
     * 关联品牌ID
     */
    private Long brandId;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 商品标题
     */
    private String title;

    /**
     * SPU 身份证 code（系统生成，行唯一身份）
     */
    private String spuCode;

    /**
     * 上游 SPU code（-1=自身源头；渠道副本填主站 spuCode）
     */
    private String sourceSpuCode;

    /**
     * 业务货号（用户填写；渠道与主站相同）
     */
    private String spuNo;

    /**
     * 业务归属（plat运营  supplier供应商, merchant=商家）
     */
    private String goodsClass;

    /**
     * 资源来源
     */
    private String goodsOrigin;

    /**
     * 卖家代码
     */
    private String memberCode;

    /**
     * 销售渠道编码（与营销活动 channelCode 对齐；ES 专区按此过滤）
     */
    private String channelCode;

    /**
     * 物权/所属代码
     */
    private String memberMcode;

    /**
     * 供应商代码
     */
    private String memberCcode;

    /**
     * 基础销售价
     */
    private BigDecimal priceSales;

    /**
     * 基础成本价
     */
    private BigDecimal priceCost;

    /**
     * 基础建议零售价
     */
    private BigDecimal priceMarket;

    /**
     * 计价单位
     */
    private String priceUnit;

    /**
     * 基础单位
     */
    private String baseUnit;

    /**
     * 单位换算比例
     */
    private Long unitRatio;

    /**
     * 单重
     */
    private BigDecimal singleWeight;

    /**
     * 排序权重
     */
    private Long weight;

    /**
     * 起订量
     */
    private Long minOrderQuantity;

    /**
     * 商品业务类型：1普通商品，2预售商品
     */
    private Long bizGoodsType;

    /**
     * 预售预计发货时间
     */
    private Date preSaleDeliveryTime;

    /**
     * 上架状态0下架，1上架
     */
    private Long saleStatus;

    /**
     * 审核状态：0待提交，1待审核/审核中，2审核通过，3审核驳回
     */
    private Long auditStatus;

    /**
     * 定时下架时间
     */
    private Date autoOffTime;

    /**
     * 商品详情富文本
     */
    private String detailDesc;

    /**
     * 运费模板ID（外系统，本库无模板表）
     */
    private Long freightTemplateId;

    /**
     * 删除标志：0存在，1逻辑删除
     */
    @TableLogic
    private String delFlag;


}
