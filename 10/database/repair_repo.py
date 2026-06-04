from typing import Optional, List
from datetime import datetime

from .connection import DatabaseConnection
from models import RepairRecord
from config.settings import REPAIR_STATUSES


class InvalidRepairStatusError(Exception):
    pass


class RepairRecordRepository:
    def __init__(self, db: DatabaseConnection):
        self.db = db

    def get_all(self, collection_id: Optional[int] = None) -> List[RepairRecord]:
        query = "SELECT * FROM repair_records WHERE 1=1"
        params = []

        if collection_id:
            query += " AND collection_id = ?"
            params.append(collection_id)

        query += " ORDER BY repair_date DESC"

        with self.db.get_cursor() as cursor:
            cursor.execute(query, params)
            rows = cursor.fetchall()
            return [RepairRecord.from_dict(dict(row)) for row in rows]

    def get_by_id(self, record_id: int) -> Optional[RepairRecord]:
        with self.db.get_cursor() as cursor:
            cursor.execute("SELECT * FROM repair_records WHERE id = ?", (record_id,))
            row = cursor.fetchone()
            return RepairRecord.from_dict(dict(row)) if row else None

    def create(self, record: RepairRecord, changed_by: str = "system") -> int:
        if record.status not in REPAIR_STATUSES:
            raise InvalidRepairStatusError(f"无效的修复状态: {record.status}")

        record.created_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        record.updated_at = record.created_at

        with self.db.get_cursor() as cursor:
            cursor.execute(
                """
                INSERT INTO repair_records (
                    collection_id, repair_date, repairer, reason, description,
                    cost, status, notes, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                (
                    record.collection_id,
                    record.repair_date,
                    record.repairer,
                    record.reason,
                    record.description,
                    record.cost,
                    record.status,
                    record.notes,
                    record.created_at,
                    record.updated_at,
                ),
            )
            record_id = cursor.lastrowid
            self.db.log_audit("repair_records", record_id, "INSERT", None, record.to_dict(), changed_by)
            return record_id

    def update(self, record: RepairRecord, changed_by: str = "system") -> bool:
        if record.status not in REPAIR_STATUSES:
            raise InvalidRepairStatusError(f"无效的修复状态: {record.status}")

        old_record = self.get_by_id(record.id)
        if not old_record:
            return False

        record.updated_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

        with self.db.get_cursor() as cursor:
            cursor.execute(
                """
                UPDATE repair_records SET
                    repair_date = ?, repairer = ?, reason = ?, description = ?,
                    cost = ?, status = ?, notes = ?, updated_at = ?
                WHERE id = ?
                """,
                (
                    record.repair_date,
                    record.repairer,
                    record.reason,
                    record.description,
                    record.cost,
                    record.status,
                    record.notes,
                    record.updated_at,
                    record.id,
                ),
            )
            self.db.log_audit("repair_records", record.id, "UPDATE", old_record.to_dict(), record.to_dict(), changed_by)
            return cursor.rowcount > 0

    def update_status(self, record_id: int, new_status: str, changed_by: str = "system") -> bool:
        if new_status not in REPAIR_STATUSES:
            raise InvalidRepairStatusError(f"无效的修复状态: {new_status}")

        old_record = self.get_by_id(record_id)
        if not old_record:
            return False

        updated_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

        with self.db.get_cursor() as cursor:
            cursor.execute(
                "UPDATE repair_records SET status = ?, updated_at = ? WHERE id = ?",
                (new_status, updated_at, record_id),
            )
            self.db.log_audit(
                "repair_records",
                record_id,
                "UPDATE",
                {"status": old_record.status},
                {"status": new_status},
                changed_by,
            )
            return cursor.rowcount > 0

    def delete(self, record_id: int, changed_by: str = "system") -> bool:
        old_record = self.get_by_id(record_id)
        if not old_record:
            return False

        with self.db.get_cursor() as cursor:
            cursor.execute("DELETE FROM repair_records WHERE id = ?", (record_id,))
            self.db.log_audit("repair_records", record_id, "DELETE", old_record.to_dict(), None, changed_by)
            return cursor.rowcount > 0

    def get_statistics(self) -> dict:
        stats = {}
        with self.db.get_cursor() as cursor:
            cursor.execute("SELECT COUNT(*) FROM repair_records")
            stats["total_repairs"] = cursor.fetchone()[0]

            cursor.execute("SELECT status, COUNT(*) FROM repair_records GROUP BY status")
            stats["by_status"] = dict(cursor.fetchall())

            cursor.execute("SELECT SUM(cost) FROM repair_records WHERE cost IS NOT NULL")
            stats["total_cost"] = cursor.fetchone()[0] or 0

        return stats
