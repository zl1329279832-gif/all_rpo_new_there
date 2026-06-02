CREATE DATABASE IF NOT EXISTS medical_device DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE medical_device;

CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    dept_id BIGINT COMMENT '科室ID',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除'
) COMMENT '用户表';

CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(200) COMMENT '角色描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除'
) COMMENT '角色表';

CREATE TABLE sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT '用户角色关联表';

CREATE TABLE sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '权限ID',
    permission_name VARCHAR(50) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    type TINYINT COMMENT '类型: 1-菜单, 2-按钮',
    parent_id BIGINT DEFAULT 0 COMMENT '父级ID',
    path VARCHAR(200) COMMENT '路由路径',
    component VARCHAR(200) COMMENT '组件路径',
    icon VARCHAR(50) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除'
) COMMENT '权限表';

CREATE TABLE sys_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT '角色权限关联表';

CREATE TABLE department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '科室ID',
    dept_name VARCHAR(100) NOT NULL COMMENT '科室名称',
    dept_code VARCHAR(50) UNIQUE COMMENT '科室编码',
    parent_id BIGINT DEFAULT 0 COMMENT '父级科室ID',
    leader VARCHAR(50) COMMENT '负责人',
    phone VARCHAR(20) COMMENT '联系电话',
    address VARCHAR(200) COMMENT '科室地址',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除'
) COMMENT '科室表';

CREATE TABLE device (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '设备ID',
    device_name VARCHAR(200) NOT NULL COMMENT '设备名称',
    device_code VARCHAR(50) UNIQUE COMMENT '设备编号',
    device_type VARCHAR(100) COMMENT '设备类型',
    device_model VARCHAR(100) COMMENT '设备型号',
    manufacturer VARCHAR(200) COMMENT '生产厂家',
    serial_number VARCHAR(100) UNIQUE COMMENT '序列号',
    purchase_date DATE COMMENT '采购日期',
    warranty_start DATE COMMENT '保修开始日期',
    warranty_end DATE COMMENT '保修结束日期',
    dept_id BIGINT COMMENT '所属科室ID',
    location VARCHAR(200) COMMENT '存放位置',
    status TINYINT DEFAULT 1 COMMENT '设备状态: 1-正常使用, 2-维修中, 3-停机, 4-报废, 5-校准中, 6-质控中',
    risk_level TINYINT DEFAULT 2 COMMENT '风险等级: 1-高风险, 2-中风险, 3-低风险',
    last_maintenance_date DATE COMMENT '最后维护日期',
    next_maintenance_date DATE COMMENT '下次维护日期',
    total_downtime INT DEFAULT 0 COMMENT '累计停机时间(小时)',
    maintainer VARCHAR(50) COMMENT '维护人员',
    description TEXT COMMENT '设备描述',
    qc_status TINYINT DEFAULT 1 COMMENT '质控状态: 1-合格, 2-不合格',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_dept_id (dept_id),
    INDEX idx_status (status),
    INDEX idx_risk_level (risk_level)
) COMMENT '设备档案表';

CREATE TABLE inspection_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '计划ID',
    plan_name VARCHAR(200) NOT NULL COMMENT '计划名称',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    cycle_type TINYINT NOT NULL COMMENT '周期类型: 1-每日, 2-每周, 3-每月, 4-每季度, 5-每年',
    cycle_days INT COMMENT '周期天数',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE COMMENT '结束日期',
    next_execution_date DATE COMMENT '下次执行日期',
    inspector VARCHAR(50) COMMENT '巡检人员',
    check_items TEXT COMMENT '检查项目(JSON格式)',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    description TEXT COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_device_id (device_id),
    INDEX idx_next_execution (next_execution_date)
) COMMENT '巡检计划表';

