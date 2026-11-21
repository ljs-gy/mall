package org.dromara.dis.service;

import org.dromara.dis.domain.dto.PcGoodsSearchParamDTO;
import org.dromara.dis.domain.vo.PcGoodsSearchResponseVO;

/**
 * PC 渠道商品列表搜索
 *
 * @author ljs
 * @date 2026-08-08
 */
public interface IPcGoodsSearchService {

    PcGoodsSearchResponseVO searchGoodsList(PcGoodsSearchParamDTO param);
}
