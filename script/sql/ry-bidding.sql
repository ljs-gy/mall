-- ----------------------------
-- 竞价中心业务表结构（库：ruoyi-bidding）
-- 菜单请在 ry-cloud 库执行各表代码生成导出的 *Menu.sql
-- ----------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 完整 DDL 见 Navicat 导出，此处仅作版本管理占位
-- 表清单：
-- bid_resource, bid_rule_setting, bid_session, bid_announcement_item,
-- bid_announcement_item_goods, bid_apply_record, bid_apply_item_rel,
-- bid_financial_apply, bid_order, bid_bid_log, user_deposit_log

-- 标的独立截拍时间
ALTER TABLE `bid_announcement_item`
    ADD COLUMN `original_end_time` datetime DEFAULT NULL COMMENT '初始原定截拍截止时间' AFTER `item_status`,
    ADD COLUMN `item_end_time` datetime DEFAULT NULL COMMENT '当前包裹独立动态截拍截止时间' AFTER `original_end_time`;

-- 子商品底价精度
ALTER TABLE `bid_announcement_item_goods`
    MODIFY COLUMN `priceset_n_price` decimal(15, 3) NOT NULL COMMENT '单项底价（快照）';

-- 出价流水：bid_price 为单位溢价，追加成交总金额
ALTER TABLE `bid_bid_log`
    MODIFY COLUMN `bid_price` decimal(15, 2) NOT NULL COMMENT '买家报出的单位重量溢价金额(元/吨这类)',
    ADD COLUMN `bid_total_money` decimal(15, 2) NOT NULL DEFAULT 0.00 COMMENT '折算出的成交总金额(总重量*折算单价)' AFTER `bid_price_unit`;

-- 场次规则快照字段（从 rule 冗余到 session，便于 Redis 预热）
ALTER TABLE `bid_session`
    ADD COLUMN `bid_model` tinyint NOT NULL DEFAULT 2 COMMENT '默认竞价模型：1-自由竞价, 2-公开竞价, 3-拆分竞价, 4-反向公开竞价, 5-反向自由竞价' AFTER `rule_id`,
    ADD COLUMN `bid_type` tinyint NOT NULL DEFAULT 1 COMMENT '1-常规竞价，2-资质竞价' AFTER `bid_model`,
    ADD COLUMN `min_apply_users` int NOT NULL DEFAULT 1 COMMENT '最少报名人数红线' AFTER `bid_type`,
    ADD COLUMN `max_winner_users` int NOT NULL DEFAULT 1 COMMENT '可中标人数限制' AFTER `min_apply_users`,
    ADD COLUMN `is_delay_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否开启动态延时：0-关闭，1-开启' AFTER `max_winner_users`,
    ADD COLUMN `delay_trigger_minutes` int DEFAULT 5 COMMENT '单次延时触发周期（分钟）' AFTER `is_delay_enabled`,
    ADD COLUMN `max_delay_minutes` int DEFAULT 60 COMMENT '最大总延时时长' AFTER `delay_trigger_minutes`,
    ADD COLUMN `commission_type` tinyint NOT NULL DEFAULT 0 COMMENT '平台佣金：0-不需要，1-比例，2-固定额度' AFTER `max_delay_minutes`,
    ADD COLUMN `commission_value` decimal(15, 2) DEFAULT 0.00 COMMENT '佣金数值（比例或固定金额快照）' AFTER `commission_type`;

SET FOREIGN_KEY_CHECKS = 1;
