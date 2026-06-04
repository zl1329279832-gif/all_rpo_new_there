from dataclasses import dataclass, field
from datetime import datetime
from typing import Optional, List


@dataclass
class Collection:
    id: Optional[int] = None
    collection_no: str = ""
    name: str = ""
    era: str = ""
    category: str = ""
    source: str = ""
    conservation_status: str = ""
    entry_date: str = ""
    description: str = ""
    location: str = ""
    estimated_value: Optional[float] = None
    created_at: str = field(default_factory=lambda: datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
    updated_at: str = field(default_factory=lambda: datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
    created_by: str = "system"
    updated_by: str = "system"

    def validate(self) -> List[str]:
        errors = []
        if not self.collection_no.strip():
            errors.append("藏品编号不能为空")
        if not self.name.strip():
            errors.append("藏品名称不能为空")
        if not self.era:
            errors.append("请选择年代")
        if not self.category:
            errors.append("请选择类别")
        if not self.source:
            errors.append("请选择来源")
        if not self.conservation_status:
            errors.append("请选择保存状态")
        return errors

    def to_dict(self) -> dict:
        return {
            "id": self.id,
            "collection_no": self.collection_no,
            "name": self.name,
            "era": self.era,
            "category": self.category,
            "source": self.source,
            "conservation_status": self.conservation_status,
            "entry_date": self.entry_date,
            "description": self.description,
            "location": self.location,
            "estimated_value": self.estimated_value,
            "created_at": self.created_at,
            "updated_at": self.updated_at,
            "created_by": self.created_by,
            "updated_by": self.updated_by,
        }

    @classmethod
    def from_dict(cls, data: dict) -> "Collection":
        return cls(
            id=data.get("id"),
            collection_no=data.get("collection_no", ""),
            name=data.get("name", ""),
            era=data.get("era", ""),
            category=data.get("category", ""),
            source=data.get("source", ""),
            conservation_status=data.get("conservation_status", ""),
            entry_date=data.get("entry_date", ""),
            description=data.get("description", ""),
            location=data.get("location", ""),
            estimated_value=data.get("estimated_value"),
            created_at=data.get("created_at", ""),
            updated_at=data.get("updated_at", ""),
            created_by=data.get("created_by", "system"),
            updated_by=data.get("updated_by", "system"),
        )
