package org.dromara.bidding.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;
import java.math.BigDecimal;

/**
 * 竞价场次对象 bid_session
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bid_session")
public class Session extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 竞价场次全局唯一业务编号
     */
    private String sessionCode;

    /**
     * 标题
     */
    private String title;

    /**
     * 封面配图
     */
    private String coverImage;

    /**
     * 宣传视频地址
     */
    private String videoUrl;

    /**
     * 场次模式：1-预告，2-正式公告
     */
    private Integer sessionMode;

    /**
     * 竞价日期
     */
    private Date bidDate;

    /**
     * 精准开拍时刻
     */
    private String startTime;

    /**
     * 精准截拍时刻
     */
    private String endTime;

    /**
     * 竞价交保及报名截止死线
     */
    private Date applyDeadline;

    /**
     * 可参与人员：0-全部，1-仅限个人，2-仅限企业用户
     */
    private Integer allowedUserType;

    /**
     * 绑定的竞价规则ID
     */
    private Long ruleId;

    /**
     * 保证金收取方式：1-按场次收门票，2-按具体商品/包裹独立收，3-同时勾选叠加收取
     */
    private Integer depositType;

    /**
     * 全场固定保证金金额
     */
    private BigDecimal fullDepositAmount;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 仓库地址
     */
    private String warehouseAddress;

    /**
     * 现场联系人
     */
    private String contactName;

    /**
     * 现场联系电话
     */
    private String contactPhone;

    /**
     * 是否支持现场看货：0-否，1-是
     */
    private Integer isLookSupported;

    /**
     * 允许现场看货的开始日期
     */
    private Date lookStartDate;

    /**
     * 允许现场看货的截止日期
     */
    private Date lookEndDate;

    /**
     * 看货报名登记截止时间
     */
    private Date lookApplyDeadline;

    /**
     * 详情介绍（富文本描述）
     */
    private String detailContent;

    /**
     * 竞价交易须知/交易说明
     */
    private String tradeDesc;

    /**
     * 业务专用联系人及电话
     */
    private String businessContact;

    /**
     * 特别提示（免责、按现状交付声明）
     */
    private String specialTips;

    /**
     * 附件清单JSON字符串数组
     */
    private String sessionAttachments;

    /**
     * 审批状态：0-草稿, 1-提交待审, 2-审核通过, 3-审核驳回
     */
    private Integer auditStatus;

    /**
     * 竞价生命周期：0-未开拍, 1-正在竞价中, 2-已到期截拍, 3-正常完结, 4-人数不足自动流拍
     */
    private Integer bidStatus;

    /**
     * 审批操作人
     */
    private String auditUser;

    /**
     * 审批驳回原因/批注
     */
    private String auditOpinion;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    private String delFlag;

}