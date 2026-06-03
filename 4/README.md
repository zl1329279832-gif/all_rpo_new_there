# 医院门诊运营分析平台

基于 Python、Streamlit、Pandas、Plotly 构建的医院门诊运营数据分析平台。

## 功能特性

### 数据管理
- 支持多份 CSV 数据上传（科室、医生、挂号记录、就诊记录、检查项目、药品费用、候诊时间、患者满意度）
- 字段完整性校验
- 数据关联检查
- 缺失值智能提示
- 异常数据自动识别

### 分析模块
1. **运营总览** - 门诊量趋势、关键指标概览
2. **科室分析** - 科室接诊能力、异常科室识别
3. **医生分析** - 医生工作负荷、绩效对比
4. **费用结构** - 费用构成分析、检查转化率
5. **患者体验** - 候诊时间分析、满意度分布

### 报告导出
- Excel 格式报告
- HTML 交互式报告
- 支持按日期、科室、医生、患者类型筛选

## 项目结构

```
hospital_outpatient_analysis/
├── app.py                 # 主应用入口
├── requirements.txt       # 依赖包
├── .gitignore            # Git 忽略配置
├── README.md             # 项目说明
├── data/                 # 数据目录
│   └── demo/             # 演示数据
├── ingestion/            # 数据导入模块
├── validation/           # 数据校验模块
├── transform/            # 数据转换模块
├── metrics/              # 指标计算模块
├── visualization/        # 可视化模块
├── pages/                # Streamlit 页面
├── export/               # 报告导出模块
└── tests/                # 测试模块
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

- **前端框架**: Streamlit
- **数据处理**: Pandas, NumPy
- **可视化**: Plotly
- **统计分析**: SciPy
- **报告导出**: OpenPyXL

## 许可证

MIT License
