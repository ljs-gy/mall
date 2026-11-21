package org.dromara.pay.controller;

import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.pay.service.IWxPayRefundNotifyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信退款异步回调
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/wx")
public class WxPayRefundNotifyController {

    private final NotificationParser notificationParser;
    private final IWxPayRefundNotifyService wxPayRefundNotifyService;

    /**
     * 微信退款异步回调入口
     * @param wechatPaySerial  证书序列号
     * @param wechatPaySignature  签名
     * @param wechatPayTimestamp
     * @param wechatPayNonce
     * @param signType  签名算法
     * @param requestBody  原始请求体
     * @return
     */
    @PostMapping("/refund/notify")
    public ResponseEntity<Map<String, String>> handleRefundNotify(
        @RequestHeader("Wechatpay-Serial") String wechatPaySerial,
        @RequestHeader("Wechatpay-Signature") String wechatPaySignature,
        @RequestHeader("Wechatpay-Timestamp") String wechatPayTimestamp,
        @RequestHeader("Wechatpay-Nonce") String wechatPayNonce,
        @RequestHeader(value = "Wechatpay-Signature-Type", defaultValue = "WECHATPAY2-SHA256-RSA2048") String signType,
        @RequestBody String requestBody) {
        log.info("接收到微信退款回调请求");
        Map<String, String> response = new HashMap<>();
        try {
            RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(wechatPaySerial)
                .nonce(wechatPayNonce)
                .signature(wechatPaySignature)
                .timestamp(wechatPayTimestamp)
                .signType(signType)
                .body(requestBody)
                .build();
            RefundNotification notification = notificationParser.parse(requestParam, RefundNotification.class);
            wxPayRefundNotifyService.processRefundNotify(notification);
            response.put("code", "SUCCESS");
            response.put("message", "成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("微信退款回调验签或处理失败", e);
            response.put("code", "FAIL");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
