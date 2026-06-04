import unittest
import pandas as pd
import os
import sys
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from metrics import MetricsCalculator


class TestMetricsCalculator(unittest.TestCase):
    def setUp(self):
        self.test_data = {
            'departments': pd.DataFrame({
                'department_id': ['D001', 'D002'],
                'department_name': ['内科', '外科'],
                'type': ['临床', '临床']
            }),
            'doctors': pd.DataFrame({
                'doctor_id': ['DOC001', 'DOC002'],
                'doctor_name': ['张医生', '李医生'],
                'department_id': ['D001', 'D002'],
                'department_name': ['内科', '外科'],
                'title': ['主任医师', '副主任医师']
            }),
            'registrations': pd.DataFrame({
                'reg_id': ['REG001', 'REG002', 'REG003', 'REG004', 'REG005', 'REG006'],
                'patient_id': ['PAT001', 'PAT002', 'PAT003', 'PAT004', 'PAT005', 'PAT006'],
                'department_id': ['D001', 'D001', 'D002', 'D001', 'D002', 'D001'],
                'department_name': ['内科', '内科', '外科', '内科', '外科', '内科'],
                'doctor_id': ['DOC001', 'DOC001', 'DOC002', 'DOC001', 'DOC002', 'DOC001'],
                'doctor_name': ['张医生', '张医生', '李医生', '张医生', '李医生', '张医生'],
                'reg_date': pd.to_datetime(['2024-01-01', '2024-01-02', '2024-01-02', '2024-02-01', '2024-02-01', '2024-02-02']),
                'reg_time': ['08:00:00', '09:00:00', '10:00:00', '08:30:00', '09:30:00', '10:30:00'],
                'patient_type': ['普通门诊', '专家门诊', '普通门诊', '普通门诊', '专家门诊', '普通门诊'],
                'year': [2024, 2024, 2024, 2024, 2024, 2024],
                'month': [1, 1, 1, 2, 2, 2],
                'is_weekend': [0, 0, 0, 0, 0, 0]
            }),
            'visits': pd.DataFrame({
                'visit_id': ['VIS001', 'VIS002', 'VIS003', 'VIS004', 'VIS005', 'VIS006'],
                'reg_id': ['REG001', 'REG002', 'REG003', 'REG004', 'REG005', 'REG006'],
                'doctor_id': ['DOC001', 'DOC001', 'DOC002', 'DOC001', 'DOC002', 'DOC001'],
                'doctor_name': ['张医生', '张医生', '李医生', '张医生', '李医生', '张医生'],
                'department_id': ['D001', 'D001', 'D002', 'D001', 'D002', 'D001'],
                'department_name': ['内科', '内科', '外科', '内科', '外科', '内科'],
                'visit_date': pd.to_datetime(['2024-01-01', '2024-01-02', '2024-01-02', '2024-02-01', '2024-02-01', '2024-02-02']),
                'diagnosis': ['感冒', '发烧', '咳嗽', '头痛', '扭伤', '胃炎'],
                'total_exam_fee': [100, 200, 150, 120, 180, 90],
                'total_drug_fee': [50, 80, 60, 70, 90, 40],
                'total_fee': [150, 280, 210, 190, 270, 130],
                'has_examination': [1, 1, 0, 1, 1, 0],
                'year': [2024, 2024, 2024, 2024, 2024, 2024],
                'month': [1, 1, 1, 2, 2, 2]
            }),
            'examinations': pd.DataFrame({
                'exam_id': ['E001', 'E002', 'E003', 'E004'],
                'visit_id': ['VIS001', 'VIS002', 'VIS004', 'VIS005'],
                'exam_item': ['血常规', 'CT检查', 'X光', 'MRI'],
                'exam_fee': [50, 200, 120, 180],
                'exam_result': ['正常', '异常', '正常', '异常']
            }),
            'medications': pd.DataFrame({
                'med_id': ['M001', 'M002', 'M003', 'M004', 'M005', 'M006'],
                'visit_id': ['VIS001', 'VIS002', 'VIS003', 'VIS004', 'VIS005', 'VIS006'],
                'drug_name': ['感冒药A', '退烧药B', '止咳药C', '头痛药D', '止痛药E', '胃药F'],
                'drug_category': ['感冒用药', '退烧用药', '止咳用药', '止痛用药', '止痛用药', '胃药'],
                'drug_fee': [30, 50, 40, 35, 60, 25],
                'quantity': [1, 2, 1, 1, 2, 1]
            }),
            'waiting_times': pd.DataFrame({
                'wait_id': ['W001', 'W002', 'W003', 'W004', 'W005', 'W006'],
                'reg_id': ['REG001', 'REG002', 'REG003', 'REG004', 'REG005', 'REG006'],
                'arrival_time': ['08:00:00', '09:00:00', '10:00:00', '08:30:00', '09:30:00', '10:30:00'],
                'call_time': ['08:15:00', '09:35:00', '10:20:00', '09:00:00', '10:05:00', '11:00:00'],
                'wait_minutes': [15, 35, 20, 30, 35, 30]
            }),
            'satisfaction': pd.DataFrame({
                'survey_id': ['S001', 'S002', 'S003', 'S004', 'S005', 'S006'],
                'visit_id': ['VIS001', 'VIS002', 'VIS003', 'VIS004', 'VIS005', 'VIS006'],
                'overall_score': [4, 3, 4, 5, 3, 4],
                'wait_score': [3, 2, 4, 5, 2, 3],
                'service_score': [5, 4, 4, 5, 4, 5],
                'would_recommend': [1, 0, 1, 1, 0, 1]
            })
        }
        self.metrics = MetricsCalculator(self.test_data)

    def test_get_overview_metrics(self):
        result = self.metrics.get_overview_metrics()
        self.assertIsInstance(result, dict)
        self.assertIn('total_registrations', result)
        self.assertEqual(result['total_registrations'], 6)
        self.assertIn('unique_patients', result)
        self.assertIn('total_revenue', result)

    def test_get_daily_trends(self):
        result = self.metrics.get_daily_trends()
        self.assertIsNotNone(result)
        self.assertIsInstance(result, pd.DataFrame)
        self.assertIn('date', result.columns)
        self.assertIn('registrations', result.columns)

    def test_get_department_metrics(self):
        result = self.metrics.get_department_metrics()
        self.assertIsNotNone(result)
        self.assertIsInstance(result, pd.DataFrame)
        self.assertIn('department_name', result.columns)
        self.assertIn('total_registrations', result.columns)

    def test_get_doctor_metrics(self):
        result = self.metrics.get_doctor_metrics()
        self.assertIsNotNone(result)
        self.assertIsInstance(result, pd.DataFrame)
        self.assertIn('doctor_name', result.columns)
        self.assertIn('total_visits', result.columns)

    def test_get_fee_structure(self):
        result = self.metrics.get_fee_structure()
        self.assertIsInstance(result, dict)
        self.assertIn('total_revenue', result)
        self.assertIn('exam_revenue', result)
        self.assertIn('drug_revenue', result)

    def test_get_exam_conversion_rate(self):
        result = self.metrics.get_exam_conversion_rate()
        self.assertIsInstance(result, dict)
        self.assertIn('total_visits', result)
        self.assertIn('visits_with_exam', result)
        self.assertIn('conversion_rate', result)

    def test_get_satisfaction_distribution(self):
        result = self.metrics.get_satisfaction_distribution()
        self.assertIsInstance(result, dict)
        self.assertIn('avg_scores', result)

    def test_get_monthly_trends(self):
        result = self.metrics.get_monthly_trends()
        self.assertIsNotNone(result)
        self.assertIsInstance(result, pd.DataFrame)

    def test_get_mom_changes(self):
        result = self.metrics.get_mom_changes()
        self.assertIsInstance(result, dict)
        self.assertIn('挂号量环比', result)
        self.assertIn('就诊量环比', result)
        self.assertIn('收入环比', result)
        self.assertIn('候诊时间环比', result)
        self.assertIn('满意度环比', result)
        self.assertIsInstance(result['挂号量环比'], list)
        if len(result['挂号量环比']) > 0:
            self.assertIn('月份', result['挂号量环比'][0])
            self.assertIn('数量', result['挂号量环比'][0])
            self.assertIn('环比变化率', result['挂号量环比'][0])

    def test_get_weekday_weekend_comparison(self):
        result = self.metrics.get_weekday_weekend_comparison()
        self.assertIsInstance(result, dict)
        self.assertIn('挂号量', result)
        self.assertIn('就诊量', result)
        self.assertIn('候诊时间', result)
        self.assertIn('满意度', result)
        self.assertIn('工作日', result['挂号量'])
        self.assertIn('周末', result['挂号量'])

    def test_get_peak_hours(self):
        result = self.metrics.get_peak_hours()
        self.assertIsNotNone(result)
        self.assertIsInstance(result, pd.DataFrame)
        self.assertIn('hour', result.columns)
        self.assertIn('registrations', result.columns)
        self.assertIn('is_peak', result.columns)

    def test_get_department_capacity_utilization(self):
        result = self.metrics.get_department_capacity_utilization()
        self.assertIsNotNone(result)
        self.assertIsInstance(result, pd.DataFrame)
        self.assertIn('department_name', result.columns)
        self.assertIn('capacity', result.columns)
        self.assertIn('actual_visits', result.columns)
        self.assertIn('utilization_rate', result.columns)

    def test_get_doctor_workload_balance(self):
        result = self.metrics.get_doctor_workload_balance()
        self.assertIsInstance(result, dict)
        self.assertIn('基尼系数', result)
        self.assertIn('变异系数', result)
        self.assertIn('最高负荷最低负荷比', result)
        self.assertIn('超负荷医生占比', result)
        self.assertIn('超负荷医生人数', result)
        self.assertIn('平均负荷', result)
        self.assertIn('超负荷阈值', result)

    def test_get_wait_time_stratification(self):
        result = self.metrics.get_wait_time_stratification()
        self.assertIsInstance(result, dict)
        self.assertIn('小于15分钟', result)
        self.assertIn('15至30分钟', result)
        self.assertIn('30至60分钟', result)
        self.assertIn('大于60分钟', result)
        self.assertIn('人数', result['小于15分钟'])
        self.assertIn('占比', result['小于15分钟'])

    def test_get_anomaly_cause_analysis(self):
        result = self.metrics.get_anomaly_cause_analysis()
        self.assertIsInstance(result, list)
        if len(result) > 0:
            anomaly = result[0]
            self.assertIn('科室名称', anomaly)
            self.assertIn('异常类型', anomaly)
            self.assertIn('可能原因', anomaly)
            self.assertIn('关键指标', anomaly)
            self.assertIsInstance(anomaly['可能原因'], list)


if __name__ == '__main__':
    unittest.main()
