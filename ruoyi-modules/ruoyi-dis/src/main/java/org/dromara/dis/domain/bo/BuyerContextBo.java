package org.dromara.dis.domain.bo;

import lombok.Data;

/**
 * 买家渠道准入上下文
 *
 * @author ljs
 * @date 2026-08-08
 */
@Data
public class BuyerContextBo {

    private String userinfoId;
    /** 归属业务组织编码（salesman_org_code） */
    private String orgCode;
    private Long salesmanDeptId;
    private String salesmanEmpCode;
    private String provinceCode;
    private String cityCode;
    private String districtCode;
}
