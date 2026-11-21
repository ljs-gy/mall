package org.dromara.pm.mq;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 定向券全员推送的 MQ 分页接力消息体（等级人群走名单异步，不走本消息）。
 */
@Data
public class CouponDirectAllPageMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long templateId;

    private String tenantId;

    private String channelCode;

    /** 上一页最后的 userId，首页为 0 */
    private long lastUserId;

    private int pageSize;
}
