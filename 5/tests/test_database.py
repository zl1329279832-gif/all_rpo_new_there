import unittest
import sys
import os
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent.parent))

from database.connection import DatabaseConnection
from database.schema import DatabaseSchema
from config.settings import DATABASE_PATH


class TestDatabase(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.db = DatabaseConnection()
        cls.schema = DatabaseSchema()

    def test_connection(self):
        conn = self.db.get_connection()
        self.assertIsNotNone(conn)

    def test_create_tables(self):
        self.schema.create_tables()

        tables = ['samples', 'test_records', 'attachments', 'audit_logs', 'system_settings']
        for table in tables:
            result = self.db.fetch_one(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                (table,)
            )
            self.assertIsNotNone(result, f"Table {table} should exist")

    def test_execute_and_fetch(self):
        self.db.execute("DELETE FROM samples WHERE sample_no LIKE 'TEST_%'")

        sql = """
        INSERT INTO samples (sample_no, sample_name, source_unit, receiver, receive_time, test_items)
        VALUES (?, ?, ?, ?, ?, ?)
        """
        params = ('TEST_DB_001', '测试样品', '测试单位', '测试人', '2024-01-01 10:00:00', '理化检测')
        last_id = self.db.execute(sql, params)
        self.assertGreater(last_id, 0)

        result = self.db.fetch_one("SELECT * FROM samples WHERE sample_no = ?", ('TEST_DB_001',))
        self.assertIsNotNone(result)
        self.assertEqual(result['sample_name'], '测试样品')

    def test_fetch_all(self):
        results = self.db.fetch_all("SELECT * FROM samples LIMIT 5")
        self.assertIsInstance(results, list)

    def test_transaction_rollback(self):
        try:
            with self.db.cursor() as cursor:
                cursor.execute("DELETE FROM samples WHERE sample_no LIKE 'TEST_ROLLBACK_%'")
                cursor.execute("""
                    INSERT INTO samples (sample_no, sample_name, source_unit, receiver, receive_time, test_items)
                    VALUES (?, ?, ?, ?, ?, ?)
                """, ('TEST_ROLLBACK_001', '回滚测试', '测试单位', '测试人', '2024-01-01', '测试项目'))
                raise Exception("Intentional rollback")
        except:
            pass

        result = self.db.fetch_one("SELECT * FROM samples WHERE sample_no = ?", ('TEST_ROLLBACK_001',))
        self.assertIsNone(result)


if __name__ == '__main__':
    unittest.main()