CREATE TABLE inspection_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    task_code VARCHAR(50) UNIQUE COMMENT '任务编号',
    plan_id BIGINT COMMENT '关联计划ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
    task_type TINYINT DEFAULT 1 COMMENT '任务类型: 1-定期巡检, 2-临时巡检',
    plan_date DATE NOT NULL COMMENT '计划巡检日期',
    actual_date DATETIME COMMENT '实际巡检日期',
    inspector_id BIGINT COMMENT '巡检人员ID',
    inspector_name VARCHAR(50) COMMENT '巡检人员姓名',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-待执行, 2-执行中, 3-已完成, 4-已逾期, 5-已取消',
    check_result TINYINT COMMENT '检查结果: 1-正常, 2-异常',
    abnormal_description TEXT COMMENT '异常描述',
    handle_suggestion TEXT COMMENT '处理建议',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_device_id (device_id),
    INDEX idx_plan_id (plan_id),
    INDEX idx_status (status),
    INDEX idx_plan_date (plan_date)
) COMMENT '巡检任务表';

CREATE TABLE repair_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '工单ID',
    order_code VARCHAR(50) UNIQUE COMMENT '工单编号',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    fault_type VARCHAR(100) COMMENT '故障类型',
    fault_description TEXT NOT NULL COMMENT '故障描述',
    fault_level TINYINT DEFAULT 2 COMMENT '故障等级: 1-紧急, 2-一般, 3-轻微',
    reporter_id BIGINT COMMENT '报修人ID',
    reporter_name VARCHAR(50) COMMENT '报修人姓名',
    report_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报修时间',
    repairer_id BIGINT COMMENT '维修人员ID',
    repairer_name VARCHAR(50) COMMENT '维修人员姓名',
    assign_time DATETIME COMMENT '派单时间',
    start_time DATETIME COMMENT '开始维修时间',
    complete_time DATETIME COMMENT '完成时间',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-待派单, 2-待维修, 3-维修中, 4-待验收, 5-已完成, 6-已取消',
    repair_content TEXT COMMENT '维修内容',
    repair_result TINYINT COMMENT '维修结果: 1-已修复, 2-需更换配件, 3-无法修复',
    downtime INT DEFAULT 0 COMMENT '停机时长(小时)',
    repair_cost DECIMAL(10,2) DEFAULT 0 COMMENT '维修费用',
    remark TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_device_id (device_id),
    INDEX idx_status (status),
    INDEX idx_report_time (report_time)
) COMMENT '维修工单表';

CREATE TABLE spare_part (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配件ID',
    part_name VARCHAR(200) NOT NULL COMMENT '配件名称',
    part_code VARCHAR(50) UNIQUE COMMENT '配件编号',
    part_model VARCHAR(100) COMMENT '配件型号',
    manufacturer VARCHAR(200) COMMENT '生产厂家',
    unit VARCHAR(20) COMMENT '单位',
    price DECIMAL(10,2) COMMENT '单价',
    stock_quantity INT DEFAULT 0 COMMENT '库存数量',
    min_stock INT DEFAULT 10 COMMENT '最低库存',
    description TEXT COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除'
) COMMENT '配件表';

CREATE TABLE part_replacement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '更换记录ID',
    repair_order_id BIGINT NOT NULL COMMENT '维修工单ID',
    spare_part_id BIGINT NOT NULL COMMENT '配件ID',
    part_name VARCHAR(200) COMMENT '配件名称',
    part_model VARCHAR(100) COMMENT '配件型号',
    quantity INT NOT NULL COMMENT '更换数量',
    unit_price DECIMAL(10,2) COMMENT '单价',
    total_price DECIMAL(10,2) COMMENT '总价',
    replace_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更换时间',
    operator VARCHAR(50) COMMENT '操作人员',
    remark TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_repair_order_id (repair_order_id),
    INDEX idx_spare_part_id (spare_part_id)
) COMMENT '配件更换记录表';

