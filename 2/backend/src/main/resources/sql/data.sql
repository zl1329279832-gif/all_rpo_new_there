USE medical_device;

INSERT INTO sys_role (role_name, role_code, description) VALUES
('系统管理员', 'ADMIN', '系统超级管理员，拥有所有权限'),
('设备管理员', 'DEVICE_ADMIN', '负责设备档案管理和日常维护'),
('维修工程师', 'ENGINEER', '负责设备维修和保养'),
('质控人员', 'QC_STAFF', '负责设备质量控制和校准'),
('科室主任', 'DEPT_HEAD', '科室负责人，查看本科室设备情况'),
('普通用户', 'USER', '普通操作人员');

INSERT INTO sys_user (username, password, real_name, email, phone, dept_id, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin@hospital.com', '13800138000', NULL, 1),
('device_admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张设备', 'device@hospital.com', '13800138001', NULL, 1),
('engineer1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李工', 'li@hospital.com', '13800138002', NULL, 1),
('engineer2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王工', 'wang@hospital.com', '13800138003', NULL, 1),
('qc_staff', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '陈质控', 'qc@hospital.com', '13800138004', NULL, 1);

INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 3),
(5, 4);

INSERT INTO department (dept_name, dept_code, parent_id, leader, phone, address, sort_order, status) VALUES
('门诊部', 'OUTPATIENT', 0, '王主任', '010-88880001', '门诊楼1-3层', 1, 1),
('住院部', 'INPATIENT', 0, '李主任', '010-88880002', '住院楼A/B/C区', 2, 1),
('急诊科', 'EMERGENCY', 0, '张主任', '010-88880003', '急诊楼1-2层', 3, 1),
('手术室', 'OPERATING', 0, '刘主任', '010-88880004', '手术楼', 4, 1),
('检验科', 'LABORATORY', 0, '陈主任', '010-88880005', '医技楼1层', 5, 1),
('放射科', 'RADIOLOGY', 0, '赵主任', '010-88880006', '医技楼2层', 6, 1),
('心内科', 'CARDIOLOGY', 2, '孙主任', '010-88880101', '住院楼A区5层', 7, 1),
('神经内科', 'NEUROLOGY', 2, '周主任', '010-88880102', '住院楼A区6层', 8, 1),
('骨科', 'ORTHOPEDICS', 2, '吴主任', '010-88880103', '住院楼B区3层', 9, 1),
('ICU', 'ICU', 2, '郑主任', '010-88880104', '住院楼C区1层', 10, 1);

