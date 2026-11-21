package org.dromara.pay.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.order.api.RemoteOrderService;
import org.dromara.order.api.domain.RemotePayOrderSummary;
import org.dromara.pay.config.WeChatPayProperties;
import org.dromara.pay.domain.bo.WxJsapiPrepayBo;
import org.dromara.pay.domain.bo.WxNativePrepayBo;
import org.dromara.pay.domain.vo.WxNativePrepayVo;
import org.dromara.pay.service.IWxPayCloseService;
import org.dromara.pay.service.IWxPayPrepayService;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 微信预下单：合并付(parentOrderNo) / 单店付(orderNo)；全程持父单通道锁。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WxPayPrepayServiceImpl implements IWxPayPrepayService {

    private final WeChatPayProperties wxPayProperties;
    private final NativePayService nativePayService;
    private final JsapiServiceExtension jsapiServiceExtension;
    private final RedissonClient redissonClient;
    private final IWxPayCloseService wxPayCloseService;

    @DubboReference
    private RemoteOrderService remoteOrderService;

    @Override
    public WxNativePrepayVo createNativePrepay(WxNativePrepayBo bo, String tenantId, Long userinfoId) {
        validatePayTarget(bo.getParentOrderNo(), bo.getOrderNo());
        if (StringUtils.isNotBlank(bo.getOrderNo())) {
            return createSubNativePrepay(bo.getOrderNo(), tenantId, userinfoId);
        }
        return createParentNativePrepay(bo.getParentOrderNo(), tenantId, userinfoId);
    }

    @Override
    public PrepayWithRequestPaymentResponse createJsapiPrepay(WxJsapiPrepayBo bo, String tenantId, Long userinfoId) {
        validatePayTarget(bo.getParentOrderNo(), bo.getOrderNo());
        if (StringUtils.isNotBlank(bo.getOrderNo())) {
            return createSubJsapiPrepay(bo.getOrderNo(), bo.getOpenId(), tenantId, userinfoId);
        }
        return createParentJsapiPrepay(bo.getParentOrderNo(), bo.getOpenId(), tenantId, userinfoId);
    }

    /**
     * 用父单号向微信要一张付款码，一次付清该父单下未付子单。
     */
    private WxNativePrepayVo createParentNativePrepay(String parentOrderNo, String tenantId, Long userinfoId) {
        return withParentChannelLock(parentOrderNo, () -> {
            RemotePayOrderSummary parent = loadPayableParent(tenantId, userinfoId, parentOrderNo);
            PrepayRequest request = buildNativeRequest(parent.getParentOrderNo(), parent.getPayAmount(), parent.getTenantId());
            PrepayResponse response = nativePayService.prepay(request);
            WxNativePrepayVo vo = new WxNativePrepayVo();
            vo.setParentOrderNo(parentOrderNo);
            vo.setCodeUrl(response.getCodeUrl());
            return vo;
        });
    }

    private PrepayWithRequestPaymentResponse createParentJsapiPrepay(String parentOrderNo, String openId,
                                                                     String tenantId, Long userinfoId) {
        return withParentChannelLock(parentOrderNo, () -> {
            RemotePayOrderSummary parent = loadPayableParent(tenantId, userinfoId, parentOrderNo);
            return jsapiServiceExtension.prepayWithRequestPayment(
                buildJsapiRequest(parent.getParentOrderNo(), parent.getPayAmount(), parent.getTenantId(), openId));
        });
    }

    /**
     * 用子单号向微信申请付款码，给用户扫码付这一家。
     */
    private WxNativePrepayVo createSubNativePrepay(String orderNo, String tenantId, Long userinfoId) {
        RemotePayOrderSummary sub = loadPayableSub(tenantId, userinfoId, orderNo);
        return withParentChannelLock(sub.getParentOrderNo(), () -> {
            RemotePayOrderSummary latest = loadPayableSub(tenantId, userinfoId, orderNo);
            // 先关掉可能已存在的合单微信单。
            // 场景：用户成功页先点过合并支付，又回列表改付一家——合单码还开着会冲突，所以单付前先关合单通道。
            closeParentWxOrderQuietly(latest.getParentOrderNo());
            PrepayRequest request = buildNativeRequest(latest.getOrderNo(), latest.getPayAmount(), latest.getTenantId());
            PrepayResponse response = nativePayService.prepay(request);
            WxNativePrepayVo vo = new WxNativePrepayVo();
            vo.setOrderNo(orderNo);
            vo.setParentOrderNo(latest.getParentOrderNo());
            vo.setCodeUrl(response.getCodeUrl());
            return vo;
        });
    }

    private PrepayWithRequestPaymentResponse createSubJsapiPrepay(String orderNo, String openId,
                                                                  String tenantId, Long userinfoId) {
        RemotePayOrderSummary sub = loadPayableSub(tenantId, userinfoId, orderNo);
        return withParentChannelLock(sub.getParentOrderNo(), () -> {
            RemotePayOrderSummary latest = loadPayableSub(tenantId, userinfoId, orderNo);
            closeParentWxOrderQuietly(latest.getParentOrderNo());
            return jsapiServiceExtension.prepayWithRequestPayment(
                buildJsapiRequest(latest.getOrderNo(), latest.getPayAmount(), latest.getTenantId(), openId));
        });
    }

    private RemotePayOrderSummary loadPayableSub(String tenantId, Long userinfoId, String orderNo) {
        RemotePayOrderSummary order = remoteOrderService.getPayableOrder(tenantId, userinfoId, orderNo);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        if (Boolean.TRUE.equals(order.getPaid())) {
            throw new ServiceException("订单已支付");
        }
        if (!Boolean.TRUE.equals(order.getUnpaid())) {
            throw new ServiceException("订单状态非待支付");
        }
        if (order.getPayAmount() == null || order.getPayAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("订单应付金额异常");
        }
        return order;
    }

    private RemotePayOrderSummary loadPayableParent(String tenantId, Long userinfoId, String parentOrderNo) {
        RemotePayOrderSummary order = remoteOrderService.getPayableParentOrder(tenantId, userinfoId, parentOrderNo);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        if (Boolean.TRUE.equals(order.getPaid()) || !Boolean.TRUE.equals(order.getUnpaid())) {
            throw new ServiceException("合并订单已支付或无可付子单");
        }
        if (order.getPayAmount() == null || order.getPayAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("合并应付金额异常");
        }
        return order;
    }

    /**
     * 抢父单支付通道锁（和取消、合单预下单同一把），避免一边合单付、一边单店付、一边关单。
     */
    private <T> T withParentChannelLock(String parentOrderNo, Supplier<T> action) {
        if (StringUtils.isBlank(parentOrderNo)) {
            return action.get();
        }
        String lockKey = "lock:pay:channel:" + parentOrderNo;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(3, 30, TimeUnit.SECONDS)) {
                throw new ServiceException("正在处理支付或取消，请稍后重试");
            }
            try {
                return action.get();
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException("拉起支付中断，请稍后重试");
        }
    }

    private void closeParentWxOrderQuietly(String parentOrderNo) {
        if (StringUtils.isBlank(parentOrderNo)) {
            return;
        }
        try {
            wxPayCloseService.closeUnpaidOrder(parentOrderNo);
            log.info("单付前已关闭合并支付通道 parentOrderNo={}", parentOrderNo);
        } catch (Exception e) {
            log.warn("关闭合并支付通道异常(忽略后继续单付) parentOrderNo={}", parentOrderNo, e);
        }
    }

    private void validatePayTarget(String parentOrderNo, String orderNo) {
        boolean hasParent = StringUtils.isNotBlank(parentOrderNo);
        boolean hasOrder = StringUtils.isNotBlank(orderNo);
        if (hasParent == hasOrder) {
            throw new ServiceException("请传 parentOrderNo 或 orderNo（二选一）");
        }
    }

    private PrepayRequest buildNativeRequest(String outTradeNo, BigDecimal payAmountYuan, String tenantId) {
        PrepayRequest request = new PrepayRequest();
        request.setAppid(wxPayProperties.getAppid());
        request.setMchid(wxPayProperties.getMerchantId());
        request.setDescription("订单支付-" + outTradeNo);
        request.setOutTradeNo(outTradeNo);
        request.setNotifyUrl(wxPayProperties.getNotifyUrl());
        request.setAttach(tenantId);
        Amount amount = new Amount();
        amount.setTotal(toCent(payAmountYuan));
        amount.setCurrency("CNY");
        request.setAmount(amount);
        return request;
    }

    private com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest buildJsapiRequest(
        String outTradeNo, BigDecimal payAmountYuan, String tenantId, String openId) {
        com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest request =
            new com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest();
        request.setAppid(wxPayProperties.getAppid());
        request.setMchid(wxPayProperties.getMerchantId());
        request.setDescription("订单支付-" + outTradeNo);
        request.setOutTradeNo(outTradeNo);
        request.setNotifyUrl(wxPayProperties.getNotifyUrl());
        request.setAttach(tenantId);
        com.wechat.pay.java.service.payments.jsapi.model.Amount amount =
            new com.wechat.pay.java.service.payments.jsapi.model.Amount();
        amount.setTotal(toCent(payAmountYuan));
        amount.setCurrency("CNY");
        request.setAmount(amount);
        Payer payer = new Payer();
        payer.setOpenid(openId);
        request.setPayer(payer);
        return request;
    }

    private int toCent(BigDecimal yuan) {
        BigDecimal amount = yuan == null ? BigDecimal.ZERO : yuan;
        return amount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).intValue();
    }
}
