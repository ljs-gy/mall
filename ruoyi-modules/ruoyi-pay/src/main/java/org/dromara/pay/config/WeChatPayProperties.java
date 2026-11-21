package org.dromara.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "wxpay")
public class WeChatPayProperties {

    private String appid;

    private String merchantId;

    private String privateKeyPath;

    private String merchantSerialNumber;

    private String apiV3Key;

    private String notifyUrl;

    /**
     * 退款异步回调地址；为空时回退为 notifyUrl 同域 /pay/wx/refund/notify 需自行配置完整 URL
     */
    private String refundNotifyUrl;
}
