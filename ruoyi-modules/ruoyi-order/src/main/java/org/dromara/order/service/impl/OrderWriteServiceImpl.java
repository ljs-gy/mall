package org.dromara.order.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.order.api.domain.RemoteOrderCreateBo;
import org.dromara.order.api.domain.RemoteOrderItemCreateBo;
import org.dromara.order.domain.Order;
import org.dromara.order.domain.OrderItem;
import org.dromara.order.mapper.OrderItemMapper;
import org.dromara.order.mapper.OrderMapper;
import org.dromara.order.service.IOrderWriteService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 订单本地写入实现
 */
@Service
@RequiredArgsConstructor
public class OrderWriteServiceImpl implements IOrderWriteService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    public Long createOrder(RemoteOrderCreateBo bo) {
        if (bo == null || StringUtils.isBlank(bo.getOrderNo())) {
            return null;
        }
        Order order = new Order();
        order.setTenantId(bo.getTenantId());
        order.setOrderNo(bo.getOrderNo());
        order.setParentOrderNo(bo.getParentOrderNo());
        order.setPayTradeNo("");
        order.setChannelCode(bo.getChannelCode());
        order.setMemberCode(bo.getMemberCode());
        order.setUserinfoId(bo.getUserinfoId());
        order.setMemberBcode(bo.getMemberBcode());
        order.setUserLevelId(bo.getUserLevelId());
        order.setOrderStatus(bo.getOrderStatus());
        order.setPayStatus(bo.getPayStatus());
        order.setGoodsTotalAmount(bo.getGoodsTotalAmount());
        order.setFreightAmount(bo.getFreightAmount());
        order.setDiscountAmount(bo.getDiscountAmount());
        order.setPayAmount(bo.getPayAmount());
        order.setConsigneeName(bo.getConsigneeName());
        order.setConsigneeMobile(bo.getConsigneeMobile());
        order.setReceiverAddressText(bo.getReceiverAddressText());
        order.setBuyerRemark(bo.getBuyerRemark());
        orderMapper.insert(order);
        return order.getId();
    }

    @Override
    public Long createOrderItem(RemoteOrderItemCreateBo bo) {
        if (bo == null || StringUtils.isBlank(bo.getOrderNo())) {
            return null;
        }
        OrderItem item = new OrderItem();
        item.setTenantId(bo.getTenantId());
        item.setOrderNo(bo.getOrderNo());
        item.setSpuId(bo.getSpuId());
        item.setSkuId(bo.getSkuId());
        item.setSkuCode(StringUtils.blankToDefault(bo.getSkuCode(), ""));
        item.setMemberCode(bo.getMemberCode());
        item.setGoodsTitle(bo.getGoodsTitle());
        item.setSpecText(bo.getSpecText());
        item.setCoverImage(bo.getCoverImage());
        item.setPriceSales(bo.getPriceSales());
        item.setSharePrice(bo.getSharePrice());
        item.setQuantity(bo.getQuantity());
        item.setIsGift(bo.getIsGift());
        item.setTierRuleId(bo.getTierRuleId());
        item.setSubTotal(bo.getSubTotal());
        item.setRealPayAmount(bo.getRealPayAmount());
        item.setRefundingQty(0);
        item.setRefundingAmount(BigDecimal.ZERO);
        item.setRefundedQty(0);
        item.setRefundedAmount(BigDecimal.ZERO);
        item.setDelFlag("0");
        orderItemMapper.insert(item);
        return item.getId();
    }
}
