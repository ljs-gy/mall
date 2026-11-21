package org.dromara.goods.service;

import org.dromara.goods.domain.vo.SpuParameterValueVo;
import org.dromara.goods.domain.bo.SpuParameterValueBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 商品SPU参数值Service接口
 *
 * @author ljs
 * @date 2026-07-28
 */
public interface ISpuParameterValueService {

    /**
     * 查询商品SPU参数值
     */
    SpuParameterValueVo queryById(Long id);

    /**
     * 分页查询商品SPU参数值列表
     */
    TableDataInfo<SpuParameterValueVo> queryPageList(SpuParameterValueBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的商品SPU参数值列表
     */
    List<SpuParameterValueVo> queryList(SpuParameterValueBo bo);

    /**
     * 新增商品SPU参数值
     */
    Boolean insertByBo(SpuParameterValueBo bo);

    /**
     * 修改商品SPU参数值
     */
    Boolean updateByBo(SpuParameterValueBo bo);

    /**
     * 校验并批量删除商品SPU参数值信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
