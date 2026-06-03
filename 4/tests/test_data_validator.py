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
                'reg_id': ['REG001'],
                'patient_id': ['PAT001'],
                'department_id': ['D001'],
                'doctor_id': ['DOC001'],
                'reg_date': ['2024-01-01'],
                'reg_time': ['08:00:00'],
                'patient_type': ['普通门诊']
            }),
            'visits': None,
            'examinations': None,
            'medications': None,
            'waiting_times': None,
            'satisfaction': None
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


if __name__ == '__main__':
    unittest.main()
