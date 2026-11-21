package org.dromara.pm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.pm.domain.PromoMutexGroup;
import org.dromara.pm.domain.vo.PromoMutexGroupVo;
import org.dromara.pm.mapper.PromoMutexGroupMapper;
import org.dromara.pm.service.IPromoMutexGroupService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 营销互斥组配置
 */
@RequiredArgsConstructor
@Service
public class PromoMutexGroupServiceImpl implements IPromoMutexGroupService {

    private final PromoMutexGroupMapper baseMapper;

    @Override
    public List<PromoMutexGroupVo> listEnabled() {
        return baseMapper.selectVoList(Wrappers.<PromoMutexGroup>lambdaQuery()
            .eq(PromoMutexGroup::getStatus, 1L)
            .orderByDesc(PromoMutexGroup::getPriority)
            .orderByAsc(PromoMutexGroup::getId));
    }

    @Override
    public Map<String, PromoMutexGroup> mapEnabledByPromoType() {
        List<PromoMutexGroup> list = baseMapper.selectList(Wrappers.<PromoMutexGroup>lambdaQuery()
            .eq(PromoMutexGroup::getStatus, 1L)
            .orderByDesc(PromoMutexGroup::getPriority));
        return list.stream().collect(Collectors.toMap(
            PromoMutexGroup::getPromoType,
            Function.identity(),
            (a, b) -> a,
            LinkedHashMap::new));
    }

    @Override
    public PromoMutexGroup getEnabledByPromoType(String promoType) {
        if (promoType == null) {
            return null;
        }
        return baseMapper.selectOne(Wrappers.<PromoMutexGroup>lambdaQuery()
            .eq(PromoMutexGroup::getPromoType, promoType)
            .eq(PromoMutexGroup::getStatus, 1L)
            .last("LIMIT 1"));
    }

}
