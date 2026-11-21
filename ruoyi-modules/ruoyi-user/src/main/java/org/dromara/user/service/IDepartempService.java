package org.dromara.user.service;

import org.dromara.user.domain.Departemp;
import org.dromara.user.domain.vo.DepartempVo;
import org.dromara.user.domain.bo.DepartempBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 部门员工职位关系Service接口
 *
 * @author ljs
 * @date 2026-07-27
 */
public interface IDepartempService {

    /**
     * 查询部门员工职位关系
     *
     * @param id 主键
     * @return 部门员工职位关系
     */
    DepartempVo queryById(Long id);

    /**
     * 分页查询部门员工职位关系列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 部门员工职位关系分页列表
     */
    TableDataInfo<DepartempVo> queryPageList(DepartempBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的部门员工职位关系列表
     *
     * @param bo 查询条件
     * @return 部门员工职位关系列表
     */
    List<DepartempVo> queryList(DepartempBo bo);

    /**
     * 新增部门员工职位关系
     *
     * @param bo 部门员工职位关系
     * @return 是否新增成功
     */
    Boolean insertByBo(DepartempBo bo);

    /**
     * 修改部门员工职位关系
     *
     * @param bo 部门员工职位关系
     * @return 是否修改成功
     */
    Boolean updateByBo(DepartempBo bo);

    /**
     * 校验并批量删除部门员工职位关系信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
