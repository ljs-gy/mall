package org.dromara.pm.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商品详情页营销信息查询参数
 */
@Data
public class PcPdpMarketingQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @NotBlank(message = "渠道编码不能为空")
    private String channelCode;

    private Long spuId;

    @NotNull(message = "SKU ID不能为空")
    private Long skuId;


    private Long categoryBackendId;

    private Long brandId;

    private String memberCode;

    /** 供应商编码（member_ccode） */
    private String memberCcode;
}
