-- =====================================================
-- 智慧仓储管理系统数据库初始化脚本
-- Database: MySQL 8.0+
-- Created: 2026
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------
-- 创建数据库
-- -----------------------------------------------------
CREATE DATABASE IF NOT EXISTS wms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE wms;

-- =====================================================
-- 一、基础数据表
-- =====================================================

-- -----------------------------------------------------
-- 1. 仓库表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wms_warehouse`;
CREATE TABLE `wms_warehouse` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `warehouse_code` VARCHAR(64) NOT NULL COMMENT '仓库编码',
  `warehouse_name` VARCHAR(128) NOT NULL COMMENT '仓库名称',
  `warehouse_type` TINYINT NOT NULL DEFAULT 1 COMMENT '仓库类型：1-普通仓 2-恒温仓 3-冷藏仓 4-危险品仓',
  `address` VARCHAR(256) COMMENT '仓库地址',
  `manager` VARCHAR(64) COMMENT '负责人',
  `phone` VARCHAR(32) COMMENT '联系电话',
  `area` DECIMAL(12,2) COMMENT '面积(平方米)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `remark` VARCHAR(512) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(64) COMMENT '创建人',
  `update_by` VARCHAR(64) COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_code` (`warehouse_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='仓库表';

-- -----------------------------------------------------
-- 2. 库区表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wms_area`;
CREATE TABLE `wms_area` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `area_code` VARCHAR(64) NOT NULL COMMENT '库区编码',
  `area_name` VARCHAR(128) NOT NULL COMMENT '库区名称',
  `area_type` TINYINT NOT NULL DEFAULT 1 COMMENT '库区类型：1-存储区 2-拣货区 3-质检区 4-退货区 5-待处理区',
  `location_count` INT DEFAULT 0 COMMENT '库位数量',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `remark` VARCHAR(512) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(64) COMMENT '创建人',
  `update_by` VARCHAR(64) COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_area_code` (`area_code`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库区表';

-- -----------------------------------------------------
-- 3. 库位表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wms_location`;
CREATE TABLE `wms_location` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `area_id` BIGINT NOT NULL COMMENT '库区ID',
  `location_code` VARCHAR(64) NOT NULL COMMENT '库位编码',
  `location_name` VARCHAR(128) COMMENT '库位名称',
  `location_type` TINYINT NOT NULL DEFAULT 1 COMMENT '库位类型：1-普通位 2-冷藏位 3-危险品位 4-大件位',
  `row_num` INT COMMENT '排号',
  `column_num` INT COMMENT '列号',
  `layer_num` INT COMMENT '层号',
  `max_capacity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '最大容量',
  `current_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '当前数量',
  `available_capacity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '可用容量',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-空闲 2-占用 3-锁定',
  `remark` VARCHAR(512) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(64) COMMENT '创建人',
  `update_by` VARCHAR(64) COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_location_code` (`location_code`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_area_id` (`area_id`),
  KEY `idx_status` (`status`),
  KEY `idx_row_column_layer` (`row_num`, `column_num`, `layer_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库位表';

-- -----------------------------------------------------
-- 4. 供应商表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wms_supplier`;
CREATE TABLE `wms_supplier` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `supplier_code` VARCHAR(64) NOT NULL COMMENT '供应商编码',
  `supplier_name` VARCHAR(128) NOT NULL COMMENT '供应商名称',
  `contact` VARCHAR(64) COMMENT '联系人',
  `phone` VARCHAR(32) COMMENT '联系电话',
  `email` VARCHAR(64) COMMENT '邮箱',
  `address` VARCHAR(256) COMMENT '地址',
  `credit_level` TINYINT DEFAULT 3 COMMENT '信用等级：1-优 2-良 3-中 4-差',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `remark` VARCHAR(512) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(64) COMMENT '创建人',
  `update_by` VARCHAR(64) COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_code` (`supplier_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商表';

-- -----------------------------------------------------
-- 5. 商品表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wms_product`;
CREATE TABLE `wms_product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_code` VARCHAR(64) NOT NULL COMMENT '商品编码',
  `product_name` VARCHAR(128) NOT NULL COMMENT '商品名称',
  `barcode` VARCHAR(64) COMMENT '条码',
  `specification` VARCHAR(128) COMMENT '规格',
  `unit` VARCHAR(32) NOT NULL COMMENT '单位',
  `category` VARCHAR(64) COMMENT '分类',
  `brand` VARCHAR(64) COMMENT '品牌',
  `weight` DECIMAL(10,2) COMMENT '重量(kg)',
  `volume` DECIMAL(10,2) COMMENT '体积(m³)',
  `price` DECIMAL(12,2) COMMENT '单价',
  `shelf_life` INT COMMENT '保质期(天)',
  `warning_days` INT DEFAULT 30 COMMENT '预警天数',
  `min_stock` DECIMAL(12,2) DEFAULT 0 COMMENT '最低库存',
  `max_stock` DECIMAL(12,2) DEFAULT 0 COMMENT '最高库存',
  `storage_condition` TINYINT DEFAULT 1 COMMENT '存储条件：1-常温 2-冷藏 3-冷冻 4-恒温',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `remark` VARCHAR(512) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(64) COMMENT '创建人',
  `update_by` VARCHAR(64) COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_code` (`product_code`),
  KEY `idx_barcode` (`barcode`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- =====================================================
-- 二、库存相关表
-- =====================================================

-- -----------------------------------------------------
-- 6. 批次库存表（同一商品多批次管理）
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wms_inventory_batch`;
CREATE TABLE `wms_inventory_batch` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `location_id` BIGINT NOT NULL COMMENT '库位ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `batch_no` VARCHAR(64) NOT NULL COMMENT '批次号',
  `supplier_id` BIGINT COMMENT '供应商ID',
  `quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '库存数量',
  `available_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '可用数量',
  `locked_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '锁定数量',
  `frozen_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '冻结数量',
  `unit` VARCHAR(32) NOT NULL COMMENT '单位',
  `produce_date` DATE COMMENT '生产日期',
  `expire_date` DATE COMMENT '过期日期',
  `inbound_date` DATE COMMENT '入库日期',
  `inventory_status` TINYINT NOT NULL DEFAULT 1 COMMENT '库存状态：1-正常 2-临期 3-过期 4-冻结',
  `inspection_status` TINYINT DEFAULT 0 COMMENT '质检状态：0-未质检 1-质检中 2-合格 3-不合格',
  `cost_price` DECIMAL(12,2) DEFAULT 0 COMMENT '成本价',
  `remark` VARCHAR(512) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(64) COMMENT '创建人',
  `update_by` VARCHAR(64) COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch_location` (`batch_no`, `location_id`, `product_id`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_location_id` (`location_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_batch_no` (`batch_no`),
  KEY `idx_expire_date` (`expire_date`),
  KEY `idx_inventory_status` (`inventory_status`),
  KEY `idx_supplier_id` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='批次库存表';

-- -----------------------------------------------------
-- 7. 库存流水表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wms_inventory_log`;
CREATE TABLE `wms_inventory_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `log_no` VARCHAR(64) NOT NULL COMMENT '流水单号',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `location_id` BIGINT NOT NULL COMMENT '库位ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `batch_no` VARCHAR(64) NOT NULL COMMENT '批次号',
  `business_type` TINYINT NOT NULL COMMENT '业务类型：1-入库 2-出库 3-调拨 4-盘点 5-冻结 6-解冻 7-退货 8-调整',
  `business_no` VARCHAR(64) COMMENT '业务单据号',
  `before_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '变更前数量',
  `change_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '变更数量',
  `after_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '变更后数量',
  `unit` VARCHAR(32) NOT NULL COMMENT '单位',
  `operation_type` TINYINT NOT NULL COMMENT '操作类型：1-增加 2-减少',
  `operator` VARCHAR(64) COMMENT '操作人',
  `operation_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `remark` VARCHAR(512) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_log_no` (`log_no`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_batch_no` (`batch_no`),
  KEY `idx_business_type` (`business_type`),
  KEY `idx_business_no` (`business_no`),
  KEY `idx_operation_time` (`operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存流水表';

-- -----------------------------------------------------
-- 8. 库存冻结表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wms_inventory_freeze`;
CREATE TABLE `wms_inventory_freeze` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `freeze_no` VARCHAR(64) NOT NULL COMMENT '冻结单号',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `location_id` BIGINT NOT NULL COMMENT '库位ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `batch_no` VARCHAR(64) NOT NULL COMMENT '批次号',
  `freeze_type` TINYINT NOT NULL COMMENT '冻结类型：1-盘点冻结 2-质检冻结 3-异常冻结 4-其他',
  `freeze_reason` VARCHAR(256) NOT NULL COMMENT '冻结原因',
  `freeze_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '冻结数量',
  `unit` VARCHAR(32) NOT NULL COMMENT '单位',
  `freeze_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '冻结时间',
  `freeze_operator` VARCHAR(64) COMMENT '冻结操作人',
  `unfreeze_time` DATETIME COMMENT '解冻时间',
  `unfreeze_operator` VARCHAR(64) COMMENT '解冻操作人',
  `unfreeze_reason` VARCHAR(256) COMMENT '解冻原因',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-已冻结 2-已解冻',
  `business_no` VARCHAR(64) COMMENT '关联业务单号',
  `remark` VARCHAR(512) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_freeze_no` (`freeze_no`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_batch_no` (`batch_no`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存冻结表';

-- -----------------------------------------------------
-- 9. 库存预警表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wms_inventory_alert`;
CREATE TABLE `wms_inventory_alert` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `alert_no` VARCHAR(64) NOT NULL COMMENT '预警单号',
  `alert_type` TINYINT NOT NULL COMMENT '预警类型：1-库存不足 2-库存过量 3-效期临期 4-效期过期 5-库位容量不足',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `location_id` BIGINT COMMENT '库位ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `batch_no` VARCHAR(64) COMMENT '批次号',
  `current_quantity` DECIMAL(12,2) DEFAULT 0 COMMENT '当前数量',
  `threshold_quantity` DECIMAL(12,2) DEFAULT 0 COMMENT '阈值数量',
  `current_date` DATE COMMENT '当前日期',
  `expire_date` DATE COMMENT '过期日期',
  `remaining_days` INT COMMENT '剩余天数',
  `alert_level` TINYINT NOT NULL DEFAULT 2 COMMENT '预警级别：1-低 2-中 3-高',
  `alert_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '预警时间',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-待处理 2-处理中 3-已处理 4-已忽略',
  `handler` VARCHAR(64) COMMENT '处理人',
  `handle_time` DATETIME COMMENT '处理时间',
  `handle_result` VARCHAR(512) COMMENT '处理结果',
  `remark` VARCHAR(512) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_alert_no` (`alert_no`),
  KEY `idx_alert_type` (`alert_type`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_status` (`status`),
  KEY `idx_alert_level` (`alert_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存预警表';

-- =====================================================
-- 三、入库相关表
-- =====================================================

-- -----------------------------------------------------
-- 10. 入库单表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wms_receipt_order`;
CREATE TABLE `wms_receipt_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `receipt_no` VARCHAR(64) NOT NULL COMMENT '入库单号',
  `receipt_type` TINYINT NOT NULL COMMENT '入库类型：1-采购入库 2-退货入库 3-调拨入库 4-盘盈入库',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `supplier_id` BIGINT COMMENT '供应商ID',
  `order_status` TINYINT NOT NULL DEFAULT 1 COMMENT '单据状态：1-待到货 2-已到货 3-质检中 4-质检完成 5-待入库 6-入库中 7-已完成 8-已取消',
  `total_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '总数量',
  `actual_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '实际数量',
  `qualified_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '合格数量',
  `unqualified_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '不合格数量',
  `arrival_time` DATETIME COMMENT '到货时间',
  `inspection_time` DATETIME COMMENT '质检时间',
  `complete_time` DATETIME COMMENT '完成时间',
  `source_order_no` VARCHAR(64) COMMENT '来源单号',
  `remark` VARCHAR(512) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(64) COMMENT '创建人',
  `update_by` VARCHAR(64) COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_receipt_no` (`receipt_no`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入库单表';

-- -----------------------------------------------------
-- 11. 入库单明细表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wms_receipt_order_detail`;
CREATE TABLE `wms_receipt_order_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `receipt_order_id` BIGINT NOT NULL COMMENT '入库单ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `batch_no` VARCHAR(64) NOT NULL COMMENT '批次号',
  `plan_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '计划数量',
  `arrival_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '到货数量',
  `qualified_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '合格数量',
  `unqualified_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '不合格数量',
  `actual_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '实际入库数量',
  `unit` VARCHAR(32) NOT NULL COMMENT '单位',
  `produce_date` DATE COMMENT '生产日期',
  `expire_date` DATE COMMENT '过期日期',
  `inspection_result` TINYINT COMMENT '质检结果：1-合格 2-不合格',
  `inspection_remark` VARCHAR(256) COMMENT '质检备注',
  `location_id` BIGINT COMMENT '分配库位ID',
  `cost_price` DECIMAL(12,2) DEFAULT 0 COMMENT '成本价',
  `remark` VARCHAR(512) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_receipt_order_id` (`receipt_order_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_batch_no` (`batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入库单明细表';

-- -----------------------------------------------------
-- 12. 入库质检表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wms_inspection`;
CREATE TABLE `wms_inspection` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `inspection_no` VARCHAR(64) NOT NULL COMMENT '质检单号',
  `receipt_order_id` BIGINT NOT NULL COMMENT '入库单ID',
  `receipt_detail_id` BIGINT NOT NULL COMMENT '入库单明细ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `batch_no` VARCHAR(64) NOT NULL COMMENT '批次号',
  `inspection_type` TINYINT NOT NULL DEFAULT 1 COMMENT '质检类型：1-全检 2-抽检',
  `sample_quantity` DECIMAL(12,2) DEFAULT 0 COMMENT '抽检数量',
  `checked_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '已检数量',
  `qualified_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '合格数量',
  `unqualified_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '不合格数量',
  `unqualified_reason` VARCHAR(256) COMMENT '不合格原因',
  `inspection_result` TINYINT COMMENT '质检结果：1-合格 2-不合格',
  `inspector` VARCHAR(64) COMMENT '质检员',
  `inspection_time` DATETIME COMMENT '质检时间',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-待质检 2-质检中 3-已完成 4-已取消',
  `remark` VARCHAR(512) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inspection_no` (`inspection_no`),
  KEY `idx_receipt_order_id` (`receipt_order_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_batch_no` (`batch_no`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入库质检表';

-- =====================================================
-- 四、出库相关表
-- =====================================================

-- -----------------------------------------------------
-- 13. 出库单表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wms_shipment_order`;
CREATE TABLE `wms_shipment_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shipment_no` VARCHAR(64) NOT NULL COMMENT '出库单号',
  `shipment_type` TINYINT NOT NULL COMMENT '出库类型：1-销售出库 2-调拨出库 3-退货出库 4-盘亏出库 5-报废出库',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `customer_name` VARCHAR(128) COMMENT '客户名称',
  `order_status` TINYINT NOT NULL DEFAULT 1 COMMENT '单据状态：1-待确认 2-已确认 3-拣货中 4-拣货完成 5-复核中 6-复核完成 7-已出库 8-已取消',
  `total_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '总数量',
  `picked_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '已拣数量',
  `reviewed_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '已复核数量',
  `actual_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '实际出库数量',
  `allocate_time` DATETIME COMMENT '分配时间',
  `picking_time` DATETIME COMMENT '拣货时间',
  `review_time` DATETIME COMMENT '复核时间',
  `shipment_time` DATETIME COMMENT '出库时间',
  `cancel_time` DATETIME COMMENT '取消时间',
  `cancel_reason` VARCHAR(256) COMMENT '取消原因',
  `source_order_no` VARCHAR(64) COMMENT '来源单号',
  `remark` VARCHAR(512) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(64) COMMENT '创建人',
  `update_by` VARCHAR(64) COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shipment_no` (`shipment_no`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出库单表';

-- -----------------------------------------------------
-- 14. 出库单明细表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wms_shipment_order_detail`;
CREATE TABLE `wms_shipment_order_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shipment_order_id` BIGINT NOT NULL COMMENT '出库单ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `plan_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '计划数量',
  `allocated_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '已分配数量',
  `picked_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '已拣数量',
  `reviewed_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '已复核数量',
  `actual_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '实际出库数量',
  `unit` VARCHAR(32) NOT NULL COMMENT '单位',
  `outbound_strategy` TINYINT DEFAULT 1 COMMENT '出库策略：1-先进先出(FIFO) 2-效期优先(FEFO) 3-指定批次',
  `price` DECIMAL(12,2) DEFAULT 0 COMMENT '单价',
  `remark` VARCHAR(512) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_shipment_order_id` (`shipment_order_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出库单明细表';

-- -----------------------------------------------------
-- 15. 出库分配明细表（记录从哪个批次哪个库位出多少）
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wms_shipment_allocate_detail`;
CREATE TABLE `wms_shipment_allocate_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shipment_order_id` BIGINT NOT NULL COMMENT '出库单ID',
  `shipment_detail_id` BIGINT NOT NULL COMMENT '出库单明细ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `batch_no` VARCHAR(64) NOT NULL COMMENT '批次号',
  `location_id` BIGINT NOT NULL COMMENT '库位ID',
  `allocate_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '分配数量',
  `picked_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '已拣数量',
  `reviewed_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '已复核数量',
  `unit` VARCHAR(32) NOT NULL COMMENT '单位',
  `produce_date` DATE COMMENT '生产日期',
  `expire_date` DATE COMMENT '过期日期',
  `cost_price` DECIMAL(12,2) DEFAULT 0 COMMENT '成本价',
  `is_picked` TINYINT DEFAULT 0 COMMENT '是否已拣货：0-否 1-是',
  `is_reviewed` TINYINT DEFAULT 0 COMMENT '是否已复核：0-否 1-是',
  `remark` VARCHAR(512) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_shipment_order_id` (`shipment_order_id`),
  KEY `idx_shipment_detail_id` (`shipment_detail_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_batch_no` (`batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出库分配明细表';

-- -----------------------------------------------------
-- 16. 拣货任务表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wms_picking_task`;
CREATE TABLE `wms_picking_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_no` VARCHAR(64) NOT NULL COMMENT '拣货任务号',
  `shipment_order_id` BIGINT NOT NULL COMMENT '出库单ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `picker` VARCHAR(64) COMMENT '拣货员',
  `task_type` TINYINT NOT NULL DEFAULT 1 COMMENT '任务类型：1-订单拣货 2-补货拣货 3-调拨拣货',
  `picking_mode` TINYINT NOT NULL DEFAULT 1 COMMENT '拣货模式：1-按单拣货 2-批量拣货 3-波次拣货',
  `total_items` INT NOT NULL DEFAULT 0 COMMENT '商品行数',
  `total_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '总数量',
  `picked_items` INT NOT NULL DEFAULT 0 COMMENT '已拣行数',
  `picked_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '已拣数量',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-待拣货 2-拣货中 3-已完成 4-已取消',
  `priority` TINYINT DEFAULT 3 COMMENT '优先级：1-最高 2-高 3-中 4-低',
  `assign_time` DATETIME COMMENT '分配时间',
  `start_time` DATETIME COMMENT '开始时间',
  `finish_time` DATETIME COMMENT '完成时间',
  `remark` VARCHAR(512) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_no` (`task_no`),
  KEY `idx_shipment_order_id` (`shipment_order_id`),
  KEY `idx_picker` (`picker`),
  KEY `idx_status` (`status`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拣货任务表';

-- -----------------------------------------------------
-- 17. 拣货任务明细表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wms_picking_task_detail`;
CREATE TABLE `wms_picking_task_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `picking_task_id` BIGINT NOT NULL COMMENT '拣货任务ID',
  `shipment_allocate_id` BIGINT NOT NULL COMMENT '出库分配明细ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `batch_no` VARCHAR(64) NOT NULL COMMENT '批次号',
  `location_id` BIGINT NOT NULL COMMENT '库位ID',
  `location_code` VARCHAR(64) COMMENT '库位编码',
  `plan_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '计划数量',
  `picked_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '已拣数量',
  `unit` VARCHAR(32) NOT NULL COMMENT '单位',
  `is_picked` TINYINT DEFAULT 0 COMMENT '是否已拣：0-否 1-是',
  `pick_time` DATETIME COMMENT '拣货时间',
  `pick_operator` VARCHAR(64) COMMENT '拣货人',
  `remark` VARCHAR(512) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_picking_task_id` (`picking_task_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_location_id` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拣货任务明细表';

-- =====================================================
-- 五、盘点相关表
-- =====================================================

-- -----------------------------------------------------
-- 18. 盘点单表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wms_stocktake_order`;
CREATE TABLE `wms_stocktake_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `stocktake_no` VARCHAR(64) NOT NULL COMMENT '盘点单号',
  `stocktake_type` TINYINT NOT NULL COMMENT '盘点类型：1-全盘 2-抽盘 3-循环盘点',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `area_id` BIGINT COMMENT '库区ID',
  `stocktake_method` TINYINT NOT NULL DEFAULT 1 COMMENT '盘点方式：1-人工盘点 2-扫码盘点 3-PDA盘点',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-新建 2-已确认 3-盘点中 4-差异处理中 5-已完成 6-已取消',
  `total_items` INT DEFAULT 0 COMMENT '盘点行数',
  `total_quantity` DECIMAL(12,2) DEFAULT 0 COMMENT '系统总数量',
  `count_quantity` DECIMAL(12,2) DEFAULT 0 COMMENT '实盘总数量',
  `profit_quantity` DECIMAL(12,2) DEFAULT 0 COMMENT '盘盈总数量',
  `loss_quantity` DECIMAL(12,2) DEFAULT 0 COMMENT '盘亏总数量',
  `confirm_time` DATETIME COMMENT '确认时间',
  `start_time` DATETIME COMMENT '开始时间',
  `finish_time` DATETIME COMMENT '完成时间',
  `handler` VARCHAR(64) COMMENT '盘点人',
  `remark` VARCHAR(512) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(64) COMMENT '创建人',
  `update_by` VARCHAR(64) COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stocktake_no` (`stocktake_no`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='盘点单表';

-- -----------------------------------------------------
-- 19. 盘点单明细表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wms_stocktake_order_detail`;
CREATE TABLE `wms_stocktake_order_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `stocktake_order_id` BIGINT NOT NULL COMMENT '盘点单ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `batch_no` VARCHAR(64) NOT NULL COMMENT '批次号',
  `location_id` BIGINT NOT NULL COMMENT '库位ID',
  `system_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '系统数量',
  `first_count` DECIMAL(12,2) DEFAULT 0 COMMENT '初盘数量',
  `second_count` DECIMAL(12,2) DEFAULT 0 COMMENT '复盘数量',
  `final_count` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '最终数量',
  `diff_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '差异数量',
  `diff_type` TINYINT COMMENT '差异类型：1-盘盈 2-盘亏 3-无差异',
  `unit` VARCHAR(32) NOT NULL COMMENT '单位',
  `is_counted` TINYINT DEFAULT 0 COMMENT '是否已盘点：0-否 1-是',
  `count_time` DATETIME COMMENT '盘点时间',
  `counter` VARCHAR(64) COMMENT '盘点人',
  `diff_reason` VARCHAR(256) COMMENT '差异原因',
  `process_status` TINYINT DEFAULT 0 COMMENT '处理状态：0-未处理 1-处理中 2-已处理',
  `process_result` VARCHAR(256) COMMENT '处理结果',
  `process_time` DATETIME COMMENT '处理时间',
  `processor` VARCHAR(64) COMMENT '处理人',
  `remark` VARCHAR(512) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_stocktake_order_id` (`stocktake_order_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_batch_no` (`batch_no`),
  KEY `idx_diff_type` (`diff_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='盘点单明细表';

-- =====================================================
-- 六、退货相关表
-- =====================================================

-- -----------------------------------------------------
-- 20. 退货入库单表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wms_return_order`;
CREATE TABLE `wms_return_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `return_no` VARCHAR(64) NOT NULL COMMENT '退货单号',
  `return_type` TINYINT NOT NULL COMMENT '退货类型：1-销售退货 2-调拨退货 3-质量退货',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `customer_name` VARCHAR(128) COMMENT '客户名称',
  `original_shipment_no` VARCHAR(64) COMMENT '原出库单号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-待入库 2-质检中 3-待上架 4-已完成 5-已取消',
  `total_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '退货总数量',
  `actual_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '实际入库数量',
  `return_reason` VARCHAR(256) COMMENT '退货原因',
  `receive_time` DATETIME COMMENT '收货时间',
  `complete_time` DATETIME COMMENT '完成时间',
  `remark` VARCHAR(512) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(64) COMMENT '创建人',
  `update_by` VARCHAR(64) COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_return_no` (`return_no`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_status` (`status`),
  KEY `idx_original_shipment_no` (`original_shipment_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退货入库单表';

-- -----------------------------------------------------
-- 21. 退货入库单明细表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wms_return_order_detail`;
CREATE TABLE `wms_return_order_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `return_order_id` BIGINT NOT NULL COMMENT '退货单ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `batch_no` VARCHAR(64) NOT NULL COMMENT '退回批次号',
  `original_batch_no` VARCHAR(64) COMMENT '原出库批次号',
  `return_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '退货数量',
  `actual_quantity` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '实际入库数量',
  `unit` VARCHAR(32) NOT NULL COMMENT '单位',
  `inspection_result` TINYINT COMMENT '质检结果：1-合格 2-不合格 3-待检',
  `location_id` BIGINT COMMENT '入库库位ID',
  `remark` VARCHAR(512) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_return_order_id` (`return_order_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退货入库单明细表';

-- =====================================================
-- 七、系统相关表
-- =====================================================

-- -----------------------------------------------------
-- 22. 用户表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wms_user`;
CREATE TABLE `wms_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(64) NOT NULL COMMENT '用户名',
  `password` VARCHAR(128) NOT NULL COMMENT '密码',
  `real_name` VARCHAR(64) COMMENT '真实姓名',
  `phone` VARCHAR(32) COMMENT '手机号',
  `email` VARCHAR(64) COMMENT '邮箱',
  `avatar` VARCHAR(256) COMMENT '头像',
  `department` VARCHAR(64) COMMENT '部门',
  `position` VARCHAR(64) COMMENT '职位',
  `warehouse_id` BIGINT COMMENT '所属仓库ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `last_login_time` DATETIME COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(64) COMMENT '最后登录IP',
  `remark` VARCHAR(512) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- -----------------------------------------------------
-- 23. 角色表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wms_role`;
CREATE TABLE `wms_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_code` VARCHAR(64) NOT NULL COMMENT '角色编码',
  `role_name` VARCHAR(64) NOT NULL COMMENT '角色名称',
  `description` VARCHAR(256) COMMENT '角色描述',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `remark` VARCHAR(512) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- -----------------------------------------------------
-- 24. 权限表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wms_permission`;
CREATE TABLE `wms_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `permission_code` VARCHAR(128) NOT NULL COMMENT '权限编码',
  `permission_name` VARCHAR(128) NOT NULL COMMENT '权限名称',
  `permission_type` TINYINT NOT NULL COMMENT '权限类型：1-菜单 2-按钮 3-接口',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父级ID',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `path` VARCHAR(256) COMMENT '路由路径',
  `component` VARCHAR(256) COMMENT '组件路径',
  `icon` VARCHAR(64) COMMENT '图标',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `remark` VARCHAR(512) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- -----------------------------------------------------
-- 25. 用户角色关联表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wms_user_role`;
CREATE TABLE `wms_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- -----------------------------------------------------
-- 26. 角色权限关联表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wms_role_permission`;
CREATE TABLE `wms_role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- =====================================================
-- 八、初始化测试数据
-- =====================================================

-- -----------------------------------------------------
-- 初始化用户数据（密码：123456，MD5加密后）
-- -----------------------------------------------------
INSERT INTO `wms_user` (`username`, `password`, `real_name`, `phone`, `email`, `status`) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', '13800138000', 'admin@wms.com', 1),
('warehouse', 'e10adc3949ba59abbe56e057f20f883e', '仓库管理员', '13800138001', 'warehouse@wms.com', 1),
('inspector', 'e10adc3949ba59abbe56e057f20f883e', '质检员', '13800138002', 'inspector@wms.com', 1),
('picker', 'e10adc3949ba59abbe56e057f20f883e', '拣货员', '13800138003', 'picker@wms.com', 1);

-- -----------------------------------------------------
-- 初始化角色数据
-- -----------------------------------------------------
INSERT INTO `wms_role` (`role_code`, `role_name`, `description`, `status`) VALUES
('ADMIN', '系统管理员', '拥有系统所有权限', 1),
('WAREHOUSE_MANAGER', '仓库管理员', '负责仓库日常管理', 1),
('INSPECTOR', '质检员', '负责商品质检', 1),
('PICKER', '拣货员', '负责拣货作业', 1);

-- -----------------------------------------------------
-- 初始化用户角色关联
-- -----------------------------------------------------
INSERT INTO `wms_user_role` (`user_id`, `role_id`) VALUES
(1, 1), (2, 2), (3, 3), (4, 4);

-- -----------------------------------------------------
-- 初始化仓库数据
-- -----------------------------------------------------
INSERT INTO `wms_warehouse` (`warehouse_code`, `warehouse_name`, `warehouse_type`, `address`, `manager`, `phone`, `area`, `status`) VALUES
('WH001', '华南中心仓', 1, '广东省广州市天河区', '张三', '13800138010', 5000.00, 1),
('WH002', '生鲜冷链仓', 3, '广东省广州市番禺区', '李四', '13800138011', 2000.00, 1);

-- -----------------------------------------------------
-- 初始化库区数据
-- -----------------------------------------------------
INSERT INTO `wms_area` (`warehouse_id`, `area_code`, `area_name`, `area_type`, `location_count`, `status`) VALUES
(1, 'A01', 'A区-存储区', 1, 100, 1),
(1, 'A02', 'A区-拣货区', 2, 50, 1),
(1, 'A03', 'A区-质检区', 3, 20, 1),
(1, 'A04', 'A区-退货区', 4, 20, 1),
(1, 'A05', 'A区-待处理区', 5, 30, 1);

-- -----------------------------------------------------
-- 初始化库位数据（示例数据）
-- -----------------------------------------------------
INSERT INTO `wms_location` (`warehouse_id`, `area_id`, `location_code`, `location_type`, `row_num`, `column_num`, `layer_num`, `max_capacity`, `current_quantity`, `available_capacity`, `status`) VALUES
(1, 1, 'A01-01-01-01', 1, 1, 1, 1, 1000.00, 0, 1000.00, 1),
(1, 1, 'A01-01-01-02', 1, 1, 1, 2, 1000.00, 0, 1000.00, 1),
(1, 1, 'A01-01-02-01', 1, 1, 2, 1, 1000.00, 0, 1000.00, 1),
(1, 1, 'A01-01-02-02', 1, 1, 2, 2, 1000.00, 0, 1000.00, 1),
(1, 1, 'A01-02-01-01', 1, 2, 1, 1, 1000.00, 0, 1000.00, 1),
(1, 1, 'A01-02-01-02', 1, 2, 1, 2, 1000.00, 0, 1000.00, 1),
(1, 1, 'A01-02-02-01', 1, 2, 2, 1, 1000.00, 0, 1000.00, 1),
(1, 1, 'A01-02-02-02', 1, 2, 2, 2, 1000.00, 0, 1000.00, 1),
(1, 2, 'A02-01-01-01', 1, 1, 1, 1, 500.00, 0, 500.00, 1),
(1, 2, 'A02-01-01-02', 1, 1, 1, 2, 500.00, 0, 500.00, 1);

-- -----------------------------------------------------
-- 初始化供应商数据
-- -----------------------------------------------------
INSERT INTO `wms_supplier` (`supplier_code`, `supplier_name`, `contact`, `phone`, `email`, `address`, `credit_level`, `status`) VALUES
('SUP001', '深圳市电子科技有限公司', '王经理', '13900139001', 'wang@supplier.com', '深圳市南山区科技园', 1, 1),
('SUP002', '广州市日用品有限公司', '李经理', '13900139002', 'li@supplier.com', '广州市白云区', 2, 1),
('SUP003', '东莞市食品有限公司', '张经理', '13900139003', 'zhang@supplier.com', '东莞市虎门镇', 1, 1);

-- -----------------------------------------------------
-- 初始化商品数据
-- -----------------------------------------------------
INSERT INTO `wms_product` (`product_code`, `product_name`, `barcode`, `specification`, `unit`, `category`, `brand`, `weight`, `volume`, `price`, `shelf_life`, `warning_days`, `min_stock`, `max_stock`, `storage_condition`, `status`) VALUES
('PROD001', '无线蓝牙耳机', '6901234567891', '黑色/入耳式', '台', '电子产品', '小米', 0.05, 0.001, 199.00, NULL, 30, 100, 1000, 1, 1),
('PROD002', '智能手机充电器', '6901234567892', '20W快充', '个', '电子产品', '华为', 0.10, 0.0005, 89.00, NULL, 30, 200, 2000, 1, 1),
('PROD003', '牛奶250ml', '6901234567893', '250ml/盒', '盒', '食品饮料', '蒙牛', 0.27, 0.00025, 3.50, 180, 30, 500, 5000, 1, 1),
('PROD004', '面包', '6901234567894', '500g/袋', '袋', '食品饮料', '桃李', 0.50, 0.001, 12.00, 7, 3, 100, 500, 1, 1),
('PROD005', '矿泉水550ml', '6901234567895', '550ml/瓶', '瓶', '食品饮料', '农夫山泉', 0.57, 0.00055, 2.00, 365, 60, 1000, 10000, 1, 1),
('PROD006', '洗衣液2kg', '6901234567896', '2kg/瓶', '瓶', '日用品', '蓝月亮', 2.10, 0.002, 35.00, 730, 60, 200, 2000, 1, 1);

-- -----------------------------------------------------
-- 初始化库存批次数据（测试数据）
-- -----------------------------------------------------
INSERT INTO `wms_inventory_batch` (`warehouse_id`, `location_id`, `product_id`, `batch_no`, `supplier_id`, `quantity`, `available_quantity`, `locked_quantity`, `frozen_quantity`, `unit`, `produce_date`, `expire_date`, `inbound_date`, `inventory_status`, `inspection_status`, `cost_price`) VALUES
(1, 1, 1, 'B20260101001', 1, 500.00, 500.00, 0, 0, '台', '2026-01-01', NULL, '2026-01-15', 1, 2, 120.00),
(1, 2, 1, 'B20260201001', 1, 300.00, 300.00, 0, 0, '台', '2026-02-01', NULL, '2026-02-10', 1, 2, 125.00),
(1, 3, 3, 'B20260501001', 3, 2000.00, 2000.00, 0, 0, '盒', '2026-05-01', '2026-10-28', '2026-05-10', 1, 2, 2.00),
(1, 4, 4, 'B20260601001', 3, 300.00, 300.00, 0, 0, '袋', '2026-06-01', '2026-06-08', '2026-06-02', 2, 2, 8.00),
(1, 5, 5, 'B20260301001', 3, 5000.00, 5000.00, 0, 0, '瓶', '2026-03-01', '2027-03-01', '2026-03-15', 1, 2, 1.00),
(1, 6, 6, 'B20260401001', 2, 1000.00, 1000.00, 0, 0, '瓶', '2026-04-01', '2028-04-01', '2026-04-10', 1, 2, 20.00);

-- -----------------------------------------------------
-- 初始化库存流水数据
-- -----------------------------------------------------
INSERT INTO `wms_inventory_log` (`log_no`, `warehouse_id`, `location_id`, `product_id`, `batch_no`, `business_type`, `business_no`, `before_quantity`, `change_quantity`, `after_quantity`, `unit`, `operation_type`, `operator`) VALUES
('LOG20260601001', 1, 1, 1, 'B20260101001', 1, 'REC202601001', 0, 500.00, 500.00, '台', 1, 'admin'),
('LOG20260601002', 1, 2, 1, 'B20260201001', 1, 'REC202602001', 0, 300.00, 300.00, '台', 1, 'admin'),
('LOG20260601003', 1, 3, 3, 'B20260501001', 1, 'REC202605001', 0, 2000.00, 2000.00, '盒', 1, 'admin'),
('LOG20260601004', 1, 4, 4, 'B20260601001', 1, 'REC202606001', 0, 300.00, 300.00, '袋', 1, 'admin'),
('LOG20260601005', 1, 5, 5, 'B20260301001', 1, 'REC202603001', 0, 5000.00, 5000.00, '瓶', 1, 'admin'),
('LOG20260601006', 1, 6, 6, 'B20260401001', 1, 'REC202604001', 0, 1000.00, 1000.00, '瓶', 1, 'admin');

-- -----------------------------------------------------
-- 初始化库存预警数据（临期商品预警）
-- -----------------------------------------------------
INSERT INTO `wms_inventory_alert` (`alert_no`, `alert_type`, `warehouse_id`, `location_id`, `product_id`, `batch_no`, `current_quantity`, `threshold_quantity`, `current_date`, `expire_date`, `remaining_days`, `alert_level`, `status`) VALUES
('ALT20260601001', 3, 1, 4, 4, 'B20260601001', 300.00, 0, '2026-06-03', '2026-06-08', 5, 3, 1),
('ALT20260601002', 1, 1, 1, 1, 'B20260101001', 500.00, 100.00, NULL, NULL, NULL, 2, 1);

SET FOREIGN_KEY_CHECKS = 1;
