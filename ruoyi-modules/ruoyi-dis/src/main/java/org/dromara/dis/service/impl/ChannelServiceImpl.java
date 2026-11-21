package org.dromara.dis.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.dis.api.constant.DisRedisConstants;
import org.dromara.dis.domain.Channel;
import org.dromara.dis.domain.bo.ChannelBo;
import org.dromara.dis.domain.vo.ChannelVo;
import org.dromara.dis.mapper.ChannelMapper;
import org.dromara.dis.service.IChannelService;
import org.dromara.dis.service.IDisChannelConfigService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Service
public class ChannelServiceImpl implements IChannelService {

    private final ChannelMapper baseMapper;
    private final StringRedisTemplate redisTemplate;
    private final IDisChannelConfigService channelConfigService;

    @Override
    public ChannelVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<ChannelVo> queryPageList(ChannelBo bo, PageQuery pageQuery) {
        Page<ChannelVo> result = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(result);
    }

    @Override
    public List<ChannelVo> queryList(ChannelBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<Channel> buildQueryWrapper(ChannelBo bo) {
        return Wrappers.lambdaQuery(Channel.class)
            .orderByAsc(Channel::getId)
            .eq(StringUtils.isNotBlank(bo.getChannelCode()), Channel::getChannelCode, bo.getChannelCode())
            .like(StringUtils.isNotBlank(bo.getChannelName()), Channel::getChannelName, bo.getChannelName())
            .eq(bo.getChannelType() != null, Channel::getChannelType, bo.getChannelType())
            .eq(bo.getChannelSort() != null, Channel::getChannelSort, bo.getChannelSort())
            .eq(StringUtils.isNotBlank(bo.getParentChannelCode()), Channel::getParentChannelCode, bo.getParentChannelCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(ChannelBo bo) {
        String tenantId = LoginHelper.getTenantId();
        assertChannelCodeUnique(tenantId, bo.getChannelCode());
        Channel add = MapstructUtils.convert(bo, Channel.class);
        add.setTenantId(tenantId);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
            putChannelCache(tenantId, add);
            channelConfigService.initDefaultPublishConfig(tenantId, add.getChannelCode());
        }
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(ChannelBo bo) {
        String tenantId = LoginHelper.getTenantId();
        Channel update = MapstructUtils.convert(bo, Channel.class);
        boolean flag = baseMapper.updateById(update) > 0;
        if (!flag) {
            throw new ServiceException("渠道不存在");
        }
        update.setTenantId(tenantId);
        putChannelCache(tenantId, update);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByIds(Collection<Long> ids) {
        String tenantId = LoginHelper.getTenantId();
        List<Channel> channels = baseMapper.selectList(Wrappers.lambdaQuery(Channel.class).in(Channel::getId, ids));
        for (Channel channel : channels) {
            channelConfigService.removeByChannel(tenantId, channel.getChannelCode());
            removeChannelCache(tenantId, channel.getChannelCode());
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

    private void assertChannelCodeUnique(String tenantId, String channelCode) {
        Long count = baseMapper.selectCount(Wrappers.lambdaQuery(Channel.class)
            .eq(Channel::getTenantId, tenantId)
            .eq(Channel::getChannelCode, channelCode));
        if (count != null && count > 0) {
            throw new ServiceException("渠道编码已存在");
        }
    }

    private void putChannelCache(String tenantId, Channel channel) {
        if (channel == null || StringUtils.isBlank(channel.getChannelCode())) {
            return;
        }
        String redisKey = String.format(DisRedisConstants.KEY_CHANNEL_DETAIL_HASH, tenantId);
        redisTemplate.opsForHash().put(redisKey, channel.getChannelCode(), JSON.toJSONString(channel));
    }

    private void removeChannelCache(String tenantId, String channelCode) {
        if (StringUtils.isBlank(channelCode)) {
            return;
        }
        String redisKey = String.format(DisRedisConstants.KEY_CHANNEL_DETAIL_HASH, tenantId);
        redisTemplate.opsForHash().delete(redisKey, channelCode);
    }
}
