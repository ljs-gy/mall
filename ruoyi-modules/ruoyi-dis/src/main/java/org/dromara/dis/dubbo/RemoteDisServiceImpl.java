package org.dromara.dis.dubbo;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.dis.api.RemoteDisService;
import org.dromara.dis.domain.DistLevel;
import org.dromara.dis.domain.DistRelation;
import org.dromara.dis.mapper.DistLevelMapper;
import org.dromara.dis.mapper.DistRelationMapper;
import org.dromara.user.api.RemoteUserBizService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 分销中心远程服务
 */
@Slf4j
@RequiredArgsConstructor
@Service
@DubboService
public class RemoteDisServiceImpl implements RemoteDisService {

    private final DistLevelMapper distLevelMapper;
    private final DistRelationMapper distRelationMapper;

    @DubboReference
    private RemoteUserBizService remoteUserBizService;

    @Override
    public Long getDefaultLevelId() {
        DistLevel level = distLevelMapper.selectOne(
            Wrappers.<DistLevel>lambdaQuery()
                .orderByAsc(DistLevel::getLevelWeight)
                .orderByAsc(DistLevel::getId)
                .last("LIMIT 1")
        );
        return level == null ? null : level.getId();
    }

    @Override
    public void bindByInviteCode(Long userinfoId, String inviteCode) {
        if (userinfoId == null) {
            throw new ServiceException("用户不能为空");
        }
        if (StringUtils.isBlank(inviteCode)) {
            return;
        }
        Long parentId = remoteUserBizService.getUserinfoIdByInviteCode(inviteCode.trim());
        if (parentId == null) {
            throw new ServiceException("邀请码无效");
        }
        if (parentId.equals(userinfoId)) {
            throw new ServiceException("不能填写自己的邀请码");
        }
        Long exist = distRelationMapper.selectCount(
            Wrappers.<DistRelation>lambdaQuery().eq(DistRelation::getUserinfoId, userinfoId)
        );
        if (exist != null && exist > 0) {
            return;
        }
        DistRelation relation = new DistRelation();
        relation.setUserinfoId(userinfoId);
        relation.setParentUserinfoId(parentId);
        relation.setBindTime(new Date());
        distRelationMapper.insert(relation);
    }
}
