from dataclasses import dataclass, field
from datetime import datetime
from pathlib import Path
from typing import Optional


@dataclass
class Attachment:
    id: Optional[int] = None
    sample_id: int = 0
    file_name: str = ""
    file_path: str = ""
    file_size: int = 0
    file_type: str = ""
    uploaded_by: str = ""
    uploaded_at: str = field(default_factory=lambda: datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
    description: str = ""

    def to_dict(self) -> dict:
        return {
            'id': self.id,
            'sample_id': self.sample_id,
            'file_name': self.file_name,
            'file_path': self.file_path,
            'file_size': self.file_size,
            'file_type': self.file_type,
            'uploaded_by': self.uploaded_by,
            'uploaded_at': self.uploaded_at,
            'description': self.description
        }

    @classmethod
    def from_dict(cls, data: dict) -> 'Attachment':
        return cls(
            id=data.get('id'),
            sample_id=data.get('sample_id', 0),
            file_name=data.get('file_name', ''),
            file_path=data.get('file_path', ''),
            file_size=data.get('file_size', 0),
            file_type=data.get('file_type', ''),
            uploaded_by=data.get('uploaded_by', ''),
            uploaded_at=data.get('uploaded_at', ''),
            description=data.get('description', '')
        )

    def file_exists(self) -> bool:
        return Path(self.file_path).exists()

    def get_file_extension(self) -> str:
        return Path(self.file_name).suffix.lower()

    def format_file_size(self) -> str:
        size = self.file_size
        for unit in ['B', 'KB', 'MB', 'GB']:
            if size < 1024:
                return f"{size:.1f} {unit}"
            size /= 1024
        return f"{size:.1f} TB"
