package org.dromara.dis.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;


@Data
public class DisUserScopeSaveDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "渠道编码不能为空")
    private String channelCode;

    @Valid
    private List<DisUserScopeItem> scopeItems;
}
