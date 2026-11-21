package org.dromara.goods.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.dromara.goods.domain.bo.GoodsSearchBo;
import org.dromara.goods.domain.vo.GoodsSearchResultVo;
import org.dromara.goods.service.IGoodsSearchService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PC 商城商品搜索
 * 网关路径：/goods/mall/goods/search
 *
 * @author ljs
 * @date 2026-07-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/mall/goods")
public class GoodsSearchController extends BaseController {

    private final IGoodsSearchService goodsSearchService;

    /**
     * 商品列表搜索（关键字/分类/品牌/价格/排序 + 分类聚合筛选项）
     */
    @GetMapping("/search")
    public R<GoodsSearchResultVo> search(GoodsSearchBo bo) {
        return R.ok(goodsSearchService.searchGoodsList(bo));
    }
}
