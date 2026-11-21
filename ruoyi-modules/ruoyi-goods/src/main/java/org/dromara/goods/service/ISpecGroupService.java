package org.dromara.goods.service;

import org.dromara.goods.domain.SpecGroup;
import org.dromara.goods.domain.vo.SpecGroupVo;
import org.dromara.goods.domain.bo.SpecGroupBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 商品规格组Service接口
 *
 * @author ljs
 * @date 2026-07-28
 */
public interface ISpecGroupService {

    /**
     * 查询商品规格组
     *
     * @param id 主键
     * @return 商品规格组
     */
    SpecGroupVo queryById(Long id);

    /**
     * 分页查询商品规格组列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商品规格组分页列表
     */
    TableDataInfo<SpecGroupVo> queryPageList(SpecGroupBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的商品规格组列表
     *
     * @param bo 查询条件
     * @return 商品规格组列表
     */
    List<SpecGroupVo> queryList(SpecGroupBo bo);

    /**
     * 新增商品规格组
     *
     * @param bo 商品规格组
     * @return 是否新增成功
     */
    Boolean insertByBo(SpecGroupBo bo);

    /**
     * 修改商品规格组
     *
     * @param bo 商品规格组
     * @return 是否修改成功
     */
    Boolean updateByBo(SpecGroupBo bo);

    /**
     * 校验并批量删除商品规格组信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
