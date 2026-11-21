package org.dromara.goods.service;

import org.dromara.goods.domain.bo.GoodsSearchBo;
import org.dromara.goods.domain.vo.GoodsSearchResultVo;

/**
 * PC 商城商品搜索（ES）
 *
 * @author ljs
 * @date 2026-07-28
 */
public interface IGoodsSearchService {

    GoodsSearchResultVo searchGoodsList(GoodsSearchBo bo);
}
