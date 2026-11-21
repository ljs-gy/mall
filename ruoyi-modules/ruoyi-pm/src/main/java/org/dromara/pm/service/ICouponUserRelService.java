package org.dromara.pm.service;

import org.dromara.pm.domain.CouponUserRel;
import org.dromara.pm.domain.vo.CouponUserRelVo;
import org.dromara.pm.domain.bo.CouponUserRelBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 用户优惠券领用记录Service接口
 *
 * @author ljs
 * @date 2026-07-21
 */
public interface ICouponUserRelService {

    /**
     * 查询用户优惠券领用记录
     *
     * @param id 主键
     * @return 用户优惠券领用记录
     */
    CouponUserRelVo queryById(Long id);

    /**
     * 分页查询用户优惠券领用记录列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户优惠券领用记录分页列表
     */
    TableDataInfo<CouponUserRelVo> queryPageList(CouponUserRelBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的用户优惠券领用记录列表
     *
     * @param bo 查询条件
     * @return 用户优惠券领用记录列表
     */
    List<CouponUserRelVo> queryList(CouponUserRelBo bo);

    /**
     * 新增用户优惠券领用记录
     *
     * @param bo 用户优惠券领用记录
     * @return 是否新增成功
     */
    Boolean insertByBo(CouponUserRelBo bo);

    /**
     * 修改用户优惠券领用记录
     *
     * @param bo 用户优惠券领用记录
     * @return 是否修改成功
     */
    Boolean updateByBo(CouponUserRelBo bo);

    /**
     * 校验并批量删除用户优惠券领用记录信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
