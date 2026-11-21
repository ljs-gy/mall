package org.dromara.user.service;

import org.dromara.user.domain.Userinfo;
import org.dromara.user.domain.vo.UserinfoVo;
import org.dromara.user.domain.bo.UserinfoBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 用户信息Service接口
 *
 * @author ljs
 * @date 2026-07-27
 */
public interface IUserinfoService {

    /**
     * 查询用户信息
     *
     * @param id 主键
     * @return 用户信息
     */
    UserinfoVo queryById(Long id);

    /**
     * 分页查询用户信息列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户信息分页列表
     */
    TableDataInfo<UserinfoVo> queryPageList(UserinfoBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的用户信息列表
     *
     * @param bo 查询条件
     * @return 用户信息列表
     */
    List<UserinfoVo> queryList(UserinfoBo bo);

    /**
     * 新增用户信息
     *
     * @param bo 用户信息
     * @return 是否新增成功
     */
    Boolean insertByBo(UserinfoBo bo);

    /**
     * 修改用户信息
     *
     * @param bo 用户信息
     * @return 是否修改成功
     */
    Boolean updateByBo(UserinfoBo bo);

    /**
     * 校验并批量删除用户信息信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
