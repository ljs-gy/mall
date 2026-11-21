package org.dromara.goods.service;

import org.dromara.goods.domain.bo.AuditGoodsBo;
import org.dromara.goods.domain.bo.BatchSaleStatusBo;
import org.dromara.goods.domain.bo.PublishGoodsBo;
import org.dromara.goods.domain.bo.SaleStatusBo;

public interface IGoodsPublishService {


    Long publish(PublishGoodsBo bo);


    Long edit(PublishGoodsBo bo);

    Boolean audit(AuditGoodsBo bo);


    Boolean updateSaleStatus(SaleStatusBo bo);


    Boolean batchUpdateSaleStatus(BatchSaleStatusBo bo);
}
