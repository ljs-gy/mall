package org.dromara.pay.config;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.util.PemUtil;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.refund.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;

/**
 * 微信支付 SDK Bean 配置
 */
@Configuration
@RequiredArgsConstructor
public class WeChatPayConfig {

    private final WeChatPayProperties properties;

    @Bean
    public Config rsaAutoCertificateConfig() throws IOException {
        Resource resource = new DefaultResourceLoader().getResource(properties.getPrivateKeyPath());
        String privateKeyPem;
        try (InputStream inputStream = resource.getInputStream()) {
            privateKeyPem = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
        PrivateKey privateKey = PemUtil.loadPrivateKeyFromString(privateKeyPem);
        return new RSAAutoCertificateConfig.Builder()
            .merchantId(properties.getMerchantId())
            .privateKey(privateKey)
            .merchantSerialNumber(properties.getMerchantSerialNumber())
            .apiV3Key(properties.getApiV3Key())
            .build();
    }

    @Bean
    public NativePayService nativePayService(Config config) {
        return new NativePayService.Builder().config(config).build();
    }

    @Bean
    public JsapiServiceExtension jsapiServiceExtension(Config config) {
        return new JsapiServiceExtension.Builder().config(config).build();
    }

    @Bean
    public NotificationParser notificationParser(Config config) {
        return new NotificationParser(config);
    }

    @Bean
    public RefundService refundService(Config config) {
        return new RefundService.Builder().config(config).build();
    }
}
