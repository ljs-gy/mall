package org.dromara.pm.service;

import org.dromara.pm.domain.CouponTemplate;
import org.dromara.pm.domain.vo.CouponTemplateVo;
import org.dromara.pm.domain.bo.CouponTemplateAuditBo;
import org.dromara.pm.domain.bo.CouponTemplateBo;
import org.dromara.pm.mq.CouponDirectAllPageMessage;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 优惠券Service接口
 *
 * @author ljs
 * @date 2026-07-21
 */
public interface ICouponTemplateService {

    CouponTemplateVo queryById(Long id);

    TableDataInfo<CouponTemplateVo> queryPageList(CouponTemplateBo bo, PageQuery pageQuery);

    List<CouponTemplateVo> queryList(CouponTemplateBo bo);


    Boolean insertByBo(CouponTemplateBo bo);

    Boolean updateByBo(CouponTemplateBo bo);

    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);


    void auditCouponTemplate(CouponTemplateAuditBo bo);


    void cacheCouponTemplate(Long templateId);


    void clearCouponCache(Long templateId);


    void sendDirectCouponsAsync(CouponTemplate template, Long perUserCount, List<Long> userIds);


    void dispatchDirectSend(CouponTemplate template);

    void startDirectGrant(CouponTemplate template);


    void processDirectAllPage(CouponDirectAllPageMessage message);

}
