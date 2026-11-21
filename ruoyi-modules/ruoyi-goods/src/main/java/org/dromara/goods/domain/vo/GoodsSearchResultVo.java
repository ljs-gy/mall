package org.dromara.goods.domain.vo;

import lombok.Data;
import org.dromara.goods.domain.dto.GoodsEsIndexDTO;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * PC 商城商品搜索结果
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
public class GoodsSearchResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<GoodsEsIndexDTO> goodsList;

    private Long total;

    private Integer totalPages;

    /** 筛选项：相关前台分类及商品数 */
    private List<CategoryFilterVo> categoryFilters;
}
