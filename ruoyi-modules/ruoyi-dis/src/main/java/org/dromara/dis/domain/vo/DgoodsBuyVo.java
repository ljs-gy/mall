package org.dromara.dis.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 渠道用户范围视图
 *
 * @author ljs
 * @date 2026-08-07
 */
@Data
public class DgoodsBuyVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String channelCode;
    private String dgoodsBuyType;
    private String dgoodsBuyTerm;
    private String dgoodsBuyValue;
    private String ruleValueName;
}
