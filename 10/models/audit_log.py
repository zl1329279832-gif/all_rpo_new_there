from dataclasses import dataclass
from datetime import datetime
from typing import Optional, List


@dataclass
class AuditLog:
    id: Optional[int] = None
    table_name: str = ""
    record_id: int = 0
    action: str = ""
    old_value: str = ""
    new_value: str = ""
    changed_by: str = "system"
    changed_at: str = ""

    ACTION_INSERT = "INSERT"
    ACTION_UPDATE = "UPDATE"
    ACTION_DELETE = "DELETE"

    def to_dict(self) -> dict:
        return {
            "id": self.id,
            "table_name": self.table_name,
            "record_id": self.record_id,
            "action": self.action,
            "old_value": self.old_value,
            "new_value": self.new_value,
            "changed_by": self.changed_by,
            "changed_at": self.changed_at,
        }

    @classmethod
    def from_dict(cls, data: dict) -> "AuditLog":
        return cls(
            id=data.get("id"),
            table_name=data.get("table_name", ""),
            record_id=data.get("record_id", 0),
            action=data.get("action", ""),
            old_value=data.get("old_value", ""),
            new_value=data.get("new_value", ""),
            changed_by=data.get("changed_by", "system"),
            changed_at=data.get("changed_at", ""),
        )
