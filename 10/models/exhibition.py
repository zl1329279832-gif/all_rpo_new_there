from dataclasses import dataclass
from datetime import datetime
from typing import Optional, List


@dataclass
class Exhibition:
    id: Optional[int] = None
    collection_id: int = 0
    exhibition_name: str = ""
    location: str = ""
    start_date: str = ""
    end_date: str = ""
    organizer: str = ""
    notes: str = ""
    created_at: str = ""

    def validate(self) -> List[str]:
        errors = []
        if self.collection_id <= 0:
            errors.append("藏品ID无效")
        if not self.exhibition_name.strip():
            errors.append("展览名称不能为空")
        if not self.start_date:
            errors.append("开始日期不能为空")
        if not self.end_date:
            errors.append("结束日期不能为空")
        if self.start_date and self.end_date and self.start_date > self.end_date:
            errors.append("结束日期不能早于开始日期")
        return errors

    def to_dict(self) -> dict:
        return {
            "id": self.id,
            "collection_id": self.collection_id,
            "exhibition_name": self.exhibition_name,
            "location": self.location,
            "start_date": self.start_date,
            "end_date": self.end_date,
            "organizer": self.organizer,
            "notes": self.notes,
            "created_at": self.created_at,
        }

    @classmethod
    def from_dict(cls, data: dict) -> "Exhibition":
        return cls(
            id=data.get("id"),
            collection_id=data.get("collection_id", 0),
            exhibition_name=data.get("exhibition_name", ""),
            location=data.get("location", ""),
            start_date=data.get("start_date", ""),
            end_date=data.get("end_date", ""),
            organizer=data.get("organizer", ""),
            notes=data.get("notes", ""),
            created_at=data.get("created_at", ""),
        )
