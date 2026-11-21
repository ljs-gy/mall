package org.dromara.dis.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.dromara.dis.domain.dto.PcGoodsSearchParamDTO;
import org.dromara.dis.domain.vo.PcGoodsSearchResponseVO;
import org.dromara.dis.service.IPcGoodsSearchService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PC 渠道商品搜索（索引 dis_goods）
 * 网关路径：/dis/mall/goods/search
 *
 * @author ljs
 * @date 2026-08-08
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/mall/goods")
public class PcGoodsSearchController extends BaseController {

    private final IPcGoodsSearchService pcGoodsSearchService;

    /**
     * 渠道商品列表搜索（多渠道准入 + 品牌/属性聚合）
     */
    @GetMapping("/search")
    public R<PcGoodsSearchResponseVO> search(@Valid PcGoodsSearchParamDTO param) {
        return R.ok(pcGoodsSearchService.searchGoodsList(param));
    }
}
