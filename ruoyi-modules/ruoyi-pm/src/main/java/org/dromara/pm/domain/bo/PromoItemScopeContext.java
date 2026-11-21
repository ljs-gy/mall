package org.dromara.pm.domain.bo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

/**
 * 促销活动商品圈货上下文（从活动配置 + Redis 名单加载，供行级资格判断复用）。
 * <ul>
 *   <li>{@code scopeSupplierType}：0=不限供应商；1=仅白名单供应商</li>
 *   <li>{@code scopeItemType}：0=不限商品；1=SKU；2=类目；3=品牌</li>
 *   <li>{@code itemParticipateType}：1=白名单；2=黑名单</li>
 * </ul>
 */
@Getter
@AllArgsConstructor
public class PromoItemScopeContext {

    /** 供应商范围类型 */
    private final Long scopeSupplierType;
    /** 商品参与类型（白/黑名单） */
    private final Long itemParticipateType;
    /** 商品范围维度 */
    private final Long scopeItemType;
    /** 供应商白名单（ID 字符串） */
    private final Set<String> supplierWhiteSet;
    /** 商品目标名单（SKU/类目/品牌 ID 字符串，白或黑取决于 participateType） */
    private final Set<String> itemTargetSet;
}
