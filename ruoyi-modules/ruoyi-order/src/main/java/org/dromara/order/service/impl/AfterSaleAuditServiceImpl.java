package org.dromara.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.order.domain.AfterSale;
import org.dromara.order.domain.AfterSaleItem;
import org.dromara.order.domain.MerchantReturnAddress;
import org.dromara.order.domain.bo.AfterSaleAuditCommand;
import org.dromara.order.domain.bo.AfterSaleAuditCommand.AuditorType;
import org.dromara.order.domain.bo.AfterSaleQueryBo;
import org.dromara.order.domain.vo.AfterSaleItemVo;
import org.dromara.order.domain.vo.AfterSaleVo;
import org.dromara.order.domain.vo.PcAfterSaleDetailVo;
import org.dromara.order.mapper.AfterSaleItemMapper;
import org.dromara.order.mapper.AfterSaleMapper;
import org.dromara.order.mapper.MerchantReturnAddressMapper;
import org.dromara.order.service.IAfterSaleAuditService;
import org.dromara.order.service.IAfterSaleRefundService;
import org.dromara.order.util.LoginBizCodeHelper;
import org.dromara.order.util.OrderItemRefundOccupyWriter;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class AfterSaleAuditServiceImpl implements IAfterSaleAuditService {

    private static final Long STATUS_PENDING_AUDIT = 10L;
    private static final Long STATUS_WAIT_RETURN = 20L;
    private static final Long STATUS_REFUNDING = 40L;
    private static final Long STATUS_REJECTED = 90L;

    private static final Long SERVICE_REFUND_ONLY = 1L;
    private static final Long SERVICE_RETURN_REFUND = 2L;

    private final AfterSaleMapper afterSaleMapper;
    private final AfterSaleItemMapper afterSaleItemMapper;
    private final MerchantReturnAddressMapper merchantReturnAddressMapper;
    private final IAfterSaleRefundService afterSaleRefundService;
    private final LoginBizCodeHelper loginBizCodeHelper;
    private final OrderItemRefundOccupyWriter orderItemRefundOccupyWriter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(AfterSaleAuditCommand command) {
        if (command == null
            || StringUtils.isBlank(command.getTenantId())
            || StringUtils.isBlank(command.getAfterSaleNo())
            || command.getPass() == null
            || command.getAuditorType() == null) {
            throw new ServiceException("审核参数不完整");
        }
        AfterSale afterSale = afterSaleMapper.selectOne(Wrappers.<AfterSale>lambdaQuery()
            .eq(AfterSale::getTenantId, command.getTenantId())
            .eq(AfterSale::getAfterSaleNo, command.getAfterSaleNo())
            .last("LIMIT 1"));
        if (afterSale == null) {
            throw new ServiceException("售后单不存在");
        }
        String auditBy = loginBizCodeHelper.requireUserinfoCode();
        if (command.getAuditorType() == AuditorType.MERCHANT) {
            if (!auditBy.equals(afterSale.getMemberCode())) {
                throw new ServiceException("无权审核该店铺售后单");
            }
        } else if (command.getAuditorType() != AuditorType.OPS) {
            throw new ServiceException("不支持的审核角色");
        }
        if (!STATUS_PENDING_AUDIT.equals(afterSale.getStatus())) {
            throw new ServiceException("售后单状态不可审核");
        }
        Date now = new Date();
        String remark = command.getAuditRemark();
        if (!Boolean.TRUE.equals(command.getPass())) {
            if (StringUtils.isBlank(remark)) {
                throw new ServiceException("拒绝时请填写审核留言");
            }
            int rows = afterSaleMapper.auditByStatusCas(
                command.getTenantId(), command.getAfterSaleNo(),
                STATUS_PENDING_AUDIT, STATUS_REJECTED,
                now, auditBy, remark,
                null, null, null);
            if (rows <= 0) {
                throw new ServiceException("审核失败，单据可能已被处理");
            }
            List<AfterSaleItem> items = afterSaleItemMapper.selectList(Wrappers.<AfterSaleItem>lambdaQuery()
                .eq(AfterSaleItem::getTenantId, command.getTenantId())
                .eq(AfterSaleItem::getAfterSaleNo, command.getAfterSaleNo()));
            orderItemRefundOccupyWriter.subRefunding(items);
            log.info("售后审核拒绝 afterSaleNo={} by={}", command.getAfterSaleNo(), auditBy);
            return;
        }
        Long serviceType = afterSale.getServiceType();
        if (SERVICE_REFUND_ONLY.equals(serviceType)) {
            int rows = afterSaleMapper.auditByStatusCas(
                command.getTenantId(), command.getAfterSaleNo(),
                STATUS_PENDING_AUDIT, STATUS_REFUNDING,
                now, auditBy, remark,
                null, null, null);
            if (rows <= 0) {
                throw new ServiceException("审核失败，单据可能已被处理");
            }
            afterSaleRefundService.triggerRefundAfterCommit(command.getTenantId(), command.getAfterSaleNo());
            log.info("仅退款审核通过 afterSaleNo={} by={}", command.getAfterSaleNo(), auditBy);
            return;
        }

        if (SERVICE_RETURN_REFUND.equals(serviceType)) {
            if (command.getReturnAddressId() == null) {
                throw new ServiceException("退货退款审核通过时请选择退货收货地址");
            }
            MerchantReturnAddress address = merchantReturnAddressMapper.selectOne(Wrappers.<MerchantReturnAddress>lambdaQuery()
                .eq(MerchantReturnAddress::getTenantId, command.getTenantId())
                .eq(MerchantReturnAddress::getId, command.getReturnAddressId())
                .eq(MerchantReturnAddress::getMemberCode, afterSale.getMemberCode())
                .last("LIMIT 1"));
            if (address == null) {
                throw new ServiceException("退货收货地址不存在或不属于该商家");
            }
            String fullAddress = String.format("%s %s %s %s",
                StringUtils.blankToDefault(address.getProvinceName(), ""),
                StringUtils.blankToDefault(address.getCityName(), ""),
                StringUtils.blankToDefault(address.getDistrictName(), ""),
                StringUtils.blankToDefault(address.getDetailAddress(), "")).trim();
            int rows = afterSaleMapper.auditByStatusCas(
                command.getTenantId(), command.getAfterSaleNo(),
                STATUS_PENDING_AUDIT, STATUS_WAIT_RETURN,
                now, auditBy, remark,
                address.getConsigneeName(), address.getConsigneeMobile(), fullAddress);
            if (rows <= 0) {
                throw new ServiceException("审核失败，单据可能已被处理");
            }
            log.info("退货退款审核通过 afterSaleNo={} addressId={} by={}", command.getAfterSaleNo(), command.getReturnAddressId(), auditBy);
            return;
        }
        throw new ServiceException("不支持的售后类型");
    }

    @Override
    public TableDataInfo<AfterSaleVo> queryPage(AfterSaleQueryBo query, String tenantId, PageQuery pageQuery) {
        LambdaQueryWrapper<AfterSale> lqw = Wrappers.lambdaQuery();
        lqw.eq(AfterSale::getTenantId, tenantId);
        if (query != null) {
            lqw.eq(StringUtils.isNotBlank(query.getAfterSaleNo()), AfterSale::getAfterSaleNo, query.getAfterSaleNo());
            lqw.eq(StringUtils.isNotBlank(query.getOrderNo()), AfterSale::getOrderNo, query.getOrderNo());
            lqw.eq(query.getServiceType() != null, AfterSale::getServiceType, query.getServiceType());
            lqw.eq(query.getStatus() != null, AfterSale::getStatus, query.getStatus());
            lqw.eq(StringUtils.isNotBlank(query.getMemberCode()), AfterSale::getMemberCode, query.getMemberCode());
        }
        lqw.orderByDesc(AfterSale::getCreateTime);
        Page<AfterSaleVo> page = afterSaleMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Override
    public PcAfterSaleDetailVo detail(String afterSaleNo, String tenantId, String memberCodeRestrict) {
        if (StringUtils.isBlank(afterSaleNo)) {
            throw new ServiceException("售后单号不能为空");
        }
        AfterSale afterSale = afterSaleMapper.selectOne(Wrappers.<AfterSale>lambdaQuery()
            .eq(AfterSale::getTenantId, tenantId)
            .eq(AfterSale::getAfterSaleNo, afterSaleNo)
            .last("LIMIT 1"));
        if (afterSale == null) {
            throw new ServiceException("售后单不存在");
        }
        if (StringUtils.isNotBlank(memberCodeRestrict)
            && !memberCodeRestrict.equals(afterSale.getMemberCode())) {
            throw new ServiceException("无权查看该售后单");
        }
        PcAfterSaleDetailVo detail = new PcAfterSaleDetailVo();
        BeanUtils.copyProperties(afterSale, detail);
        List<AfterSaleItemVo> items = afterSaleItemMapper.selectVoList(Wrappers.<AfterSaleItem>lambdaQuery()
            .eq(AfterSaleItem::getTenantId, tenantId)
            .eq(AfterSaleItem::getAfterSaleNo, afterSaleNo));
        detail.setItems(items);
        return detail;
    }
}
