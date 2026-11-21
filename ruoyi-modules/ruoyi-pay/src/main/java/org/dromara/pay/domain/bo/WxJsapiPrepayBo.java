package org.dromara.pay.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * JSAPI 预下单：parentOrderNo（合并付）与 orderNo（单店付）二选一
 */
@Data
public class WxJsapiPrepayBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 父单号（提交成功页合并支付） */
    private String parentOrderNo;

    /** 子单号（订单列表单店支付） */
    private String orderNo;

    @NotBlank(message = "openId不能为空")
    private String openId;
}
