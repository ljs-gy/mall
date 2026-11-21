package org.dromara.bidding.domain.bo;

import org.dromara.bidding.domain.Session;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.util.Date;
import java.math.BigDecimal;

/**
 * 竞价场次业务对象 bid_session
 *
 * @author ljs
 * @date 2026-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Session.class, reverseConvertGenerate = false)
public class SessionBo extends BaseEntity {

    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    /**
     * 主键
     */
    private Long id;

    @NotBlank(message = "竞价场次全局唯一业务编号不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 竞价场次全局唯一业务编号
     */
    private String sessionCode;

    @NotBlank(message = "标题不能为空", groups = { AddGroup.class, EditGroup.class })
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

    @NotNull(message = "场次模式：1-预告，2-正式公告不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 场次模式：1-预告，2-正式公告
     */
    private Integer sessionMode;

    @NotNull(message = "竞价日期不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 竞价日期
     */
    private Date bidDate;

    @NotNull(message = "精准开拍时刻不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 精准开拍时刻
     */
    private Date startTime;

    @NotNull(message = "精准截拍时刻不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 精准截拍时刻
     */
    private Date endTime;

    @NotNull(message = "竞价交保及报名截止死线不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 竞价交保及报名截止死线
     */
    private Date applyDeadline;

    @NotNull(message = "可参与人员：0-全部，1-仅限个人，2-仅限企业用户不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 可参与人员：0-全部，1-仅限个人，2-仅限企业用户
     */
    private Integer allowedUserType;

    /**
     * 绑定的竞价规则ID
     */
    private Long ruleId;

    /**
     * 默认竞价模型：1-自由竞价, 2-公开竞价, 3-拆分竞价, 4-反向公开竞价, 5-反向自由竞价
     */
    private Integer bidModel;

    /**
     * 1-常规竞价，2-资质竞价
     */
    private Integer bidType;

    /**
     * 最少报名人数红线
     */
    private Integer minApplyUsers;

    /**
     * 可中标人数限制
     */
    private Integer maxWinnerUsers;

    /**
     * 是否开启动态延时：0-关闭，1-开启
     */
    private Integer isDelayEnabled;

    /**
     * 单次延时触发周期（分钟）
     */
    private Integer delayTriggerMinutes;

    /**
     * 最大总延时时长（分钟）
     */
    private Integer maxDelayMinutes;

    /**
     * 平台佣金：0-不需要，1-比例，2-固定额度
     */
    private Integer commissionType;

    /**
     * 佣金数值（比例或固定金额快照）
     */
    private BigDecimal commissionValue;

    @NotNull(message = "保证金收取方式：1-按场次收门票，2-按具体商品/包裹独立收，3-同时勾选叠加收取不能为空", groups = { AddGroup.class, EditGroup.class })
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

    @NotNull(message = "是否支持现场看货：0-否，1-是不能为空", groups = { AddGroup.class, EditGroup.class })
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

    @NotNull(message = "审批状态：0-草稿, 1-提交待审, 2-审核通过, 3-审核驳回不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 审批状态：0-草稿, 1-提交待审, 2-审核通过, 3-审核驳回
     */
    private Integer auditStatus;

    @NotNull(message = "竞价生命周期：0-未开拍, 1-正在竞价中, 2-已到期截拍, 3-正常完结, 4-人数不足自动流拍不能为空", groups = { AddGroup.class, EditGroup.class })
    /**
     * 竞价生命周期：0-未开拍, 1-正在竞价中, 2-已到期截拍, 3-正常完结, 4-人数不足自动流拍
     */
    private Integer bidStatus;

    /**
     * 审批操作人ID
     */
    private Long auditBy;

    /**
     * 审批时间
     */
    private Date auditTime;

    /**
     * 审批驳回原因/批注
     */
    private String auditOpinion;

}