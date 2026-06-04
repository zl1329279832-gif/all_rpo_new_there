import sqlite3
import json
from datetime import datetime
from pathlib import Path
from typing import Optional, List, Dict, Any, Tuple
from contextlib import contextmanager

from config.settings import DATABASE_PATH
from models import Collection, RepairRecord, Attachment, Exhibition, AuditLog


class DatabaseInitializationError(Exception):
    pass


class DatabaseConnection:
    _instance: Optional["DatabaseConnection"] = None
    _conn: Optional[sqlite3.Connection] = None

    def __new__(cls) -> "DatabaseConnection":
        if cls._instance is None:
            cls._instance = super().__new__(cls)
        return cls._instance

    def __init__(self):
        if self._conn is None:
            self._connect()

    def _connect(self):
        try:
            db_path = Path(DATABASE_PATH)
            db_path.parent.mkdir(parents=True, exist_ok=True)
            self._conn = sqlite3.connect(str(db_path), check_same_thread=False)
            self._conn.row_factory = sqlite3.Row
            self._conn.execute("PRAGMA foreign_keys = ON")
        except sqlite3.Error as e:
            raise DatabaseInitializationError(f"数据库连接失败: {str(e)}")

    @contextmanager
    def get_cursor(self):
        cursor = self._conn.cursor()
        try:
            yield cursor
            self._conn.commit()
        except Exception as e:
            self._conn.rollback()
            raise e
        finally:
            cursor.close()

    def close(self):
        if self._conn:
            self._conn.close()
            self._conn = None

    def initialize_database(self) -> bool:
        try:
            with self.get_cursor() as cursor:
                self._create_tables(cursor)
                self._create_indexes(cursor)
                self._create_triggers(cursor)
            return True
        except sqlite3.Error as e:
            raise DatabaseInitializationError(f"数据库初始化失败: {str(e)}")

    def _create_tables(self, cursor: sqlite3.Cursor):
        cursor.executescript(
            """
            CREATE TABLE IF NOT EXISTS collections (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                collection_no TEXT NOT NULL UNIQUE,
                name TEXT NOT NULL,
                era TEXT NOT NULL,
                category TEXT NOT NULL,
                source TEXT NOT NULL,
                conservation_status TEXT NOT NULL,
                entry_date TEXT,
                description TEXT,
                location TEXT,
                estimated_value REAL,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
                created_by TEXT DEFAULT 'system',
                updated_by TEXT DEFAULT 'system'
            );

            CREATE TABLE IF NOT EXISTS repair_records (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                collection_id INTEGER NOT NULL,
                repair_date TEXT NOT NULL,
                repairer TEXT,
                reason TEXT NOT NULL,
                description TEXT,
                cost REAL,
                status TEXT DEFAULT '待修复',
                notes TEXT,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (collection_id) REFERENCES collections(id) ON DELETE CASCADE
            );

            CREATE TABLE IF NOT EXISTS attachments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                collection_id INTEGER NOT NULL,
                file_name TEXT NOT NULL,
                file_path TEXT NOT NULL,
                file_size INTEGER DEFAULT 0,
                file_type TEXT,
                is_image INTEGER DEFAULT 0,
                description TEXT,
                uploaded_at TEXT DEFAULT CURRENT_TIMESTAMP,
                uploaded_by TEXT DEFAULT 'system',
                FOREIGN KEY (collection_id) REFERENCES collections(id) ON DELETE CASCADE
            );

            CREATE TABLE IF NOT EXISTS exhibitions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                collection_id INTEGER NOT NULL,
                exhibition_name TEXT NOT NULL,
                location TEXT,
                start_date TEXT,
                end_date TEXT,
                organizer TEXT,
                notes TEXT,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (collection_id) REFERENCES collections(id) ON DELETE CASCADE
            );

            CREATE TABLE IF NOT EXISTS audit_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                table_name TEXT NOT NULL,
                record_id INTEGER NOT NULL,
                action TEXT NOT NULL,
                old_value TEXT,
                new_value TEXT,
                changed_by TEXT DEFAULT 'system',
                changed_at TEXT DEFAULT CURRENT_TIMESTAMP
            );
            """
        )

    def _create_indexes(self, cursor: sqlite3.Cursor):
        cursor.executescript(
            """
            CREATE INDEX IF NOT EXISTS idx_collections_no ON collections(collection_no);
            CREATE INDEX IF NOT EXISTS idx_collections_name ON collections(name);
            CREATE INDEX IF NOT EXISTS idx_collections_category ON collections(category);
            CREATE INDEX IF NOT EXISTS idx_collections_era ON collections(era);
            CREATE INDEX IF NOT EXISTS idx_repair_collection ON repair_records(collection_id);
            CREATE INDEX IF NOT EXISTS idx_attachments_collection ON attachments(collection_id);
            CREATE INDEX IF NOT EXISTS idx_exhibitions_collection ON exhibitions(collection_id);
            CREATE INDEX IF NOT EXISTS idx_audit_table ON audit_logs(table_name, record_id);
            """
        )

    def _create_triggers(self, cursor: sqlite3.Cursor):
        cursor.executescript(
            """
            CREATE TRIGGER IF NOT EXISTS update_collections_timestamp
            AFTER UPDATE ON collections
            BEGIN
                UPDATE collections SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
            END;

            CREATE TRIGGER IF NOT EXISTS update_repair_records_timestamp
            AFTER UPDATE ON repair_records
            BEGIN
                UPDATE repair_records SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
            END;
            """
        )

    def log_audit(
        self,
        table_name: str,
        record_id: int,
        action: str,
        old_value: Optional[Dict[str, Any]] = None,
        new_value: Optional[Dict[str, Any]] = None,
        changed_by: str = "system",
    ):
        with self.get_cursor() as cursor:
            cursor.execute(
                """
                INSERT INTO audit_logs (table_name, record_id, action, old_value, new_value, changed_by)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                (
                    table_name,
                    record_id,
                    action,
                    json.dumps(old_value, ensure_ascii=False) if old_value else None,
                    json.dumps(new_value, ensure_ascii=False) if new_value else None,
                    changed_by,
                ),
            )

    def get_audit_logs(self, table_name: Optional[str] = None, record_id: Optional[int] = None) -> List[AuditLog]:
        query = "SELECT * FROM audit_logs WHERE 1=1"
        params = []

        if table_name:
            query += " AND table_name = ?"
            params.append(table_name)
        if record_id:
            query += " AND record_id = ?"
            params.append(record_id)

        query += " ORDER BY changed_at DESC"

        with self.get_cursor() as cursor:
            cursor.execute(query, params)
            rows = cursor.fetchall()
            return [AuditLog.from_dict(dict(row)) for row in rows]
