package org.dromara.user.service;

import org.dromara.user.domain.Department;
import org.dromara.user.domain.vo.DepartmentVo;
import org.dromara.user.domain.bo.DepartmentBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 部门Service接口
 *
 * @author ljs
 * @date 2026-07-27
 */
public interface IDepartmentService {

    /**
     * 查询部门
     *
     * @param id 主键
     * @return 部门
     */
    DepartmentVo queryById(Long id);

    /**
     * 分页查询部门列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 部门分页列表
     */
    TableDataInfo<DepartmentVo> queryPageList(DepartmentBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的部门列表
     *
     * @param bo 查询条件
     * @return 部门列表
     */
    List<DepartmentVo> queryList(DepartmentBo bo);

    /**
     * 新增部门
     *
     * @param bo 部门
     * @return 是否新增成功
     */
    Boolean insertByBo(DepartmentBo bo);

    /**
     * 修改部门
     *
     * @param bo 部门
     * @return 是否修改成功
     */
    Boolean updateByBo(DepartmentBo bo);

    /**
     * 校验并批量删除部门信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
