package org.dromara.dis.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * PC 渠道商品列表搜索出参
 *
 * @author ljs
 * @date 2026-08-08
 */
@Data
public class PcGoodsSearchResponseVO {

    private Long total;
    private Integer pageNum;
    private Integer pageSize;

    /** 实际生效并查 ES 的渠道编码列表 */
    private List<String> actualChannels;

    private List<PcGoodsSearchItem> list;
    private List<PcFilterBrandVO> brands;
    private List<PcFilterAttrVO> attributes;
}
