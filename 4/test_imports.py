import sys
import traceback

print("开始测试导入检查...")

try:
    from utils.error_handler import ErrorHandler
    print("✓ ErrorHandler 导入成功")
except Exception as e:
    print(f"✗ ErrorHandler 导入失败: {e}")
    traceback.print_exc()
    sys.exit(1)

try:
    from ingestion import DataLoader
    print("✓ DataLoader 导入成功")
except Exception as e:
    print(f"✗ DataLoader 导入失败: {e}")
    traceback.print_exc()
    sys.exit(1)

try:
    from validation import DataValidator
    print("✓ DataValidator 导入成功")
except Exception as e:
    print(f"✗ DataValidator 导入失败: {e}")
    traceback.print_exc()
    sys.exit(1)

try:
    from transform import DataTransformer
    print("✓ DataTransformer 导入成功")
except Exception as e:
    print(f"✗ DataTransformer 导入失败: {e}")
    traceback.print_exc()
    sys.exit(1)

try:
    from metrics import MetricsCalculator
    print("✓ MetricsCalculator 导入成功")
except Exception as e:
    print(f"✗ MetricsCalculator 导入失败: {e}")
    traceback.print_exc()
    sys.exit(1)

try:
    from visualization import ChartGenerator
    print("✓ ChartGenerator 导入成功")
except Exception as e:
    print(f"✗ ChartGenerator 导入失败: {e}")
    traceback.print_exc()
    sys.exit(1)

try:
    from export import ReportExporter
    print("✓ ReportExporter 导入成功")
except Exception as e:
    print(f"✗ ReportExporter 导入失败: {e}")
    traceback.print_exc()
    sys.exit(1)

try:
    from pages import DataImportPage, OverviewPage, DepartmentAnalysisPage, DoctorAnalysisPage, FeeStructurePage, PatientExperiencePage, ReportExportPage
    print("✓ 所有页面导入成功")
except Exception as e:
    print(f"✗ 页面导入失败: {e}")
    traceback.print_exc()
    sys.exit(1)

print("\n✅ 所有模块导入测试通过！")
