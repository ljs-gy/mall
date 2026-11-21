package org.dromara.order.domain.bo;

import lombok.Getter;

import java.math.BigDecimal;


/**
 * 营销倒挤冲正时用的内存汇总对象：把某条正向流水已经被红字冲掉了多少加总起来，用来算还剩多少件、多少钱可冲。
 */
@Getter
public class ReverseAgg {

    // 已冲正件数合计
    private int qty;
    // 已冲正优惠金额合计
    private BigDecimal discount = BigDecimal.ZERO;
    // 已冲正平台补贴合计
    private BigDecimal platform = BigDecimal.ZERO;
    // 已冲正商家承担合计
    private BigDecimal merchant = BigDecimal.ZERO;

    /**
     * 把一条逆向流水的件数和三类金额累加进去。多次部分退会有多条逆向，循环 add 后得到总量。
     */
    public void add(Integer addQty, BigDecimal addDiscount, BigDecimal addPlatform, BigDecimal addMerchant) {
        if (addQty != null && addQty > 0) {
            this.qty += addQty;
        }
        if (addDiscount != null) {
            this.discount = this.discount.add(addDiscount);
        }
        if (addPlatform != null) {
            this.platform = this.platform.add(addPlatform);
        }
        if (addMerchant != null) {
            this.merchant = this.merchant.add(addMerchant);
        }
    }
}
