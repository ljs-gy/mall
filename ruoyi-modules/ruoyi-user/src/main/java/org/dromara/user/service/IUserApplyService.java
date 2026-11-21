package org.dromara.user.service;

import org.dromara.user.domain.vo.UserApplyVo;
import org.dromara.user.domain.bo.UserApplyAuditBo;
import org.dromara.user.domain.bo.UserApplyBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 用户申请审核记录Service接口
 *
 * @author ljs
 * @date 2026-07-27
 */
public interface IUserApplyService {

    /**
     * 查询用户申请审核记录
     *
     * @param id 主键
     * @return 用户申请审核记录
     */
    UserApplyVo queryById(Long id);

    /**
     * 分页查询用户申请审核记录列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户申请审核记录分页列表
     */
    TableDataInfo<UserApplyVo> queryPageList(UserApplyBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的用户申请审核记录列表
     *
     * @param bo 查询条件
     * @return 用户申请审核记录列表
     */
    List<UserApplyVo> queryList(UserApplyBo bo);

    /**
     * 新增用户申请审核记录
     *
     * @param bo 用户申请审核记录
     * @return 是否新增成功
     */
    Boolean insertByBo(UserApplyBo bo);

    /**
     * 修改用户申请审核记录
     *
     * @param bo 用户申请审核记录
     * @return 是否修改成功
     */
    Boolean updateByBo(UserApplyBo bo);

    /**
     * 校验并批量删除用户申请审核记录信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 审核申请：通过时写入身份；分销员额外赋默认等级并生成邀请码
     */
    Boolean audit(UserApplyAuditBo bo);
}
