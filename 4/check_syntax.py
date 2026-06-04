import ast
import os

files = [
    'app.py',
    'pages/overview.py',
    'pages/department_analysis.py',
    'pages/patient_experience.py',
    'pages/data_import.py',
    'pages/doctor_analysis.py',
    'pages/fee_structure.py',
    'pages/report_export.py',
    'export/report_exporter.py',
    'metrics/metrics_calculator.py',
    'validation/data_validator.py'
]

for f in files:
    if os.path.exists(f):
        try:
            with open(f, 'r', encoding='utf-8') as fp:
                ast.parse(fp.read())
            print(f'OK: {f}')
        except SyntaxError as e:
            print(f'ERROR in {f}: line {e.lineno} - {e.msg}')
        except Exception as e:
            print(f'ERROR in {f}: {e}')
    else:
        print(f'MISSING: {f}')

print('\n语法检查完成！')
