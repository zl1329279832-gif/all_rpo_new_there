from typing import Optional, List
from datetime import datetime

from .connection import DatabaseConnection
from models import Exhibition


class ExhibitionRepository:
    def __init__(self, db: DatabaseConnection):
        self.db = db

    def get_all(self, collection_id: Optional[int] = None) -> List[Exhibition]:
        query = "SELECT * FROM exhibitions WHERE 1=1"
        params = []

        if collection_id:
            query += " AND collection_id = ?"
            params.append(collection_id)

        query += " ORDER BY start_date DESC"

        with self.db.get_cursor() as cursor:
            cursor.execute(query, params)
            rows = cursor.fetchall()
            return [Exhibition.from_dict(dict(row)) for row in rows]

    def get_by_id(self, exhibition_id: int) -> Optional[Exhibition]:
        with self.db.get_cursor() as cursor:
            cursor.execute("SELECT * FROM exhibitions WHERE id = ?", (exhibition_id,))
            row = cursor.fetchone()
            return Exhibition.from_dict(dict(row)) if row else None

    def create(self, exhibition: Exhibition, changed_by: str = "system") -> int:
        exhibition.created_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

        with self.db.get_cursor() as cursor:
            cursor.execute(
                """
                INSERT INTO exhibitions (
                    collection_id, exhibition_name, location, start_date,
                    end_date, organizer, notes, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                (
                    exhibition.collection_id,
                    exhibition.exhibition_name,
                    exhibition.location,
                    exhibition.start_date,
                    exhibition.end_date,
                    exhibition.organizer,
                    exhibition.notes,
                    exhibition.created_at,
                ),
            )
            exhibition_id = cursor.lastrowid
            self.db.log_audit("exhibitions", exhibition_id, "INSERT", None, exhibition.to_dict(), changed_by)
            return exhibition_id

    def update(self, exhibition: Exhibition, changed_by: str = "system") -> bool:
        old_exhibition = self.get_by_id(exhibition.id)
        if not old_exhibition:
            return False

        with self.db.get_cursor() as cursor:
            cursor.execute(
                """
                UPDATE exhibitions SET
                    exhibition_name = ?, location = ?, start_date = ?,
                    end_date = ?, organizer = ?, notes = ?
                WHERE id = ?
                """,
                (
                    exhibition.exhibition_name,
                    exhibition.location,
                    exhibition.start_date,
                    exhibition.end_date,
                    exhibition.organizer,
                    exhibition.notes,
                    exhibition.id,
                ),
            )
            self.db.log_audit(
                "exhibitions",
                exhibition.id,
                "UPDATE",
                old_exhibition.to_dict(),
                exhibition.to_dict(),
                changed_by,
            )
            return cursor.rowcount > 0

    def delete(self, exhibition_id: int, changed_by: str = "system") -> bool:
        old_exhibition = self.get_by_id(exhibition_id)
        if not old_exhibition:
            return False

        with self.db.get_cursor() as cursor:
            cursor.execute("DELETE FROM exhibitions WHERE id = ?", (exhibition_id,))
            self.db.log_audit(
                "exhibitions",
                exhibition_id,
                "DELETE",
                old_exhibition.to_dict(),
                None,
                changed_by,
            )
            return cursor.rowcount > 0

    def get_count_by_collection(self, collection_id: int) -> int:
        with self.db.get_cursor() as cursor:
            cursor.execute("SELECT COUNT(*) FROM exhibitions WHERE collection_id = ?", (collection_id,))
            return cursor.fetchone()[0]

    def get_current_exhibitions(self, date_str: Optional[str] = None) -> List[Exhibition]:
        if date_str is None:
            date_str = datetime.now().strftime("%Y-%m-%d")

        with self.db.get_cursor() as cursor:
            cursor.execute(
                """
                SELECT * FROM exhibitions
                WHERE start_date <= ? AND end_date >= ?
                ORDER BY start_date DESC
                """,
                (date_str, date_str),
            )
            rows = cursor.fetchall()
            return [Exhibition.from_dict(dict(row)) for row in rows]
