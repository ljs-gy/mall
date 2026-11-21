package org.dromara.dis.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 库存规则保存
 *
 * @author ljs
 * @date 2026-08-07
 */
@Data
public class DisStockRuleSaveDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "关联渠道编码不能为空")
    private String channelCode;

    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    @NotNull(message = "库存同步规则不能为空")
    private Long dgnumPro;

    @NotNull(message = "库存规则数值不能为空")
    private BigDecimal dgnumPrice;

    @NotNull(message = "库存自动更新开关不能为空")
    private Long dgnumSync;

    @NotNull(message = "安全库存最低阈值不能为空")
    private Long dgnumMin;


    @NotNull(message = "库存计算取整规则不能为空")
    private Long dgnumVer;

    @NotNull(message = "优先级权重不能为空")
    private Long dgnumWeight;

    @NotNull(message = "生效开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dgnumStydate;

    @NotNull(message = "生效失效时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dgnumEydate;
}
