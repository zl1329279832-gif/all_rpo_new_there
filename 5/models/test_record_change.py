from dataclasses import dataclass, field
from datetime import datetime
from typing import Optional


@dataclass
class TestRecordChange:
    id: Optional[int] = None
    test_record_id: int = 0
    sample_id: int = 0
    field_name: str = ""
    old_value: str = ""
    new_value: str = ""
    change_reason: str = ""
    operator: str = ""
    change_time: str = field(default_factory=lambda: datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
    remarks: str = ""

    def to_dict(self) -> dict:
        return {
            'id': self.id,
            'test_record_id': self.test_record_id,
            'sample_id': self.sample_id,
            'field_name': self.field_name,
            'old_value': self.old_value,
            'new_value': self.new_value,
            'change_reason': self.change_reason,
            'operator': self.operator,
            'change_time': self.change_time,
            'remarks': self.remarks
        }

    @classmethod
    def from_dict(cls, data: dict) -> 'TestRecordChange':
        return cls(
            id=data.get('id'),
            test_record_id=data.get('test_record_id', 0),
            sample_id=data.get('sample_id', 0),
            field_name=data.get('field_name', ''),
            old_value=data.get('old_value', ''),
            new_value=data.get('new_value', ''),
            change_reason=data.get('change_reason', ''),
            operator=data.get('operator', ''),
            change_time=data.get('change_time', ''),
            remarks=data.get('remarks', '')
        )
