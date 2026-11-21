package org.dromara.bidding.service;

import org.dromara.bidding.domain.vo.SessionVo;
import org.dromara.bidding.domain.bo.SessionBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 竞价场次Service接口
 *
 * @author ljs
 * @date 2026-06-21
 */
public interface ISessionService {

    /**
     * 查询竞价场次
     *
     * @param id 主键
     * @return 竞价场次
     */
    SessionVo queryById(Long id);

    /**
     * 分页查询竞价场次列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 竞价场次分页列表
     */
    TableDataInfo<SessionVo> queryPageList(SessionBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的竞价场次列表
     *
     * @param bo 查询条件
     * @return 竞价场次列表
     */
    List<SessionVo> queryList(SessionBo bo);

    /**
     * 新增竞价场次
     *
     * @param bo 竞价场次
     * @return 是否新增成功
     */
    Boolean insertByBo(SessionBo bo);

    /**
     * 修改竞价场次
     *
     * @param bo 竞价场次
     * @return 是否修改成功
     */
    Boolean updateByBo(SessionBo bo);

    /**
     * 校验并批量删除竞价场次信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 审核通过公告并触发缓存刷新、延迟开拍/截拍
     *
     * @param sessionId 场次ID
     */
    void approveAnnouncement(Long sessionId);
}
