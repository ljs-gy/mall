package org.dromara.pay.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class WxNativePrepayVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 子单号（单店支付时有值） */
    private String orderNo;

    /** 父单号（合并支付时有值） */
    private String parentOrderNo;

    private String codeUrl;
}
