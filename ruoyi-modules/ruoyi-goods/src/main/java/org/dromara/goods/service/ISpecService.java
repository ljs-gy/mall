package org.dromara.goods.service;

import org.dromara.goods.domain.Spec;
import org.dromara.goods.domain.vo.SpecVo;
import org.dromara.goods.domain.bo.SpecBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 商品规格Service接口
 *
 * @author ljs
 * @date 2026-07-28
 */
public interface ISpecService {

    /**
     * 查询商品规格
     *
     * @param id 主键
     * @return 商品规格
     */
    SpecVo queryById(Long id);

    /**
     * 分页查询商品规格列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商品规格分页列表
     */
    TableDataInfo<SpecVo> queryPageList(SpecBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的商品规格列表
     *
     * @param bo 查询条件
     * @return 商品规格列表
     */
    List<SpecVo> queryList(SpecBo bo);

    /**
     * 新增商品规格
     *
     * @param bo 商品规格
     * @return 是否新增成功
     */
    Boolean insertByBo(SpecBo bo);

    /**
     * 修改商品规格
     *
     * @param bo 商品规格
     * @return 是否修改成功
     */
    Boolean updateByBo(SpecBo bo);

    /**
     * 校验并批量删除商品规格信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
