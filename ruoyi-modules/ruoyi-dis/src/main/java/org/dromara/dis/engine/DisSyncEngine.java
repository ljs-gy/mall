package org.dromara.dis.engine;

import org.dromara.dis.domain.PriceRule;
import org.dromara.dis.domain.StockRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class DisSyncEngine {


    public BigDecimal calculateSalesPrice(BigDecimal basePrice, PriceRule rule) {
        if (basePrice == null) {
            return BigDecimal.ZERO;
        }
        if (rule == null || !on(rule.getDpriceSync())) {
            return basePrice;
        }
        return applyPrice(basePrice, rule.getDpriceSyncType(),
            rule.getSalesPriceRuleVal(), rule.getDefaultSalesPrice(), rule.getDpriceVer());
    }

    public BigDecimal calculateMarketPrice(BigDecimal basePrice, PriceRule rule) {
        if (basePrice == null) {
            return BigDecimal.ZERO;
        }
        if (rule == null || !on(rule.getDpriceSync())) {
            return basePrice;
        }
        return applyPrice(basePrice, rule.getDpriceSyncType(),
            rule.getMarketPriceRuleVal(), rule.getDefaultMarketPrice(), rule.getDpriceVer());
    }

    private BigDecimal applyPrice(BigDecimal basePrice, Long type, BigDecimal ruleVal,
                                  BigDecimal defaultPrice, Long ver) {
        BigDecimal result = basePrice;
        if (Long.valueOf(1L).equals(type)) {
            result = basePrice.multiply(ruleVal).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        } else if (Long.valueOf(2L).equals(type)) {
            result = basePrice.add(ruleVal);
        } else if (Long.valueOf(3L).equals(type)) {
            result = ruleVal != null ? ruleVal : defaultPrice != null ? defaultPrice : basePrice;
        }
        int scale = Long.valueOf(3L).equals(ver) ? 0 : Long.valueOf(2L).equals(ver) ? 1 : 2;
        return result.setScale(scale, RoundingMode.HALF_UP).max(BigDecimal.ZERO);
    }

    /** 1百分比 2固定值 */
    public Integer calculateStock(Integer baseStock, StockRule rule) {
        if (baseStock == null) {
            return 0;
        }
        if (rule == null || !on(rule.getDgnumSync())) {
            return baseStock;
        }
        BigDecimal result = BigDecimal.valueOf(baseStock);
        BigDecimal val = rule.getDgnumPrice();
        if (Long.valueOf(1L).equals(rule.getDgnumPro())) {
            result = result.multiply(val).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else if (Long.valueOf(2L).equals(rule.getDgnumPro())) {
            result = val;
        }
        int scale = Long.valueOf(3L).equals(rule.getDgnumVer()) ? 2
            : Long.valueOf(2L).equals(rule.getDgnumVer()) ? 1 : 0;
        return Math.max(0, result.setScale(scale, RoundingMode.HALF_UP).intValue());
    }

    private static boolean on(Long flag) {
        return Long.valueOf(1L).equals(flag);
    }
}
