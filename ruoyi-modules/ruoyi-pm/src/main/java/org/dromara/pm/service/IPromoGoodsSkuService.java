package org.dromara.pm.service;

import org.dromara.pm.domain.PromoGoodsSku;
import org.dromara.pm.domain.vo.PromoGoodsSkuVo;
import org.dromara.pm.domain.bo.PromoGoodsSkuBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 活动商品配置Service接口
 *
 * @author ljs
 * @date 2026-07-21
 */
public interface IPromoGoodsSkuService {

    /**
     * 查询活动商品配置
     *
     * @param id 主键
     * @return 活动商品配置
     */
    PromoGoodsSkuVo queryById(Long id);

    /**
     * 分页查询活动商品配置列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 活动商品配置分页列表
     */
    TableDataInfo<PromoGoodsSkuVo> queryPageList(PromoGoodsSkuBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的活动商品配置列表
     *
     * @param bo 查询条件
     * @return 活动商品配置列表
     */
    List<PromoGoodsSkuVo> queryList(PromoGoodsSkuBo bo);

    /**
     * 新增活动商品配置
     *
     * @param bo 活动商品配置
     * @return 是否新增成功
     */
    Boolean insertByBo(PromoGoodsSkuBo bo);

    /**
     * 修改活动商品配置
     *
     * @param bo 活动商品配置
     * @return 是否修改成功
     */
    Boolean updateByBo(PromoGoodsSkuBo bo);

    /**
     * 校验并批量删除活动商品配置信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
