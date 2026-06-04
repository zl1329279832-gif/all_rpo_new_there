from typing import Optional, List, Dict, Any
from datetime import datetime

from database import DatabaseConnection, RepairRecordRepository, InvalidRepairStatusError
from models import RepairRecord
from config.settings import REPAIR_STATUSES


class RepairService:
    def __init__(self, db: DatabaseConnection):
        self.db = db
        self.repo = RepairRecordRepository(db)

    def get_repair_records(self, collection_id: Optional[int] = None) -> List[RepairRecord]:
        return self.repo.get_all(collection_id)

    def get_repair_record(self, record_id: int) -> Optional[RepairRecord]:
        return self.repo.get_by_id(record_id)

    def create_repair_record(self, record: RepairRecord, changed_by: str = "system") -> int:
        errors = record.validate()
        if errors:
            raise ValueError("; ".join(errors))

        return self.repo.create(record, changed_by)

    def update_repair_record(self, record: RepairRecord, changed_by: str = "system") -> bool:
        errors = record.validate()
        if errors:
            raise ValueError("; ".join(errors))

        return self.repo.update(record, changed_by)

    def update_repair_status(
        self, record_id: int, new_status: str, changed_by: str = "system"
    ) -> bool:
        return self.repo.update_status(record_id, new_status, changed_by)

    def delete_repair_record(self, record_id: int, changed_by: str = "system") -> bool:
        return self.repo.delete(record_id, changed_by)

    def get_statistics(self) -> Dict[str, Any]:
        return self.repo.get_statistics()

    def get_repair_statuses(self) -> List[str]:
        return REPAIR_STATUSES

    def can_transition_status(self, current_status: str, new_status: str) -> bool:
        valid_transitions = {
            "待修复": ["修复中", "已取消"],
            "修复中": ["已完成", "已取消"],
            "已完成": [],
            "已取消": [],
        }
        return new_status in valid_transitions.get(current_status, [])
