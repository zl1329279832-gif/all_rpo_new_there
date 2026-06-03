from dataclasses import dataclass, field
from datetime import datetime
from typing import Optional


@dataclass
class TestRecord:
    id: Optional[int] = None
    sample_id: int = 0
    test_item: str = ""
    tester: str = ""
    test_time: str = ""
    test_result: str = ""
    result_value: str = ""
    standard_value: str = ""
    is_qualified: bool = True
    remarks: str = ""
    created_at: str = field(default_factory=lambda: datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
    updated_at: str = field(default_factory=lambda: datetime.now().strftime("%Y-%m-%d %H:%M:%S"))

    def to_dict(self) -> dict:
        return {
            'id': self.id,
            'sample_id': self.sample_id,
            'test_item': self.test_item,
            'tester': self.tester,
            'test_time': self.test_time,
            'test_result': self.test_result,
            'result_value': self.result_value,
            'standard_value': self.standard_value,
            'is_qualified': 1 if self.is_qualified else 0,
            'remarks': self.remarks,
            'created_at': self.created_at,
            'updated_at': self.updated_at
        }

    @classmethod
    def from_dict(cls, data: dict) -> 'TestRecord':
        return cls(
            id=data.get('id'),
            sample_id=data.get('sample_id', 0),
            test_item=data.get('test_item', ''),
            tester=data.get('tester', ''),
            test_time=data.get('test_time', ''),
            test_result=data.get('test_result', ''),
            result_value=data.get('result_value', ''),
            standard_value=data.get('standard_value', ''),
            is_qualified=bool(data.get('is_qualified', 1)),
            remarks=data.get('remarks', ''),
            created_at=data.get('created_at', ''),
            updated_at=data.get('updated_at', '')
        )
