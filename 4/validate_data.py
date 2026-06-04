import pandas as pd
import numpy as np

dept = pd.read_csv('data/demo/departments.csv')
reg = pd.read_csv('data/demo/registrations.csv')
doc = pd.read_csv('data/demo/doctors.csv')
visits = pd.read_csv('data/demo/visits.csv')
med = pd.read_csv('data/demo/medications.csv')
wait = pd.read_csv('data/demo/waiting_times.csv')
sat = pd.read_csv('data/demo/satisfaction.csv')

print('=== 1. daily_capacity ===')
print(dept[['department_name','daily_capacity']].to_string(index=False))

print('\n=== 2. doctors per dept ===')
doc_count = doc.groupby('department_id').size().reset_index(name='doctor_count')
doc_count = doc_count.merge(dept[['department_id','department_name']], on='department_id')
print(doc_count[['department_name','doctor_count']].to_string(index=False))

print('\n=== 3. registration dept distribution ===')
reg_dept = reg.merge(dept[['department_id','department_name']], on='department_id')
dist = reg_dept['department_name'].value_counts(normalize=True)
for name, pct in dist.items():
    print(f'  {name}: {pct*100:.1f}%')

print('\n=== 3b. weekday vs weekend ===')
print(f'  weekday: {(reg.is_weekend==0).sum()} ({(reg.is_weekend==0).mean()*100:.1f}%)')
print(f'  weekend: {(reg.is_weekend==1).sum()} ({(reg.is_weekend==1).mean()*100:.1f}%)')

print('\n=== 3c. workday 8-10 peak (of total) ===')
workday = reg[reg.is_weekend==0]
peak_all = reg[(reg.is_weekend==0) & (reg.reg_time.str[:2].astype(int).between(8,10))]
print(f'  total records: {len(reg)}')
print(f'  workday 8-10 records: {len(peak_all)} ({len(peak_all)/len(reg)*100:.1f}%)')

print('\n=== 4. examination rate ===')
exam_rate = visits.groupby('department_id')['has_examination'].mean()
exam_rate = exam_rate.reset_index().merge(dept[['department_id','department_name']], on='department_id')
for _, row in exam_rate.iterrows():
    print(f'  {row.department_name}: {row.has_examination*100:.1f}%')

print('\n=== 5. drug abnormal price ===')
abnormal_visit_count = 0
total_med_count = 0
for vn, vg in med.groupby('visit_id'):
    total_med_count += len(vg)
    max_price = vg['unit_price'].max()
    drug_names = vg['drug_name'].tolist()
    for _, row in vg.iterrows():
        cat = row['drug_category']
        dn = row['drug_name']
        uf = row['unit_price']
        if cat == '抗生素':
            normal_max = 60
        elif cat == '解热镇痛':
            normal_max = 25
        elif cat == '消化系统':
            normal_max = 40
        elif cat == '呼吸系统':
            normal_max = 45
        elif cat == '心血管':
            normal_max = 70
        elif cat == '维生素':
            normal_max = 50
        elif cat == '外用药':
            normal_max = 30
        else:
            normal_max = 100
        if uf > normal_max * 2.5:
            abnormal_visit_count += 1
            break
print(f'  visits with abnormal drug price: {abnormal_visit_count} / {med.visit_id.nunique()} ({abnormal_visit_count/med.visit_id.nunique()*100:.1f}%)')

print('\n=== 6. wait time ===')
high_depts = ['D001', 'D010']
high_reg = reg[reg.department_id.isin(high_depts)]
high_workday_peak = high_reg[(high_reg.is_weekend==0) & (high_reg.reg_time.str[:2].astype(int).between(8,10))]
high_workday_nonpeak = high_reg[(high_reg.is_weekend==0) & ~high_reg.reg_time.str[:2].astype(int).between(8,10)]
other_reg = reg[~reg.department_id.isin(high_depts)]

high_peak_wait = wait[wait.reg_id.isin(high_workday_peak.reg_id)]
high_nonpeak_wait = wait[wait.reg_id.isin(high_workday_nonpeak.reg_id)]
other_wait = wait[wait.reg_id.isin(other_reg.reg_id)]
print(f'  high-load peak avg wait: {high_peak_wait.wait_minutes.mean():.0f} min (count: {len(high_peak_wait)})')
print(f'  high-load non-peak workday avg: {high_nonpeak_wait.wait_minutes.mean():.0f} min')
print(f'  other dept avg wait: {other_wait.wait_minutes.mean():.0f} min')
print(f'  records with wait>60: {(wait.wait_minutes>60).sum()} / {len(wait)} ({(wait.wait_minutes>60).mean()*100:.1f}%)')

print('\n=== 7. satisfaction ===')
low_sat = sat[sat.overall_score <= 2]
print(f'  low satisfaction ratio: {len(low_sat)/len(sat)*100:.1f}%')
print(f'  score distribution: {sat.overall_score.value_counts().sort_index().to_dict()}')
