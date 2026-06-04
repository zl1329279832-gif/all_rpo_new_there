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

    def can_preview(self, attachment: Attachment) -> tuple[bool, str]:
        if not attachment.file_exists():
            return False, "文件不存在"

        preview_extensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.txt', '.pdf']
        ext = attachment.get_file_extension()

        if ext in preview_extensions:
            return True, "可以预览"
        else:
            return False, "该文件类型不支持预览，请下载后查看"

    def open_preview(self, attachment: Attachment):
        if not attachment.file_exists():
            raise FileNotFoundError("附件文件不存在")

        can_preview, message = self.can_preview(attachment)
        if not can_preview:
            raise ValueError(message)

        import os
        if os.name == 'nt':
            os.startfile(attachment.file_path)
        else:
            import subprocess
            subprocess.run(['xdg-open', attachment.file_path])

    def rebind_attachment(self, attachment_id: int, new_file_path: str) -> Attachment:
        attachment = self.get_attachment_by_id(attachment_id)
        if not attachment:
            raise ValueError("附件不存在")

        new_file = Path(new_file_path)
        if not new_file.exists():
            raise FileNotFoundError("新文件不存在")

        old_file = Path(attachment.file_path)
        if old_file.exists():
            try:
                old_file.unlink()
            except:
                pass

        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        dest_filename = f"{attachment.sample_id}_{timestamp}_{new_file.name}"
        dest_path = self.attachment_dir / dest_filename

        shutil.copy2(new_file_path, dest_path)

        attachment.file_name = new_file.name
        attachment.file_path = str(dest_path)
        attachment.file_size = new_file.stat().st_size
        attachment.file_type = new_file.suffix.lower()

        sql = """
        UPDATE attachments 
        SET file_name = ?, file_path = ?, file_size = ?, file_type = ?, uploaded_at = ?
        WHERE id = ?
        """
        params = (
            attachment.file_name,
            attachment.file_path,
            attachment.file_size,
            attachment.file_type,
            datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
            attachment.id
        )

        self.db.execute(sql, params)

        return attachment

    def validate_attachment_path(self, file_path: str) -> tuple[bool, str]:
        path = Path(file_path)

        if not path.is_absolute():
            return False, "文件路径必须是绝对路径"

        if not path.exists():
            return False, "文件不存在"

        if not path.is_file():
            return False, "路径指向的不是文件"

        if path.stat().st_size == 0:
            return False, "文件为空"

        max_size = 100 * 1024 * 1024
        if path.stat().st_size > max_size:
            return False, f"文件大小超过限制 (最大 {max_size // 1024 // 1024}MB)"

        return True, "文件路径有效"

    def get_attachment_statistics(self) -> dict:
        sql = "SELECT COUNT(*) as count, SUM(file_size) as total_size FROM attachments"
        result = self.db.fetch_one(sql)

        missing = len(self.get_missing_attachments())

        stats = {
            'total_count': result['count'] if result else 0,
            'total_size': result['total_size'] if result and result['total_size'] else 0,
            'missing_count': missing
        }

        return stats

    def get_preview_info(self, attachment: Attachment) -> dict:
        can_preview, message = self.can_preview(attachment)
        return {
            'can_preview': can_preview,
            'message': message,
            'file_exists': attachment.file_exists(),
            'file_size_formatted': attachment.format_file_size(),
            'extension': attachment.get_file_extension()
        }
