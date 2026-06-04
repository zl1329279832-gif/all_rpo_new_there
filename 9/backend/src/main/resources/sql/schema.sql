-- =============================================
-- 连锁烘焙门店生产与临期管理系统 - 数据库脚本
-- Database: MySQL 8.0
-- =============================================

DROP DATABASE IF EXISTS bakery_management;
CREATE DATABASE bakery_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bakery_management;

-- =============================================
-- 1. 门店表
-- =============================================
DROP TABLE IF EXISTS `sys_store`;
CREATE TABLE `sys_store` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `store_code` VARCHAR(32) NOT NULL COMMENT '门店编码',
  `store_name` VARCHAR(64) NOT NULL COMMENT '门店名称',
  `store_type` TINYINT NOT NULL DEFAULT 1 COMMENT '门店类型:1-中心工厂 2-直营门店',
  `address` VARCHAR(200) DEFAULT NULL COMMENT '地址',
  `manager` VARCHAR(32) DEFAULT NULL COMMENT '负责人',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-禁用 1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_store_code` (`store_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店表';

-- =============================================
-- 2. 原料表
-- =============================================
DROP TABLE IF EXISTS `base_material`;
CREATE TABLE `base_material` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `material_code` VARCHAR(32) NOT NULL COMMENT '原料编码',
  `material_name` VARCHAR(64) NOT NULL COMMENT '原料名称',
  `category` VARCHAR(32) DEFAULT NULL COMMENT '分类:面粉-糖类-奶油-馅料-其他',
  `unit` VARCHAR(16) NOT NULL COMMENT '单位:kg-g-袋-个',
  `spec` VARCHAR(64) DEFAULT NULL COMMENT '规格',
  `shelf_life_days` INT NOT NULL COMMENT '保质期(天)',
  `warning_days` INT NOT NULL DEFAULT 7 COMMENT '临期预警天数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-禁用 1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_material_code` (`material_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='原料表';

