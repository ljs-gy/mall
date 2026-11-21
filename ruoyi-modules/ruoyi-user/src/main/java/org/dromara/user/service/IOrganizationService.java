package org.dromara.user.service;

import org.dromara.user.domain.Organization;
import org.dromara.user.domain.vo.OrganizationVo;
import org.dromara.user.domain.bo.OrganizationBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 组织Service接口
 *
 * @author ljs
 * @date 2026-07-27
 */
public interface IOrganizationService {

    /**
     * 查询组织
     *
     * @param id 主键
     * @return 组织
     */
    OrganizationVo queryById(Long id);

    /**
     * 分页查询组织列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 组织分页列表
     */
    TableDataInfo<OrganizationVo> queryPageList(OrganizationBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的组织列表
     *
     * @param bo 查询条件
     * @return 组织列表
     */
    List<OrganizationVo> queryList(OrganizationBo bo);

    /**
     * 新增组织
     *
     * @param bo 组织
     * @return 是否新增成功
     */
    Boolean insertByBo(OrganizationBo bo);

    /**
     * 修改组织
     *
     * @param bo 组织
     * @return 是否修改成功
     */
    Boolean updateByBo(OrganizationBo bo);

    /**
     * 校验并批量删除组织信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
