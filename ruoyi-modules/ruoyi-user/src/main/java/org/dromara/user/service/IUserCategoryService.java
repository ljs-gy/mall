package org.dromara.user.service;

import org.dromara.user.domain.UserCategory;
import org.dromara.user.domain.vo.UserCategoryVo;
import org.dromara.user.domain.bo.UserCategoryBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 用户经营类目资质Service接口
 *
 * @author ljs
 * @date 2026-07-27
 */
public interface IUserCategoryService {

    /**
     * 查询用户经营类目资质
     *
     * @param id 主键
     * @return 用户经营类目资质
     */
    UserCategoryVo queryById(Long id);

    /**
     * 分页查询用户经营类目资质列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户经营类目资质分页列表
     */
    TableDataInfo<UserCategoryVo> queryPageList(UserCategoryBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的用户经营类目资质列表
     *
     * @param bo 查询条件
     * @return 用户经营类目资质列表
     */
    List<UserCategoryVo> queryList(UserCategoryBo bo);

    /**
     * 新增用户经营类目资质
     *
     * @param bo 用户经营类目资质
     * @return 是否新增成功
     */
    Boolean insertByBo(UserCategoryBo bo);

    /**
     * 修改用户经营类目资质
     *
     * @param bo 用户经营类目资质
     * @return 是否修改成功
     */
    Boolean updateByBo(UserCategoryBo bo);

    /**
     * 校验并批量删除用户经营类目资质信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
