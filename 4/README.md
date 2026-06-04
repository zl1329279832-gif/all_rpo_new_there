# 医院门诊运营分析平台

基于 Python、Streamlit、Pandas、Plotly 构建的医院门诊运营数据分析平台。

## 功能特性

### 数据治理
- 支持多份 CSV 数据上传（科室、医生、挂号记录、就诊记录、检查项目、药品费用、候诊时间、患者满意度）
- 字段完整性校验
- 数据关联检查
- 缺失值智能提示
- 异常数据自动识别
- 主键重复检测
- 无效医生ID检测
- 就诊挂号不匹配检测
- 费用异常检测
- 候诊时间异常检测
- 满意度缺失检测
- 详细问题定位与修复建议

### 经营洞察
#### 核心分析模块
1. **运营总览** - 门诊量趋势、关键指标概览
2. **科室分析** - 科室接诊能力、异常科室识别
3. **医生分析** - 医生工作负荷、绩效对比
4. **费用结构** - 费用构成分析、检查转化率
5. **患者体验** - 候诊时间分析、满意度分布

#### 新增分析维度
- **同环比分析** - 挂号量、就诊量、收入、候诊时间、满意度月度环比变化
- **工作日周末对比** - 工作日与周末的门诊量、就诊量、候诊时间、满意度差异
- **高峰时段分析** - 每日各时段挂号量分布，识别就诊高峰时段
- **科室容量利用率** - 基于医生配置计算科室接诊容量与实际利用率
- **医生工作负荷均衡度** - 基尼系数、变异系数、高低负荷比、超负荷医生占比
- **候诊时间分层** - 按等待时长分层统计（<15分钟、15-30分钟、30-60分钟、>60分钟）
- **异常原因分析** - 针对高候诊、低满意度、高负荷科室的智能原因诊断

### 筛选联动功能
- 支持按日期范围筛选
- 支持按科室筛选
- 支持按医生筛选
- 支持按患者类型筛选
- 筛选条件实时联动所有分析图表

### 报告导出
- Excel 格式报告
- HTML 交互式报告
- 支持按筛选条件导出定制化报告

## 项目结构

```
hospital_outpatient_analysis/
├── app.py                 # 主应用入口
├── requirements.txt       # 依赖包
├── .gitignore            # Git 忽略配置
├── README.md             # 项目说明
├── data/                 # 数据目录
│   ├── __init__.py
│   └── generate_demo_data.py  # 演示数据生成
├── ingestion/            # 数据导入模块
│   ├── __init__.py
│   └── data_loader.py
├── validation/           # 数据校验模块
│   ├── __init__.py
│   └── data_validator.py
├── transform/            # 数据转换模块
│   ├── __init__.py
│   └── data_transformer.py
├── metrics/              # 指标计算模块
│   ├── __init__.py
│   └── metrics_calculator.py
├── visualization/        # 可视化模块
│   ├── __init__.py
│   └── charts.py
├── pages/                # Streamlit 页面
│   ├── __init__.py
│   ├── data_import.py
│   ├── overview.py
│   ├── department_analysis.py
│   ├── doctor_analysis.py
│   ├── fee_structure.py
│   ├── patient_experience.py
│   └── report_export.py
├── export/               # 报告导出模块
│   ├── __init__.py
│   └── report_exporter.py
├── utils/                # 工具模块
│   ├── __init__.py
│   └── error_handler.py
└── tests/                # 测试模块
    ├── __init__.py
    ├── run_tests.py
    ├── test_data_loader.py
    ├── test_data_validator.py
    └── test_metrics.py
```

## 快速开始

### 1. 安装依赖

```bash
pip install -r requirements.txt
```

### 2. 生成演示数据

```bash
python -m data.generate_demo_data
```

### 3. 启动应用

```bash
streamlit run app.py
```

### 4. 访问应用

打开浏览器访问: http://localhost:8501

### 5. 运行测试

```bash
python -m tests.run_tests
```

## 数据格式说明

平台支持以下 CSV 数据文件：

| 文件 | 关键字段 |
|------|----------|
| departments.csv | department_id, department_name, type |
| doctors.csv | doctor_id, doctor_name, department_id, title |
| registrations.csv | reg_id, patient_id, department_id, doctor_id, reg_date, reg_time, patient_type |
| visits.csv | visit_id, reg_id, doctor_id, department_id, visit_date, diagnosis |
| examinations.csv | exam_id, visit_id, exam_item, exam_fee, exam_result |
| medications.csv | med_id, visit_id, drug_name, drug_fee, quantity |
| waiting_times.csv | wait_id, reg_id, arrival_time, call_time, wait_minutes |
| satisfaction.csv | survey_id, visit_id, overall_score, wait_score, service_score |

## 技术栈

- **前端框架**: Streamlit >= 1.28.0
- **数据处理**: Pandas >= 2.0.0, NumPy >= 1.24.0
- **可视化**: Plotly >= 5.17.0
- **统计分析**: SciPy >= 1.11.0
- **报告导出**: OpenPyXL >= 3.1.0
- **日期处理**: python-dateutil >= 2.8.0

## 许可证

MIT License
