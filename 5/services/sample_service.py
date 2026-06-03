import sqlite3
from datetime import datetime
from typing import List, Optional, Dict, Any

from database.connection import DatabaseConnection
from models.sample import Sample


class SampleService:
    def __init__(self):
        self.db = DatabaseConnection()

    def sample_no_exists(self, sample_no: str, exclude_id: Optional[int] = None) -> bool:
        sql = "SELECT id FROM samples WHERE sample_no = ?"
        params = [sample_no]
        if exclude_id:
            sql += " AND id != ?"
            params.append(exclude_id)
        result = self.db.fetch_one(sql, tuple(params))
        return result is not None

    def create_sample(self, sample: Sample, operator: str = "系统") -> int:
        if not sample.sample_no:
            raise ValueError("样品编号不能为空")

        if self.sample_no_exists(sample.sample_no):
            raise ValueError(f"样品编号 '{sample.sample_no}' 已存在")

        if not sample.sample_name:
            raise ValueError("样品名称不能为空")

        if not sample.source_unit:
            raise ValueError("来源单位不能为空")

        if not sample.receive_time:
            sample.receive_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

        sample.updated_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

        sql = """
        INSERT INTO samples (sample_no, sample_name, source_unit, sender, receiver, 
                           receive_time, test_items, status, description, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
        params = (
            sample.sample_no,
            sample.sample_name,
            sample.source_unit,
            sample.sender,
            sample.receiver,
            sample.receive_time,
            sample.test_items,
            sample.status,
            sample.description,
            sample.created_at,
            sample.updated_at
        )

        sample_id = self.db.execute(sql, params)

        self._log_audit(sample_id, "新增", operator, "创建新样品")

        return sample_id

    def update_sample(self, sample: Sample, operator: str = "系统") -> bool:
        if not sample.id:
            raise ValueError("样品ID不能为空")

        if self.sample_no_exists(sample.sample_no, sample.id):
            raise ValueError(f"样品编号 '{sample.sample_no}' 已存在")

        old_sample = self.get_sample_by_id(sample.id)
        if not old_sample:
            raise ValueError("样品不存在")

        sample.updated_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

        old_dict = old_sample.to_dict()
        new_dict = sample.to_dict()

        for field in ['sample_no', 'sample_name', 'source_unit', 'status', 'test_items']:
            if old_dict.get(field) != new_dict.get(field):
                self._log_audit(
                    sample.id,
                    "修改",
                    operator,
                    f"修改字段: {field}",
                    field,
                    str(old_dict.get(field, "")),
                    str(new_dict.get(field, ""))
                )

        sql = """
        UPDATE samples 
        SET sample_no = ?, sample_name = ?, source_unit = ?, sender = ?, receiver = ?,
            receive_time = ?, test_items = ?, status = ?, description = ?, updated_at = ?
        WHERE id = ?
        """
        params = (
            sample.sample_no,
            sample.sample_name,
            sample.source_unit,
            sample.sender,
            sample.receiver,
            sample.receive_time,
            sample.test_items,
            sample.status,
            sample.description,
            sample.updated_at,
            sample.id
        )

        self.db.execute(sql, params)
        return True

    def delete_sample(self, sample_id: int, operator: str = "系统") -> bool:
        sample = self.get_sample_by_id(sample_id)
        if not sample:
            raise ValueError("样品不存在")

        self._log_audit(sample_id, "删除", operator, f"删除样品: {sample.sample_no}")

        sql = "DELETE FROM samples WHERE id = ?"
        self.db.execute(sql, (sample_id,))
        return True

    def get_sample_by_id(self, sample_id: int) -> Optional[Sample]:
        sql = "SELECT * FROM samples WHERE id = ?"
        result = self.db.fetch_one(sql, (sample_id,))
        if result:
            return Sample.from_dict(result)
        return None

    def get_sample_by_no(self, sample_no: str) -> Optional[Sample]:
        sql = "SELECT * FROM samples WHERE sample_no = ?"
        result = self.db.fetch_one(sql, (sample_no,))
        if result:
            return Sample.from_dict(result)
        return None

    def get_all_samples(
        self,
        keyword: str = "",
        status: str = "",
        start_date: str = "",
        end_date: str = "",
        offset: int = 0,
        limit: int = 100
    ) -> List[Sample]:
        sql = "SELECT * FROM samples WHERE 1=1"
        params = []

        if keyword:
            sql += " AND (sample_no LIKE ? OR sample_name LIKE ? OR source_unit LIKE ?)"
            params.extend([f"%{keyword}%", f"%{keyword}%", f"%{keyword}%"])

        if status:
            sql += " AND status = ?"
            params.append(status)

        if start_date:
            sql += " AND receive_time >= ?"
            params.append(f"{start_date} 00:00:00")

        if end_date:
            sql += " AND receive_time <= ?"
            params.append(f"{end_date} 23:59:59")

        sql += " ORDER BY receive_time DESC LIMIT ? OFFSET ?"
        params.extend([limit, offset])

        results = self.db.fetch_all(sql, tuple(params))
        return [Sample.from_dict(r) for r in results]

    def count_samples(
        self,
        keyword: str = "",
        status: str = "",
        start_date: str = "",
        end_date: str = ""
    ) -> int:
        sql = "SELECT COUNT(*) as count FROM samples WHERE 1=1"
        params = []

        if keyword:
            sql += " AND (sample_no LIKE ? OR sample_name LIKE ? OR source_unit LIKE ?)"
            params.extend([f"%{keyword}%", f"%{keyword}%", f"%{keyword}%"])

        if status:
            sql += " AND status = ?"
            params.append(status)

        if start_date:
            sql += " AND receive_time >= ?"
            params.append(f"{start_date} 00:00:00")

        if end_date:
            sql += " AND receive_time <= ?"
            params.append(f"{end_date} 23:59:59")

        result = self.db.fetch_one(sql, tuple(params))
        return result['count'] if result else 0

    def update_status(self, sample_id: int, new_status: str, operator: str = "系统") -> bool:
        sample = self.get_sample_by_id(sample_id)
        if not sample:
            raise ValueError("样品不存在")

        old_status = sample.status
        sample.status = new_status
        sample.updated_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

        sql = "UPDATE samples SET status = ?, updated_at = ? WHERE id = ?"
        self.db.execute(sql, (new_status, sample.updated_at, sample_id))

        self._log_audit(
            sample_id,
            "状态变更",
            operator,
            f"状态从 {old_status} 变为 {new_status}",
            "status",
            old_status,
            new_status
        )

        return True

    def get_statistics(self) -> Dict[str, Any]:
        stats = {}

        result = self.db.fetch_one("SELECT COUNT(*) as count FROM samples")
        stats['total'] = result['count'] if result else 0

        statuses = ["待检测", "检测中", "检测完成", "报告已生成", "已归档"]
        for status in statuses:
            result = self.db.fetch_one(
                "SELECT COUNT(*) as count FROM samples WHERE status = ?",
                (status,)
            )
            stats[status] = result['count'] if result else 0

        result = self.db.fetch_one("""
            SELECT COUNT(*) as count FROM samples 
            WHERE DATE(receive_time) = DATE('now')
        """)
        stats['today'] = result['count'] if result else 0

        return stats

    def _log_audit(
        self,
        sample_id: int,
        operation_type: str,
        operator: str,
        remarks: str = "",
        field_name: str = "",
        old_value: str = "",
        new_value: str = ""
    ):
        sql = """
        INSERT INTO audit_logs (sample_id, operation_type, field_name, old_value, new_value, operator, remarks)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """
        params = (sample_id, operation_type, field_name, old_value, new_value, operator, remarks)
        self.db.execute(sql, params)
