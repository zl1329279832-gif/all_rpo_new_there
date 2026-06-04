import os
import sys
sys.path.insert(0, '.')

output_dir = 'data/demo'
os.makedirs(output_dir, exist_ok=True)

exec(compile(open('data/generate_demo_data.py', encoding='utf-8').read(), 'generate_demo_data.py', 'exec'))

try:
    save_all_data(output_dir)
    print("\n数据生成完成！")
except Exception as e:
    print(f"\n错误: {e}")
    import traceback
    traceback.print_exc()
