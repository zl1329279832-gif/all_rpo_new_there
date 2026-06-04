import sys
sys.path.insert(0, '.')

print("开始导入模块...")
try:
    from data.generate_demo_data import generate_departments, generate_doctors, generate_registrations, generate_visits, generate_examinations, generate_medications, generate_waiting_times, generate_satisfaction, save_all_data
    print("✓ 模块导入成功")
except Exception as e:
    print(f"✗ 模块导入失败: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)

print("\n开始生成数据...")
try:
    depts = generate_departments()
    print(f"✓ 生成科室: {len(depts)} 条")
except Exception as e:
    print(f"✗ 生成科室失败: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)

try:
    doctors = generate_doctors(depts)
    print(f"✓ 生成医生: {len(doctors)} 条")
except Exception as e:
    print(f"✗ 生成医生失败: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)

try:
    regs = generate_registrations(depts, doctors, 100)
    print(f"✓ 生成挂号: {len(regs)} 条")
except Exception as e:
    print(f"✗ 生成挂号失败: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)

print("\n测试成功！")
