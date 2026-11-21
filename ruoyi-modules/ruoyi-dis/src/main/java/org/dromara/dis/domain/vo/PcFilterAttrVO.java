package org.dromara.dis.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * PC 商品筛选项-属性
 *
 * @author ljs
 * @date 2026-08-08
 */
@Data
public class PcFilterAttrVO {

    private Long attrId;
    private String attrName;
    private List<PcFilterAttrOptionVO> options;
}
