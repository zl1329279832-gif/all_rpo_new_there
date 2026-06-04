import unittest
import tempfile
import os
from pathlib import Path

from database import DatabaseConnection, CollectionRepository, CollectionNumberExistsError
from models import Collection


class TestDatabaseConnection(unittest.TestCase):
    def setUp(self):
        self.temp_dir = tempfile.mkdtemp()
        self.db_path = Path(self.temp_dir) / "test.db"
        os.environ["DATABASE_PATH"] = str(self.db_path)

    def tearDown(self):
        import shutil
        shutil.rmtree(self.temp_dir, ignore_errors=True)

    def test_database_initialization(self):
        db = DatabaseConnection()
        result = db.initialize_database()
        self.assertTrue(result)
        db.close()

    def test_singleton_connection(self):
        db1 = DatabaseConnection()
        db2 = DatabaseConnection()
        self.assertIs(db1, db2)
        db1.close()


class TestCollectionRepository(unittest.TestCase):
    def setUp(self):
        self.temp_dir = tempfile.mkdtemp()
        self.db_path = Path(self.temp_dir) / "test.db"
        os.environ["DATABASE_PATH"] = str(self.db_path)

        self.db = DatabaseConnection()
        self.db.initialize_database()
        self.repo = CollectionRepository(self.db)

    def tearDown(self):
        self.db.close()
        import shutil
        shutil.rmtree(self.temp_dir, ignore_errors=True)

    def test_create_collection(self):
        collection = Collection(
            collection_no="TEST001",
            name="测试文物",
            era="宋",
            category="陶瓷器",
            source="考古发掘",
            conservation_status="完好",
        )
        collection_id = self.repo.create(collection)
        self.assertGreater(collection_id, 0)

    def test_collection_no_duplicate(self):
        collection1 = Collection(
            collection_no="TEST001",
            name="测试文物1",
            era="宋",
            category="陶瓷器",
            source="考古发掘",
            conservation_status="完好",
        )
        self.repo.create(collection1)

        collection2 = Collection(
            collection_no="TEST001",
            name="测试文物2",
            era="明",
            category="玉器",
            source="捐赠",
            conservation_status="完好",
        )
        with self.assertRaises(CollectionNumberExistsError):
            self.repo.create(collection2)

    def test_get_collection(self):
        collection = Collection(
            collection_no="TEST001",
            name="测试文物",
            era="宋",
            category="陶瓷器",
            source="考古发掘",
            conservation_status="完好",
        )
        collection_id = self.repo.create(collection)

        retrieved = self.repo.get_by_id(collection_id)
        self.assertIsNotNone(retrieved)
        self.assertEqual(retrieved.collection_no, "TEST001")

    def test_update_collection(self):
        collection = Collection(
            collection_no="TEST001",
            name="测试文物",
            era="宋",
            category="陶瓷器",
            source="考古发掘",
            conservation_status="完好",
        )
        collection_id = self.repo.create(collection)

        collection.id = collection_id
        collection.name = "更新后的名称"
        result = self.repo.update(collection)
        self.assertTrue(result)

        updated = self.repo.get_by_id(collection_id)
        self.assertEqual(updated.name, "更新后的名称")

    def test_delete_collection(self):
        collection = Collection(
            collection_no="TEST001",
            name="测试文物",
            era="宋",
            category="陶瓷器",
            source="考古发掘",
            conservation_status="完好",
        )
        collection_id = self.repo.create(collection)

        result = self.repo.delete(collection_id)
        self.assertTrue(result)

        deleted = self.repo.get_by_id(collection_id)
        self.assertIsNone(deleted)

    def test_get_statistics(self):
        for i in range(5):
            collection = Collection(
                collection_no=f"TEST{i:03d}",
                name=f"测试文物{i}",
                era="宋",
                category="陶瓷器",
                source="考古发掘",
                conservation_status="完好",
            )
            self.repo.create(collection)

        stats = self.repo.get_statistics()
        self.assertEqual(stats["total_collections"], 5)
        self.assertIn("陶瓷器", stats["by_category"])


if __name__ == "__main__":
    unittest.main()
