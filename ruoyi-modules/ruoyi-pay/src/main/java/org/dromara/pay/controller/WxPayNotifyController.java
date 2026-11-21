package org.dromara.pay.controller;

import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.pay.service.IWxPayNotifyService;
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
 * 微信支付异步回调
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/wx")
public class WxPayNotifyController {

    private final NotificationParser notificationParser;
    private final IWxPayNotifyService wxPayNotifyService;

    /**
     *
     * @param wechatPaySerial  平台证书序列号，用来找验签公钥
     * @param wechatPaySignature 请求签名
     * @param wechatPayTimestamp  时间戳 防重放
     * @param wechatPayNonce
     * @param signType   签名算法
     * @param requestBody  加密后的通知 JSON 原文，必须整段参与验签
     * 这些要原样交给官方 SDK，不能自己乱改 body。
     */
    @PostMapping("/notify")
    public ResponseEntity<Map<String, String>> handleWechatPayNotify(
        @RequestHeader("Wechatpay-Serial") String wechatPaySerial,
        @RequestHeader("Wechatpay-Signature") String wechatPaySignature,
        @RequestHeader("Wechatpay-Timestamp") String wechatPayTimestamp,
        @RequestHeader("Wechatpay-Nonce") String wechatPayNonce,
        @RequestHeader(value = "Wechatpay-Signature-Type", defaultValue = "WECHATPAY2-SHA256-RSA2048") String signType,
        @RequestBody String requestBody) {
        log.info("接收到微信支付回调请求");
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
            // 验签，解密反序列化成 Transaction
            Transaction transaction = notificationParser.parse(requestParam, Transaction.class);
            if (!wxPayNotifyService.processPaymentSuccess(transaction)) {
                response.put("code", "FAIL");
                response.put("message", "订单支付确认失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            response.put("code", "SUCCESS");
            response.put("message", "成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("微信支付回调验签或处理失败", e);
            response.put("code", "FAIL");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
