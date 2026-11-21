package org.dromara.dis.service;

import org.dromara.dis.domain.DistUserGroup;
import org.dromara.dis.domain.vo.DistUserGroupVo;
import org.dromara.dis.domain.bo.DistUserGroupBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 分销员与分组关系Service接口
 *
 * @author ljs
 * @date 2026-08-06
 */
public interface IDistUserGroupService {

    /**
     * 查询分销员与分组关系
     *
     * @param id 主键
     * @return 分销员与分组关系
     */
    DistUserGroupVo queryById(Long id);

    /**
     * 分页查询分销员与分组关系列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分销员与分组关系分页列表
     */
    TableDataInfo<DistUserGroupVo> queryPageList(DistUserGroupBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的分销员与分组关系列表
     *
     * @param bo 查询条件
     * @return 分销员与分组关系列表
     */
    List<DistUserGroupVo> queryList(DistUserGroupBo bo);

    /**
     * 新增分销员与分组关系
     *
     * @param bo 分销员与分组关系
     * @return 是否新增成功
     */
    Boolean insertByBo(DistUserGroupBo bo);

    /**
     * 修改分销员与分组关系
     *
     * @param bo 分销员与分组关系
     * @return 是否修改成功
     */
    Boolean updateByBo(DistUserGroupBo bo);

    /**
     * 校验并批量删除分销员与分组关系信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