INSERT INTO device (device_name, device_code, device_type, device_model, manufacturer, serial_number, purchase_date, warranty_start, warranty_end, dept_id, location, status, risk_level, total_downtime, maintainer, qc_status, description) VALUES
('磁共振成像系统', 'DEV-2024-0001', '影像设备', 'Signa Architect 3.0T', 'GE医疗', 'SN-MRI-001', '2023-06-15', '2023-07-01', '2026-06-30', 6, '放射科MRI室1', 1, 1, 0, '李工', 1, '3.0T超导磁共振，用于全身各部位的磁共振检查'),
('CT扫描仪', 'DEV-2024-0002', '影像设备', 'Revolution Apex', 'GE医疗', 'SN-CT-001', '2023-08-20', '2023-09-01', '2026-08-31', 6, '放射科CT室1', 1, 1, 24, '王工', 1, '256排高端CT扫描仪'),
('X光机', 'DEV-2024-0003', '影像设备', 'DRX-Ascend', '柯达医疗', 'SN-XRAY-001', '2023-05-10', '2023-06-01', '2025-05-31', 6, '放射科DR室1', 1, 2, 8, '李工', 1, '数字化X光机'),
('彩色多普勒超声诊断仪', 'DEV-2024-0004', '超声设备', 'LOGIQ E10', 'GE医疗', 'SN-US-001', '2023-09-01', '2023-10-01', '2026-09-30', 1, '门诊超声室1', 1, 2, 0, '王工', 1, '高端彩色超声诊断仪'),
('心电图机', 'DEV-2024-0005', '心电设备', 'MAC 5500HD', 'GE医疗', 'SN-ECG-001', '2024-01-15', '2024-02-01', '2027-01-31', 7, '心内科检查室1', 1, 2, 0, '张设备', 1, '12导联心电图机'),
('监护仪', 'DEV-2024-0006', '监护设备', 'IntelliVue MX800', '飞利浦', 'SN-MON-001', '2024-02-20', '2024-03-01', '2027-02-28', 10, 'ICU病床1', 1, 1, 0, '李工', 1, '重症监护专用监护仪'),
('监护仪', 'DEV-2024-0007', '监护设备', 'IntelliVue MX700', '飞利浦', 'SN-MON-002', '2024-02-20', '2024-03-01', '2027-02-28', 10, 'ICU病床2', 2, 1, 48, '王工', 2, '重症监护专用监护仪'),
('呼吸机', 'DEV-2024-0008', '生命支持', 'Servo-U', '迈柯唯', 'SN-VENT-001', '2023-11-10', '2023-12-01', '2026-11-30', 10, 'ICU病房1', 1, 1, 0, '李工', 1, '高端有创呼吸机'),
('麻醉机', 'DEV-2024-0009', '手术设备', 'Aisys CS2', 'GE医疗', 'SN-ANES-001', '2023-07-25', '2023-08-01', '2026-07-31', 4, '手术室1', 1, 1, 0, '王工', 1, '麻醉工作站'),
('全自动生化分析仪', 'DEV-2024-0010', '检验设备', 'cobas 8000', '罗氏诊断', 'SN-LAB-001', '2023-10-15', '2023-11-01', '2026-10-31', 5, '检验科生化室', 1, 2, 16, '张设备', 1, '模块化生化免疫分析系统'),
('血液分析仪', 'DEV-2024-0011', '检验设备', 'XN-3000', '希森美康', 'SN-LAB-002', '2024-03-01', '2024-04-01', '2027-03-31', 5, '检验科血液室', 1, 2, 0, '李工', 1, '全自动血液分析流水线'),
('输液泵', 'DEV-2024-0012', '护理设备', 'Agilia', '费森尤斯', 'SN-INF-001', '2024-04-10', '2024-05-01', '2027-04-30', 7, '心内科病房1', 1, 3, 0, '王工', 1, '智能输液泵'),
('除颤仪', 'DEV-2024-0013', '急救设备', 'Lifepak 20e', '美敦力', 'SN-DEF-001', '2023-12-05', '2024-01-01', '2025-12-31', 3, '急诊抢救室', 1, 1, 0, '张设备', 1, '双相波除颤监护仪'),
('手术床', 'DEV-2024-0014', '手术设备', 'Maquet Magnus', '迈柯唯', 'SN-OT-001', '2023-09-20', '2023-10-01', '2026-09-30', 4, '手术室1', 1, 2, 0, '李工', 1, '多功能电动手术床'),
('脑电图机', 'DEV-2024-0015', '神经设备', 'Nicolet EEG', 'Natus', 'SN-EEG-001', '2024-01-20', '2024-02-01', '2027-01-31', 8, '神经内科检查室', 1, 2, 0, '王工', 1, '数字脑电图系统');

INSERT INTO inspection_plan (plan_name, device_id, cycle_type, cycle_days, start_date, next_execution_date, inspector, check_items, status, description) VALUES
('MRI日常巡检', 1, 1, 1, '2024-06-01', '2024-06-02', '李工', '["外观检查","电源检查","冷却系统检查","控制面板检查"]', 1, '每日一次常规巡检'),
('CT每周维护', 2, 2, 7, '2024-06-01', '2024-06-08', '王工', '["球管温度检测","探测器校准","图像质量检查","机械润滑"]', 1, '每周一次全面维护'),
('超声设备月度检查', 4, 3, 30, '2024-06-01', '2024-07-01', '张设备', '["探头检查","图像质量测试","功能按键测试","清洁消毒"]', 1, '每月一次专业检查'),
('呼吸机季度保养', 8, 4, 90, '2024-04-01', '2024-07-01', '李工', '["呼吸回路检测","压力校准","流量传感器校准","气密性测试"]', 1, '每季度专业保养'),
('除颤仪年度检测', 13, 5, 365, '2024-01-01', '2025-01-01', '张设备', '["电池测试","放电能量检测","心电图波形检测","报警功能测试"]', 1, '年度专业检测');

