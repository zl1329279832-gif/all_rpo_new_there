import unittest
import sys
import os
import tempfile
from pathlib import Path
from datetime import datetime, timedelta

sys.path.insert(0, str(Path(__file__).parent.parent))

from models.sample import Sample
from models.test_record import TestRecord
from models.attachment import Attachment
from services.sample_service import SampleService
from services.test_record_service import TestRecordService
from services.attachment_service import AttachmentService
from services.database_service import DatabaseService
from config.settings import STATUS_TRANSITION_RULES


class TestBoundaryScenarios(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.db_service = DatabaseService()
        cls.db_service.initialize_database()
        cls.sample_service = SampleService()
        cls.test_record_service = TestRecordService()
        cls.attachment_service = AttachmentService()

    def setUp(self):
        self.db_service.clear_all_data()

    def test_duplicate_sample_no(self):
        sample = Sample(
            sample_no="BOUNDARY_DUP_001",
            sample_name="重复编号测试",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        self.sample_service.create_sample(sample)

        sample2 = Sample(
            sample_no="BOUNDARY_DUP_001",
            sample_name="重复编号样品2",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        with self.assertRaises(ValueError) as context:
            self.sample_service.create_sample(sample2)
        self.assertIn("已存在", str(context.exception))

    def test_duplicate_sample_no_update(self):
        sample1 = Sample(
            sample_no="BOUNDARY_DUP_002",
            sample_name="样品1",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        id1 = self.sample_service.create_sample(sample1)

        sample2 = Sample(
            sample_no="BOUNDARY_DUP_003",
            sample_name="样品2",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        id2 = self.sample_service.create_sample(sample2)

        found = self.sample_service.get_sample_by_id(id2)
        found.sample_no = "BOUNDARY_DUP_002"
        with self.assertRaises(ValueError) as context:
            self.sample_service.update_sample(found)
        self.assertIn("已存在", str(context.exception))

    def test_update_sample_no_to_same(self):
        sample = Sample(
            sample_no="BOUNDARY_DUP_004",
            sample_name="测试样品",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        sample_id = self.sample_service.create_sample(sample)

        found = self.sample_service.get_sample_by_id(sample_id)
        found.sample_name = "更新名称"
        result = self.sample_service.update_sample(found)
        self.assertTrue(result)

    def test_empty_sample_no(self):
        sample = Sample(
            sample_no="",
            sample_name="空编号测试",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        with self.assertRaises(ValueError) as context:
            self.sample_service.create_sample(sample)
        self.assertIn("样品编号不能为空", str(context.exception))

    def test_empty_sample_name(self):
        sample = Sample(
            sample_no="BOUNDARY_EMPTY_001",
            sample_name="",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        with self.assertRaises(ValueError) as context:
            self.sample_service.create_sample(sample)
        self.assertIn("样品名称不能为空", str(context.exception))

    def test_empty_source_unit(self):
        sample = Sample(
            sample_no="BOUNDARY_EMPTY_002",
            sample_name="测试样品",
            source_unit="",
            receive_time="2024-01-01 10:00:00"
        )
        with self.assertRaises(ValueError) as context:
            self.sample_service.create_sample(sample)
        self.assertIn("来源单位不能为空", str(context.exception))

    def test_status_transition_all_valid(self):
        sample = Sample(
            sample_no="BOUNDARY_STATUS_001",
            sample_name="状态流转测试",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00",
            status="待检测"
        )
        sample_id = self.sample_service.create_sample(sample)

        self.sample_service.update_status(sample_id, "检测中")
        sample = self.sample_service.get_sample_by_id(sample_id)
        self.assertEqual(sample.status, "检测中")

        self.sample_service.update_status(sample_id, "检测完成")
        sample = self.sample_service.get_sample_by_id(sample_id)
        self.assertEqual(sample.status, "检测完成")

        self.sample_service.update_status(sample_id, "报告已生成")
        sample = self.sample_service.get_sample_by_id(sample_id)
        self.assertEqual(sample.status, "报告已生成")

        self.sample_service.update_status(sample_id, "已归档")
        sample = self.sample_service.get_sample_by_id(sample_id)
        self.assertEqual(sample.status, "已归档")

    def test_status_transition_same_status(self):
        sample = Sample(
            sample_no="BOUNDARY_STATUS_002",
            sample_name="相同状态测试",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00",
            status="待检测"
        )
        sample_id = self.sample_service.create_sample(sample)

        result = self.sample_service.update_status(sample_id, "待检测")
        self.assertTrue(result)

        sample = self.sample_service.get_sample_by_id(sample_id)
        self.assertEqual(sample.status, "待检测")

    def test_can_transition_status_all_combinations(self):
        statuses = ["待检测", "检测中", "检测完成", "报告已生成", "已归档", "已作废"]

        for current_status in statuses:
            allowed = STATUS_TRANSITION_RULES.get(current_status, [])
            for new_status in statuses:
                can_trans, message = self.sample_service.can_transition_status(current_status, new_status)
                if current_status == new_status:
                    self.assertTrue(can_trans, f"从{current_status}到{new_status}应该允许（未变更）")
                elif new_status in allowed:
                    self.assertTrue(can_trans, f"从{current_status}到{new_status}应该允许")
                else:
                    self.assertFalse(can_trans, f"从{current_status}到{new_status}应该不允许")

    def test_get_allowed_transitions(self):
        transitions = self.sample_service.get_allowed_transitions("待检测")
        self.assertEqual(transitions, ["检测中", "已作废"])

        transitions = self.sample_service.get_allowed_transitions("已作废")
        self.assertEqual(transitions, [])

        transitions = self.sample_service.get_allowed_transitions("未知状态")
        self.assertEqual(transitions, [])

    def test_test_record_missing_fields(self):
        record = TestRecord(
            sample_id=0,
            test_item="",
            tester=""
        )
        with self.assertRaises(ValueError) as context:
            self.test_record_service.create_test_record(record)
        self.assertIn("样品ID不能为空", str(context.exception))

        record.sample_id = 99999
        record.test_item = ""
        with self.assertRaises(ValueError) as context:
            self.test_record_service.create_test_record(record)
        self.assertIn("检测项目不能为空", str(context.exception))

        record.test_item = "理化检测"
        record.tester = ""
        with self.assertRaises(ValueError) as context:
            self.test_record_service.create_test_record(record)
        self.assertIn("检测人员不能为空", str(context.exception))

    def test_test_record_update_without_reason(self):
        sample = Sample(
            sample_no="BOUNDARY_TEST_001",
            sample_name="检测记录测试",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        sample_id = self.sample_service.create_sample(sample)

        record = TestRecord(
            sample_id=sample_id,
            test_item="理化检测",
            tester="张三",
            test_result="合格"
        )
        record_id = self.test_record_service.create_test_record(record)

        found = self.test_record_service.get_test_record_by_id(record_id)
        found.test_result = "不合格"
        with self.assertRaises(ValueError) as context:
            self.test_record_service.update_test_record(found, change_reason="")
        self.assertIn("修改原因不能为空", str(context.exception))

    def test_test_record_change_logging(self):
        sample = Sample(
            sample_no="BOUNDARY_TEST_002",
            sample_name="修改记录测试",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        sample_id = self.sample_service.create_sample(sample)

        record = TestRecord(
            sample_id=sample_id,
            test_item="理化检测",
            tester="张三",
            test_result="合格",
            result_value="100",
            standard_value="≥90"
        )
        record_id = self.test_record_service.create_test_record(record)

        found = self.test_record_service.get_test_record_by_id(record_id)
        found.test_result = "不合格"
        found.result_value = "80"
        self.test_record_service.update_test_record(
            found,
            operator="李四",
            change_reason="复检结果更正",
            change_remark="仪器重新校准后检测"
        )

        changes = self.test_record_service.get_changes_by_test_record_id(record_id)
        self.assertEqual(len(changes), 2)

        field_names = [c.field_name for c in changes]
        self.assertIn("检测结果", field_names)
        self.assertIn("结果值", field_names)

        for change in changes:
            self.assertEqual(change.change_reason, "复检结果更正")
            self.assertEqual(change.operator, "李四")
            self.assertEqual(change.remarks, "仪器重新校准后检测")

    def test_can_generate_report_incomplete(self):
        sample = Sample(
            sample_no="BOUNDARY_REPORT_001",
            sample_name="报告测试",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        sample_id = self.sample_service.create_sample(sample)

        can_generate, message = self.test_record_service.can_generate_report(sample_id)
        self.assertFalse(can_generate)
        self.assertIn("没有检测记录", message)

        record = TestRecord(
            sample_id=sample_id,
            test_item="理化检测",
            tester="张三",
            test_result="检测中"
        )
        self.test_record_service.create_test_record(record)

        can_generate, message = self.test_record_service.can_generate_report(sample_id)
        self.assertFalse(can_generate)
        self.assertIn("尚未完成", message)

    def test_can_generate_report_complete(self):
        sample = Sample(
            sample_no="BOUNDARY_REPORT_002",
            sample_name="报告完成测试",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        sample_id = self.sample_service.create_sample(sample)

        record1 = TestRecord(
            sample_id=sample_id,
            test_item="理化检测",
            tester="张三",
            test_result="合格"
        )
        self.test_record_service.create_test_record(record1)

        record2 = TestRecord(
            sample_id=sample_id,
            test_item="微生物检测",
            tester="李四",
            test_result="不合格"
        )
        self.test_record_service.create_test_record(record2)

        can_generate, message = self.test_record_service.can_generate_report(sample_id)
        self.assertTrue(can_generate)
        self.assertIn("已完成", message)

    def test_attachment_validate_path(self):
        with tempfile.NamedTemporaryFile(delete=False, suffix='.txt') as f:
            f.write(b"test content")
            temp_path = f.name

        try:
            valid, message = self.attachment_service.validate_attachment_path(temp_path)
            self.assertTrue(valid)
            self.assertEqual(message, "文件路径有效")
        finally:
            os.unlink(temp_path)

        nonexistent_abs_path = str(Path.cwd() / "nonexistent_file.txt")
        valid, message = self.attachment_service.validate_attachment_path(nonexistent_abs_path)
        self.assertFalse(valid)
        self.assertIn("不存在", message)

        valid, message = self.attachment_service.validate_attachment_path(str(Path(temp_path).parent))
        self.assertFalse(valid)
        self.assertIn("不是文件", message)

        with tempfile.NamedTemporaryFile(delete=False, suffix='.txt') as f:
            empty_path = f.name

        try:
            valid, message = self.attachment_service.validate_attachment_path(empty_path)
            self.assertFalse(valid)
            self.assertIn("为空", message)
        finally:
            os.unlink(empty_path)

    def test_attachment_missing_detection(self):
        sample = Sample(
            sample_no="BOUNDARY_ATTACH_001",
            sample_name="附件测试",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        sample_id = self.sample_service.create_sample(sample)

        with tempfile.NamedTemporaryFile(delete=False, suffix='.txt') as f:
            f.write(b"test content")
            temp_path = f.name

        try:
            attachment = self.attachment_service.upload_attachment(
                sample_id=sample_id,
                source_path=temp_path,
                uploaded_by="测试员"
            )

            existing, missing = self.attachment_service.check_attachments_exist(sample_id)
            self.assertEqual(len(existing), 1)
            self.assertEqual(len(missing), 0)

            os.unlink(attachment.file_path)

            existing, missing = self.attachment_service.check_attachments_exist(sample_id)
            self.assertEqual(len(existing), 0)
            self.assertEqual(len(missing), 1)

            missing_all = self.attachment_service.get_missing_attachments()
            self.assertGreaterEqual(len(missing_all), 1)
        finally:
            os.unlink(temp_path)

    def test_attachment_rebind(self):
        sample = Sample(
            sample_no="BOUNDARY_ATTACH_002",
            sample_name="附件重新绑定测试",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        sample_id = self.sample_service.create_sample(sample)

        with tempfile.NamedTemporaryFile(delete=False, suffix='.txt') as f:
            f.write(b"original content")
            original_path = f.name

        with tempfile.NamedTemporaryFile(delete=False, suffix='.txt') as f:
            f.write(b"new content")
            new_path = f.name

        try:
            attachment = self.attachment_service.upload_attachment(
                sample_id=sample_id,
                source_path=original_path,
                uploaded_by="测试员"
            )

            old_file_path = attachment.file_path
            self.assertTrue(Path(old_file_path).exists())

            rebinded = self.attachment_service.rebind_attachment(attachment.id, new_path)

            self.assertEqual(rebinded.file_name, Path(new_path).name)
            self.assertTrue(Path(rebinded.file_path).exists())
            self.assertNotEqual(rebinded.file_path, old_file_path)
            self.assertFalse(Path(old_file_path).exists())
        finally:
            os.unlink(original_path)
            os.unlink(new_path)

    def test_statistics_empty_database(self):
        stats = self.sample_service.get_statistics()
        self.assertEqual(stats['total'], 0)
        self.assertEqual(stats['待检测'], 0)
        self.assertEqual(stats['completion_rate'], 0.0)
        self.assertEqual(stats['abnormal'], 0)
        self.assertEqual(stats['tester_workload'], [])

    def test_statistics_with_data(self):
        for i in range(5):
            sample = Sample(
                sample_no=f"BOUNDARY_STAT_{i:03d}",
                sample_name=f"统计测试{i}",
                source_unit="测试单位",
                receive_time="2024-01-01 10:00:00",
                status="待检测" if i < 3 else "检测完成"
            )
            sample_id = self.sample_service.create_sample(sample)

            if i >= 3:
                record = TestRecord(
                    sample_id=sample_id,
                    test_item="理化检测",
                    tester="张三" if i == 3 else "李四",
                    test_result="合格"
                )
                self.test_record_service.create_test_record(record)

        stats = self.sample_service.get_statistics()
        self.assertEqual(stats['total'], 5)
        self.assertEqual(stats['待检测'], 3)
        self.assertEqual(stats['检测完成'], 2)
        self.assertEqual(stats['待检测_rate'], 60.0)
        self.assertEqual(stats['检测完成_rate'], 40.0)
        self.assertEqual(stats['completed'], 2)
        self.assertEqual(stats['completion_rate'], 40.0)
        self.assertEqual(len(stats['tester_workload']), 2)

    def test_get_samples_pagination(self):
        for i in range(25):
            sample = Sample(
                sample_no=f"BOUNDARY_PAGE_{i:03d}",
                sample_name=f"分页测试{i}",
                source_unit="测试单位",
                receive_time="2024-01-01 10:00:00"
            )
            self.sample_service.create_sample(sample)

        page1 = self.sample_service.get_all_samples(limit=10, offset=0)
        self.assertEqual(len(page1), 10)

        page2 = self.sample_service.get_all_samples(limit=10, offset=10)
        self.assertEqual(len(page2), 10)

        page3 = self.sample_service.get_all_samples(limit=10, offset=20)
        self.assertEqual(len(page3), 5)

        count = self.sample_service.count_samples()
        self.assertEqual(count, 25)

    def test_search_filters(self):
        sample1 = Sample(
            sample_no="SEARCH_001",
            sample_name="饮用水",
            source_unit="自来水公司",
            receive_time="2024-01-15 10:00:00",
            status="待检测"
        )
        self.sample_service.create_sample(sample1)

        sample2 = Sample(
            sample_no="SEARCH_002",
            sample_name="土壤样本",
            source_unit="农业局",
            receive_time="2024-02-20 10:00:00",
            status="检测完成"
        )
        self.sample_service.create_sample(sample2)

        results = self.sample_service.get_all_samples(keyword="饮用水")
        self.assertEqual(len(results), 1)
        self.assertEqual(results[0].sample_no, "SEARCH_001")

        results = self.sample_service.get_all_samples(keyword="自来水")
        self.assertEqual(len(results), 1)

        results = self.sample_service.get_all_samples(status="检测完成")
        self.assertEqual(len(results), 1)
        self.assertEqual(results[0].sample_no, "SEARCH_002")

        results = self.sample_service.get_all_samples(start_date="2024-01-01", end_date="2024-01-31")
        self.assertEqual(len(results), 1)

        results = self.sample_service.get_all_samples(keyword="不存在")
        self.assertEqual(len(results), 0)

    def test_delete_nonexistent_sample(self):
        with self.assertRaises(ValueError) as context:
            self.sample_service.delete_sample(999999)
        self.assertIn("样品不存在", str(context.exception))

    def test_update_nonexistent_sample(self):
        sample = Sample(
            sample_no="BOUNDARY_UPDATE_001",
            sample_name="测试",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        sample.id = 999999
        with self.assertRaises(ValueError) as context:
            self.sample_service.update_sample(sample)
        self.assertIn("样品不存在", str(context.exception))

    def test_get_nonexistent_sample(self):
        result = self.sample_service.get_sample_by_id(999999)
        self.assertIsNone(result)

    def test_attachment_upload_nonexistent_file(self):
        sample = Sample(
            sample_no="BOUNDARY_ATTACH_003",
            sample_name="附件上传测试",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        sample_id = self.sample_service.create_sample(sample)

        with self.assertRaises(FileNotFoundError):
            self.attachment_service.upload_attachment(
                sample_id=sample_id,
                source_path="nonexistent_file.txt"
            )

    def test_get_status_description(self):
        desc = self.sample_service.get_status_description("待检测")
        self.assertIn("等待开始检测", desc)

        desc = self.sample_service.get_status_description("已作废")
        self.assertIn("流程终止", desc)

        desc = self.sample_service.get_status_description("未知状态")
        self.assertEqual(desc, "")

    def test_sample_no_exists_with_exclude(self):
        sample = Sample(
            sample_no="BOUNDARY_EXISTS_001",
            sample_name="存在测试",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        sample_id = self.sample_service.create_sample(sample)

        self.assertTrue(self.sample_service.sample_no_exists("BOUNDARY_EXISTS_001"))
        self.assertFalse(self.sample_service.sample_no_exists("BOUNDARY_EXISTS_001", exclude_id=sample_id))
        self.assertFalse(self.sample_service.sample_no_exists("BOUNDARY_EXISTS_002"))

    def test_check_sample_test_complete(self):
        sample = Sample(
            sample_no="BOUNDARY_CHECK_001",
            sample_name="完成检查测试",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        sample_id = self.sample_service.create_sample(sample)

        self.assertFalse(self.test_record_service.check_sample_test_complete(sample_id))

        record1 = TestRecord(
            sample_id=sample_id,
            test_item="理化检测",
            tester="张三",
            test_result="合格"
        )
        self.test_record_service.create_test_record(record1)

        self.assertTrue(self.test_record_service.check_sample_test_complete(sample_id))

        record2 = TestRecord(
            sample_id=sample_id,
            test_item="微生物检测",
            tester="李四",
            test_result="检测中"
        )
        self.test_record_service.create_test_record(record2)

        self.assertFalse(self.test_record_service.check_sample_test_complete(sample_id))


if __name__ == '__main__':
    unittest.main()
