package org.dromara.dis.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.dis.api.constant.DisRedisConstants;
import org.dromara.dis.domain.bo.BuyerContextBo;
import org.dromara.dis.service.IBuyerAccessService;
import org.dromara.user.api.RemoteUserBizService;
import org.dromara.user.api.domain.RemoteBuyerContextBo;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 买家渠道准入：Dubbo 取用户上下文 + Redis 用户范围校验
 *
 * @author ljs
 * @date 2026-08-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BuyerAccessServiceImpl implements IBuyerAccessService {

    private final StringRedisTemplate redisTemplate;

    @DubboReference
    private RemoteUserBizService remoteUserBizService;

    @Override
    public BuyerContextBo getBuyerContext(Long userinfoId) {
        RemoteBuyerContextBo remote = remoteUserBizService.getBuyerContext(userinfoId);
        if (remote == null || Long.valueOf(1L).equals(remote.getIsBlacklisted())) {
            throw new ServiceException("买家账号异常或已被拉黑");
        }
        BuyerContextBo bo = new BuyerContextBo();
        bo.setUserinfoId(String.valueOf(remote.getUserinfoId()));
        bo.setOrgCode(remote.getOrgCode());
        bo.setSalesmanDeptId(remote.getSalesmanDeptId());
        bo.setSalesmanEmpCode(remote.getSalesmanEmpCode());
        bo.setProvinceCode(remote.getProvinceCode());
        bo.setCityCode(remote.getCityCode());
        bo.setDistrictCode(remote.getDistrictCode());
        return bo;
    }

    @Override
    public List<String> getAccessibleChannels(String tenantId, String defaultChannelCode, BuyerContextBo buyer) {
        String channelMapKey = String.format(DisRedisConstants.KEY_CHANNEL_DETAIL_HASH, tenantId);
        Set<Object> allChannelKeys = redisTemplate.opsForHash().keys(channelMapKey);
        if (CollUtil.isEmpty(allChannelKeys)) {
            return Collections.singletonList(defaultChannelCode);
        }

        List<String> allowedChannels = new ArrayList<>();
        for (Object objKey : allChannelKeys) {
            String chCode = objKey.toString();
            if (checkChannelAccess(tenantId, chCode, buyer)) {
                allowedChannels.add(chCode);
            }
        }
        if (allowedChannels.isEmpty()) {
            return Collections.singletonList(defaultChannelCode);
        }
        return allowedChannels;
    }

    @Override
    public boolean checkChannelAccess(String tenantId, String channelCode, BuyerContextBo buyer) {
        String termKey = String.format(DisRedisConstants.KEY_USER_SCOPE_TERM, tenantId, channelCode);
        Map<Object, Object> termMap = redisTemplate.opsForHash().entries(termKey);
        if (termMap.isEmpty()) {
            return true;
        }
        if (!checkDimension(tenantId, channelCode, "USER", asStr(termMap.get("USER")), buyer.getUserinfoId())) {
            return false;
        }
        if (!checkDimension(tenantId, channelCode, "ORG", asStr(termMap.get("ORG")), buyer.getOrgCode())) {
            return false;
        }
        if (!checkDimension(tenantId, channelCode, "PROVINCE", asStr(termMap.get("PROVINCE")), buyer.getProvinceCode())) {
            return false;
        }
        if (!checkDimension(tenantId, channelCode, "CITY", asStr(termMap.get("CITY")), buyer.getCityCode())) {
            return false;
        }
        if (!checkDimension(tenantId, channelCode, "DISTRICT", asStr(termMap.get("DISTRICT")), buyer.getDistrictCode())) {
            return false;
        }
        return true;
    }

    private boolean checkDimension(String tenantId, String channelCode, String type, String term, String targetVal) {
        if (StrUtil.isEmpty(term)) {
            return true;
        }
        if (StrUtil.isEmpty(targetVal)) {
            return "NOT_IN".equalsIgnoreCase(term) || "!=".equals(term);
        }
        String setKey = String.format(DisRedisConstants.KEY_USER_SCOPE_SET, tenantId, channelCode, type);
        Boolean isMember = redisTemplate.opsForSet().isMember(setKey, targetVal);
        boolean inSet = Boolean.TRUE.equals(isMember);
        return ("IN".equalsIgnoreCase(term) || "=".equals(term)) ? inSet : !inSet;
    }

    private static String asStr(Object val) {
        return val == null ? null : val.toString();
    }
}
