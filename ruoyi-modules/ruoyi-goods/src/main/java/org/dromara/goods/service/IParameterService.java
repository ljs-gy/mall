package org.dromara.goods.service;

import org.dromara.goods.domain.Parameter;
import org.dromara.goods.domain.vo.ParameterVo;
import org.dromara.goods.domain.bo.ParameterBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 商品参数Service接口
 *
 * @author ljs
 * @date 2026-07-28
 */
public interface IParameterService {

    /**
     * 查询商品参数
     *
     * @param id 主键
     * @return 商品参数
     */
    ParameterVo queryById(Long id);

    /**
     * 分页查询商品参数列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商品参数分页列表
     */
    TableDataInfo<ParameterVo> queryPageList(ParameterBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的商品参数列表
     *
     * @param bo 查询条件
     * @return 商品参数列表
     */
    List<ParameterVo> queryList(ParameterBo bo);

    /**
     * 新增商品参数
     *
     * @param bo 商品参数
     * @return 是否新增成功
     */
    Boolean insertByBo(ParameterBo bo);

    /**
     * 修改商品参数
     *
     * @param bo 商品参数
     * @return 是否修改成功
     */
    Boolean updateByBo(ParameterBo bo);

    /**
     * 校验并批量删除商品参数信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