CREATE TABLE calibration_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '校准记录ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    calibration_type VARCHAR(100) COMMENT '校准类型',
    calibration_date DATE NOT NULL COMMENT '校准日期',
    calibration_agency VARCHAR(200) COMMENT '校准机构',
    calibration_person VARCHAR(50) COMMENT '校准人员',
    certificate_number VARCHAR(100) COMMENT '校准证书编号',
    valid_until DATE COMMENT '有效期至',
    calibration_result TINYINT COMMENT '校准结果: 1-合格, 2-不合格, 3-待确认',
    calibration_items TEXT COMMENT '校准项目(JSON)',
    deviation_value DECIMAL(10,4) COMMENT '偏差值',
    description TEXT COMMENT '校准说明',
    next_calibration_date DATE COMMENT '下次校准日期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_device_id (device_id),
    INDEX idx_calibration_date (calibration_date)
) COMMENT '校准记录表';

CREATE TABLE downtime_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    repair_order_id BIGINT COMMENT '关联工单ID',
    downtime_type TINYINT NOT NULL COMMENT '停机类型: 1-故障停机, 2-维护停机, 3-校准停机, 4-其他',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration INT COMMENT '持续时间(小时)',
    reason TEXT COMMENT '停机原因',
    description TEXT COMMENT '详细描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_device_id (device_id),
    INDEX idx_start_time (start_time)
) COMMENT '停机记录表';

CREATE TABLE maintenance_contract (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '合同ID',
    contract_code VARCHAR(50) UNIQUE COMMENT '合同编号',
    contract_name VARCHAR(200) NOT NULL COMMENT '合同名称',
    contract_type TINYINT COMMENT '合同类型: 1-维保合同, 2-维修合同, 3-校准合同',
    device_id BIGINT COMMENT '设备ID',
    supplier VARCHAR(200) COMMENT '供应商',
    contact_person VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    contract_amount DECIMAL(12,2) COMMENT '合同金额',
    payment_status TINYINT DEFAULT 0 COMMENT '付款状态: 0-未付款, 1-部分付款, 2-已付清',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-有效, 2-即将到期, 3-已过期, 0-作废',
    content TEXT COMMENT '合同内容',
    remark TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_device_id (device_id),
    INDEX idx_end_date (end_date),
    INDEX idx_status (status)
) COMMENT '维保合同表';

CREATE TABLE qc_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '质控计划ID',
    plan_name VARCHAR(200) NOT NULL COMMENT '计划名称',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    qc_type VARCHAR(100) COMMENT '质控类型',
    cycle_type TINYINT NOT NULL COMMENT '周期类型: 1-每日, 2-每周, 3-每月, 4-每季度, 5-每年',
    start_date DATE NOT NULL COMMENT '开始日期',
    next_execution_date DATE COMMENT '下次执行日期',
    qc_standard TEXT COMMENT '质控标准',
    qc_items TEXT COMMENT '质控项目(JSON)',
    executor VARCHAR(50) COMMENT '执行人',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_device_id (device_id),
    INDEX idx_next_execution (next_execution_date)
) COMMENT '质控计划表';

CREATE TABLE qc_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '质控记录ID',
    plan_id BIGINT COMMENT '关联计划ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    qc_date DATE NOT NULL COMMENT '质控日期',
    qc_type VARCHAR(100) COMMENT '质控类型',
    executor_id BIGINT COMMENT '执行人ID',
    executor_name VARCHAR(50) COMMENT '执行人姓名',
    qc_result TINYINT COMMENT '质控结果: 1-合格, 2-不合格, 3-待复检',
    qc_data TEXT COMMENT '质控数据(JSON)',
    deviation_description TEXT COMMENT '偏差描述',
    handle_measure TEXT COMMENT '处理措施',
    recheck_date DATE COMMENT '复检日期',
    recheck_result TINYINT COMMENT '复检结果',
    remark TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_device_id (device_id),
    INDEX idx_plan_id (plan_id),
    INDEX idx_qc_date (qc_date)
) COMMENT '质控记录表';
