from typing import List, Optional

from database.connection import DatabaseConnection
from models.audit_log import AuditLog


class AuditService:
    def __init__(self):
        self.db = DatabaseConnection()

    def log_operation(
        self,
        sample_id: Optional[int],
        operation_type: str,
        operator: str,
        remarks: str = "",
        field_name: str = "",
        old_value: str = "",
        new_value: str = ""
    ) -> int:
        sql = """
        INSERT INTO audit_logs (sample_id, operation_type, field_name, old_value, new_value, operator, remarks)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """
        params = (sample_id, operation_type, field_name, old_value, new_value, operator, remarks)
        return self.db.execute(sql, params)

    def get_audit_logs_by_sample_id(self, sample_id: int) -> List[AuditLog]:
        sql = "SELECT * FROM audit_logs WHERE sample_id = ? ORDER BY operation_time DESC"
        results = self.db.fetch_all(sql, (sample_id,))
        return [AuditLog.from_dict(r) for r in results]

    def get_all_audit_logs(
        self,
        operation_type: str = "",
        operator: str = "",
        start_date: str = "",
        end_date: str = "",
        offset: int = 0,
        limit: int = 100
    ) -> List[AuditLog]:
        sql = "SELECT * FROM audit_logs WHERE 1=1"
        params = []

        if operation_type:
            sql += " AND operation_type = ?"
            params.append(operation_type)

        if operator:
            sql += " AND operator LIKE ?"
            params.append(f"%{operator}%")

        if start_date:
            sql += " AND operation_time >= ?"
            params.append(f"{start_date} 00:00:00")

        if end_date:
            sql += " AND operation_time <= ?"
            params.append(f"{end_date} 23:59:59")

        sql += " ORDER BY operation_time DESC LIMIT ? OFFSET ?"
        params.extend([limit, offset])

        results = self.db.fetch_all(sql, tuple(params))
        return [AuditLog.from_dict(r) for r in results]

    def count_audit_logs(
        self,
        operation_type: str = "",
        operator: str = "",
        start_date: str = "",
        end_date: str = ""
    ) -> int:
        sql = "SELECT COUNT(*) as count FROM audit_logs WHERE 1=1"
        params = []

        if operation_type:
            sql += " AND operation_type = ?"
            params.append(operation_type)

        if operator:
            sql += " AND operator LIKE ?"
            params.append(f"%{operator}%")

        if start_date:
            sql += " AND operation_time >= ?"
            params.append(f"{start_date} 00:00:00")

        if end_date:
            sql += " AND operation_time <= ?"
            params.append(f"{end_date} 23:59:59")

        result = self.db.fetch_one(sql, tuple(params))
        return result['count'] if result else 0
