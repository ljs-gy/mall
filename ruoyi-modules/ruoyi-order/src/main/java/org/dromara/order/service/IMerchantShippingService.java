package org.dromara.order.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.order.domain.bo.MerchantShippingQueryBo;
import org.dromara.order.domain.bo.MerchantShippingShipBo;
import org.dromara.order.domain.vo.MerchantShippingDetailVo;
import org.dromara.order.domain.vo.ShippingOrderVo;

/**
 * 商家发货
 */
public interface IMerchantShippingService {

    TableDataInfo<ShippingOrderVo> queryPage(MerchantShippingQueryBo query, String tenantId, PageQuery pageQuery);

    MerchantShippingDetailVo detail(String shippingNo, String tenantId, String memberCode);

    void ship(MerchantShippingShipBo bo, String tenantId, String memberCode);
}
