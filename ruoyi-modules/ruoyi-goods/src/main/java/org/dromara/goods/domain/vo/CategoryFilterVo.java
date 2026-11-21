package org.dromara.goods.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 搜索结果-前台分类筛选项
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryFilterVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long categoryId;

    private String categoryName;

    private Long goodsCount;
}
