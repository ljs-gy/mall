package org.dromara.order.service.impl;

import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.dromara.order.domain.bo.MerchantReturnAddressBo;
import org.dromara.order.domain.vo.MerchantReturnAddressVo;
import org.dromara.order.domain.MerchantReturnAddress;
import org.dromara.order.mapper.MerchantReturnAddressMapper;
import org.dromara.order.service.IMerchantReturnAddressService;

import java.util.Collection;
import java.util.List;

/**
 * 商家退货收货地址库Service业务层处理
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MerchantReturnAddressServiceImpl implements IMerchantReturnAddressService {

    private static final Long DEFAULT_YES = 1L;
    private static final Long DEFAULT_NO = 0L;

    private final MerchantReturnAddressMapper baseMapper;

    @Override
    public MerchantReturnAddressVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<MerchantReturnAddressVo> queryPageList(MerchantReturnAddressBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<MerchantReturnAddress> lqw = buildQueryWrapper(bo);
        Page<MerchantReturnAddressVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<MerchantReturnAddressVo> queryList(MerchantReturnAddressBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<MerchantReturnAddress> buildQueryWrapper(MerchantReturnAddressBo bo) {
        LambdaQueryWrapper<MerchantReturnAddress> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(MerchantReturnAddress::getIsDefault);
        lqw.orderByDesc(MerchantReturnAddress::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getMemberCode()), MerchantReturnAddress::getMemberCode, bo.getMemberCode());
        lqw.like(StringUtils.isNotBlank(bo.getConsigneeName()), MerchantReturnAddress::getConsigneeName, bo.getConsigneeName());
        lqw.eq(StringUtils.isNotBlank(bo.getConsigneeMobile()), MerchantReturnAddress::getConsigneeMobile, bo.getConsigneeMobile());
        lqw.eq(StringUtils.isNotBlank(bo.getProvinceCode()), MerchantReturnAddress::getProvinceCode, bo.getProvinceCode());
        lqw.eq(StringUtils.isNotBlank(bo.getCityCode()), MerchantReturnAddress::getCityCode, bo.getCityCode());
        lqw.eq(StringUtils.isNotBlank(bo.getDistrictCode()), MerchantReturnAddress::getDistrictCode, bo.getDistrictCode());
        lqw.eq(bo.getIsDefault() != null, MerchantReturnAddress::getIsDefault, bo.getIsDefault());
        return lqw;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(MerchantReturnAddressBo bo) {
        MerchantReturnAddress add = MapstructUtils.convert(bo, MerchantReturnAddress.class);
        if (add.getIsDefault() == null) {
            add.setIsDefault(DEFAULT_NO);
        }
        if (DEFAULT_YES.equals(add.getIsDefault())) {
            clearOtherDefaults(add.getMemberCode(), null);
        }
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(MerchantReturnAddressBo bo) {
        MerchantReturnAddress update = MapstructUtils.convert(bo, MerchantReturnAddress.class);
        if (DEFAULT_YES.equals(update.getIsDefault())) {
            clearOtherDefaults(update.getMemberCode(), update.getId());
        }
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 同一商家只保留一个默认地址
     */
    private void clearOtherDefaults(String memberCode, Long excludeId) {
        if (StringUtils.isBlank(memberCode)) {
            return;
        }
        LambdaUpdateWrapper<MerchantReturnAddress> uw = Wrappers.lambdaUpdate();
        uw.eq(MerchantReturnAddress::getMemberCode, memberCode)
            .eq(MerchantReturnAddress::getIsDefault, DEFAULT_YES)
            .ne(excludeId != null, MerchantReturnAddress::getId, excludeId)
            .set(MerchantReturnAddress::getIsDefault, DEFAULT_NO);
        baseMapper.update(null, uw);
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }
}
