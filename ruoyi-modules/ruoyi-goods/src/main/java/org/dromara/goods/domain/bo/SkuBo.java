package org.dromara.goods.domain.bo;

import org.dromara.goods.domain.Sku;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import lombok.EqualsAndHashCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

/**
 * 商品货品SKU业务对象 goods_sku
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Sku.class, reverseConvertGenerate = false)
public class SkuBo extends BaseEntity {

    /**
     * SKU主键ID
     */
    @NotNull(message = "SKU主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 卖家代码
     */
    @NotBlank(message = "卖家代码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String memberCode;

    /**
     * 物权/所属代码
     */
    private String memberMcode;

    /**
     * 供应商代码
     */
    private String memberCcode;

    /**
     * 销售渠道编码
     */
    private String channelCode;

    /**
     * 关联SPU ID
     */
    @NotNull(message = "关联SPU ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long spuId;

    /**
     * 货品编号
     */
    private String skuCode;

    private String sourceSkuCode;

    @NotBlank(message = "业务货号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String skuNo;

    /**
     * 规格文本
     */
    @NotBlank(message = "规格文本不能为空", groups = { AddGroup.class, EditGroup.class })
    private String specText;

    /**
     * SKU销售价
     */
    @NotNull(message = "SKU销售价不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal priceSales;

    /**
     * SKU建议零售价
     */
    @NotNull(message = "SKU建议零售价不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal priceMarket;

    /**
     * SKU成本价
     */
    @NotNull(message = "SKU成本价不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal priceCost;

    /**
     * 包装单位/计价单位
     */
    @NotBlank(message = "包装单位/计价单位不能为空", groups = { AddGroup.class, EditGroup.class })
    private String priceUnit;

    /**
     * 基础单位
     */
    @NotBlank(message = "基础单位不能为空", groups = { AddGroup.class, EditGroup.class })
    private String baseUnit;

    /**
     * 单重(kg)
     */
    @NotNull(message = "单重(kg)不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal singleWeight;

    /**
     * 该规格起订量
     */
    @NotNull(message = "该规格起订量不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long minOrderQuantity;

    /**
     * 当前物理库存
     */
    @NotNull(message = "当前物理库存不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long stock;

    /**
     * 锁定/冻结库存
     */
    @NotNull(message = "锁定/冻结库存不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long lockStock;

    /**
     * 是否倍数购买（0否，1是）
     */
    @NotNull(message = "是否倍数购买（0否，1是）不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long isMultiplierBuy;

    /**
     * 上下架状态：0下架，1上架
     */
    @NotNull(message = "上下架状态：0下架，1上架不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long skuSaleStatus;

    /**
     * 销量
     */
    private Long salesVolume;


    @NotEmpty(message = "规格选项不能为空")
    private List<Long> specOptionIds;


    @Valid
    private List<ImageBo> skuImages;

}
