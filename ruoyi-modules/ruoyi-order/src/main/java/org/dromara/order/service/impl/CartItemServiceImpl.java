package org.dromara.order.service.impl;

import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.dromara.order.domain.bo.CartItemBo;
import org.dromara.order.domain.vo.CartItemVo;
import org.dromara.order.domain.CartItem;
import org.dromara.order.mapper.CartItemMapper;
import org.dromara.order.service.ICartItemService;
import org.dromara.order.util.LoginBizCodeHelper;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 购物车明细Service业务层处理
 *
 * @author ljs
 * @date 2026-08-20
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CartItemServiceImpl implements ICartItemService {

    private final CartItemMapper baseMapper;
    private final LoginBizCodeHelper loginBizCodeHelper;

    /**
     * 查询购物车明细
     *
     * @param id 主键
     * @return 购物车明细
     */
    @Override
    public CartItemVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询购物车明细列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 购物车明细分页列表
     */
    @Override
    public TableDataInfo<CartItemVo> queryPageList(CartItemBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CartItem> lqw = buildQueryWrapper(bo);
        Page<CartItemVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的购物车明细列表
     *
     * @param bo 查询条件
     * @return 购物车明细列表
     */
    @Override
    public List<CartItemVo> queryList(CartItemBo bo) {
        LambdaQueryWrapper<CartItem> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CartItem> buildQueryWrapper(CartItemBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CartItem> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CartItem::getId);
        lqw.eq(bo.getUserinfoId() != null, CartItem::getUserinfoId, bo.getUserinfoId());
        lqw.eq(StringUtils.isNotBlank(bo.getMemberBcode()), CartItem::getMemberBcode, bo.getMemberBcode());
        lqw.eq(StringUtils.isNotBlank(bo.getChannelCode()), CartItem::getChannelCode, bo.getChannelCode());
        lqw.eq(StringUtils.isNotBlank(bo.getMemberCode()), CartItem::getMemberCode, bo.getMemberCode());
        lqw.eq(bo.getSpuId() != null, CartItem::getSpuId, bo.getSpuId());
        lqw.eq(bo.getSkuId() != null, CartItem::getSkuId, bo.getSkuId());
        lqw.eq(StringUtils.isNotBlank(bo.getSkuCode()), CartItem::getSkuCode, bo.getSkuCode());
        lqw.eq(bo.getQuantity() != null, CartItem::getQuantity, bo.getQuantity());
        lqw.eq(bo.getAddPrice() != null, CartItem::getAddPrice, bo.getAddPrice());
        lqw.eq(bo.getIsChecked() != null, CartItem::getIsChecked, bo.getIsChecked());
        return lqw;
    }

    /**
     * 新增购物车明细
     *
     * @param bo 购物车明细
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CartItemBo bo) {
        if (StringUtils.isBlank(bo.getMemberBcode())) {
            bo.setMemberBcode(loginBizCodeHelper.requireUserinfoCode());
        }
        CartItem add = MapstructUtils.convert(bo, CartItem.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改购物车明细
     *
     * @param bo 购物车明细
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CartItemBo bo) {
        CartItem update = MapstructUtils.convert(bo, CartItem.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CartItem entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除购物车明细信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }
}
