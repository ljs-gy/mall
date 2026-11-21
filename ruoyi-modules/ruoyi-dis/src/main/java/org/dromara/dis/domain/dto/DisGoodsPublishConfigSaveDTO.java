package org.dromara.dis.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 渠道商品发布设置保存
 *
 * @author ljs
 * @date 2026-08-07
 */
@Data
public class DisGoodsPublishConfigSaveDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "渠道编码不能为空")
    private String channelCode;

    /** 商品发布权限（0无，1允许） */
    @NotNull(message = "商品发布权限不能为空")
    private Long dgoodsType;

    /** 商品自动同步（0否，1是） */
    @NotNull(message = "商品自动同步不能为空")
    private Long dgoodsSync;

    /** 商品自动上架（0否，1是） */
    @NotNull(message = "商品自动上架不能为空")
    private Long dgoodsPut;

    /** 接受上游价格（0否，1是） */
    @NotNull(message = "接受上游价格不能为空")
    private Long dgoodsUprice;

    /** 接受上游库存（0否，1是） */
    @NotNull(message = "接受上游库存不能为空")
    private Long dgoodsUnum;

    /** 修改自动更新（0否，1是） */
    @NotNull(message = "修改自动更新不能为空")
    private Long dgoodsEdit;

    /** 接受上游规则 / 无选品是否接全量（0否，1是） */
    @NotNull(message = "接受上游规则不能为空")
    private Long productNonAll;
}
