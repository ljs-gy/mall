package org.dromara.user.service;

import org.dromara.user.domain.Store;
import org.dromara.user.domain.vo.StoreVo;
import org.dromara.user.domain.bo.StoreBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 商家店铺Service接口
 *
 * @author ljs
 * @date 2026-07-27
 */
public interface IStoreService {

    /**
     * 查询商家店铺
     *
     * @param id 主键
     * @return 商家店铺
     */
    StoreVo queryById(Long id);

    /**
     * 分页查询商家店铺列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商家店铺分页列表
     */
    TableDataInfo<StoreVo> queryPageList(StoreBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的商家店铺列表
     *
     * @param bo 查询条件
     * @return 商家店铺列表
     */
    List<StoreVo> queryList(StoreBo bo);

    /**
     * 新增商家店铺
     *
     * @param bo 商家店铺
     * @return 是否新增成功
     */
    Boolean insertByBo(StoreBo bo);

    /**
     * 修改商家店铺
     *
     * @param bo 商家店铺
     * @return 是否修改成功
     */
    Boolean updateByBo(StoreBo bo);

    /**
     * 校验并批量删除商家店铺信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
