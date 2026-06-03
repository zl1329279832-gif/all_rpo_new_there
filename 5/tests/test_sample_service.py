import unittest
import sys
import os
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent.parent))

from models.sample import Sample
from services.sample_service import SampleService
from services.database_service import DatabaseService


class TestSampleService(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.db_service = DatabaseService()
        cls.db_service.initialize_database()
        cls.sample_service = SampleService()

    def setUp(self):
        pass

    def test_create_sample(self):
        sample = Sample(
            sample_no="TEST001",
            sample_name="测试样品",
            source_unit="测试单位",
            sender="测试人",
            receiver="接收人",
            receive_time="2024-01-01 10:00:00",
            test_items="理化检测",
            status="待检测",
            description="测试描述"
        )

        sample_id = self.sample_service.create_sample(sample)
        self.assertIsNotNone(sample_id)
        self.assertGreater(sample_id, 0)

    def test_duplicate_sample_no(self):
        sample = Sample(
            sample_no="TEST002",
            sample_name="测试样品2",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        self.sample_service.create_sample(sample)

        sample2 = Sample(
            sample_no="TEST002",
            sample_name="测试样品重复",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        with self.assertRaises(ValueError):
            self.sample_service.create_sample(sample2)

    def test_get_sample_by_id(self):
        sample = Sample(
            sample_no="TEST003",
            sample_name="查询测试样品",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        sample_id = self.sample_service.create_sample(sample)

        found = self.sample_service.get_sample_by_id(sample_id)
        self.assertIsNotNone(found)
        self.assertEqual(found.sample_no, "TEST003")
        self.assertEqual(found.sample_name, "查询测试样品")

    def test_update_sample(self):
        sample = Sample(
            sample_no="TEST004",
            sample_name="更新测试样品",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        sample_id = self.sample_service.create_sample(sample)

        found = self.sample_service.get_sample_by_id(sample_id)
        found.sample_name = "更新后的样品名称"
        self.sample_service.update_sample(found)

        updated = self.sample_service.get_sample_by_id(sample_id)
        self.assertEqual(updated.sample_name, "更新后的样品名称")

    def test_update_status(self):
        sample = Sample(
            sample_no="TEST005",
            sample_name="状态测试样品",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        sample_id = self.sample_service.create_sample(sample)

        self.sample_service.update_status(sample_id, "检测中")
        updated = self.sample_service.get_sample_by_id(sample_id)
        self.assertEqual(updated.status, "检测中")

    def test_delete_sample(self):
        sample = Sample(
            sample_no="TEST006",
            sample_name="删除测试样品",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        sample_id = self.sample_service.create_sample(sample)

        self.sample_service.delete_sample(sample_id)
        found = self.sample_service.get_sample_by_id(sample_id)
        self.assertIsNone(found)

    def test_sample_no_exists(self):
        sample = Sample(
            sample_no="TEST007",
            sample_name="存在测试样品",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        sample_id = self.sample_service.create_sample(sample)

        self.assertTrue(self.sample_service.sample_no_exists("TEST007"))
        self.assertFalse(self.sample_service.sample_no_exists("NOT_EXIST"))
        self.assertFalse(self.sample_service.sample_no_exists("TEST007", exclude_id=sample_id))


if __name__ == '__main__':
    unittest.main()
