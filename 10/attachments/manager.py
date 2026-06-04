import shutil
import uuid
from pathlib import Path
from typing import Optional, List, Tuple

from config.settings import ATTACHMENTS_DIR, ALLOWED_IMAGE_EXTENSIONS, ALLOWED_DOC_EXTENSIONS, MAX_ATTACHMENT_SIZE
from database import DatabaseConnection, AttachmentRepository
from models import Attachment


class AttachmentError(Exception):
    pass


class AttachmentManager:
    def __init__(self, db: DatabaseConnection):
        self.db = db
        self.repo = AttachmentRepository(db)
        self.attachments_dir = Path(ATTACHMENTS_DIR)
        self.attachments_dir.mkdir(parents=True, exist_ok=True)

    def _get_collection_dir(self, collection_id: int) -> Path:
        collection_dir = self.attachments_dir / str(collection_id)
        collection_dir.mkdir(parents=True, exist_ok=True)
        return collection_dir

    def _generate_unique_filename(self, original_name: str) -> str:
        ext = Path(original_name).suffix.lower()
        unique_name = f"{uuid.uuid4().hex}{ext}"
        return unique_name

    def validate_file(self, file_path: str) -> Tuple[bool, str]:
        path = Path(file_path)
        if not path.exists():
            return False, "文件不存在"

        if path.stat().st_size > MAX_ATTACHMENT_SIZE:
            return False, f"文件大小超过限制 (最大 {MAX_ATTACHMENT_SIZE // (1024 * 1024)}MB)"

        ext = path.suffix.lower()
        allowed_extensions = ALLOWED_IMAGE_EXTENSIONS | ALLOWED_DOC_EXTENSIONS
        if ext not in allowed_extensions:
            return False, f"不支持的文件类型: {ext}"

        return True, "验证通过"

    def is_image_file(self, file_path: str) -> bool:
        ext = Path(file_path).suffix.lower()
        return ext in ALLOWED_IMAGE_EXTENSIONS

    def add_attachment(
        self,
        collection_id: int,
        source_path: str,
        description: str = "",
        changed_by: str = "system",
    ) -> Optional[int]:
        is_valid, error_msg = self.validate_file(source_path)
        if not is_valid:
            raise AttachmentError(error_msg)

        source = Path(source_path)
        collection_dir = self._get_collection_dir(collection_id)
        unique_filename = self._generate_unique_filename(source.name)
        dest_path = collection_dir / unique_filename

        try:
            shutil.copy2(source_path, dest_path)
        except OSError as e:
            raise AttachmentError(f"文件复制失败: {str(e)}")

        attachment = Attachment(
            collection_id=collection_id,
            file_name=source.name,
            file_path=str(dest_path),
            file_size=source.stat().st_size,
            file_type=source.suffix.lower().lstrip("."),
            is_image=self.is_image_file(source_path),
            description=description,
        )

        return self.repo.create(attachment, changed_by)

    def get_attachments(self, collection_id: Optional[int] = None, only_images: bool = False) -> List[Attachment]:
        return self.repo.get_all(collection_id, only_images)

    def get_attachment(self, attachment_id: int) -> Optional[Attachment]:
        return self.repo.get_by_id(attachment_id)

    def get_missing_attachments(self) -> List[Attachment]:
        return self.repo.get_missing_files()

    def update_attachment_description(self, attachment_id: int, description: str, changed_by: str = "system") -> bool:
        attachment = self.repo.get_by_id(attachment_id)
        if not attachment:
            return False

        attachment.description = description
        return self.repo.update(attachment, changed_by)

    def delete_attachment(self, attachment_id: int, delete_file: bool = True, changed_by: str = "system") -> bool:
        return self.repo.delete(attachment_id, delete_file, changed_by)

    def get_attachment_count(self, collection_id: int) -> int:
        return self.repo.get_count_by_collection(collection_id)

    def get_image_attachments(self, collection_id: int) -> List[Attachment]:
        return self.get_attachments(collection_id, only_images=True)

    def open_file(self, attachment_id: int) -> bool:
        import subprocess
        import platform

        attachment = self.get_attachment(attachment_id)
        if not attachment:
            return False

        if not attachment.file_exists():
            raise AttachmentError("文件不存在或已丢失")

        try:
            if platform.system() == "Windows":
                subprocess.Popen(["explorer", "/select,", attachment.file_path])
            elif platform.system() == "Darwin":
                subprocess.Popen(["open", attachment.file_path])
            else:
                subprocess.Popen(["xdg-open", attachment.file_path])
            return True
        except OSError:
            return False

    def get_file_preview_path(self, attachment_id: int) -> Optional[str]:
        attachment = self.get_attachment(attachment_id)
        if not attachment or not attachment.is_image or not attachment.file_exists():
            return None
        return attachment.file_path
