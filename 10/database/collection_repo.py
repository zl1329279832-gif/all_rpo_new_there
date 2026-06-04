import sqlite3
from typing import Optional, List, Dict, Any, Tuple
from datetime import datetime

from .connection import DatabaseConnection
from models import Collection
from config.settings import CONSERVATION_STATUSES


class CollectionNumberExistsError(Exception):
    pass


class CollectionRepository:
    def __init__(self, db: DatabaseConnection):
        self.db = db

    def get_all(self, filters: Optional[Dict[str, Any]] = None) -> List[Collection]:
        query = "SELECT * FROM collections WHERE 1=1"
        params = []

        if filters:
            if filters.get("collection_no"):
                query += " AND collection_no LIKE ?"
                params.append(f"%{filters['collection_no']}%")
            if filters.get("name"):
                query += " AND name LIKE ?"
                params.append(f"%{filters['name']}%")
            if filters.get("category"):
                query += " AND category = ?"
                params.append(filters["category"])
            if filters.get("era"):
                query += " AND era = ?"
                params.append(filters["era"])
            if filters.get("source"):
                query += " AND source = ?"
                params.append(filters["source"])
            if filters.get("conservation_status"):
                query += " AND conservation_status = ?"
                params.append(filters["conservation_status"])
            if filters.get("start_date"):
                query += " AND entry_date >= ?"
                params.append(filters["start_date"])
            if filters.get("end_date"):
                query += " AND entry_date <= ?"
                params.append(filters["end_date"])

        query += " ORDER BY created_at DESC"

        with self.db.get_cursor() as cursor:
            cursor.execute(query, params)
            rows = cursor.fetchall()
            return [Collection.from_dict(dict(row)) for row in rows]

    def get_by_id(self, collection_id: int) -> Optional[Collection]:
        with self.db.get_cursor() as cursor:
            cursor.execute("SELECT * FROM collections WHERE id = ?", (collection_id,))
            row = cursor.fetchone()
            return Collection.from_dict(dict(row)) if row else None

    def get_by_collection_no(self, collection_no: str) -> Optional[Collection]:
        with self.db.get_cursor() as cursor:
            cursor.execute("SELECT * FROM collections WHERE collection_no = ?", (collection_no,))
            row = cursor.fetchone()
            return Collection.from_dict(dict(row)) if row else None

    def collection_no_exists(self, collection_no: str, exclude_id: Optional[int] = None) -> bool:
        query = "SELECT COUNT(*) FROM collections WHERE collection_no = ?"
        params = [collection_no]
        if exclude_id:
            query += " AND id != ?"
            params.append(exclude_id)

        with self.db.get_cursor() as cursor:
            cursor.execute(query, params)
            return cursor.fetchone()[0] > 0

    def create(self, collection: Collection, changed_by: str = "system") -> int:
        if self.collection_no_exists(collection.collection_no):
            raise CollectionNumberExistsError(f"藏品编号 '{collection.collection_no}' 已存在")

        collection.created_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        collection.updated_at = collection.created_at
        collection.created_by = changed_by
        collection.updated_by = changed_by

        with self.db.get_cursor() as cursor:
            cursor.execute(
                """
                INSERT INTO collections (
                    collection_no, name, era, category, source, conservation_status,
                    entry_date, description, location, estimated_value,
                    created_at, updated_at, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                (
                    collection.collection_no,
                    collection.name,
                    collection.era,
                    collection.category,
                    collection.source,
                    collection.conservation_status,
                    collection.entry_date,
                    collection.description,
                    collection.location,
                    collection.estimated_value,
                    collection.created_at,
                    collection.updated_at,
                    collection.created_by,
                    collection.updated_by,
                ),
            )
            collection_id = cursor.lastrowid
            self.db.log_audit(
                "collections", collection_id, "INSERT", None, collection.to_dict(), changed_by
            )
            return collection_id

    def update(self, collection: Collection, changed_by: str = "system") -> bool:
        if self.collection_no_exists(collection.collection_no, collection.id):
            raise CollectionNumberExistsError(f"藏品编号 '{collection.collection_no}' 已存在")

        old_collection = self.get_by_id(collection.id)
        if not old_collection:
            return False

        collection.updated_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        collection.updated_by = changed_by

        with self.db.get_cursor() as cursor:
            cursor.execute(
                """
                UPDATE collections SET
                    collection_no = ?, name = ?, era = ?, category = ?, source = ?,
                    conservation_status = ?, entry_date = ?, description = ?,
                    location = ?, estimated_value = ?, updated_at = ?, updated_by = ?
                WHERE id = ?
                """,
                (
                    collection.collection_no,
                    collection.name,
                    collection.era,
                    collection.category,
                    collection.source,
                    collection.conservation_status,
                    collection.entry_date,
                    collection.description,
                    collection.location,
                    collection.estimated_value,
                    collection.updated_at,
                    collection.updated_by,
                    collection.id,
                ),
            )
            self.db.log_audit(
                "collections", collection.id, "UPDATE", old_collection.to_dict(), collection.to_dict(), changed_by
            )
            return cursor.rowcount > 0

    def update_conservation_status(
        self, collection_id: int, new_status: str, changed_by: str = "system"
    ) -> bool:
        if new_status not in CONSERVATION_STATUSES:
            raise ValueError(f"无效的保存状态: {new_status}")

        old_collection = self.get_by_id(collection_id)
        if not old_collection:
            return False

        updated_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

        with self.db.get_cursor() as cursor:
            cursor.execute(
                """
                UPDATE collections SET conservation_status = ?, updated_at = ?, updated_by = ?
                WHERE id = ?
                """,
                (new_status, updated_at, changed_by, collection_id),
            )

            new_collection = self.get_by_id(collection_id)
            self.db.log_audit(
                "collections",
                collection_id,
                "UPDATE",
                {"conservation_status": old_collection.conservation_status},
                {"conservation_status": new_status},
                changed_by,
            )
            return cursor.rowcount > 0

    def delete(self, collection_id: int, changed_by: str = "system") -> bool:
        old_collection = self.get_by_id(collection_id)
        if not old_collection:
            return False

        with self.db.get_cursor() as cursor:
            cursor.execute("DELETE FROM collections WHERE id = ?", (collection_id,))
            self.db.log_audit(
                "collections", collection_id, "DELETE", old_collection.to_dict(), None, changed_by
            )
            return cursor.rowcount > 0

    def get_statistics(self) -> Dict[str, Any]:
        stats = {}

        with self.db.get_cursor() as cursor:
            cursor.execute("SELECT COUNT(*) FROM collections")
            stats["total_collections"] = cursor.fetchone()[0]

            cursor.execute("SELECT category, COUNT(*) FROM collections GROUP BY category")
            stats["by_category"] = dict(cursor.fetchall())

            cursor.execute("SELECT era, COUNT(*) FROM collections GROUP BY era")
            stats["by_era"] = dict(cursor.fetchall())

            cursor.execute("SELECT conservation_status, COUNT(*) FROM collections GROUP BY conservation_status")
            stats["by_status"] = dict(cursor.fetchall())

            cursor.execute("SELECT source, COUNT(*) FROM collections GROUP BY source")
            stats["by_source"] = dict(cursor.fetchall())

            cursor.execute(
                "SELECT DATE(entry_date) as date, COUNT(*) FROM collections "
                "WHERE entry_date IS NOT NULL GROUP BY DATE(entry_date) ORDER BY date DESC LIMIT 30"
            )
            stats["recent_entries"] = cursor.fetchall()

        return stats

    def get_distinct_values(self, column: str) -> List[str]:
        with self.db.get_cursor() as cursor:
            cursor.execute(f"SELECT DISTINCT {column} FROM collections WHERE {column} IS NOT NULL")
            return [row[0] for row in cursor.fetchall()]
