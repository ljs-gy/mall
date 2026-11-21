package org.dromara.pay.service.impl;

import com.wechat.pay.java.service.refund.model.RefundNotification;
import com.wechat.pay.java.service.refund.model.Status;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.order.api.RemoteOrderService;
import org.dromara.order.api.domain.RemoteAfterSaleRefundConfirmBo;
import org.dromara.pay.service.IWxPayRefundNotifyService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 退款回调：SUCCESS → 确认售后单退款完成
 */
@Slf4j
@Service
public class WxPayRefundNotifyServiceImpl implements IWxPayRefundNotifyService {

    @DubboReference
    private RemoteOrderService remoteOrderService;

    @Override
    public void processRefundNotify(RefundNotification notification) {
        if (notification == null || StringUtils.isBlank(notification.getOutRefundNo())) {
            throw new ServiceException("退款回调数据不完整");
        }
        String outRefundNo = notification.getOutRefundNo();
        Status refundStatus = notification.getRefundStatus();
        log.info("收到退款回调 outRefundNo={} status={} refundId={}", outRefundNo, refundStatus, notification.getRefundId());
        if (refundStatus == null) {
            return;
        }
        if (!Status.SUCCESS.equals(refundStatus)) {
            log.info("退款非成功状态，忽略 outRefundNo={} status={}", outRefundNo, refundStatus);
            return;
        }
        RemoteAfterSaleRefundConfirmBo confirmBo = new RemoteAfterSaleRefundConfirmBo();
        confirmBo.setAfterSaleNo(outRefundNo);
        confirmBo.setRefundTradeNo(notification.getRefundId());
        confirmBo.setRefundTime(new Date());
        boolean ok = remoteOrderService.confirmAfterSaleRefunded(confirmBo);
        if (!ok) {
            throw new ServiceException("售后退款确认失败");
        }
    }
}
