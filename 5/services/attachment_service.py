import shutil
from datetime import datetime
from pathlib import Path
from typing import List, Optional

from database.connection import DatabaseConnection
from models.attachment import Attachment
from config.settings import ATTACHMENT_DIR


class AttachmentService:
    def __init__(self):
        self.db = DatabaseConnection()
        self.attachment_dir = ATTACHMENT_DIR
        self.attachment_dir.mkdir(parents=True, exist_ok=True)

    def upload_attachment(
        self,
        sample_id: int,
        source_path: str,
        file_name: Optional[str] = None,
        uploaded_by: str = "系统",
        description: str = ""
    ) -> Attachment:
        source = Path(source_path)
        if not source.exists():
            raise FileNotFoundError(f"源文件不存在: {source_path}")

        if sample_id <= 0:
            raise ValueError("样品ID不能为空")

        if not file_name:
            file_name = source.name

        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        dest_filename = f"{sample_id}_{timestamp}_{source.name}"
        dest_path = self.attachment_dir / dest_filename

        shutil.copy2(source_path, dest_path)

        attachment = Attachment(
            sample_id=sample_id,
            file_name=file_name,
            file_path=str(dest_path),
            file_size=source.stat().st_size,
            file_type=source.suffix.lower(),
            uploaded_by=uploaded_by,
            description=description
        )

        sql = """
        INSERT INTO attachments (sample_id, file_name, file_path, file_size, file_type, uploaded_by, description)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """
        params = (
            attachment.sample_id,
            attachment.file_name,
            attachment.file_path,
            attachment.file_size,
            attachment.file_type,
            attachment.uploaded_by,
            attachment.description
        )

        attachment.id = self.db.execute(sql, params)
        return attachment

    def delete_attachment(self, attachment_id: int, delete_file: bool = True) -> bool:
        attachment = self.get_attachment_by_id(attachment_id)
        if not attachment:
            raise ValueError("附件不存在")

        if delete_file:
            try:
                file_path = Path(attachment.file_path)
                if file_path.exists():
                    file_path.unlink()
            except Exception as e:
                print(f"删除文件失败: {e}")

        sql = "DELETE FROM attachments WHERE id = ?"
        self.db.execute(sql, (attachment_id,))
        return True

    def get_attachment_by_id(self, attachment_id: int) -> Optional[Attachment]:
        sql = "SELECT * FROM attachments WHERE id = ?"
        result = self.db.fetch_one(sql, (attachment_id,))
        if result:
            return Attachment.from_dict(result)
        return None

    def get_attachments_by_sample_id(self, sample_id: int) -> List[Attachment]:
        sql = "SELECT * FROM attachments WHERE sample_id = ? ORDER BY uploaded_at DESC"
        results = self.db.fetch_all(sql, (sample_id,))
        return [Attachment.from_dict(r) for r in results]

    def get_missing_attachments(self) -> List[Attachment]:
        attachments = self.get_all_attachments()
        missing = []
        for att in attachments:
            if not att.file_exists():
                missing.append(att)
        return missing

    def check_attachments_exist(self, sample_id: int) -> tuple[List[Attachment], List[Attachment]]:
        attachments = self.get_attachments_by_sample_id(sample_id)
        existing = []
        missing = []
        for att in attachments:
            if att.file_exists():
                existing.append(att)
            else:
                missing.append(att)
        return existing, missing

    def get_all_attachments(self) -> List[Attachment]:
        sql = "SELECT * FROM attachments ORDER BY uploaded_at DESC"
        results = self.db.fetch_all(sql)
        return [Attachment.from_dict(r) for r in results]
