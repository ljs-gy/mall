package org.dromara.order.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.order.domain.bo.PcAfterSaleApplyBo;
import org.dromara.order.domain.bo.PcAfterSaleReturnExpressBo;
import org.dromara.order.domain.vo.AfterSaleVo;
import org.dromara.order.domain.vo.PcAfterSaleApplyResultVo;
import org.dromara.order.domain.vo.PcAfterSaleDetailVo;


public interface IPcAfterSaleService {

    PcAfterSaleApplyResultVo apply(PcAfterSaleApplyBo bo, String tenantId, Long userinfoId);

    TableDataInfo<AfterSaleVo> listMine(String tenantId, Long userinfoId, PageQuery pageQuery);

    PcAfterSaleDetailVo detail(String afterSaleNo, String tenantId, Long userinfoId);

    void fillReturnExpress(PcAfterSaleReturnExpressBo bo, String tenantId, Long userinfoId);

    void revoke(String afterSaleNo, String tenantId, Long userinfoId);
}
