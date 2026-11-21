package org.dromara.order.domain.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;



@Data
@EqualsAndHashCode(callSuper = true)
public class PcCartOrderSubmitBo extends PcOrderSubmitBaseBo {

    @Serial
    private static final long serialVersionUID = 1L;
}
