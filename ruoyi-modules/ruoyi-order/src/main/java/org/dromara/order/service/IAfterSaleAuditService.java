package org.dromara.order.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.order.domain.bo.AfterSaleAuditCommand;
import org.dromara.order.domain.bo.AfterSaleQueryBo;
import org.dromara.order.domain.vo.AfterSaleVo;
import org.dromara.order.domain.vo.PcAfterSaleDetailVo;


public interface IAfterSaleAuditService {

    void audit(AfterSaleAuditCommand command);

    TableDataInfo<AfterSaleVo> queryPage(AfterSaleQueryBo query, String tenantId, PageQuery pageQuery);

    PcAfterSaleDetailVo detail(String afterSaleNo, String tenantId, String memberCodeRestrict);
}
