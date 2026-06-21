package org.dromara.bidding.service;

import org.dromara.bidding.domain.vo.AnnouncementItemGoodsVo;
import org.dromara.bidding.domain.bo.AnnouncementItemGoodsBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 包裹子商品Service接口
 *
 * @author ljs
 * @date 2026-06-21
 */
public interface IAnnouncementItemGoodsService {

    /**
     * 查询包裹子商品
     *
     * @param id 主键
     * @return 包裹子商品
     */
    AnnouncementItemGoodsVo queryById(Long id);

    /**
     * 分页查询包裹子商品列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 包裹子商品分页列表
     */
    TableDataInfo<AnnouncementItemGoodsVo> queryPageList(AnnouncementItemGoodsBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的包裹子商品列表
     *
     * @param bo 查询条件
     * @return 包裹子商品列表
     */
    List<AnnouncementItemGoodsVo> queryList(AnnouncementItemGoodsBo bo);

    /**
     * 新增包裹子商品
     *
     * @param bo 包裹子商品
     * @return 是否新增成功
     */
    Boolean insertByBo(AnnouncementItemGoodsBo bo);

    /**
     * 修改包裹子商品
     *
     * @param bo 包裹子商品
     * @return 是否修改成功
     */
    Boolean updateByBo(AnnouncementItemGoodsBo bo);

    /**
     * 校验并批量删除包裹子商品信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
