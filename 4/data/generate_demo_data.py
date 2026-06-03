import pandas as pd
import numpy as np
from datetime import datetime, timedelta
import random
import os


def generate_departments():
    departments = [
        {'department_id': 'D001', 'department_name': '内科', 'type': '临床科室'},
        {'department_id': 'D002', 'department_name': '外科', 'type': '临床科室'},
        {'department_id': 'D003', 'department_name': '儿科', 'type': '临床科室'},
        {'department_id': 'D004', 'department_name': '妇产科', 'type': '临床科室'},
        {'department_id': 'D005', 'department_name': '眼科', 'type': '临床科室'},
        {'department_id': 'D006', 'department_name': '口腔科', 'type': '临床科室'},
        {'department_id': 'D007', 'department_name': '皮肤科', 'type': '临床科室'},
        {'department_id': 'D008', 'department_name': '骨科', 'type': '临床科室'},
        {'department_id': 'D009', 'department_name': '神经内科', 'type': '临床科室'},
        {'department_id': 'D010', 'department_name': '心血管内科', 'type': '临床科室'},
    ]
    return pd.DataFrame(departments)


def generate_doctors(departments_df):
    doctor_names = [
        '张伟', '李娜', '王强', '刘洋', '陈静', '杨帆', '赵敏', '周杰',
        '吴芳', '郑军', '孙丽', '马超', '朱婷', '胡军', '林峰', '雷雨',
        '罗晨', '梁欣', '宋涛', '唐悦', '许峰', '韩冰', '冯雪', '董明'
    ]
    titles = ['主任医师', '副主任医师', '主治医师', '住院医师']
    title_weights = [0.2, 0.3, 0.3, 0.2]

    doctors = []
    doctor_id = 1
    for _, dept in departments_df.iterrows():
        num_doctors = random.randint(2, 4)
        for i in range(num_doctors):
            doctors.append({
                'doctor_id': f'DOC{doctor_id:03d}',
                'doctor_name': random.choice(doctor_names),
                'department_id': dept['department_id'],
                'title': np.random.choice(titles, p=title_weights),
                'years_of_experience': random.randint(3, 30)
            })
            doctor_id += 1
    return pd.DataFrame(doctors)


def generate_registrations(departments_df, doctors_df, num_records=1200):
    patient_types = ['普通门诊', '专家门诊', '急诊', '特需门诊']
    patient_type_weights = [0.5, 0.25, 0.15, 0.1]

    start_date = datetime(2024, 1, 1)
    end_date = datetime(2024, 3, 31)
    date_range = [start_date + timedelta(days=x) for x in range((end_date - start_date).days + 1)]

    registrations = []
    for reg_id in range(1, num_records + 1):
        reg_date = random.choice(date_range)
        dept_row = departments_df.sample(n=1).iloc[0]
        dept_id = dept_row['department_id']

        dept_doctors = doctors_df[doctors_df['department_id'] == dept_id]
        if len(dept_doctors) > 0:
            doctor_row = dept_doctors.sample(n=1).iloc[0]
            doctor_id = doctor_row['doctor_id']
        else:
            doctor_id = doctors_df.sample(n=1).iloc[0]['doctor_id']

        hour = random.randint(7, 17)
        minute = random.randint(0, 59)
        reg_time = f'{hour:02d}:{minute:02d}:00'

        registrations.append({
            'reg_id': f'REG{reg_id:05d}',
            'patient_id': f'PAT{random.randint(10000, 99999)}',
            'department_id': dept_id,
            'doctor_id': doctor_id,
            'reg_date': reg_date.strftime('%Y-%m-%d'),
            'reg_time': reg_time,
            'patient_type': np.random.choice(patient_types, p=patient_type_weights),
            'is_weekend': 1 if reg_date.weekday() >= 5 else 0
        })

    return pd.DataFrame(registrations)