INSERT INTO inspection_task (task_code, plan_id, device_id, task_name, task_type, plan_date, actual_date, inspector_id, inspector_name, status, check_result, abnormal_description, handle_suggestion) VALUES
('IT-20240601-001', 1, 1, 'MRI日常巡检', 1, '2024-06-01', '2024-06-01 09:30:00', 3, '李工', 3, 1, NULL, NULL),
('IT-20240601-002', 1, 1, 'MRI日常巡检', 1, '2024-06-02', NULL, NULL, NULL, 1, NULL, NULL, NULL),
('IT-20240601-003', NULL, 7, '监护仪故障排查', 2, '2024-06-01', '2024-06-01 14:20:00', 4, '王工', 3, 2, '心电波形显示异常，存在干扰', '建议检查接地线路和屏蔽效果'),
('IT-20240605-001', 2, 2, 'CT每周维护', 1, '2024-06-05', NULL, 4, '王工', 2, NULL, NULL, NULL),
('IT-20240610-001', NULL, 13, '除颤仪突击检查', 2, '2024-06-10', NULL, NULL, NULL, 1, NULL, NULL, NULL);

INSERT INTO repair_order (order_code, device_id, fault_type, fault_description, fault_level, reporter_id, reporter_name, report_time, repairer_id, repairer_name, assign_time, start_time, complete_time, status, repair_content, repair_result, downtime, repair_cost, remark) VALUES
('RO-20240601-0001', 7, '电气故障', '监护仪显示屏幕闪烁，心电波形不稳定', 2, 2, '张设备', '2024-06-01 08:30:00', 4, '王工', '2024-06-01 09:00:00', '2024-06-01 09:30:00', NULL, 3, '正在检查电源模块和显示面板', NULL, 48, 0, '需要更换电源模块'),
('RO-20240515-0001', 10, '机械故障', '样本针运动异常，时有卡顿现象', 2, 2, '张设备', '2024-05-15 10:00:00', 3, '李工', '2024-05-15 10:30:00', '2024-05-15 14:00:00', '2024-05-16 11:00:00', 5, '清洁并润滑样本针导轨，更换密封圈', 1, 16, 3500.00, '运行正常'),
('RO-20240520-0001', 3, '软件故障', '工作站偶尔死机，图像传输中断', 3, 2, '张设备', '2024-05-20 14:30:00', 4, '王工', '2024-05-20 15:00:00', '2024-05-21 09:00:00', '2024-05-21 16:00:00', 5, '重新安装驱动程序，更新系统补丁', 1, 8, 1500.00, '建议定期清理系统垃圾'),
('RO-20240603-0001', 12, '配件损坏', '输液泵报警按键失灵', 3, 2, '张设备', '2024-06-03 11:00:00', NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, 0, 0, NULL);

INSERT INTO spare_part (part_name, part_code, part_model, manufacturer, unit, price, stock_quantity, min_stock, description) VALUES
('监护仪电源模块', 'SP-MON-001', 'PM-800', '飞利浦', '个', 2800.00, 5, 2, 'MX系列监护仪专用电源模块'),
('输液泵按键面板', 'SP-INF-001', 'KP-200', '费森尤斯', '套', 680.00, 8, 3, '输液泵操作按键面板'),
('CT球管密封圈', 'SP-CT-001', 'OS-500', 'GE', '个', 350.00, 20, 10, 'CT球管密封套件'),
('超声探头耦合剂', 'SP-US-001', 'GEL-100', '通用', '瓶', 45.00, 100, 30, '医用超声耦合剂'),
('呼吸机过滤器', 'SP-VEN-001', 'FIL-300', '迈柯唯', '个', 180.00, 30, 10, '呼吸机空气过滤器'),
('心电图机打印纸', 'SP-ECG-001', 'PAP-210', '通用', '卷', 25.00, 50, 20, '心电图纸'),
('除颤仪电池', 'SP-DEF-001', 'BAT-900', '美敦力', '块', 1800.00, 4, 2, '除颤仪专用锂电池'),
('生化分析仪样本针', 'SP-LAB-001', 'NEEDLE-100', '罗氏', '根', 4200.00, 2, 1, '生化仪样本穿刺针');

INSERT INTO part_replacement (repair_order_id, spare_part_id, part_name, part_model, quantity, unit_price, total_price, operator, remark) VALUES
(2, 8, '生化分析仪样本针', 'NEEDLE-100', 1, 4200.00, 4200.00, '李工', '更换新样本针'),
(2, 3, 'CT球管密封圈', 'OS-500', 2, 350.00, 700.00, '李工', '更换密封圈');

