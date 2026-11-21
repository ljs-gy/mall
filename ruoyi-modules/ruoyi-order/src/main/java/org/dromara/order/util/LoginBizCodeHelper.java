package org.dromara.order.util;

import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.user.api.RemoteUserBizService;
import org.springframework.stereotype.Component;


@Component
public class LoginBizCodeHelper {

    @DubboReference
    private RemoteUserBizService remoteUserBizService;

    /**
     * 拿用户code（优先登录态；历史 Token 未带时远程补一次）
     */
    public String requireUserinfoCode() {
        String code = LoginHelper.getUserinfoCode();
        if (StringUtils.isNotBlank(code)) {
            return code;
        }
        Long userId = LoginHelper.getUserId();
        if (userId == null) {
            throw new ServiceException("登录态缺少业务编码");
        }
        code = remoteUserBizService.getUserinfoCode(userId);
        if (StringUtils.isBlank(code)) {
            throw new ServiceException("登录态缺少业务编码");
        }
        LoginHelper.setUserinfoCode(code);
        return code;
    }
}
