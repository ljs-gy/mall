package org.dromara.order.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 结算确认页 - 商品行
 * <p>
 * 行优惠金额仅来自服务端当次算价回写，提交入参不得传入；落明细以重算结果为准。
 * </p>
 */
@Data
public class PcCheckoutProductVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** SPU ID */
    private Long spuId;
    /** SKU ID */
    private Long skuId;
    /** 货品编号 */
    private String skuCode;
    /** 商品标题 */
    private String title;
    /** 主图 URL（对应商品 coverImage） */
    private String mainPicture;
    /** 规格文本 */
    private String specText;
    /** 日常原售价 */
    private BigDecimal originalPrice;
    /** 结算单价（秒杀/特价改价后） */
    private BigDecimal actualUnitPrice;
    /** 购买数量 */
    private Integer buyCount;
    /** 行小计金额（结算单价 × 数量） */
    private BigDecimal subTotalAmount;
    /** 后台分类祖先路径（含自身，算价圈货用） */
    private List<Long> categoryAncestorIds;
    /** 品牌 ID */
    private Long brandId;
    /** 供应商 ID */
    /** 供应商编码（member_ccode） */
    private String memberCcode;

    /**
     * 本行店促选择（入参）：宜自购物车带入当前选中；null 行级默认；-1 不用；&gt;0 指定。
     * 装包未达标服务端静默清促；结算出参看 selectedShopActivityId。
     */
    private Long specifiedShopActivityId;
    /** 本行最终选中的店促活动 ID（服务端算价回写；无店促为 null） */
    private Long selectedShopActivityId;
    /** 本行选中店促类型 */
    private String selectedPromoType;
    /** 本行选中店促标题 */
    private String selectedActivityTitle;
    /** 本行选中店促的平台分摊比例 */
    private BigDecimal selectedShopPlatformShareRate;
    /** 本行选中店促的商家分摊比例 */
    private BigDecimal selectedShopMerchantShareRate;
    /**
     * 本行满赠赠品（结算出参；仅选中满赠且合范围时有值）。
     * 结算不回填 availablePromoOptions；购物车预览走 CART_PREVIEW 模式另取候选。
     */
    private List<PcCheckoutGiftItemVo> giftList;

    /** 本行分摊的店内促销优惠（服务端算价回写） */
    private BigDecimal shopDiscountAmount;
    /** 本行分摊的跨店促销优惠（服务端算价回写） */
    private BigDecimal crossDiscountAmount;
    /** 本行分摊的优惠券优惠（服务端算价回写） */
    private BigDecimal couponDiscountAmount;
    /** 本行实付（服务端算价回写） */
    private BigDecimal realPayAmount;
}
