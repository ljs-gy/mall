package org.dromara.dis.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 渠道商品范围视图
 *
 * @author ljs
 * @date 2026-08-07
 */
@Data
public class DgoodsScopelistVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String channelCode;
    private String scopeType;
    private String scopeTerm;
    private String scopeValue;
    private String ruleValueName;
}
