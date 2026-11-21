package org.dromara.common.satoken.utils;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.constant.TenantConstants;
import org.dromara.common.core.enums.UserType;
import org.dromara.system.api.model.LoginUser;

import java.util.Set;

/**
 * 登录鉴权助手
 * <p>
 * user_type 为 用户类型 同一个用户表 可以有多种用户类型 例如 pc,app
 * deivce 为 设备类型 同一个用户类型 可以有 多种设备类型 例如 web,ios
 * 可以组成 用户类型与设备类型多对多的 权限灵活控制
 * <p>
 * 多用户体系 针对 多种用户类型 但权限控制不一致
 * 可以组成 多用户类型表与多设备类型 分别控制权限
 *
 * @author Lion Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginHelper {

    public static final String LOGIN_USER_KEY = "loginUser";
    public static final String TENANT_KEY = "tenantId";
    public static final String USER_KEY = "userId";
    public static final String USER_NAME_KEY = "userName";
    public static final String DEPT_KEY = "deptId";
    public static final String DEPT_NAME_KEY = "deptName";
    public static final String DEPT_CATEGORY_KEY = "deptCategory";
    public static final String CLIENT_KEY = "clientid";
    public static final String USERINFO_CODE_KEY = "userinfoCode";
    public static final String CHANNEL_KEY = "channelCode";
    public static final String DEFAULT_CHANNEL_CODE = "default";

    /**
     * 登录系统 基于 设备类型
     * 针对相同用户体系不同设备
     *
     * @param loginUser 登录用户信息
     * @param model     配置参数
     */
    public static void login(LoginUser loginUser, SaLoginParameter model) {
        model = ObjectUtil.defaultIfNull(model, new SaLoginParameter());
        if (StrUtil.isBlank(loginUser.getChannelCode())) {
            loginUser.setChannelCode(DEFAULT_CHANNEL_CODE);
        }
        StpUtil.login(loginUser.getLoginId(),
            model.setExtra(TENANT_KEY, loginUser.getTenantId())
                .setExtra(USER_KEY, loginUser.getUserId())
                .setExtra(USER_NAME_KEY, loginUser.getUsername())
                .setExtra(DEPT_KEY, loginUser.getDeptId())
                .setExtra(DEPT_NAME_KEY, loginUser.getDeptName())
                .setExtra(DEPT_CATEGORY_KEY, loginUser.getDeptCategory())
                .setExtra(USERINFO_CODE_KEY, loginUser.getUserinfoCode())
                .setExtra(CHANNEL_KEY, loginUser.getChannelCode())
        );
        StpUtil.getTokenSession().set(LOGIN_USER_KEY, loginUser);
    }

    /**
     * 获取用户(多级缓存)
     */
    @SuppressWarnings("unchecked")
    public static <T extends LoginUser> T getLoginUser() {
        SaSession session = StpUtil.getTokenSession();
        if (ObjectUtil.isNull(session)) {
            return null;
        }
        return (T) session.get(LOGIN_USER_KEY);
    }

    /**
     * 获取用户基于token
     */
    @SuppressWarnings("unchecked")
    public static <T extends LoginUser> T getLoginUser(String token) {
        SaSession session = StpUtil.getTokenSessionByToken(token);
        if (ObjectUtil.isNull(session)) {
            return null;
        }
        return (T) session.get(LOGIN_USER_KEY);
    }

    /**
     * 获取用户id
     */
    public static Long getUserId() {
        return Convert.toLong(getExtra(USER_KEY));
    }

    /**
     * 获取用户id
     */
    public static String getUserIdStr() {
        return Convert.toStr(getExtra(USER_KEY));
    }

    /**
     * 获取用户账户
     */
    public static String getUsername() {
        return Convert.toStr(getExtra(USER_NAME_KEY));
    }

    /**
     * 获取租户ID
     */
    public static String getTenantId() {
        return Convert.toStr(getExtra(TENANT_KEY));
    }

    /**
     * 获取部门ID
     */
    public static Long getDeptId() {
        return Convert.toLong(getExtra(DEPT_KEY));
    }

    /**
     * 获取部门名
     */
    public static String getDeptName() {
        return Convert.toStr(getExtra(DEPT_NAME_KEY));
    }

    /**
     * 获取部门类别编码
     */
    public static String getDeptCategory() {
        return Convert.toStr(getExtra(DEPT_CATEGORY_KEY));
    }

    /**
     * 获取当前端身份业务码（买家/商家/运营共用同一字段）
     */
    public static String getUserinfoCode() {
        LoginUser loginUser = getLoginUser();
        if (loginUser != null && StrUtil.isNotBlank(loginUser.getUserinfoCode())) {
            return loginUser.getUserinfoCode();
        }
        return Convert.toStr(getExtra(USERINFO_CODE_KEY));
    }

    /**
     * 当前销售渠道编码（登录写入；空则 default）
     */
    public static String getChannelCode() {
        LoginUser loginUser = getLoginUser();
        if (loginUser != null && StrUtil.isNotBlank(loginUser.getChannelCode())) {
            return loginUser.getChannelCode();
        }
        String extra = Convert.toStr(getExtra(CHANNEL_KEY));
        return StrUtil.blankToDefault(extra, DEFAULT_CHANNEL_CODE);
    }

    /**
     * 写入当前销售渠道到登录态
     */
    public static void setChannelCode(String channelCode) {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return;
        }
        loginUser.setChannelCode(StrUtil.blankToDefault(channelCode, DEFAULT_CHANNEL_CODE));
        SaSession session = StpUtil.getTokenSession();
        if (session != null) {
            session.set(LOGIN_USER_KEY, loginUser);
        }
    }

    /**
     * 写入当前端身份业务码到登录态（登录补全、商家选店等）
     */
    public static void setUserinfoCode(String userinfoCode) {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return;
        }
        loginUser.setUserinfoCode(userinfoCode);
        SaSession session = StpUtil.getTokenSession();
        if (session != null) {
            session.set(LOGIN_USER_KEY, loginUser);
        }
    }

    /**
     * 当前买家会员/分销等级（登录时写入；未登录或无等级时为 null）
     */
    public static Long getDistLevelId() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getDistLevelId();
    }

    /**
     * 写入买家等级到登录态（等级变更后刷 Session 用）
     */
    public static void setDistLevelId(Long distLevelId) {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return;
        }
        loginUser.setDistLevelId(distLevelId);
        SaSession session = StpUtil.getTokenSession();
        if (session != null) {
            session.set(LOGIN_USER_KEY, loginUser);
        }
    }

    /**
     * 获取当前 Token 的扩展信息
     *
     * @param key 键值
     * @return 对应的扩展数据
     */
    private static Object getExtra(String key) {
        try {
            return StpUtil.getExtra(key);
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * 获取用户类型
     */
    public static UserType getUserType() {
        String loginType = StpUtil.getLoginIdAsString();
        return UserType.getUserType(loginType);
    }

    /**
     * 是否为超级管理员
     *
     * @param userId 用户ID
     * @return 结果
     */
    public static boolean isSuperAdmin(Long userId) {
        return SystemConstants.SUPER_ADMIN_ID.equals(userId);
    }

    /**
     * 是否为超级管理员
     *
     * @return 结果
     */
    public static boolean isSuperAdmin() {
        return isSuperAdmin(getUserId());
    }

    /**
     * 是否为租户管理员
     *
     * @param rolePermission 角色权限标识组
     * @return 结果
     */
    public static boolean isTenantAdmin(Set<String> rolePermission) {
        if (CollUtil.isEmpty(rolePermission)) {
            return false;
        }
        return rolePermission.contains(TenantConstants.TENANT_ADMIN_ROLE_KEY);
    }

    /**
     * 是否为租户管理员
     *
     * @return 结果
     */
    public static boolean isTenantAdmin() {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return false;
        }
        return Convert.toBool(isTenantAdmin(loginUser.getRolePermission()));
    }

    /**
     * 检查当前用户是否已登录
     *
     * @return 结果
     */
    public static boolean isLogin() {
        try {
            StpUtil.checkLogin();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
