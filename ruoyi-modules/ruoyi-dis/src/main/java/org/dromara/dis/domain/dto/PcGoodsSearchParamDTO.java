package org.dromara.dis.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * PC 渠道商品列表搜索入参
 *
 * @author ljs
 * @date 2026-08-08
 */
@Data
public class PcGoodsSearchParamDTO {

    @NotBlank(message = "租户编号不能为空")
    private String tenantId;

    /** 默认/入口渠道编码 */
    @NotBlank(message = "渠道编码不能为空")
    private String channelCode;

    /** 当前登录买家 ID（未登录传 null） */
    private Long userinfoId;

    /** 标题 / 拼音搜索词 */
    private String keyword;

    /** 后台末级分类 ID（如 "10"），匹配 category_backend_id */
    private String categoryPath;

    private Long brandId;

    /** 动态属性规格筛选 ["attrId_optionId"] */
    private List<String> attrs;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    private Integer pageNum = 1;
    private Integer pageSize = 20;

    /** 排序字段: price / sales / new */
    private String sortField;

    /** 排序方向: asc / desc */
    private String sortOrder;
}
