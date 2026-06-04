from dataclasses import dataclass
from datetime import datetime
from typing import Optional, List


@dataclass
class RepairRecord:
    id: Optional[int] = None
    collection_id: int = 0
    repair_date: str = ""
    repairer: str = ""
    reason: str = ""
    description: str = ""
    cost: Optional[float] = None
    status: str = "待修复"
    notes: str = ""
    created_at: str = ""
    updated_at: str = ""

    def validate(self) -> List[str]:
        errors = []
        if self.collection_id <= 0:
            errors.append("藏品ID无效")
        if not self.repair_date:
            errors.append("修复日期不能为空")
        if not self.reason.strip():
            errors.append("修复原因不能为空")
        if not self.status:
            errors.append("请选择修复状态")
        return errors

    def to_dict(self) -> dict:
        return {
            "id": self.id,
            "collection_id": self.collection_id,
            "repair_date": self.repair_date,
            "repairer": self.repairer,
            "reason": self.reason,
            "description": self.description,
            "cost": self.cost,
            "status": self.status,
            "notes": self.notes,
            "created_at": self.created_at,
            "updated_at": self.updated_at,
        }

    @classmethod
    def from_dict(cls, data: dict) -> "RepairRecord":
        return cls(
            id=data.get("id"),
            collection_id=data.get("collection_id", 0),
            repair_date=data.get("repair_date", ""),
            repairer=data.get("repairer", ""),
            reason=data.get("reason", ""),
            description=data.get("description", ""),
            cost=data.get("cost"),
            status=data.get("status", "待修复"),
            notes=data.get("notes", ""),
            created_at=data.get("created_at", ""),
            updated_at=data.get("updated_at", ""),
        )
