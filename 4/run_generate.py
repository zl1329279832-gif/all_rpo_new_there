import sys
sys.path.insert(0, '.')

from data.generate_demo_data import save_all_data

try:
    save_all_data()
    print("\n数据生成成功！")
except Exception as e:
    print(f"\nERROR: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)
