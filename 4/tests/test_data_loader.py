import unittest
import pandas as pd
import os
import sys
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from ingestion import DataLoader


class TestDataLoader(unittest.TestCase):
    def setUp(self):
        self.loader = DataLoader()

    def test_initialization(self):
        self.assertIsInstance(self.loader.data, dict)
        self.assertIsInstance(self.loader.load_status, dict)
        self.assertEqual(len(self.loader.data), 8)
        self.assertEqual(len(self.loader.load_status), 8)

    def test_required_files(self):
        required = DataLoader.REQUIRED_FILES
        self.assertIn('departments', required)
        self.assertIn('doctors', required)
        self.assertIn('registrations', required)
        self.assertIn('visits', required)
        self.assertIn('examinations', required)
        self.assertIn('medications', required)
        self.assertIn('waiting_times', required)
        self.assertIn('satisfaction', required)

    def test_get_missing_files(self):
        missing = self.loader.get_missing_files()
        self.assertEqual(len(missing), 8)

    def test_get_loaded_files(self):
        loaded = self.loader.get_loaded_files()
        self.assertEqual(len(loaded), 0)

    def test_is_complete(self):
        self.assertFalse(self.loader.is_complete())

    def test_get_row_count_empty(self):
        self.assertEqual(self.loader.get_row_count('departments'), 0)


if __name__ == '__main__':
    unittest.main()
