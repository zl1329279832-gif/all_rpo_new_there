from typing import Optional, List

from database import DatabaseConnection, ExhibitionRepository
from models import Exhibition


class ExhibitionService:
    def __init__(self, db: DatabaseConnection):
        self.db = db
        self.repo = ExhibitionRepository(db)

    def get_exhibitions(self, collection_id: Optional[int] = None) -> List[Exhibition]:
        return self.repo.get_all(collection_id)

    def get_exhibition(self, exhibition_id: int) -> Optional[Exhibition]:
        return self.repo.get_by_id(exhibition_id)

    def create_exhibition(self, exhibition: Exhibition, changed_by: str = "system") -> int:
        errors = exhibition.validate()
        if errors:
            raise ValueError("; ".join(errors))

        return self.repo.create(exhibition, changed_by)

    def update_exhibition(self, exhibition: Exhibition, changed_by: str = "system") -> bool:
        errors = exhibition.validate()
        if errors:
            raise ValueError("; ".join(errors))

        return self.repo.update(exhibition, changed_by)

    def delete_exhibition(self, exhibition_id: int, changed_by: str = "system") -> bool:
        return self.repo.delete(exhibition_id, changed_by)

    def get_exhibition_count(self, collection_id: int) -> int:
        return self.repo.get_count_by_collection(collection_id)

    def get_current_exhibitions(self) -> List[Exhibition]:
        return self.repo.get_current_exhibitions()
