package org.dromara.pm.domain.bo;

import lombok.Getter;
import lombok.Setter;
import org.dromara.pm.api.domain.RemotePromoLineItemBo;

import java.math.BigDecimal;


/**
 * PromoLineContext 是结算算价里的行级工作台：
 * 店促算完后，把每一行商品包成这个对象，后面跨店、选券、摊券都在它上面改状态，最后再刷回原来的商品行。
 */
@Getter
@Setter
public class PromoLineContext {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    /**
     * 原商品行
     */
    private final RemotePromoLineItemBo rawLine;

    /**
     * 小计
     */
    private final BigDecimal subTotal;

    /**
     * 已分到该行的店促优惠
     */
    private final BigDecimal shopDiscount;

    /**
     * 当前还能再扣的钱
     */
    private BigDecimal currentRemain;

    /**
     * 分摊到该行的跨店优惠
     */
    private BigDecimal crossDiscount = ZERO;

    /**
     * 分摊到该行的优惠劵金额
     */
    private BigDecimal couponDiscount = ZERO;

    /**
     * 该行实付
     */
    private BigDecimal realPay = ZERO;

    /**
     * 是否能参与当前选中跨店活动的分摊
     */
    private boolean crossEligible = false;


    /**
     * 是否能参与最终选中那张券的分摊
     */
    private boolean couponEligible = false;


    public PromoLineContext(RemotePromoLineItemBo rawLine) {
        this.rawLine = rawLine;
        this.subTotal = rawLine.getSubTotalAmount() != null ? rawLine.getSubTotalAmount() : ZERO;
        this.shopDiscount = rawLine.getShopDiscountAmount() != null ? rawLine.getShopDiscountAmount() : ZERO;
        this.currentRemain = this.subTotal.subtract(this.shopDiscount).max(ZERO);
        this.realPay = this.currentRemain;
    }


    /**
     * 把跨店分摊结果写进这一行，并更新还能再扣多少钱
     */
    public void applyCrossDiscount(BigDecimal cross) {
        this.crossDiscount = cross != null ? cross : ZERO;
        this.currentRemain = this.subTotal.subtract(this.shopDiscount).subtract(this.crossDiscount).max(ZERO);
        this.realPay = this.currentRemain;
    }

    /**
     * 清掉跨店预扣
     * 跨店预演后实际可扣<=0，不够分或活动失效，主流程清空跨店选中，并对各行调这个方法，
     */
    public void resetCrossDiscount() {
        this.crossDiscount = ZERO;
        this.currentRemain = this.subTotal.subtract(this.shopDiscount).max(ZERO);
        this.realPay = this.currentRemain;
    }


    /**
     * 把卷分摊结果写会这一行，并从当前残值里面扣掉
     */
    public void applyCouponDiscount(BigDecimal coupon) {
        this.couponDiscount = coupon != null ? coupon : ZERO;
        this.currentRemain = this.currentRemain.subtract(this.couponDiscount).max(ZERO);
        this.realPay = this.currentRemain;
    }


    /**
     * 把 Context 里算好的金额写回原来的商品行 BO。
     */
    public void flushToRawLine() {
        if (this.rawLine == null) {
            return;
        }
        this.rawLine.setShopDiscountAmount(this.shopDiscount);
        this.rawLine.setCrossDiscountAmount(this.crossDiscount);
        this.rawLine.setCouponDiscountAmount(this.couponDiscount);
        this.rawLine.setRealPayAmount(this.realPay);
    }
}
