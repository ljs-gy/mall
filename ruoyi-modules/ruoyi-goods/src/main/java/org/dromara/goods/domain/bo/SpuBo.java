package org.dromara.goods.domain.bo;

import org.dromara.goods.domain.Spu;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.util.Date;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 商品SPU主业务对象 goods_spu
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Spu.class, reverseConvertGenerate = false)
public class SpuBo extends BaseEntity {

    /**
     * SPU主键ID
     */
    @NotNull(message = "SPU主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 关联后台三级分类ID
     */
    @NotNull(message = "关联后台三级分类ID不能为空", groups = { AddGroup.class, EditGroup.class })
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
    @NotBlank(message = "商品标题不能为空", groups = { AddGroup.class, EditGroup.class })
    private String title;

    /**
     * 商品编号/货号
     */
    @NotBlank(message = "商品编号/货号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String spuCode;

    private String sourceSpuCode;

    private String spuNo;

    /**
     * 业务归属（plat运营  supplier供应商, merchant=商家）
     */
    @NotBlank(message = "业务归属（plat运营  supplier供应商, merchant=商家）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String goodsClass;

    /**
     * 资源来源
     */
    @NotBlank(message = "资源来源不能为空", groups = { AddGroup.class, EditGroup.class })
    private String goodsOrigin;

    /**
     * 卖家代码
     */
    @NotBlank(message = "卖家代码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String memberCode;

    /**
     * 销售渠道编码
     */
    @NotBlank(message = "销售渠道不能为空", groups = { AddGroup.class, EditGroup.class })
    private String channelCode;

    /**
     * 物权/所属代码
     */
    @NotBlank(message = "物权/所属代码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String memberMcode;

    /**
     * 供应商代码
     */
    @NotBlank(message = "供应商代码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String memberCcode;

    /**
     * 基础销售价
     */
    @NotNull(message = "基础销售价不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal priceSales;

    /**
     * 基础成本价
     */
    @NotNull(message = "基础成本价不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal priceCost;

    /**
     * 基础建议零售价
     */
    @NotNull(message = "基础建议零售价不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal priceMarket;

    /**
     * 计价单位
     */
    @NotBlank(message = "计价单位不能为空", groups = { AddGroup.class, EditGroup.class })
    private String priceUnit;

    /**
     * 基础单位
     */
    @NotBlank(message = "基础单位不能为空", groups = { AddGroup.class, EditGroup.class })
    private String baseUnit;

    /**
     * 单位换算比例
     */
    @NotNull(message = "单位换算比例不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long unitRatio;

    /**
     * 单重
     */
    @NotNull(message = "单重不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal singleWeight;

    /**
     * 排序权重
     */
    @NotNull(message = "排序权重不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long weight;

    /**
     * 起订量
     */
    @NotNull(message = "起订量不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long minOrderQuantity;

    /**
     * 商品业务类型：1普通商品，2预售商品
     */
    @NotNull(message = "商品业务类型：1普通商品，2预售商品不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long bizGoodsType;

    /**
     * 预售预计发货时间
     */
    private Date preSaleDeliveryTime;

    /**
     * 上架状态0下架，1上架
     */
    @NotNull(message = "上架状态0下架，1上架不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long saleStatus;

    /**
     * 审核状态：0待提交，1待审核/审核中，2审核通过，3审核驳回
     */
    @NotNull(message = "审核状态：0待提交，1待审核/审核中，2审核通过，3审核驳回不能为空", groups = { AddGroup.class, EditGroup.class })
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


}
