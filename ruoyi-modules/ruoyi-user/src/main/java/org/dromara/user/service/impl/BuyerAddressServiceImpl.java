package org.dromara.user.service.impl;

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
import org.dromara.user.domain.bo.BuyerAddressBo;
import org.dromara.user.domain.vo.BuyerAddressVo;
import org.dromara.user.domain.BuyerAddress;
import org.dromara.user.mapper.BuyerAddressMapper;
import org.dromara.user.service.IBuyerAddressService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 买家收货地址Service业务层处理
 *
 * @author ljs
 * @date 2026-08-20
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BuyerAddressServiceImpl implements IBuyerAddressService {

    private final BuyerAddressMapper baseMapper;

    /**
     * 查询买家收货地址
     *
     * @param id 主键
     * @return 买家收货地址
     */
    @Override
    public BuyerAddressVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询买家收货地址列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 买家收货地址分页列表
     */
    @Override
    public TableDataInfo<BuyerAddressVo> queryPageList(BuyerAddressBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<BuyerAddress> lqw = buildQueryWrapper(bo);
        Page<BuyerAddressVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的买家收货地址列表
     *
     * @param bo 查询条件
     * @return 买家收货地址列表
     */
    @Override
    public List<BuyerAddressVo> queryList(BuyerAddressBo bo) {
        LambdaQueryWrapper<BuyerAddress> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<BuyerAddress> buildQueryWrapper(BuyerAddressBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<BuyerAddress> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(BuyerAddress::getIsDefault);
        lqw.orderByDesc(BuyerAddress::getId);
        lqw.eq(bo.getUserinfoId() != null, BuyerAddress::getUserinfoId, bo.getUserinfoId());
        lqw.like(StringUtils.isNotBlank(bo.getConsigneeName()), BuyerAddress::getConsigneeName, bo.getConsigneeName());
        lqw.eq(StringUtils.isNotBlank(bo.getConsigneeMobile()), BuyerAddress::getConsigneeMobile, bo.getConsigneeMobile());
        lqw.eq(StringUtils.isNotBlank(bo.getProvinceCode()), BuyerAddress::getProvinceCode, bo.getProvinceCode());
        lqw.eq(StringUtils.isNotBlank(bo.getCityCode()), BuyerAddress::getCityCode, bo.getCityCode());
        lqw.eq(StringUtils.isNotBlank(bo.getDistrictCode()), BuyerAddress::getDistrictCode, bo.getDistrictCode());
        lqw.eq(bo.getIsDefault() != null, BuyerAddress::getIsDefault, bo.getIsDefault());
        return lqw;
    }

    /**
     * 新增买家收货地址
     *
     * @param bo 买家收货地址
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(BuyerAddressBo bo) {
        BuyerAddress add = MapstructUtils.convert(bo, BuyerAddress.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改买家收货地址
     *
     * @param bo 买家收货地址
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(BuyerAddressBo bo) {
        BuyerAddress update = MapstructUtils.convert(bo, BuyerAddress.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(BuyerAddress entity) {
        // TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除买家收货地址信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            // TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }
}
