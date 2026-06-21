package org.dromara.bidding.service;

import org.dromara.bidding.domain.vo.AnnouncementItemVo;
import org.dromara.bidding.domain.bo.AnnouncementItemBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 竞价标的Service接口
 *
 * @author ljs
 * @date 2026-06-21
 */
public interface IAnnouncementItemService {

    /**
     * 查询竞价标的
     *
     * @param id 主键
     * @return 竞价标的
     */
    AnnouncementItemVo queryById(Long id);

    /**
     * 分页查询竞价标的列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 竞价标的分页列表
     */
    TableDataInfo<AnnouncementItemVo> queryPageList(AnnouncementItemBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的竞价标的列表
     *
     * @param bo 查询条件
     * @return 竞价标的列表
     */
    List<AnnouncementItemVo> queryList(AnnouncementItemBo bo);

    /**
     * 新增竞价标的
     *
     * @param bo 竞价标的
     * @return 是否新增成功
     */
    Boolean insertByBo(AnnouncementItemBo bo);

    /**
     * 修改竞价标的
     *
     * @param bo 竞价标的
     * @return 是否修改成功
     */
    Boolean updateByBo(AnnouncementItemBo bo);

    /**
     * 校验并批量删除竞价标的信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
