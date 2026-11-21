package org.dromara.pm.utils;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.pm.domain.CouponTemplate;

import java.util.Date;


public final class CouponValidTimeUtils {

    private CouponValidTimeUtils() {
    }

    public static boolean isFixed(Date useStartTime, Date useEndTime, Long validMinutes) {
        return useStartTime != null && useEndTime != null
            && (validMinutes == null || validMinutes <= 0);
    }

    public static boolean isRelative(Date useStartTime, Date useEndTime, Long validMinutes) {
        return validMinutes != null && validMinutes > 0 && useStartTime == null && useEndTime == null;
    }

    public static boolean isFixed(CouponTemplate template) {
        return isFixed(template.getUseStartTime(), template.getUseEndTime(), template.getValidMinutes());
    }

    public static boolean isRelative(CouponTemplate template) {
        return isRelative(template.getUseStartTime(), template.getUseEndTime(), template.getValidMinutes());
    }

    public static void validateExclusive(Date useStartTime, Date useEndTime, Long validMinutes) {
        boolean hasStart = useStartTime != null;
        boolean hasEnd = useEndTime != null;
        boolean hasMinutes = validMinutes != null && validMinutes > 0;
        if (hasStart != hasEnd) {
            throw new ServiceException("固定有效期必须同时填写使用开始和结束时间");
        }
        if (hasStart && hasMinutes) {
            throw new ServiceException("固定时段与相对有效分钟不能同时填写");
        }
        if (!hasStart && !hasMinutes) {
            throw new ServiceException("请填写固定使用时段，或填写领取后有效分钟数");
        }
    }

    /**
     * 相对有效期无模板级使用起止，生命周期靠领用区间收口，必须成对填写。
     */
    public static void validateRelativeClaimWindow(Date claimStartTime, Date claimEndTime,
                                                   Date useStartTime, Date useEndTime, Long validMinutes) {
        if (!isRelative(useStartTime, useEndTime, validMinutes)) {
            return;
        }
        if (claimStartTime == null || claimEndTime == null) {
            throw new ServiceException("相对有效期须填写领用开始和结束时间");
        }
        if (claimEndTime.before(claimStartTime)) {
            throw new ServiceException("领用结束时间不能早于开始时间");
        }
    }

    /**
     * 给落到用户手里的那张券算可用起止时间
     */
    public static Date[] resolveUserWindow(CouponTemplate template, Date now) {
        if (isRelative(template)) {
            long minutes = template.getValidMinutes();
            return new Date[]{now, new Date(now.getTime() + minutes * 60_000L)};
        }
        if (isFixed(template)) {
            return new Date[]{template.getUseStartTime(), template.getUseEndTime()};
        }
        throw new ServiceException("优惠券可用时间配置异常");
    }
}
