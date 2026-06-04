import unittest
import pandas as pd
import os
import sys
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from validation import DataValidator


class TestDataValidator(unittest.TestCase):
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
                'title': ['主任医师', '副主任医师']
            }),
            'registrations': pd.DataFrame({
                'reg_id': ['REG001', 'REG002', 'REG003'],
                'patient_id': ['PAT001', 'PAT002', 'PAT003'],
                'department_id': ['D001', 'D001', 'D002'],
                'department_name': ['内科', '内科', '外科'],
                'doctor_id': ['DOC001', 'DOC001', 'DOC002'],
                'reg_date': ['2024-01-01', '2024-01-02', '2024-01-02'],
                'reg_time': ['08:00:00', '09:00:00', '10:00:00'],
                'patient_type': ['普通门诊', '专家门诊', '普通门诊']
            }),
            'visits': pd.DataFrame({
                'visit_id': ['VIS001', 'VIS002', 'VIS003'],
                'reg_id': ['REG001', 'REG002', 'REG003'],
                'doctor_id': ['DOC001', 'DOC001', 'DOC002'],
                'department_id': ['D001', 'D001', 'D002'],
                'visit_date': ['2024-01-01', '2024-01-02', '2024-01-02'],
                'diagnosis': ['感冒', '发烧', '咳嗽'],
                'total_exam_fee': [100, 200, 150],
                'total_drug_fee': [50, 80, 60],
                'total_fee': [150, 280, 210],
                'has_examination': [1, 1, 0]
            }),
            'examinations': pd.DataFrame({
                'exam_id': ['E001', 'E002'],
                'visit_id': ['VIS001', 'VIS002'],
                'exam_item': ['血常规', 'CT检查'],
                'exam_fee': [50, 200],
                'exam_result': ['正常', '异常']
            }),
            'medications': pd.DataFrame({
                'med_id': ['M001', 'M002', 'M003'],
                'visit_id': ['VIS001', 'VIS002', 'VIS003'],
                'drug_name': ['感冒药A', '退烧药B', '止咳药C'],
                'drug_fee': [30, 50, 40],
                'quantity': [1, 2, 1]
            }),
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
        self.validator = DataValidator(self.test_data)

    def test_validate_fields(self):
        results = self.validator._validate_fields()
        self.assertIn('departments', results)
        self.assertTrue(results['departments']['is_valid'])

    def test_check_missing_values(self):
        results = self.validator._check_missing_values()
        self.assertIn('departments', results)
        self.assertEqual(results['departments']['total_missing'], 0)

    def test_validate_relationships(self):
        issues = self.validator._validate_relationships()
        self.assertIsInstance(issues, list)

    def test_get_summary(self):
        self.validator.validate_all()
        summary = self.validator.get_summary()
        self.assertIn('total_errors', summary)
        self.assertIn('total_warnings', summary)
        self.assertIn('is_valid', summary)

    def test_get_error_count(self):
        self.validator.validate_all()
        self.assertIsInstance(self.validator.get_error_count(), int)

    def test_get_warning_count(self):
        self.validator.validate_all()
        self.assertIsInstance(self.validator.get_warning_count(), int)

    def test_detect_duplicates(self):
        dup_data = self.test_data.copy()
        dup_data['registrations'] = pd.DataFrame({
            'reg_id': ['REG001', 'REG001', 'REG002'],
            'patient_id': ['PAT001', 'PAT002', 'PAT003'],
            'department_id': ['D001', 'D001', 'D002'],
            'doctor_id': ['DOC001', 'DOC001', 'DOC002'],
            'reg_date': ['2024-01-01', '2024-01-01', '2024-01-02'],
            'reg_time': ['08:00:00', '08:30:00', '09:00:00'],
            'patient_type': ['普通门诊', '普通门诊', '专家门诊']
        })
        validator = DataValidator(dup_data)
        results = validator._detect_duplicates()
        self.assertIn('registrations', results)
        self.assertGreater(len(results['registrations']), 0)

    def test_detect_invalid_doctor_ids(self):
        invalid_data = self.test_data.copy()
        invalid_data['registrations'] = pd.DataFrame({
            'reg_id': ['REG001', 'REG002'],
            'patient_id': ['PAT001', 'PAT002'],
            'department_id': ['D001', 'D001'],
            'doctor_id': ['DOC999', 'DOC001'],
            'reg_date': ['2024-01-01', '2024-01-02'],
            'reg_time': ['08:00:00', '09:00:00'],
            'patient_type': ['普通门诊', '专家门诊']
        })
        validator = DataValidator(invalid_data)
        results = validator._detect_invalid_doctor_ids()
        self.assertIn('registrations', results)
        self.assertGreater(len(results['registrations']), 0)

    def test_detect_visit_reg_mismatch(self):
        mismatch_data = self.test_data.copy()
        mismatch_data['visits'] = pd.DataFrame({
            'visit_id': ['VIS001', 'VIS002'],
            'reg_id': ['REG999', 'REG001'],
            'doctor_id': ['DOC999', 'DOC002'],
            'department_id': ['D001', 'D002'],
            'visit_date': ['2024-01-01', '2024-01-02'],
            'diagnosis': ['感冒', '发烧'],
            'total_exam_fee': [100, 200],
            'total_drug_fee': [50, 80],
            'total_fee': [150, 280],
            'has_examination': [1, 1]
        })
        validator = DataValidator(mismatch_data)
        results = validator._detect_visit_reg_mismatch()
        self.assertIn('visits', results)
        self.assertGreater(len(results['visits']), 0)

    def test_detect_fee_anomalies(self):
        anomaly_data = self.test_data.copy()
        anomaly_data['examinations'] = pd.DataFrame({
            'exam_id': ['E001', 'E002'],
            'visit_id': ['VIS001', 'VIS002'],
            'exam_item': ['血常规', 'CT检查'],
            'exam_fee': [-50, 6000],
            'exam_result': ['正常', '异常']
        })
        anomaly_data['medications'] = pd.DataFrame({
            'med_id': ['M001', 'M002'],
            'visit_id': ['VIS001', 'VIS002'],
            'drug_name': ['感冒药A', '退烧药B'],
            'drug_fee': [0, 4000],
            'quantity': [1, 2]
        })
        validator = DataValidator(anomaly_data)
        results = validator._detect_fee_anomalies()
        self.assertIn('examinations', results)
        self.assertIn('medications', results)
        self.assertGreater(len(results['examinations']), 0)
        self.assertGreater(len(results['medications']), 0)

    def test_detect_wait_time_anomalies(self):
        anomaly_data = self.test_data.copy()
        anomaly_data['waiting_times'] = pd.DataFrame({
            'wait_id': ['W001', 'W002', 'W003'],
            'reg_id': ['REG001', 'REG002', 'REG003'],
            'arrival_time': ['08:00:00', '09:00:00', '10:00:00'],
            'call_time': ['08:15:00', '09:25:00', '10:10:00'],
            'wait_minutes': [-5, 200, 15]
        })
        validator = DataValidator(anomaly_data)
        results = validator._detect_wait_time_anomalies()
        self.assertIn('waiting_times', results)
        self.assertGreater(len(results['waiting_times']), 0)

    def test_detect_satisfaction_missing(self):
        missing_data = self.test_data.copy()
        missing_data['satisfaction'] = pd.DataFrame({
            'survey_id': ['S001'],
            'visit_id': ['VIS001'],
            'overall_score': [4],
            'wait_score': [3],
            'service_score': [5]
        })
        validator = DataValidator(missing_data)
        results = validator._detect_satisfaction_missing()
        self.assertIn('total_visits', results)
        self.assertIn('missing_rate', results)
        self.assertGreater(results['missing_rate'], 0)

    def test_get_detailed_issues(self):
        test_data = self.test_data.copy()
        test_data['registrations'] = pd.DataFrame({
            'reg_id': ['REG001', 'REG001'],
            'patient_id': ['PAT001', 'PAT002'],
            'department_id': ['D001', 'D001'],
            'doctor_id': ['DOC999', 'DOC001'],
            'reg_date': ['2024-01-01', '2024-01-01'],
            'reg_time': ['08:00:00', '08:30:00'],
            'patient_type': ['普通门诊', '普通门诊']
        })
        validator = DataValidator(test_data)
        validator.validate_all()
        issues = validator.get_detailed_issues()
        self.assertIsInstance(issues, list)
        if len(issues) > 0:
            issue = issues[0]
            self.assertIn('file_key', issue)
            self.assertIn('row_number', issue)
            self.assertIn('field', issue)
            self.assertIn('issue_type', issue)
            self.assertIn('severity', issue)
            self.assertIn('message', issue)
            self.assertIn('suggestion', issue)


if __name__ == '__main__':
    unittest.main()
