package org.dromara.goods.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 商品货品SKU对象 goods_sku
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_sku")
public class Sku extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * SKU主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 卖家代码
     */
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
    private Long spuId;

    /**
     * SKU 身份证 code（系统生成，行唯一身份）
     */
    private String skuCode;

    /**
     * 上游 SKU code（-1=自身源头；渠道副本填主站 skuCode）
     */
    private String sourceSkuCode;

    /**
     * 业务货号（用户填写；渠道与主站相同）
     */
    private String skuNo;

    /**
     * 规格文本（展示用；明细在 goods_sku_spec）
     */
    private String specText;

    /**
     * SKU销售价
     */
    private BigDecimal priceSales;

    /**
     * SKU建议零售价
     */
    private BigDecimal priceMarket;

    /**
     * SKU成本价
     */
    private BigDecimal priceCost;

    /**
     * 包装单位/计价单位
     */
    private String priceUnit;

    /**
     * 基础单位
     */
    private String baseUnit;

    /**
     * 单重(kg)
     */
    private BigDecimal singleWeight;

    /**
     * 该规格起订量
     */
    private Long minOrderQuantity;

    /**
     * 当前物理库存
     */
    private Long stock;

    /**
     * 锁定/冻结库存
     */
    private Long lockStock;

    /**
     * 乐观锁版本号
     */
    @Version
    private Long version;

    /**
     * 是否倍数购买（0否，1是）
     */
    private Long isMultiplierBuy;

    /**
     * 上下架状态：0下架，1上架
     */
    private Long skuSaleStatus;

    /**
     * 销量
     */
    private Long salesVolume;

    /**
     * 删除标志：0存在，1逻辑删除
     */
    @TableLogic
    private String delFlag;


}
