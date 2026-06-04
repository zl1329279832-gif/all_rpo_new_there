import unittest
from datetime import datetime

from models import Collection, RepairRecord, Attachment, Exhibition


class TestCollectionModel(unittest.TestCase):
    def test_collection_creation(self):
        collection = Collection(
            collection_no="TEST001",
            name="测试文物",
            era="宋",
            category="陶瓷器",
            source="考古发掘",
            conservation_status="完好",
        )
        self.assertEqual(collection.collection_no, "TEST001")
        self.assertEqual(collection.name, "测试文物")

    def test_collection_validation_valid(self):
        collection = Collection(
            collection_no="TEST001",
            name="测试文物",
            era="宋",
            category="陶瓷器",
            source="考古发掘",
            conservation_status="完好",
        )
        errors = collection.validate()
        self.assertEqual(len(errors), 0)

    def test_collection_validation_invalid(self):
        collection = Collection()
        errors = collection.validate()
        self.assertGreater(len(errors), 0)

    def test_collection_to_dict(self):
        collection = Collection(
            id=1,
            collection_no="TEST001",
            name="测试文物",
        )
        data = collection.to_dict()
        self.assertEqual(data["id"], 1)
        self.assertEqual(data["collection_no"], "TEST001")

    def test_collection_from_dict(self):
        data = {
            "id": 1,
            "collection_no": "TEST001",
            "name": "测试文物",
            "era": "宋",
        }
        collection = Collection.from_dict(data)
        self.assertEqual(collection.id, 1)
        self.assertEqual(collection.collection_no, "TEST001")


class TestRepairRecordModel(unittest.TestCase):
    def test_repair_record_validation(self):
        record = RepairRecord(
            collection_id=1,
            repair_date="2024-01-01",
            reason="测试修复",
            status="待修复",
        )
        errors = record.validate()
        self.assertEqual(len(errors), 0)

    def test_repair_record_validation_invalid(self):
        record = RepairRecord()
        errors = record.validate()
        self.assertGreater(len(errors), 0)


class TestAttachmentModel(unittest.TestCase):
    def test_attachment_format_size(self):
        attachment = Attachment(file_size=1024)
        self.assertEqual(attachment.format_file_size(), "1.0 KB")

        attachment.file_size = 1024 * 1024
        self.assertEqual(attachment.format_file_size(), "1.0 MB")

    def test_attachment_file_extension(self):
        attachment = Attachment(file_name="test.jpg")
        self.assertEqual(attachment.get_file_extension(), ".jpg")


class TestExhibitionModel(unittest.TestCase):
    def test_exhibition_validation_dates(self):
        exhibition = Exhibition(
            collection_id=1,
            exhibition_name="测试展览",
            start_date="2024-01-15",
            end_date="2024-01-10",
        )
        errors = exhibition.validate()
        self.assertIn("结束日期不能早于开始日期", errors)


if __name__ == "__main__":
    unittest.main()