INSERT INTO calibration_record (device_id, calibration_type, calibration_date, calibration_agency, calibration_person, certificate_number, valid_until, calibration_result, calibration_items, deviation_value, description, next_calibration_date) VALUES
(13, '强制检定', '2024-03-15', '市计量检定研究院', '王检定员', 'CAL-2024-0315-001', '2025-03-14', 1, '["放电能量检测","时间参数检测","报警功能检测"]', 0.002, '各项指标符合要求', '2025-03-15'),
(1, '厂家校准', '2024-04-20', 'GE医疗服务中心', '张工程师', 'CAL-2024-0420-002', '2024-10-19', 1, '["磁场均匀性检测","梯度线性校准","射频功率检测"]', 0.005, '设备状态良好', '2024-10-20'),
(2, '厂家校准', '2024-05-10', 'GE医疗服务中心', '李工程师', 'CAL-2024-0510-003', '2024-11-09', 1, '["CT值校准","空间分辨率检测","对比度检测"]', 0.003, '校准合格', '2024-11-10');

INSERT INTO downtime_record (device_id, repair_order_id, downtime_type, start_time, end_time, duration, reason, description) VALUES
(7, 1, 1, '2024-06-01 08:30:00', NULL, NULL, '电气故障', '监护仪屏幕闪烁故障停机'),
(10, 2, 1, '2024-05-15 10:00:00', '2024-05-16 11:00:00', 25, '机械故障', '样本针运动异常停机维修'),
(3, 3, 2, '2024-05-21 09:00:00', '2024-05-21 16:00:00', 7, '软件升级', '系统维护和软件升级');

INSERT INTO maintenance_contract (contract_code, contract_name, contract_type, device_id, supplier, contact_person, contact_phone, start_date, end_date, contract_amount, payment_status, status, content, remark) VALUES
('MC-2024-0001', 'MRI设备全面维保合同', 1, 1, 'GE医疗中国有限公司', '陈经理', '400-820-1188', '2024-01-01', '2024-12-31', 280000.00, 2, 1, '包含预防性维护、紧急维修、配件更换、技术支持等全方面服务', '甲级维保合同'),
('MC-2024-0002', 'CT设备保修合同', 1, 2, 'GE医疗中国有限公司', '陈经理', '400-820-1188', '2024-03-01', '2025-02-28', 180000.00, 2, 1, '整机保修，包含球管', '保修合同'),
('MC-2024-0003', '呼吸机校准服务合同', 3, 8, '市计量测试所', '刘工', '010-66668888', '2024-01-01', '2024-12-31', 5000.00, 2, 1, '年度校准服务，每季度一次', '校准服务'),
('MC-2024-0004', '生化分析仪维保', 1, 10, '罗氏诊断产品有限公司', '周经理', '400-820-8888', '2024-04-01', '2025-03-31', 120000.00, 1, 1, '年度维保服务，含定期保养', '首付50%');

INSERT INTO qc_plan (plan_name, device_id, qc_type, cycle_type, start_date, next_execution_date, qc_standard, qc_items, executor, status) VALUES
('MRI日常质控', 1, '设备质控', 1, '2024-06-01', '2024-06-02', '按照《MRI设备质量控制规范》执行', '["图像均匀性检测","信噪比测试","空间分辨率测试"]', '陈质控', 1),
('CT日常质控', 2, '设备质控', 1, '2024-06-01', '2024-06-02', '按照《CT设备质量控制规范》执行', '["CT值准确性","噪声水平测试","层厚准确性"]', '陈质控', 1),
('呼吸机季度质控', 8, '设备质控', 4, '2024-04-01', '2024-07-01', '按照《呼吸机质量控制指南》执行', '["潮气量精度检测","压力校准测试","报警功能验证"]', '陈质控', 1),
('除颤仪年度质控', 13, '设备质控', 5, '2024-01-01', '2025-01-01', '按照《除颤设备质量检测规范》执行', '["放电能量测试","同步精度检测","电池性能测试"]', '陈质控', 1);

INSERT INTO qc_record (plan_id, device_id, qc_date, qc_type, executor_id, executor_name, qc_result, qc_data, deviation_description, handle_measure, recheck_date, recheck_result, remark) VALUES
(1, 1, '2024-06-01', '设备质控', 5, '陈质控', 1, '{"uniformity": 98.5, "snr": 156, "resolution": "2.0mm"}', NULL, NULL, NULL, NULL, '质控合格'),
(2, 2, '2024-06-01', '设备质控', 5, '陈质控', 1, '{"ct_value_water": 0.5, "noise": 3.2, "slice_thickness": 5.1}', NULL, NULL, NULL, NULL, '质控合格'),
(NULL, 7, '2024-05-28', '设备质控', 5, '陈质控', 2, '{"ecg_accuracy": 92, "alarm_response": 1.2}', '心电监测精度偏低，报警响应时间略长', '通知维修部门检查，进行校准', '2024-06-05', NULL, '待复检');
