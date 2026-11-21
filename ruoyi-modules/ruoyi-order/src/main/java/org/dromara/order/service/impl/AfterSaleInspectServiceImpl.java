package org.dromara.order.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.order.domain.AfterSale;
import org.dromara.order.domain.AfterSaleItem;
import org.dromara.order.domain.bo.AfterSaleInspectCommand;
import org.dromara.order.domain.bo.AfterSaleInspectCommand.InspectorType;
import org.dromara.order.mapper.AfterSaleItemMapper;
import org.dromara.order.mapper.AfterSaleMapper;
import org.dromara.order.service.IAfterSaleInspectService;
import org.dromara.order.service.IAfterSaleRefundService;
import org.dromara.order.util.OrderItemRefundOccupyWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class AfterSaleInspectServiceImpl implements IAfterSaleInspectService {

    private static final Long STATUS_WAIT_INSPECT = 30L;
    private static final Long STATUS_REFUNDING = 40L;
    private static final Long STATUS_REJECTED = 90L;

    private final AfterSaleMapper afterSaleMapper;
    private final AfterSaleItemMapper afterSaleItemMapper;
    private final IAfterSaleRefundService afterSaleRefundService;
    private final OrderItemRefundOccupyWriter orderItemRefundOccupyWriter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inspect(AfterSaleInspectCommand command) {
        if (command == null
            || StringUtils.isBlank(command.getTenantId())
            || StringUtils.isBlank(command.getAfterSaleNo())
            || command.getPass() == null
            || command.getInspectorType() == null) {
            throw new ServiceException("验货参数不完整");
        }
        AfterSale afterSale = afterSaleMapper.selectOne(Wrappers.<AfterSale>lambdaQuery()
            .eq(AfterSale::getTenantId, command.getTenantId())
            .eq(AfterSale::getAfterSaleNo, command.getAfterSaleNo())
            .last("LIMIT 1"));
        if (afterSale == null) {
            throw new ServiceException("售后单不存在");
        }
        if (command.getInspectorType() == InspectorType.MERCHANT) {
            if (StringUtils.isBlank(command.getMemberCode())
                || !command.getMemberCode().equals(afterSale.getMemberCode())) {
                throw new ServiceException("无权验货该店铺售后单");
            }
        } else if (command.getInspectorType() != InspectorType.OPS) {
            throw new ServiceException("不支持的验货角色");
        }
        if (!STATUS_WAIT_INSPECT.equals(afterSale.getStatus())) {
            throw new ServiceException("售后单状态不可验货");
        }

        Date now = new Date();
        String remark = command.getInspectRemark();
        if (!Boolean.TRUE.equals(command.getPass())) {
            if (StringUtils.isBlank(remark)) {
                throw new ServiceException("验货不通过时请填写原因");
            }
            int rows = afterSaleMapper.inspectByStatusCas(
                command.getTenantId(), command.getAfterSaleNo(),
                STATUS_WAIT_INSPECT, STATUS_REJECTED,
                now, remark);
            if (rows <= 0) {
                throw new ServiceException("验货失败，单据可能已被处理");
            }
            List<AfterSaleItem> items = afterSaleItemMapper.selectList(Wrappers.<AfterSaleItem>lambdaQuery()
                .eq(AfterSaleItem::getTenantId, command.getTenantId())
                .eq(AfterSaleItem::getAfterSaleNo, command.getAfterSaleNo()));
            orderItemRefundOccupyWriter.subRefunding(items);
            log.info("售后验货不通过 afterSaleNo={}", command.getAfterSaleNo());
            return;
        }

        int rows = afterSaleMapper.inspectByStatusCas(
            command.getTenantId(), command.getAfterSaleNo(),
            STATUS_WAIT_INSPECT, STATUS_REFUNDING,
            now, remark);
        if (rows <= 0) {
            throw new ServiceException("验货失败，单据可能已被处理");
        }
        afterSaleRefundService.triggerRefundAfterCommit(command.getTenantId(), command.getAfterSaleNo());
        log.info("售后验货通过 afterSaleNo={}", command.getAfterSaleNo());
    }
}
