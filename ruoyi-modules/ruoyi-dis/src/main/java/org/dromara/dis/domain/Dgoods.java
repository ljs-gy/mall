package org.dromara.dis.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;

/**
 * 渠道商品发布设置 dis_dgoods
 *
 * @author ljs
 * @date 2026-08-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dis_dgoods")
public class Dgoods extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    private String channelCode;

    /** 商品发布权限（0无，1允许） */
    private Long dgoodsType;

    /** 商品自动同步（0否，1是） */
    private Long dgoodsSync;

    /** 商品自动上架（0否，1是） */
    private Long dgoodsPut;

    /** 接受上游价格（0否，1是） */
    private Long dgoodsUprice;

    /** 接受上游库存（0否，1是） */
    private Long dgoodsUnum;

    /** 修改自动更新（0否，1是） */
    private Long dgoodsEdit;

    /** 接受上游规则/无选品是否接全量（0否，1是） */
    private Long productNonAll;

    @TableLogic
    private String delFlag;
}
