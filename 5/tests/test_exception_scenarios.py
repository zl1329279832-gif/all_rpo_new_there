import unittest
import sys
import os
import tempfile
import shutil
import sqlite3
from pathlib import Path
from unittest.mock import patch, MagicMock

sys.path.insert(0, str(Path(__file__).parent.parent))

from models.sample import Sample
from models.test_record import TestRecord
from services.sample_service import SampleService
from services.test_record_service import TestRecordService
from services.attachment_service import AttachmentService
from services.database_service import DatabaseService
from database.connection import DatabaseConnection
from database.schema import DatabaseSchema


class TestExceptionScenarios(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.db_service = DatabaseService()
        cls.db_service.initialize_database()
        cls.sample_service = SampleService()
        cls.test_record_service = TestRecordService()
        cls.attachment_service = AttachmentService()

    def setUp(self):
        self.db_service.clear_all_data()

    def test_database_connection_failure(self):
        original_instance = DatabaseConnection._instance
        original_connection = DatabaseConnection._connection

        try:
            DatabaseConnection._instance = None
            DatabaseConnection._connection = None

            with patch('sqlite3.connect') as mock_connect:
                mock_connect.side_effect = sqlite3.Error("Connection failed")

                with self.assertRaises(Exception) as context:
                    db = DatabaseConnection()
                    db.get_connection()

                self.assertIn("Connection failed", str(context.exception))
        finally:
            DatabaseConnection._instance = original_instance
            DatabaseConnection._connection = original_connection

    def test_database_initialize_failure(self):
        with patch.object(DatabaseSchema, 'initialize') as mock_initialize:
            mock_initialize.side_effect = sqlite3.Error("Table creation failed")

            db_service = DatabaseService()
            with self.assertRaises(Exception) as context:
                db_service.initialize_database()

            self.assertIn("数据库初始化失败", str(context.exception))
            self.assertIn("Table creation failed", str(context.exception))

    def test_invalid_status_transition(self):
        sample = Sample(
            sample_no="EXCEPT_STATUS_001",
            sample_name="非法状态切换测试",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00",
            status="待检测"
        )
        sample_id = self.sample_service.create_sample(sample)

        with self.assertRaises(ValueError) as context:
            self.sample_service.update_status(sample_id, "已归档")

        self.assertIn("不允许从", str(context.exception))
        self.assertIn("待检测", str(context.exception))
        self.assertIn("已归档", str(context.exception))

    def test_canceled_status_transition(self):
        sample = Sample(
            sample_no="EXCEPT_STATUS_002",
            sample_name="已作废状态测试",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00",
            status="已作废"
        )
        sample_id = self.sample_service.create_sample(sample)

        with self.assertRaises(ValueError) as context:
            self.sample_service.update_status(sample_id, "检测中")

        self.assertIn("不允许切换到任何状态", str(context.exception))

    def test_update_status_nonexistent_sample(self):
        with self.assertRaises(ValueError) as context:
            self.sample_service.update_status(999999, "检测中")

        self.assertIn("样品不存在", str(context.exception))

    def test_concurrent_duplicate_sample_no(self):
        sample = Sample(
            sample_no="EXCEPT_CONCUR_001",
            sample_name="并发测试1",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        self.sample_service.create_sample(sample)

        sample2 = Sample(
            sample_no="EXCEPT_CONCUR_001",
            sample_name="并发测试2",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )

        original_execute = self.sample_service.db.execute

        def mock_execute(sql, params=()):
            if "INSERT INTO samples" in sql:
                if params[0] == "EXCEPT_CONCUR_001":
                    raise sqlite3.IntegrityError("UNIQUE constraint failed: samples.sample_no")
            return original_execute(sql, params)

        with patch.object(self.sample_service.db, 'execute', side_effect=mock_execute):
            with self.assertRaises((ValueError, sqlite3.IntegrityError)):
                self.sample_service.create_sample(sample2)

    def test_database_transaction_rollback(self):
        db = DatabaseConnection()

        try:
            with db.cursor() as cursor:
                cursor.execute("DELETE FROM samples WHERE sample_no LIKE 'EXCEPT_ROLLBACK_%'")
                cursor.execute("""
                    INSERT INTO samples (sample_no, sample_name, source_unit, receiver, receive_time, test_items)
                    VALUES (?, ?, ?, ?, ?, ?)
                """, ('EXCEPT_ROLLBACK_001', '回滚测试', '测试单位', '测试人', '2024-01-01', '测试项目'))
                raise Exception("Intentional rollback for testing")
        except Exception:
            pass

        result = db.fetch_one("SELECT * FROM samples WHERE sample_no = ?", ('EXCEPT_ROLLBACK_001',))
        self.assertIsNone(result)

    def test_delete_sample_with_related_data(self):
        sample = Sample(
            sample_no="EXCEPT_DELETE_001",
            sample_name="级联删除测试",
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

        with tempfile.NamedTemporaryFile(delete=False, suffix='.txt') as f:
            f.write(b"test content")
            temp_path = f.name

        try:
            attachment = self.attachment_service.upload_attachment(
                sample_id=sample_id,
                source_path=temp_path,
                uploaded_by="测试员"
            )
            attachment_id = attachment.id

            attachment_path = Path(attachment.file_path)
            self.assertTrue(attachment_path.exists())

            self.sample_service.delete_sample(sample_id)

            self.assertIsNone(self.sample_service.get_sample_by_id(sample_id))
            self.assertIsNone(self.test_record_service.get_test_record_by_id(record_id))
            self.assertIsNone(self.attachment_service.get_attachment_by_id(attachment_id))
            self.assertFalse(attachment_path.exists())
        finally:
            os.unlink(temp_path)

    def test_attachment_corrupted_file(self):
        sample = Sample(
            sample_no="EXCEPT_ATTACH_001",
            sample_name="损坏文件测试",
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

            self.assertTrue(Path(attachment.file_path).exists())

            with open(attachment.file_path, 'rb') as f:
                content = f.read()
            self.assertEqual(content, b"test content")

            preview_info = self.attachment_service.get_preview_info(attachment)
            self.assertTrue(preview_info['file_exists'])
            self.assertGreater(preview_info['file_size_formatted'], '')
        finally:
            os.unlink(temp_path)

    def test_attachment_large_file(self):
        sample = Sample(
            sample_no="EXCEPT_ATTACH_002",
            sample_name="大文件测试",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        sample_id = self.sample_service.create_sample(sample)

        with tempfile.NamedTemporaryFile(delete=False, suffix='.txt') as f:
            large_content = b"X" * (1024 * 1024 * 101)
            f.write(large_content)
            temp_path = f.name

        try:
            valid, message = self.attachment_service.validate_attachment_path(temp_path)
            self.assertFalse(valid)
            self.assertIn("超过限制", message)
        finally:
            os.unlink(temp_path)

    def test_report_generation_with_incomplete_data(self):
        sample = Sample(
            sample_no="EXCEPT_REPORT_001",
            sample_name="不完整报告测试",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00",
            status="检测中"
        )
        sample_id = self.sample_service.create_sample(sample)

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

    def test_test_record_update_nonexistent(self):
        record = TestRecord(
            sample_id=999999,
            test_item="理化检测",
            tester="张三",
            test_result="合格"
        )
        record.id = 999999

        with self.assertRaises(ValueError) as context:
            self.test_record_service.update_test_record(
                record,
                change_reason="测试修改"
            )

        self.assertIn("检测记录不存在", str(context.exception))

    def test_test_record_change_query_performance(self):
        sample = Sample(
            sample_no="EXCEPT_PERF_001",
            sample_name="性能测试",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        sample_id = self.sample_service.create_sample(sample)

        record = TestRecord(
            sample_id=sample_id,
            test_item="理化检测",
            tester="张三",
            test_result="合格",
            result_value="100"
        )
        record_id = self.test_record_service.create_test_record(record)

        for i in range(50):
            found = self.test_record_service.get_test_record_by_id(record_id)
            found.result_value = str(101 + i)
            self.test_record_service.update_test_record(
                found,
                operator=f"测试员{i % 5}",
                change_reason=f"第{i}次修改",
                change_remark="性能测试批量修改"
            )

        changes = self.test_record_service.get_changes_by_test_record_id(record_id)
        self.assertEqual(len(changes), 50)

        all_changes = self.test_record_service.get_changes_by_sample_id(sample_id)
        self.assertEqual(len(all_changes), 50)

        paginated = self.test_record_service.get_all_changes(limit=10, offset=0)
        self.assertEqual(len(paginated), 10)

    def test_database_sql_injection_protection(self):
        malicious_input = "'); DROP TABLE samples; --"

        try:
            result = self.sample_service.get_all_samples(keyword=malicious_input)
            self.assertIsInstance(result, list)

            count = self.sample_service.count_samples(keyword=malicious_input)
            self.assertIsInstance(count, int)

            exists = self.sample_service.sample_no_exists(malicious_input)
            self.assertFalse(exists)
        except Exception as e:
            self.fail(f"SQL injection attempt caused exception: {e}")

        result = self.sample_service.db.fetch_one("SELECT name FROM sqlite_master WHERE type='table' AND name='samples'")
        self.assertIsNotNone(result, "Samples table should still exist")

    def test_special_characters_in_data(self):
        special_chars = '测试_特殊字符!@#$%^&*()_+-=[]{}|;:,.<>?/~`\'"\\'

        sample = Sample(
            sample_no=f"EXCEPT_SPEC_001",
            sample_name=special_chars,
            source_unit=special_chars,
            receive_time="2024-01-01 10:00:00",
            description=special_chars
        )
        sample_id = self.sample_service.create_sample(sample)

        found = self.sample_service.get_sample_by_id(sample_id)
        self.assertEqual(found.sample_name, special_chars)
        self.assertEqual(found.source_unit, special_chars)
        self.assertEqual(found.description, special_chars)

        record = TestRecord(
            sample_id=sample_id,
            test_item=special_chars,
            tester=special_chars,
            test_result="合格",
            result_value=special_chars,
            remarks=special_chars
        )
        record_id = self.test_record_service.create_test_record(record)

        found_record = self.test_record_service.get_test_record_by_id(record_id)
        self.assertEqual(found_record.test_item, special_chars)
        self.assertEqual(found_record.tester, special_chars)
        self.assertEqual(found_record.remarks, special_chars)

    def test_database_connection_reuse(self):
        db1 = DatabaseConnection()
        db2 = DatabaseConnection()

        self.assertIs(db1, db2)

        conn1 = db1.get_connection()
        conn2 = db2.get_connection()

        self.assertIs(conn1, conn2)

    def test_concurrent_database_access(self):
        import threading
        import time

        results = []
        errors = []

        def create_sample(index):
            try:
                sample = Sample(
                    sample_no=f"EXCEPT_THREAD_{index:03d}",
                    sample_name=f"并发测试{index}",
                    source_unit="测试单位",
                    receive_time="2024-01-01 10:00:00"
                )
                sample_id = self.sample_service.create_sample(sample)
                results.append(sample_id)
            except Exception as e:
                errors.append(str(e))

        threads = []
        for i in range(10):
            t = threading.Thread(target=create_sample, args=(i,))
            threads.append(t)
            t.start()
            time.sleep(0.01)

        for t in threads:
            t.join()

        self.assertEqual(len(results), 10, f"Expected 10 successful creates, got {len(results)}. Errors: {errors}")
        self.assertEqual(len(errors), 0, f"Unexpected errors: {errors}")

    def test_backup_and_restore_simulation(self):
        temp_dir = tempfile.mkdtemp()
        temp_db_path = Path(temp_dir) / "test_backup.db"
        backup_path = Path(temp_dir) / "backup.db"

        original_schema_db_connection = None
        try:
            conn = sqlite3.connect(str(temp_db_path))
            conn.row_factory = sqlite3.Row
            conn.execute("PRAGMA foreign_keys = ON")

            schema = DatabaseSchema()
            original_schema_db_connection = schema.db._connection
            schema.db._connection = conn
            try:
                schema.create_tables()
                schema.create_indexes()
            finally:
                schema.db._connection = original_schema_db_connection

            conn.execute("""
                INSERT INTO samples (sample_no, sample_name, source_unit, receiver, receive_time, test_items, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """, ("EXCEPT_BACKUP_001", "备份恢复测试", "测试单位", "测试员", "2024-01-01 10:00:00", "理化检测", "待检测"))
            sample_id = conn.execute("SELECT last_insert_rowid()").fetchone()[0]

            conn.execute("""
                INSERT INTO test_records (sample_id, test_item, tester, test_result)
                VALUES (?, ?, ?, ?)
            """, (sample_id, "理化检测", "张三", "合格"))

            conn.commit()

            shutil.copy2(temp_db_path, backup_path)
            self.assertTrue(backup_path.exists())

            backup_conn = sqlite3.connect(str(backup_path))
            backup_conn.row_factory = sqlite3.Row
            try:
                result = backup_conn.execute("SELECT * FROM samples WHERE id = ?", (sample_id,)).fetchone()
                self.assertIsNotNone(result)
                self.assertEqual(result['sample_no'], "EXCEPT_BACKUP_001")

                result = backup_conn.execute("SELECT * FROM test_records WHERE sample_id = ?", (sample_id,)).fetchone()
                self.assertIsNotNone(result)
                self.assertEqual(result['test_result'], "合格")
            finally:
                backup_conn.close()

            conn.execute("DELETE FROM samples WHERE id = ?", (sample_id,))
            conn.commit()

            result = conn.execute("SELECT * FROM samples WHERE id = ?", (sample_id,)).fetchone()
            self.assertIsNone(result)

            conn.close()

            shutil.copy2(backup_path, temp_db_path)

            restored_conn = sqlite3.connect(str(temp_db_path))
            restored_conn.row_factory = sqlite3.Row
            try:
                result = restored_conn.execute("SELECT * FROM samples WHERE id = ?", (sample_id,)).fetchone()
                self.assertIsNotNone(result)
                self.assertEqual(result['sample_no'], "EXCEPT_BACKUP_001")

                result = restored_conn.execute("SELECT * FROM test_records WHERE sample_id = ?", (sample_id,)).fetchone()
                self.assertIsNotNone(result)
                self.assertEqual(result['test_result'], "合格")
            finally:
                restored_conn.close()

        finally:
            shutil.rmtree(temp_dir, ignore_errors=True)
            if original_schema_db_connection is not None:
                schema = DatabaseSchema()
                schema.db._connection = original_schema_db_connection

    def test_attachment_delete_with_missing_file(self):
        sample = Sample(
            sample_no="EXCEPT_ATTACH_003",
            sample_name="删除缺失附件测试",
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

            os.unlink(attachment.file_path)

            result = self.attachment_service.delete_attachment(attachment.id)
            self.assertTrue(result)

            self.assertIsNone(self.attachment_service.get_attachment_by_id(attachment.id))
        finally:
            os.unlink(temp_path)

    def test_attachment_preview_unsupported_type(self):
        sample = Sample(
            sample_no="EXCEPT_ATTACH_004",
            sample_name="不支持预览测试",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        sample_id = self.sample_service.create_sample(sample)

        with tempfile.NamedTemporaryFile(delete=False, suffix='.xyz') as f:
            f.write(b"test content")
            temp_path = f.name

        try:
            attachment = self.attachment_service.upload_attachment(
                sample_id=sample_id,
                source_path=temp_path,
                uploaded_by="测试员"
            )

            can_preview, message = self.attachment_service.can_preview(attachment)
            self.assertFalse(can_preview)
            self.assertIn("不支持预览", message)

            with self.assertRaises(ValueError):
                self.attachment_service.open_preview(attachment)
        finally:
            os.unlink(temp_path)

    def test_get_changes_empty(self):
        changes = self.test_record_service.get_changes_by_test_record_id(999999)
        self.assertEqual(len(changes), 0)

        changes = self.test_record_service.get_changes_by_sample_id(999999)
        self.assertEqual(len(changes), 0)

        changes = self.test_record_service.get_all_changes()
        self.assertEqual(len(changes), 0)

    def test_audit_log_generation(self):
        sample = Sample(
            sample_no="EXCEPT_AUDIT_001",
            sample_name="审计日志测试",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        sample_id = self.sample_service.create_sample(sample, operator="测试员A")

        found = self.sample_service.get_sample_by_id(sample_id)
        found.sample_name = "修改后的名称"
        self.sample_service.update_sample(found, operator="测试员B")

        self.sample_service.update_status(
            sample_id,
            "检测中",
            operator="测试员C",
            reason="开始检测"
        )

        logs = self.sample_service.db.fetch_all(
            "SELECT * FROM audit_logs WHERE sample_id = ? ORDER BY operation_time",
            (sample_id,)
        )

        self.assertGreaterEqual(len(logs), 3)

        operations = [log['operation_type'] for log in logs]
        self.assertIn("新增", operations)
        self.assertIn("修改", operations)
        self.assertIn("状态变更", operations)

        operators = [log['operator'] for log in logs]
        self.assertIn("测试员A", operators)
        self.assertIn("测试员B", operators)
        self.assertIn("测试员C", operators)

    def test_database_query_edge_cases(self):
        results = self.sample_service.get_all_samples(limit=0)
        self.assertEqual(len(results), 0)

        results = self.sample_service.get_all_samples(limit=-1)
        self.assertEqual(len(results), 0)

        results = self.sample_service.get_all_samples(offset=-1)
        self.assertIsInstance(results, list)

        count = self.sample_service.count_samples(
            keyword="",
            status="",
            start_date="",
            end_date=""
        )
        self.assertIsInstance(count, int)

        count = self.sample_service.count_samples(
            keyword=None,
            status=None,
            start_date=None,
            end_date=None
        )
        self.assertIsInstance(count, int)

    def test_attachment_statistics(self):
        stats = self.attachment_service.get_attachment_statistics()
        self.assertIsInstance(stats, dict)
        self.assertIn('total_count', stats)
        self.assertIn('total_size', stats)
        self.assertIn('missing_count', stats)

        sample = Sample(
            sample_no="EXCEPT_ATTACH_STAT_001",
            sample_name="附件统计测试",
            source_unit="测试单位",
            receive_time="2024-01-01 10:00:00"
        )
        sample_id = self.sample_service.create_sample(sample)

        with tempfile.NamedTemporaryFile(delete=False, suffix='.txt') as f:
            f.write(b"test content 12345")
            temp_path = f.name

        try:
            self.attachment_service.upload_attachment(
                sample_id=sample_id,
                source_path=temp_path,
                uploaded_by="测试员"
            )

            stats = self.attachment_service.get_attachment_statistics()
            self.assertEqual(stats['total_count'], 1)
            self.assertGreater(stats['total_size'], 0)
            self.assertEqual(stats['missing_count'], 0)
        finally:
            os.unlink(temp_path)

    def test_extreme_dates(self):
        sample = Sample(
            sample_no="EXCEPT_DATE_001",
            sample_name="极端日期测试",
            source_unit="测试单位",
            receive_time="1900-01-01 00:00:00"
        )
        sample_id = self.sample_service.create_sample(sample)

        found = self.sample_service.get_sample_by_id(sample_id)
        self.assertEqual(found.receive_time, "1900-01-01 00:00:00")

        sample2 = Sample(
            sample_no="EXCEPT_DATE_002",
            sample_name="未来日期测试",
            source_unit="测试单位",
            receive_time="2099-12-31 23:59:59"
        )
        sample_id2 = self.sample_service.create_sample(sample2)

        results = self.sample_service.get_all_samples(
            start_date="2099-01-01",
            end_date="2099-12-31"
        )
        self.assertEqual(len(results), 1)
        self.assertEqual(results[0].sample_no, "EXCEPT_DATE_002")


if __name__ == '__main__':
    unittest.main()
