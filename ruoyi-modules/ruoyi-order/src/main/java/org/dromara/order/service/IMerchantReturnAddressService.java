package org.dromara.order.service;

import org.dromara.order.domain.vo.MerchantReturnAddressVo;
import org.dromara.order.domain.bo.MerchantReturnAddressBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 商家退货收货地址库Service接口
 */
public interface IMerchantReturnAddressService {

    /**
     * 查询商家退货收货地址
     *
     * @param id 主键
     * @return 商家退货收货地址
     */
    MerchantReturnAddressVo queryById(Long id);

    /**
     * 分页查询商家退货收货地址列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商家退货收货地址分页列表
     */
    TableDataInfo<MerchantReturnAddressVo> queryPageList(MerchantReturnAddressBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的商家退货收货地址列表
     *
     * @param bo 查询条件
     * @return 商家退货收货地址列表
     */
    List<MerchantReturnAddressVo> queryList(MerchantReturnAddressBo bo);

    /**
     * 新增商家退货收货地址
     *
     * @param bo 商家退货收货地址
     * @return 是否新增成功
     */
    Boolean insertByBo(MerchantReturnAddressBo bo);

    /**
     * 修改商家退货收货地址
     *
     * @param bo 商家退货收货地址
     * @return 是否修改成功
     */
    Boolean updateByBo(MerchantReturnAddressBo bo);

    /**
     * 校验并批量删除商家退货收货地址信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
