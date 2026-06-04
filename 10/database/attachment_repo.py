from typing import Optional, List
from datetime import datetime
from pathlib import Path

from .connection import DatabaseConnection
from models import Attachment


class AttachmentRepository:
    def __init__(self, db: DatabaseConnection):
        self.db = db

    def get_all(self, collection_id: Optional[int] = None, only_images: bool = False) -> List[Attachment]:
        query = "SELECT * FROM attachments WHERE 1=1"
        params = []

        if collection_id:
            query += " AND collection_id = ?"
            params.append(collection_id)
        if only_images:
            query += " AND is_image = 1"

        query += " ORDER BY uploaded_at DESC"

        with self.db.get_cursor() as cursor:
            cursor.execute(query, params)
            rows = cursor.fetchall()
            return [Attachment.from_dict(dict(row)) for row in rows]

    def get_by_id(self, attachment_id: int) -> Optional[Attachment]:
        with self.db.get_cursor() as cursor:
            cursor.execute("SELECT * FROM attachments WHERE id = ?", (attachment_id,))
            row = cursor.fetchone()
            return Attachment.from_dict(dict(row)) if row else None

    def get_missing_files(self) -> List[Attachment]:
        attachments = self.get_all()
        return [a for a in attachments if not a.file_exists()]

    def create(self, attachment: Attachment, changed_by: str = "system") -> int:
        attachment.uploaded_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        attachment.uploaded_by = changed_by

        with self.db.get_cursor() as cursor:
            cursor.execute(
                """
                INSERT INTO attachments (
                    collection_id, file_name, file_path, file_size,
                    file_type, is_image, description, uploaded_at, uploaded_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                (
                    attachment.collection_id,
                    attachment.file_name,
                    attachment.file_path,
                    attachment.file_size,
                    attachment.file_type,
                    1 if attachment.is_image else 0,
                    attachment.description,
                    attachment.uploaded_at,
                    attachment.uploaded_by,
                ),
            )
            attachment_id = cursor.lastrowid
            self.db.log_audit("attachments", attachment_id, "INSERT", None, attachment.to_dict(), changed_by)
            return attachment_id

    def update(self, attachment: Attachment, changed_by: str = "system") -> bool:
        old_attachment = self.get_by_id(attachment.id)
        if not old_attachment:
            return False

        with self.db.get_cursor() as cursor:
            cursor.execute(
                """
                UPDATE attachments SET description = ? WHERE id = ?
                """,
                (attachment.description, attachment.id),
            )
            self.db.log_audit(
                "attachments",
                attachment.id,
                "UPDATE",
                {"description": old_attachment.description},
                {"description": attachment.description},
                changed_by,
            )
            return cursor.rowcount > 0

    def delete(self, attachment_id: int, delete_file: bool = False, changed_by: str = "system") -> bool:
        old_attachment = self.get_by_id(attachment_id)
        if not old_attachment:
            return False

        if delete_file and old_attachment.file_exists():
            try:
                Path(old_attachment.file_path).unlink()
            except OSError:
                pass

        with self.db.get_cursor() as cursor:
            cursor.execute("DELETE FROM attachments WHERE id = ?", (attachment_id,))
            self.db.log_audit(
                "attachments",
                attachment_id,
                "DELETE",
                old_attachment.to_dict(),
                None,
                changed_by,
            )
            return cursor.rowcount > 0

    def get_count_by_collection(self, collection_id: int) -> int:
        with self.db.get_cursor() as cursor:
            cursor.execute("SELECT COUNT(*) FROM attachments WHERE collection_id = ?", (collection_id,))
            return cursor.fetchone()[0]
