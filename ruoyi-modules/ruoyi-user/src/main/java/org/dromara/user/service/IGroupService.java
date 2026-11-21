package org.dromara.user.service;

import org.dromara.user.domain.Group;
import org.dromara.user.domain.vo.GroupVo;
import org.dromara.user.domain.bo.GroupBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 群组Service接口
 *
 * @author ljs
 * @date 2026-07-27
 */
public interface IGroupService {

    /**
     * 查询群组
     *
     * @param id 主键
     * @return 群组
     */
    GroupVo queryById(Long id);

    /**
     * 分页查询群组列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 群组分页列表
     */
    TableDataInfo<GroupVo> queryPageList(GroupBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的群组列表
     *
     * @param bo 查询条件
     * @return 群组列表
     */
    List<GroupVo> queryList(GroupBo bo);

    /**
     * 新增群组
     *
     * @param bo 群组
     * @return 是否新增成功
     */
    Boolean insertByBo(GroupBo bo);

    /**
     * 修改群组
     *
     * @param bo 群组
     * @return 是否修改成功
     */
    Boolean updateByBo(GroupBo bo);

    /**
     * 校验并批量删除群组信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
