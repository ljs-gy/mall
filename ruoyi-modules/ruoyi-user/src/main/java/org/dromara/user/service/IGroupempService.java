package org.dromara.user.service;

import org.dromara.user.domain.Groupemp;
import org.dromara.user.domain.vo.GroupempVo;
import org.dromara.user.domain.bo.GroupempBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 群组员工职位关系Service接口
 *
 * @author ljs
 * @date 2026-07-27
 */
public interface IGroupempService {

    /**
     * 查询群组员工职位关系
     *
     * @param id 主键
     * @return 群组员工职位关系
     */
    GroupempVo queryById(Long id);

    /**
     * 分页查询群组员工职位关系列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 群组员工职位关系分页列表
     */
    TableDataInfo<GroupempVo> queryPageList(GroupempBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的群组员工职位关系列表
     *
     * @param bo 查询条件
     * @return 群组员工职位关系列表
     */
    List<GroupempVo> queryList(GroupempBo bo);

    /**
     * 新增群组员工职位关系
     *
     * @param bo 群组员工职位关系
     * @return 是否新增成功
     */
    Boolean insertByBo(GroupempBo bo);

    /**
     * 修改群组员工职位关系
     *
     * @param bo 群组员工职位关系
     * @return 是否修改成功
     */
    Boolean updateByBo(GroupempBo bo);

    /**
     * 校验并批量删除群组员工职位关系信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
