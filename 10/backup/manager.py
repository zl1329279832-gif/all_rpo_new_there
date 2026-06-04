import shutil
import sqlite3
from pathlib import Path
from datetime import datetime
from typing import Optional, List, Dict, Any

from config.settings import DATABASE_PATH, BACKUPS_DIR, ATTACHMENTS_DIR


class BackupError(Exception):
    pass


class BackupManager:
    def __init__(self):
        self.backups_dir = Path(BACKUPS_DIR)
        self.database_path = Path(DATABASE_PATH)
        self.attachments_dir = Path(ATTACHMENTS_DIR)
        self.backups_dir.mkdir(parents=True, exist_ok=True)

    def _generate_backup_name(self, prefix: str = "backup") -> str:
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        return f"{prefix}_{timestamp}"

    def create_database_backup(self, custom_name: Optional[str] = None) -> str:
        if not self.database_path.exists():
            raise BackupError("数据库文件不存在")

        backup_name = custom_name or self._generate_backup_name("db")
        backup_filename = f"{backup_name}.db"
        backup_path = self.backups_dir / backup_filename

        try:
            src_conn = sqlite3.connect(str(self.database_path))
            dst_conn = sqlite3.connect(str(backup_path))
            src_conn.backup(dst_conn)
            src_conn.close()
            dst_conn.close()
        except sqlite3.Error as e:
            if backup_path.exists():
                backup_path.unlink(missing_ok=True)
            raise BackupError(f"数据库备份失败: {str(e)}")

        return str(backup_path)

    def create_full_backup(self, custom_name: Optional[str] = None) -> str:
        backup_name = custom_name or self._generate_backup_name("full")
        backup_path = self.backups_dir / backup_name
        backup_path.mkdir(parents=True, exist_ok=True)

        try:
            if self.database_path.exists():
                db_backup_path = backup_path / "collections.db"
                src_conn = sqlite3.connect(str(self.database_path))
                dst_conn = sqlite3.connect(str(db_backup_path))
                src_conn.backup(dst_conn)
                src_conn.close()
                dst_conn.close()

            if self.attachments_dir.exists():
                attachments_backup = backup_path / "attachments"
                shutil.copytree(
                    self.attachments_dir, attachments_backup, dirs_exist_ok=True
                )

            info_file = backup_path / "backup_info.txt"
            with open(info_file, "w", encoding="utf-8") as f:
                f.write("文物藏品管理系统 - 完整备份\n")
                f.write(f"备份时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
                f.write(f"备份类型: 完整备份\n")

            zip_path = self.backups_dir / backup_name
            shutil.make_archive(str(zip_path), "zip", root_dir=str(backup_path))
            shutil.rmtree(backup_path)

            return f"{zip_path}.zip"

        except Exception as e:
            if backup_path.exists():
                shutil.rmtree(backup_path, ignore_errors=True)
            raise BackupError(f"完整备份失败: {str(e)}")

    def restore_database_backup(self, backup_file: str, db=None) -> bool:
        backup_path = Path(backup_file)
        if not backup_path.exists():
            raise BackupError("备份文件不存在")

        try:
            if db is not None:
                db.close()

            if self.database_path.exists():
                bak_suffix = datetime.now().strftime('%Y%m%d%H%M%S')
                bak_path = f"{self.database_path}.bak_{bak_suffix}"
                self.database_path.rename(bak_path)

            src_conn = sqlite3.connect(str(backup_path))
            dst_conn = sqlite3.connect(str(self.database_path))
            src_conn.backup(dst_conn)
            src_conn.close()
            dst_conn.close()

            if db is not None:
                db.reconnect()

            return True
        except sqlite3.Error as e:
            if db is not None:
                try:
                    db.reconnect()
                except Exception:
                    pass
            raise BackupError(f"数据库恢复失败: {str(e)}")

    def get_backup_list(self) -> List[Dict[str, Any]]:
        backups = []
        for file in self.backups_dir.iterdir():
            if file.is_file() and (file.suffix == ".db" or file.suffix == ".zip"):
                backups.append(
                    {
                        "name": file.name,
                        "path": str(file),
                        "size": file.stat().st_size,
                        "type": "database" if file.suffix == ".db" else "full",
                        "created_at": datetime.fromtimestamp(file.stat().st_mtime).strftime(
                            "%Y-%m-%d %H:%M:%S"
                        ),
                    }
                )
        return sorted(backups, key=lambda x: x["created_at"], reverse=True)

    def delete_backup(self, backup_file: str) -> bool:
        backup_path = Path(backup_file)
        if not backup_path.exists():
            return False
        try:
            backup_path.unlink()
            return True
        except OSError:
            return False

    def format_size(self, size_bytes: int) -> str:
        if size_bytes < 1024:
            return f"{size_bytes} B"
        elif size_bytes < 1024 * 1024:
            return f"{size_bytes / 1024:.1f} KB"
        elif size_bytes < 1024 * 1024 * 1024:
            return f"{size_bytes / (1024 * 1024):.1f} MB"
        else:
            return f"{size_bytes / (1024 * 1024 * 1024):.2f} GB"

    def get_backup_stats(self) -> Dict[str, Any]:
        backups = self.get_backup_list()
        total_size = sum(b["size"] for b in backups)
        db_backups = [b for b in backups if b["type"] == "database"]
        full_backups = [b for b in backups if b["type"] == "full"]

        return {
            "total_count": len(backups),
            "db_count": len(db_backups),
            "full_count": len(full_backups),
            "total_size": total_size,
            "total_size_formatted": self.format_size(total_size),
        }