-- =============================================
-- 3. 原料库存表
-- =============================================
DROP TABLE IF EXISTS `base_material_stock`;
CREATE TABLE `base_material_stock` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `material_id` BIGINT NOT NULL COMMENT '原料ID',
  `store_id` BIGINT NOT NULL COMMENT '门店ID',
  `batch_no` VARCHAR(64) NOT NULL COMMENT '批次号',
  `quantity` DECIMAL(10,2) NOT NULL COMMENT '库存数量',
  `produce_date` DATE NOT NULL COMMENT '生产日期',
  `expire_date` DATE NOT NULL COMMENT '过期日期',
  `inbound_time` DATETIME NOT NULL COMMENT '入库时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_material_store` (`material_id`,`store_id`),
  KEY `idx_expire_date` (`expire_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='原料库存表';

-- =============================================
-- 4. 成品表（配方主表）
-- =============================================
DROP TABLE IF EXISTS `base_recipe`;
CREATE TABLE `base_recipe` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_code` VARCHAR(32) NOT NULL COMMENT '成品编码',
  `product_name` VARCHAR(64) NOT NULL COMMENT '成品名称',
  `category` VARCHAR(32) DEFAULT NULL COMMENT '分类:面包-蛋糕-西点-酥点',
  `unit` VARCHAR(16) NOT NULL COMMENT '单位:个-盒-斤',
  `shelf_life_hours` INT NOT NULL COMMENT '保质期(小时)',
  `warning_hours` INT NOT NULL DEFAULT 12 COMMENT '临期预警(小时)',
  `output_qty` DECIMAL(10,2) NOT NULL DEFAULT 1 COMMENT '单次产出数量',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-禁用 1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_code` (`product_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成品配方主表';

-- =============================================
-- 5. 配方明细表
-- =============================================
DROP TABLE IF EXISTS `base_recipe_detail`;
CREATE TABLE `base_recipe_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `recipe_id` BIGINT NOT NULL COMMENT '配方ID',
  `material_id` BIGINT NOT NULL COMMENT '原料ID',
  `dosage` DECIMAL(10,4) NOT NULL COMMENT '用量',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_recipe_id` (`recipe_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配方明细表';

-- =============================================
-- 6. 生产计划表
-- =============================================
DROP TABLE IF EXISTS `prod_plan`;
CREATE TABLE `prod_plan` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `plan_no` VARCHAR(32) NOT NULL COMMENT '计划编号',
  `plan_date` DATE NOT NULL COMMENT '计划日期',
  `store_id` BIGINT NOT NULL COMMENT '生产门店ID',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0-待审核 1-已审核 2-生产中 3-已完成 4-已取消',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plan_no` (`plan_no`),
  KEY `idx_plan_date` (`plan_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产计划表';

-- =============================================
-- 7. 生产计划明细表
-- =============================================
DROP TABLE IF EXISTS `prod_plan_detail`;
CREATE TABLE `prod_plan_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `plan_id` BIGINT NOT NULL COMMENT '计划ID',
  `recipe_id` BIGINT NOT NULL COMMENT '配方ID',
  `plan_qty` DECIMAL(10,2) NOT NULL COMMENT '计划生产数量',
  `actual_qty` DECIMAL(10,2) DEFAULT 0 COMMENT '实际生产数量',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_plan_id` (`plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产计划明细表';

-- =============================================
-- 8. 门店需求表
-- =============================================
DROP TABLE IF EXISTS `store_demand`;
CREATE TABLE `store_demand` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `demand_no` VARCHAR(32) NOT NULL COMMENT '需求单号',
  `demand_date` DATE NOT NULL COMMENT '需求日期',
  `store_id` BIGINT NOT NULL COMMENT '需求门店ID',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0-待确认 1-已确认 2-已发货 3-已完成',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_demand_no` (`demand_no`),
  KEY `idx_demand_date` (`demand_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店需求表';

-- =============================================
-- 9. 门店需求明细表
-- =============================================
DROP TABLE IF EXISTS `store_demand_detail`;
CREATE TABLE `store_demand_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `demand_id` BIGINT NOT NULL COMMENT '需求ID',
  `recipe_id` BIGINT NOT NULL COMMENT '成品ID',
  `demand_qty` DECIMAL(10,2) NOT NULL COMMENT '需求数量',
  `confirm_qty` DECIMAL(10,2) DEFAULT 0 COMMENT '确认数量',
  `deliver_qty` DECIMAL(10,2) DEFAULT 0 COMMENT '发货数量',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_demand_id` (`demand_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店需求明细表';

-- =============================================
-- 10. 成品批次表
-- =============================================
DROP TABLE IF EXISTS `prod_batch`;
CREATE TABLE `prod_batch` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_no` VARCHAR(64) NOT NULL COMMENT '批次号',
  `recipe_id` BIGINT NOT NULL COMMENT '成品ID',
  `plan_id` BIGINT DEFAULT NULL COMMENT '关联生产计划ID',
  `store_id` BIGINT NOT NULL COMMENT '生产门店ID',
  `produce_qty` DECIMAL(10,2) NOT NULL COMMENT '生产数量',
  `produce_time` DATETIME NOT NULL COMMENT '生产时间',
  `expire_time` DATETIME NOT NULL COMMENT '过期时间',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1-在库 2-部分出库 3-已售罄 4-已报损',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch_no` (`batch_no`),
  KEY `idx_expire_time` (`expire_time`),
  KEY `idx_recipe_store` (`recipe_id`,`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成品批次表';

-- =============================================
-- 11. 调拨记录表
-- =============================================
DROP TABLE IF EXISTS `stock_transfer`;
CREATE TABLE `stock_transfer` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `transfer_no` VARCHAR(32) NOT NULL COMMENT '调拨单号',
  `out_store_id` BIGINT NOT NULL COMMENT '调出门店ID',
  `in_store_id` BIGINT NOT NULL COMMENT '调入门店ID',
  `transfer_type` TINYINT NOT NULL DEFAULT 1 COMMENT '调拨类型:1-正常调拨 2-临期调拨',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0-待出库 1-已出库 2-已入库 3-已取消',
  `total_qty` DECIMAL(10,2) NOT NULL COMMENT '调拨总数量',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `outbound_time` DATETIME DEFAULT NULL COMMENT '出库时间',
  `inbound_time` DATETIME DEFAULT NULL COMMENT '入库时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transfer_no` (`transfer_no`),
  KEY `idx_out_store` (`out_store_id`),
  KEY `idx_in_store` (`in_store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调拨记录表';

-- =============================================
-- 12. 调拨明细表
-- =============================================
DROP TABLE IF EXISTS `stock_transfer_detail`;
CREATE TABLE `stock_transfer_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `transfer_id` BIGINT NOT NULL COMMENT '调拨ID',
  `batch_id` BIGINT NOT NULL COMMENT '批次ID',
  `recipe_id` BIGINT NOT NULL COMMENT '成品ID',
  `transfer_qty` DECIMAL(10,2) NOT NULL COMMENT '调拨数量',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_transfer_id` (`transfer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调拨明细表';

-- =============================================
-- 13. 报损记录表
-- =============================================
DROP TABLE IF EXISTS `stock_damage`;
CREATE TABLE `stock_damage` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `damage_no` VARCHAR(32) NOT NULL COMMENT '报损单号',
  `store_id` BIGINT NOT NULL COMMENT '门店ID',
  `damage_type` TINYINT NOT NULL DEFAULT 1 COMMENT '报损类型:1-临期过期 2-质量问题 3-破损 4-其他',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0-待审核 1-已审核 2-已驳回',
  `total_qty` DECIMAL(10,2) NOT NULL COMMENT '报损总数量',
  `total_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '报损总金额',
  `reason` VARCHAR(500) DEFAULT NULL COMMENT '报损原因',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `audit_by` BIGINT DEFAULT NULL COMMENT '审核人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_damage_no` (`damage_no`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报损记录表';

-- =============================================
-- 14. 报损明细表
-- =============================================
DROP TABLE IF EXISTS `stock_damage_detail`;
CREATE TABLE `stock_damage_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `damage_id` BIGINT NOT NULL COMMENT '报损ID',
  `batch_id` BIGINT NOT NULL COMMENT '批次ID',
  `recipe_id` BIGINT NOT NULL COMMENT '成品ID',
  `damage_qty` DECIMAL(10,2) NOT NULL COMMENT '报损数量',
  `unit_price` DECIMAL(10,2) DEFAULT 0 COMMENT '单价',
  `subtotal` DECIMAL(10,2) DEFAULT 0 COMMENT '小计',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_damage_id` (`damage_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报损明细表';

-- =============================================
-- 15. 销售统计表
-- =============================================
DROP TABLE IF EXISTS `sales_stat`;
CREATE TABLE `sales_stat` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `store_id` BIGINT NOT NULL COMMENT '门店ID',
  `recipe_id` BIGINT NOT NULL COMMENT '成品ID',
  `sales_qty` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '销售数量',
  `sales_amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '销售金额',
  `cost_amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '成本金额',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date_store_product` (`stat_date`,`store_id`,`recipe_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售统计表';

-- =============================================
-- 16. 库存操作日志表
-- =============================================
DROP TABLE IF EXISTS `stock_log`;
CREATE TABLE `stock_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `store_id` BIGINT NOT NULL COMMENT '门店ID',
  `biz_type` VARCHAR(32) NOT NULL COMMENT '业务类型:PRODUCE-生产入库 TRANSFER_OUT-调拨出库 TRANSFER_IN-调拨入库 DAMAGE-报损 SALE-销售',
  `biz_no` VARCHAR(64) NOT NULL COMMENT '业务单号',
  `batch_id` BIGINT DEFAULT NULL COMMENT '批次ID',
  `recipe_id` BIGINT DEFAULT NULL COMMENT '成品ID',
  `material_id` BIGINT DEFAULT NULL COMMENT '原料ID',
  `qty_before` DECIMAL(10,2) DEFAULT 0 COMMENT '变动前数量',
  `qty_change` DECIMAL(10,2) NOT NULL COMMENT '变动数量',
  `qty_after` DECIMAL(10,2) DEFAULT 0 COMMENT '变动后数量',
  `operator` VARCHAR(32) DEFAULT NULL COMMENT '操作人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_biz` (`biz_type`,`biz_no`),
  KEY `idx_store_time` (`store_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存操作日志表';
