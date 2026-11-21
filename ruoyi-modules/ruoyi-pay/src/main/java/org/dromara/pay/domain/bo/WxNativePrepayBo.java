package org.dromara.pay.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class WxNativePrepayBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 父单号（提交成功页合并支付） */
    private String parentOrderNo;

    /** 子单号（订单列表单店支付） */
    private String orderNo;
}
