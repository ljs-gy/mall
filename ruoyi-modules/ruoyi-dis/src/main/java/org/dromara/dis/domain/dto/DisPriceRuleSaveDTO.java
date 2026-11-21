package org.dromara.dis.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Data
public class DisPriceRuleSaveDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    private Long id;

    @NotBlank(message = "关联渠道编码不能为空")
    private String channelCode;

    @NotBlank(message = "规则名称不能为空")
    private String ruleName;


    @NotNull(message = "价格同步规则不能为空")
    private Long dpriceSyncType;

    @NotNull(message = "销售价规则值不能为空")
    private BigDecimal salesPriceRuleVal;

    private BigDecimal marketPriceRuleVal;

    private BigDecimal defaultSalesPrice;

    private BigDecimal defaultMarketPrice;


    @NotNull(message = "价格自动更新开关不能为空")
    private Long dpriceSync;

    @NotNull(message = "价格计算精度不能为空")
    private Long dpriceVer;

    @NotNull(message = "优先级权重不能为空")
    private Long dpriceWeight;

    @NotNull(message = "规则生效开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dpriceSydate;

    @NotNull(message = "规则生效失效时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dpriceEydate;


    @Valid
    private List<DisPriceRuleScopeItem> scopeItems;
}
