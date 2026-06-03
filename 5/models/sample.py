from dataclasses import dataclass, field
from datetime import datetime
from typing import Optional, List


@dataclass
class Sample:
    id: Optional[int] = None
    sample_no: str = ""
    sample_name: str = ""
    source_unit: str = ""
    sender: str = ""
    receiver: str = ""
    receive_time: str = ""
    test_items: str = ""
    status: str = "待检测"
    description: str = ""
    created_at: str = field(default_factory=lambda: datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
    updated_at: str = field(default_factory=lambda: datetime.now().strftime("%Y-%m-%d %H:%M:%S"))

    def to_dict(self) -> dict:
        return {
            'id': self.id,
            'sample_no': self.sample_no,
            'sample_name': self.sample_name,
            'source_unit': self.source_unit,
            'sender': self.sender,
            'receiver': self.receiver,
            'receive_time': self.receive_time,
            'test_items': self.test_items,
            'status': self.status,
            'description': self.description,
            'created_at': self.created_at,
            'updated_at': self.updated_at
        }

    @classmethod
    def from_dict(cls, data: dict) -> 'Sample':
        return cls(
            id=data.get('id'),
            sample_no=data.get('sample_no', ''),
            sample_name=data.get('sample_name', ''),
            source_unit=data.get('source_unit', ''),
            sender=data.get('sender', ''),
            receiver=data.get('receiver', ''),
            receive_time=data.get('receive_time', ''),
            test_items=data.get('test_items', ''),
            status=data.get('status', '待检测'),
            description=data.get('description', ''),
            created_at=data.get('created_at', ''),
            updated_at=data.get('updated_at', '')
        )

    def is_complete(self) -> bool:
        return self.status in ["检测完成", "报告已生成", "已归档"]

    def can_generate_report(self) -> bool:
        return self.status == "检测完成"
