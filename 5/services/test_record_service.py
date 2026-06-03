from datetime import datetime
from typing import List, Optional

from database.connection import DatabaseConnection
from models.test_record import TestRecord


class TestRecordService:
    def __init__(self):
        self.db = DatabaseConnection()

    def create_test_record(self, record: TestRecord, operator: str = "系统") -> int:
        if record.sample_id <= 0:
            raise ValueError("样品ID不能为空")

        if not record.test_item:
            raise ValueError("检测项目不能为空")

        if not record.tester:
            raise ValueError("检测人员不能为空")

        record.updated_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

        sql = """
        INSERT INTO test_records (sample_id, test_item, tester, test_time, test_result, 
                                 result_value, standard_value, is_qualified, remarks, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
        params = (
            record.sample_id,
            record.test_item,
            record.tester,
            record.test_time,
            record.test_result,
            record.result_value,
            record.standard_value,
            1 if record.is_qualified else 0,
            record.remarks,
            record.created_at,
            record.updated_at
        )

        record_id = self.db.execute(sql, params)

        return record_id

    def update_test_record(self, record: TestRecord, operator: str = "系统") -> bool:
        if not record.id:
            raise ValueError("检测记录ID不能为空")

        record.updated_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

        sql = """
        UPDATE test_records 
        SET sample_id = ?, test_item = ?, tester = ?, test_time = ?, test_result = ?,
            result_value = ?, standard_value = ?, is_qualified = ?, remarks = ?, updated_at = ?
        WHERE id = ?
        """
        params = (
            record.sample_id,
            record.test_item,
            record.tester,
            record.test_time,
            record.test_result,
            record.result_value,
            record.standard_value,
            1 if record.is_qualified else 0,
            record.remarks,
            record.updated_at,
            record.id
        )

        self.db.execute(sql, params)
        return True

    def delete_test_record(self, record_id: int) -> bool:
        sql = "DELETE FROM test_records WHERE id = ?"
        self.db.execute(sql, (record_id,))
        return True

    def get_test_record_by_id(self, record_id: int) -> Optional[TestRecord]:
        sql = "SELECT * FROM test_records WHERE id = ?"
        result = self.db.fetch_one(sql, (record_id,))
        if result:
            return TestRecord.from_dict(result)
        return None

    def get_test_records_by_sample_id(self, sample_id: int) -> List[TestRecord]:
        sql = "SELECT * FROM test_records WHERE sample_id = ? ORDER BY created_at"
        results = self.db.fetch_all(sql, (sample_id,))
        return [TestRecord.from_dict(r) for r in results]

    def get_all_test_records(self, offset: int = 0, limit: int = 100) -> List[TestRecord]:
        sql = "SELECT * FROM test_records ORDER BY created_at DESC LIMIT ? OFFSET ?"
        results = self.db.fetch_all(sql, (limit, offset))
        return [TestRecord.from_dict(r) for r in results]

    def check_sample_test_complete(self, sample_id: int) -> bool:
        sql = """
        SELECT COUNT(*) as total,
               SUM(CASE WHEN test_result = '合格' OR test_result = '不合格' THEN 1 ELSE 0 END) as completed
        FROM test_records 
        WHERE sample_id = ?
        """
        result = self.db.fetch_one(sql, (sample_id,))
        if result:
            total = result['total'] or 0
            completed = result['completed'] or 0
            return total > 0 and total == completed
        return False

    def can_generate_report(self, sample_id: int) -> tuple[bool, str]:
        records = self.get_test_records_by_sample_id(sample_id)

        if not records:
            return False, "该样品没有检测记录"

        for record in records:
            if not record.test_result or record.test_result == '检测中':
                return False, f"检测项目 '{record.test_item}' 尚未完成"

        return True, "所有检测项目已完成"
