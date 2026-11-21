package org.dromara.order.mapper;

import org.dromara.order.domain.AfterSale;
import org.dromara.order.domain.vo.AfterSaleVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * 售后退货单Mapper接口
 */
public interface AfterSaleMapper extends BaseMapperPlus<AfterSale, AfterSaleVo> {

    /**
     * 审核条件更新（fromStatus → toStatus）
     */
    int auditByStatusCas(@Param("tenantId") String tenantId,
                         @Param("afterSaleNo") String afterSaleNo,
                         @Param("fromStatus") Long fromStatus,
                         @Param("toStatus") Long toStatus,
                         @Param("auditTime") Date auditTime,
                         @Param("auditBy") String auditBy,
                         @Param("auditRemark") String auditRemark,
                         @Param("receiverName") String receiverName,
                         @Param("receiverMobile") String receiverMobile,
                         @Param("receiverAddress") String receiverAddress);

    /**
     * 买家填退货快递（20 → 30）
     */
    int fillReturnExpressCas(@Param("tenantId") String tenantId,
                             @Param("afterSaleNo") String afterSaleNo,
                             @Param("fromStatus") Long fromStatus,
                             @Param("toStatus") Long toStatus,
                             @Param("returnExpressCompany") String returnExpressCompany,
                             @Param("returnExpressNo") String returnExpressNo,
                             @Param("returnTime") Date returnTime);

    /**
     * 商家验货（30 → 40/90）
     */
    int inspectByStatusCas(@Param("tenantId") String tenantId,
                           @Param("afterSaleNo") String afterSaleNo,
                           @Param("fromStatus") Long fromStatus,
                           @Param("toStatus") Long toStatus,
                           @Param("inspectTime") Date inspectTime,
                           @Param("inspectRemark") String inspectRemark);

    /**
     * 退款成功（40 → 50）
     */
    int refundSuccessByStatusCas(@Param("tenantId") String tenantId,
                                 @Param("afterSaleNo") String afterSaleNo,
                                 @Param("fromStatus") Long fromStatus,
                                 @Param("toStatus") Long toStatus,
                                 @Param("refundTime") Date refundTime,
                                 @Param("refundTradeNo") String refundTradeNo);

    /**
     * 买家撤销 CAS（fromStatus → 99；允许 10，或退货退款的 20）
     */
    int revokeByStatusCas(@Param("tenantId") String tenantId,
                          @Param("afterSaleNo") String afterSaleNo,
                          @Param("fromStatus") Long fromStatus,
                          @Param("toStatus") Long toStatus,
                          @Param("updateTime") Date updateTime);
}
