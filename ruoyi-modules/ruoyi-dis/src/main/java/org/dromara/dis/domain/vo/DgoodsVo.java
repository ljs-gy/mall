package org.dromara.dis.domain.vo;

import lombok.Data;
import org.dromara.dis.domain.Dgoods;
import io.github.linpeilie.annotations.AutoMapper;

import java.io.Serial;
import java.io.Serializable;

/**
 * 渠道商品发布设置视图（同步读取用）
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
@AutoMapper(target = Dgoods.class)
public class DgoodsVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String channelCode;
    private Long dgoodsType;
    private Long dgoodsSync;
    private Long dgoodsPut;
    private Long dgoodsUprice;
    private Long dgoodsUnum;
    private Long dgoodsEdit;
    private Long productNonAll;
}
