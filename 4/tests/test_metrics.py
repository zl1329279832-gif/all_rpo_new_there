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
                'reg_id': ['REG001', 'REG002', 'REG003'],
                'patient_id': ['PAT001', 'PAT002', 'PAT003'],
                'department_id': ['D001', 'D001', 'D002'],
                'department_name': ['内科', '内科', '外科'],
                'doctor_id': ['DOC001', 'DOC001', 'DOC002'],
                'doctor_name': ['张医生', '张医生', '李医生'],
                'reg_date': pd.to_datetime(['2024-01-01', '2024-01-02', '2024-01-02']),
                'reg_time': ['08:00:00', '09:00:00', '10:00:00'],
                'patient_type': ['普通门诊', '专家门诊', '普通门诊'],
                'year': [2024, 2024, 2024],
                'month': [1, 1, 1]
            }),
            'visits': pd.DataFrame({
                'visit_id': ['VIS001', 'VIS002', 'VIS003'],
                'reg_id': ['REG001', 'REG002', 'REG003'],
                'doctor_id': ['DOC001', 'DOC001', 'DOC002'],
                'doctor_name': ['张医生', '张医生', '李医生'],
                'department_id': ['D001', 'D001', 'D002'],
                'department_name': ['内科', '内科', '外科'],
                'visit_date': pd.to_datetime(['2024-01-01', '2024-01-02', '2024-01-02']),
                'diagnosis': ['感冒', '发烧', '咳嗽'],
                'total_exam_fee': [100, 200, 150],
                'total_drug_fee': [50, 80, 60],
                'total_fee': [150, 280, 210],
                'has_examination': [1, 1, 0]
            }),
            'examinations': None,
            'medications': None,
            'waiting_times': pd.DataFrame({
                'wait_id': ['W001', 'W002', 'W003'],
                'reg_id': ['REG001', 'REG002', 'REG003'],
                'arrival_time': ['08:00:00', '09:00:00', '10:00:00'],
                'call_time': ['08:15:00', '09:25:00', '10:10:00'],
                'wait_minutes': [15, 25, 10]
            }),
            'satisfaction': pd.DataFrame({
                'survey_id': ['S001', 'S002', 'S003'],
                'visit_id': ['VIS001', 'VIS002', 'VIS003'],
                'overall_score': [4, 5, 3],
                'wait_score': [3, 4, 4],
                'service_score': [5, 5, 3],
                'would_recommend': [1, 1, 0]
            })
        }
        self.metrics = MetricsCalculator(self.test_data)

    def test_get_overview_metrics(self):
        result = self.metrics.get_overview_metrics()
        self.assertIsInstance(result, dict)
        self.assertIn('total_registrations', result)
        self.assertEqual(result['total_registrations'], 3)
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


if __name__ == '__main__':
    unittest.main()
