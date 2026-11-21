package org.dromara.goods.mapper;

import org.dromara.goods.domain.Sku;
import org.dromara.goods.domain.vo.SkuVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;


public interface SkuMapper extends BaseMapperPlus<Sku, SkuVo> {


    int deductPhysicalStock(@Param("skuId") Long skuId, @Param("qty") int qty);


    int confirmPhysicalStock(@Param("skuId") Long skuId, @Param("qty") int qty);


    int rollbackPhysicalStock(@Param("skuId") Long skuId, @Param("qty") int qty);
}
