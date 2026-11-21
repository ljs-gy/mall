package org.dromara.user.service;

import org.dromara.user.domain.UserIdentityRel;
import org.dromara.user.domain.vo.UserIdentityRelVo;
import org.dromara.user.domain.bo.UserIdentityRelBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 用户主体身份关系Service接口
 *
 * @author ljs
 * @date 2026-07-27
 */
public interface IUserIdentityRelService {

    /**
     * 查询用户主体身份关系
     *
     * @param id 主键
     * @return 用户主体身份关系
     */
    UserIdentityRelVo queryById(Long id);

    /**
     * 分页查询用户主体身份关系列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户主体身份关系分页列表
     */
    TableDataInfo<UserIdentityRelVo> queryPageList(UserIdentityRelBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的用户主体身份关系列表
     *
     * @param bo 查询条件
     * @return 用户主体身份关系列表
     */
    List<UserIdentityRelVo> queryList(UserIdentityRelBo bo);

    /**
     * 新增用户主体身份关系
     *
     * @param bo 用户主体身份关系
     * @return 是否新增成功
     */
    Boolean insertByBo(UserIdentityRelBo bo);

    /**
     * 修改用户主体身份关系
     *
     * @param bo 用户主体身份关系
     * @return 是否修改成功
     */
    Boolean updateByBo(UserIdentityRelBo bo);

    /**
     * 校验并批量删除用户主体身份关系信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