def generate_visits(registrations_df):
    diagnoses = {
        'D001': ['上呼吸道感染', '高血压', '糖尿病', '胃炎', '支气管炎'],
        'D002': ['阑尾炎', '疝气', '胆囊结石', '甲状腺结节', '体表肿物'],
        'D003': ['感冒发热', '肺炎', '腹泻', '扁桃体炎', '支气管炎'],
        'D004': ['孕期检查', '阴道炎', '月经不调', '子宫肌瘤', '卵巢囊肿'],
        'D005': ['结膜炎', '干眼症', '近视', '白内障', '青光眼'],
        'D006': ['龋齿', '牙周炎', '牙髓炎', '智齿冠周炎', '口腔溃疡'],
        'D007': ['湿疹', '荨麻疹', '痤疮', '皮炎', '真菌感染'],
        'D008': ['骨折', '腰椎间盘突出', '关节炎', '骨质疏松', '扭伤'],
        'D009': ['头痛', '偏头痛', '失眠', '脑血管病', '帕金森'],
        'D010': ['冠心病', '心律失常', '心衰', '高血压心脏病', '心肌梗死']
    }

    visits = []
    for idx, reg in registrations_df.iterrows():
        dept_id = reg['department_id']
        dept_diagnoses = diagnoses.get(dept_id, ['待查'])

        visit_date = datetime.strptime(reg['reg_date'], '%Y-%m-%d')
        if random.random() < 0.95:
            visit_days = random.randint(0, 1)
            actual_visit_date = visit_date + timedelta(days=visit_days)
        else:
            actual_visit_date = visit_date + timedelta(days=random.randint(2, 7))

        visits.append({
            'visit_id': f'VIS{idx + 1:05d}',
            'reg_id': reg['reg_id'],
            'doctor_id': reg['doctor_id'],
            'department_id': dept_id,
            'visit_date': actual_visit_date.strftime('%Y-%m-%d'),
            'diagnosis': random.choice(dept_diagnoses),
            'visit_duration_minutes': random.randint(5, 30),
            'has_examination': 1 if random.random() < 0.6 else 0,
            'has_medication': 1 if random.random() < 0.7 else 0
        })
    return pd.DataFrame(visits)


def generate_examinations(visits_df):
    exam_items = [
        {'item': '血常规', 'fee_range': (20, 50)},
        {'item': '尿常规', 'fee_range': (10, 30)},
        {'item': '肝功能', 'fee_range': (50, 120)},
        {'item': '肾功能', 'fee_range': (40, 100)},
        {'item': '心电图', 'fee_range': (30, 80)},
        {'item': 'X光片', 'fee_range': (80, 150)},
        {'item': 'CT检查', 'fee_range': (200, 500)},
        {'item': 'B超', 'fee_range': (60, 150)},
        {'item': '核磁共振', 'fee_range': (500, 1000)},
        {'item': '生化全套', 'fee_range': (150, 300)}
    ]
    exam_results = ['正常', '轻度异常', '异常', '建议复查']
    result_weights = [0.5, 0.25, 0.15, 0.1]

    examinations = []
    exam_id = 1
    for _, visit in visits_df.iterrows():
        if visit['has_examination'] == 1:
            num_exams = random.randint(1, 3)
            selected_exams = random.sample(exam_items, num_exams)
            for exam in selected_exams:
                fee = random.randint(exam['fee_range'][0], exam['fee_range'][1])
                examinations.append({
                    'exam_id': f'EXM{exam_id:05d}',
                    'visit_id': visit['visit_id'],
                    'exam_item': exam['item'],
                    'exam_fee': fee,
                    'exam_result': np.random.choice(exam_results, p=result_weights),
                    'is_abnormal': 0 if np.random.choice(exam_results, p=result_weights) == '正常' else 1
                })
                exam_id += 1
    return pd.DataFrame(examinations)


def generate_medications(visits_df):
    drug_categories = {
        '抗生素': [('阿莫西林', 15, 30), ('头孢克肟', 25, 50), ('阿奇霉素', 30, 60), ('左氧氟沙星', 20, 45)],
        '解热镇痛': [('布洛芬', 10, 25), ('对乙酰氨基酚', 8, 20), ('阿司匹林', 5, 15)],
        '消化系统': [('奥美拉唑', 20, 40), ('多潘立酮', 15, 30), ('蒙脱石散', 10, 25)],
        '呼吸系统': [('氨溴索', 15, 35), ('右美沙芬', 12, 28), ('沙丁胺醇', 20, 45)],
        '心血管': [('硝苯地平', 15, 35), ('美托洛尔', 18, 40), ('阿托伐他汀', 30, 70)],
        '维生素': [('维生素C', 10, 25), ('维生素B', 15, 35), ('复合维生素', 25, 50)],
        '外用药': [('红霉素软膏', 8, 18), ('碘伏', 5, 12), ('皮炎平', 15, 30)]
    }

    medications = []
    med_id = 1
    for _, visit in visits_df.iterrows():
        if visit['has_medication'] == 1:
            num_drugs = random.randint(1, 4)
            categories = random.sample(list(drug_categories.keys()), min(num_drugs, len(drug_categories)))
            for cat in categories:
                drug = random.choice(drug_categories[cat])
                quantity = random.randint(1, 3)
                unit_price = random.randint(drug[1], drug[2])
                medications.append({
                    'med_id': f'MED{med_id:05d}',
                    'visit_id': visit['visit_id'],
                    'drug_name': drug[0],
                    'drug_category': cat,
                    'quantity': quantity,
                    'unit_price': unit_price,
                    'drug_fee': quantity * unit_price
                })
                med_id += 1
    return pd.DataFrame(medications)


