from dataclasses import dataclass
from datetime import datetime
from typing import Optional, List
from pathlib import Path


@dataclass
class Attachment:
    id: Optional[int] = None
    collection_id: int = 0
    file_name: str = ""
    file_path: str = ""
    file_size: int = 0
    file_type: str = ""
    is_image: bool = False
    description: str = ""
    uploaded_at: str = ""
    uploaded_by: str = "system"

    def validate(self) -> List[str]:
        errors = []
        if self.collection_id <= 0:
            errors.append("藏品ID无效")
        if not self.file_name.strip():
            errors.append("文件名不能为空")
        if not self.file_path:
            errors.append("文件路径不能为空")
        return errors

    def file_exists(self) -> bool:
        return Path(self.file_path).exists()

    def get_file_extension(self) -> str:
        return Path(self.file_name).suffix.lower()

    def format_file_size(self) -> str:
        if self.file_size < 1024:
            return f"{self.file_size} B"
        elif self.file_size < 1024 * 1024:
            return f"{self.file_size / 1024:.1f} KB"
        else:
            return f"{self.file_size / (1024 * 1024):.1f} MB"

    def to_dict(self) -> dict:
        return {
            "id": self.id,
            "collection_id": self.collection_id,
            "file_name": self.file_name,
            "file_path": self.file_path,
            "file_size": self.file_size,
            "file_type": self.file_type,
            "is_image": self.is_image,
            "description": self.description,
            "uploaded_at": self.uploaded_at,
            "uploaded_by": self.uploaded_by,
        }

    @classmethod
    def from_dict(cls, data: dict) -> "Attachment":
        return cls(
            id=data.get("id"),
            collection_id=data.get("collection_id", 0),
            file_name=data.get("file_name", ""),
            file_path=data.get("file_path", ""),
            file_size=data.get("file_size", 0),
            file_type=data.get("file_type", ""),
            is_image=data.get("is_image", False),
            description=data.get("description", ""),
            uploaded_at=data.get("uploaded_at", ""),
            uploaded_by=data.get("uploaded_by", "system"),
        )
