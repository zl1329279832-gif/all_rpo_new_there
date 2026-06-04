from typing import Optional, List, Dict, Any
from datetime import datetime

from database import DatabaseConnection, CollectionRepository, CollectionNumberExistsError
from models import Collection


class CollectionService:
    def __init__(self, db: DatabaseConnection):
        self.db = db
        self.repo = CollectionRepository(db)

    def get_collections(self, filters: Optional[Dict[str, Any]] = None) -> List[Collection]:
        return self.repo.get_all(filters)

    def get_collection(self, collection_id: int) -> Optional[Collection]:
        return self.repo.get_by_id(collection_id)

    def get_collection_by_no(self, collection_no: str) -> Optional[Collection]:
        return self.repo.get_by_collection_no(collection_no)

    def collection_no_exists(self, collection_no: str, exclude_id: Optional[int] = None) -> bool:
        return self.repo.collection_no_exists(collection_no, exclude_id)

    def create_collection(self, collection: Collection, changed_by: str = "system") -> int:
        errors = collection.validate()
        if errors:
            raise ValueError("; ".join(errors))

        return self.repo.create(collection, changed_by)

    def update_collection(self, collection: Collection, changed_by: str = "system") -> bool:
        errors = collection.validate()
        if errors:
            raise ValueError("; ".join(errors))

        return self.repo.update(collection, changed_by)

    def delete_collection(self, collection_id: int, changed_by: str = "system") -> bool:
        return self.repo.delete(collection_id, changed_by)

    def update_conservation_status(
        self, collection_id: int, new_status: str, changed_by: str = "system"
    ) -> bool:
        return self.repo.update_conservation_status(collection_id, new_status, changed_by)

    def get_statistics(self) -> Dict[str, Any]:
        return self.repo.get_statistics()

    def generate_collection_no(self, prefix: str = "WW") -> str:
        today = datetime.now().strftime("%Y%m%d")
        base_no = f"{prefix}{today}"

        with self.db.get_cursor() as cursor:
            cursor.execute(
                "SELECT collection_no FROM collections WHERE collection_no LIKE ? ORDER BY collection_no DESC LIMIT 1",
                (f"{base_no}%",),
            )
            result = cursor.fetchone()

            if result:
                last_no = result[0]
                try:
                    seq = int(last_no[-3:]) + 1
                except ValueError:
                    seq = 1
            else:
                seq = 1

            return f"{base_no}{seq:03d}"

    def search_collections(self, keyword: str) -> List[Collection]:
        filters = {
            "collection_no": keyword,
            "name": keyword,
        }
        return self.repo.get_all(filters)
