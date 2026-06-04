import sys
import os
sys.path.insert(0, '.')

print("=" * 60)
print("医院门诊运营分析平台 - 系统测试")
print("=" * 60)

print("\n[1/6] 生成演示数据...")
try:
    exec(compile(open('data/generate_demo_data.py', encoding='utf-8').read(), 'generate_demo_data.py', 'exec'))
    save_all_data('data/demo')
    print("  ✓ 演示数据生成成功")
except Exception as e:
    print(f"  ✗ 演示数据生成失败: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)

print("\n[2/6] 测试数据加载...")
try:
    from ingestion import DataLoader
    loader = DataLoader()
    data = loader.load_from_directory('data/demo')
    print(f"  ✓ 数据加载成功: {len(data)} 个数据表")
    for k, v in data.items():
        if v is not None:
            print(f"    - {k}: {len(v)} 条记录")
except Exception as e:
    print(f"  ✗ 数据加载失败: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)

print("\n[3/6] 测试数据校验...")
try:
    from validation import DataValidator
    validator = DataValidator(data)
    results = validator.validate_all()
    print(f"  ✓ 数据校验完成")
    print(f"    - 错误数: {validator.get_error_count()}")
    print(f"    - 警告数: {validator.get_warning_count()}")
except Exception as e:
    print(f"  ✗ 数据校验失败: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)

print("\n[4/6] 测试数据转换...")
try:
    from transform import DataTransformer
    transformer = DataTransformer(data)
    transformed = transformer.transform_all()
    print(f"  ✓ 数据转换成功: {len(transformed)} 个数据表")
except Exception as e:
    print(f"  ✗ 数据转换失败: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)

print("\n[5/6] 测试指标计算...")
try:
    from metrics import MetricsCalculator
    metrics = MetricsCalculator(transformed)
    
    overview = metrics.get_overview_metrics()
    print(f"  ✓ 运营概览指标: {len(overview)} 项")
    
    dept_metrics = metrics.get_department_metrics()
    print(f"  ✓ 科室指标: {len(dept_metrics)} 个科室")
    
    mom = metrics.get_mom_changes()
    print(f"  ✓ 月环比指标: {len(mom)} 项")
    
    ww = metrics.get_weekday_weekend_comparison()
    print(f"  ✓ 工作日周末对比: {len(ww)} 项")
    
    peak = metrics.get_peak_hours()
    print(f"  ✓ 高峰时段: {len(peak) if peak is not None else 0} 小时")
    
    cap = metrics.get_department_capacity_utilization()
    print(f"  ✓ 容量利用率: {len(cap) if cap is not None else 0} 个科室")
    
    balance = metrics.get_doctor_workload_balance()
    print(f"  ✓ 负荷均衡度: {len(balance)} 项指标")
    
    strata = metrics.get_wait_time_stratification()
    print(f"  ✓ 候诊分层: {len(strata)} 层")
    
    anomaly = metrics.get_anomaly_cause_analysis()
    print(f"  ✓ 异常分析: {len(anomaly)} 个异常科室")
    
except Exception as e:
    print(f"  ✗ 指标计算失败: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)

print("\n[6/6] 测试报告导出...")
try:
    from export import ReportExporter
    exporter = ReportExporter(data, transformed, metrics, validator)
    
    excel_bytes = exporter.generate_excel_report()
    print(f"  ✓ Excel报告生成成功: {len(excel_bytes)} 字节")
    
    html = exporter.generate_html_report()
    print(f"  ✓ HTML报告生成成功: {len(html)} 字符")
    
except Exception as e:
    print(f"  ✗ 报告导出失败: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)

print("\n" + "=" * 60)
print("✓ 所有测试通过！系统运行正常。")
print("=" * 60)
