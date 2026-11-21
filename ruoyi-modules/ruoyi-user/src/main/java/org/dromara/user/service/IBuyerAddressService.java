package org.dromara.user.service;

import org.dromara.user.domain.vo.BuyerAddressVo;
import org.dromara.user.domain.bo.BuyerAddressBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 买家收货地址Service接口
 *
 * @author ljs
 * @date 2026-08-20
 */
public interface IBuyerAddressService {

    /**
     * 查询买家收货地址
     *
     * @param id 主键
     * @return 买家收货地址
     */
    BuyerAddressVo queryById(Long id);

    /**
     * 分页查询买家收货地址列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 买家收货地址分页列表
     */
    TableDataInfo<BuyerAddressVo> queryPageList(BuyerAddressBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的买家收货地址列表
     *
     * @param bo 查询条件
     * @return 买家收货地址列表
     */
    List<BuyerAddressVo> queryList(BuyerAddressBo bo);

    /**
     * 新增买家收货地址
     *
     * @param bo 买家收货地址
     * @return 是否新增成功
     */
    Boolean insertByBo(BuyerAddressBo bo);

    /**
     * 修改买家收货地址
     *
     * @param bo 买家收货地址
     * @return 是否修改成功
     */
    Boolean updateByBo(BuyerAddressBo bo);

    /**
     * 校验并批量删除买家收货地址信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