def generate_waiting_times(registrations_df):
    waiting_times = []
    for idx, reg in registrations_df.iterrows():
        reg_hour = int(reg['reg_time'].split(':')[0])
        reg_minute = int(reg['reg_time'].split(':')[1])

        base_wait = 15
        if 8 <= reg_hour <= 10:
            base_wait = 30
        elif 14 <= reg_hour <= 16:
            base_wait = 25
        elif reg_hour >= 17:
            base_wait = 10

        patient_type = reg['patient_type']
        if patient_type == '特需门诊':
            base_wait = max(5, base_wait - 15)
        elif patient_type == '专家门诊':
            base_wait = base_wait + 10
        elif patient_type == '急诊':
            base_wait = max(5, base_wait - 10)

        if reg['is_weekend'] == 1:
            base_wait = base_wait + 15

        wait_minutes = max(5, int(np.random.normal(base_wait, 10)))

        arrival_time = f'{reg_hour:02d}:{reg_minute:02d}:00'
        call_minutes = reg_minute + wait_minutes
        call_hour = reg_hour + call_minutes // 60
        call_minutes = call_minutes % 60
        call_time = f'{call_hour:02d}:{call_minutes:02d}:00'

        waiting_times.append({
            'wait_id': f'WTT{idx + 1:05d}',
            'reg_id': reg['reg_id'],
            'arrival_time': arrival_time,
            'call_time': call_time,
            'wait_minutes': wait_minutes,
            'is_over_30min': 1 if wait_minutes > 30 else 0
        })
    return pd.DataFrame(waiting_times)


def generate_satisfaction(visits_df):
    satisfaction = []
    survey_id = 1
    for _, visit in visits_df.iterrows():
        if random.random() < 0.8:
            base_score = random.randint(3, 5)
            overall_score = min(5, max(1, int(np.random.normal(base_score, 0.8))))
            wait_score = min(5, max(1, int(np.random.normal(base_score - 0.5, 1))))
            service_score = min(5, max(1, int(np.random.normal(base_score + 0.3, 0.7))))

            satisfaction.append({
                'survey_id': f'SRV{survey_id:05d}',
                'visit_id': visit['visit_id'],
                'overall_score': overall_score,
                'wait_score': wait_score,
                'service_score': service_score,
                'environment_score': min(5, max(1, int(np.random.normal(base_score, 0.6)))),
                'would_recommend': 1 if overall_score >= 4 else 0
            })
            survey_id += 1
    return pd.DataFrame(satisfaction)


def save_all_data(output_dir='data/demo'):
    os.makedirs(output_dir, exist_ok=True)

    print('正在生成演示数据...')

    departments_df = generate_departments()
    departments_df.to_csv(f'{output_dir}/departments.csv', index=False, encoding='utf-8-sig')
    print(f'✓ 科室数据: {len(departments_df)} 条')

    doctors_df = generate_doctors(departments_df)
    doctors_df.to_csv(f'{output_dir}/doctors.csv', index=False, encoding='utf-8-sig')
    print(f'✓ 医生数据: {len(doctors_df)} 条')

    registrations_df = generate_registrations(departments_df, doctors_df, 1200)
    registrations_df.to_csv(f'{output_dir}/registrations.csv', index=False, encoding='utf-8-sig')
    print(f'✓ 挂号记录: {len(registrations_df)} 条')

    visits_df = generate_visits(registrations_df)
    visits_df.to_csv(f'{output_dir}/visits.csv', index=False, encoding='utf-8-sig')
    print(f'✓ 就诊记录: {len(visits_df)} 条')

    examinations_df = generate_examinations(visits_df)
    examinations_df.to_csv(f'{output_dir}/examinations.csv', index=False, encoding='utf-8-sig')
    print(f'✓ 检查项目: {len(examinations_df)} 条')

    medications_df = generate_medications(visits_df)
    medications_df.to_csv(f'{output_dir}/medications.csv', index=False, encoding='utf-8-sig')
    print(f'✓ 药品费用: {len(medications_df)} 条')

    waiting_times_df = generate_waiting_times(registrations_df)
    waiting_times_df.to_csv(f'{output_dir}/waiting_times.csv', index=False, encoding='utf-8-sig')
    print(f'✓ 候诊时间: {len(waiting_times_df)} 条')

    satisfaction_df = generate_satisfaction(visits_df)
    satisfaction_df.to_csv(f'{output_dir}/satisfaction.csv', index=False, encoding='utf-8-sig')
    print(f'✓ 满意度调查: {len(satisfaction_df)} 条')

    print(f'\n演示数据已生成完成，保存至: {output_dir}/')
    print(f'就诊记录总数: {len(visits_df)} 条')


if __name__ == '__main__':
    save_all_data()
