from dataclasses import dataclass, field
from datetime import datetime
from typing import Optional


@dataclass
class AuditLog:
    id: Optional[int] = None
    sample_id: Optional[int] = None
    operation_type: str = ""
    field_name: str = ""
    old_value: str = ""
    new_value: str = ""
    operator: str = ""
    operation_time: str = field(default_factory=lambda: datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
    remarks: str = ""

    def to_dict(self) -> dict:
        return {
            'id': self.id,
            'sample_id': self.sample_id,
            'operation_type': self.operation_type,
            'field_name': self.field_name,
            'old_value': self.old_value,
            'new_value': self.new_value,
            'operator': self.operator,
            'operation_time': self.operation_time,
            'remarks': self.remarks
        }

    @classmethod
    def from_dict(cls, data: dict) -> 'AuditLog':
        return cls(
            id=data.get('id'),
            sample_id=data.get('sample_id'),
            operation_type=data.get('operation_type', ''),
            field_name=data.get('field_name', ''),
            old_value=data.get('old_value', ''),
            new_value=data.get('new_value', ''),
            operator=data.get('operator', ''),
            operation_time=data.get('operation_time', ''),
            remarks=data.get('remarks', '')
        )

    @classmethod
    def create_update_log(
        cls,
        sample_id: int,
        field_name: str,
        old_value: str,
        new_value: str,
        operator: str,
        remarks: str = ""
    ) -> 'AuditLog':
        return cls(
            sample_id=sample_id,
            operation_type="修改",
            field_name=field_name,
            old_value=old_value,
            new_value=new_value,
            operator=operator,
            remarks=remarks
        )

    @classmethod
    def create_delete_log(
        cls,
        sample_id: int,
        operator: str,
        remarks: str = ""
    ) -> 'AuditLog':
        return cls(
            sample_id=sample_id,
            operation_type="删除",
            operator=operator,
            remarks=remarks
        )
