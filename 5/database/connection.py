import sqlite3
from contextlib import contextmanager
from pathlib import Path
from typing import List, Dict, Any, Optional, Tuple

from config.settings import DATABASE_PATH, ensure_directories


class DatabaseConnection:
    _instance = None
    _connection = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
        return cls._instance

    def __init__(self):
        if self._connection is None:
            ensure_directories()
            self._connect()

    def _connect(self):
        try:
            self._connection = sqlite3.connect(
                str(DATABASE_PATH),
                detect_types=sqlite3.PARSE_DECLTYPES | sqlite3.PARSE_COLNAMES
            )
            self._connection.row_factory = sqlite3.Row
            self._connection.execute("PRAGMA foreign_keys = ON")
        except sqlite3.Error as e:
            raise Exception(f"数据库连接失败: {str(e)}")

    def get_connection(self) -> sqlite3.Connection:
        if self._connection is None:
            self._connect()
        return self._connection

    def close(self):
        if self._connection:
            self._connection.close()
            self._connection = None

    @contextmanager
    def cursor(self):
        conn = self.get_connection()
        cursor = conn.cursor()
        try:
            yield cursor
            conn.commit()
        except Exception as e:
            conn.rollback()
            raise e
        finally:
            cursor.close()

    def execute(self, sql: str, params: Tuple = ()) -> int:
        with self.cursor() as cursor:
            cursor.execute(sql, params)
            return cursor.lastrowid

    def execute_many(self, sql: str, params_list: List[Tuple]) -> None:
        with self.cursor() as cursor:
            cursor.executemany(sql, params_list)

    def fetch_one(self, sql: str, params: Tuple = ()) -> Optional[Dict[str, Any]]:
        with self.cursor() as cursor:
            cursor.execute(sql, params)
            row = cursor.fetchone()
            return dict(row) if row else None

    def fetch_all(self, sql: str, params: Tuple = ()) -> List[Dict[str, Any]]:
        with self.cursor() as cursor:
            cursor.execute(sql, params)
            rows = cursor.fetchall()
            return [dict(row) for row in rows]
