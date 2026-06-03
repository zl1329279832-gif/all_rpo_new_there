import sqlite3
from datetime import datetime
from typing import List, Optional

from database.connection import DatabaseConnection
from database.schema import DatabaseSchema
from config.settings import TESTERS, DEPARTMENTS, TEST_ITEMS, SAMPLE_STATUS


class DatabaseService:
    def __init__(self):
        self.db = DatabaseConnection()
        self.schema = DatabaseSchema()

    def initialize_database(self):
        try:
            self.schema.initialize()
        except sqlite3.Error as e:
            raise Exception(f"数据库初始化失败: {str(e)}")

    def insert_demo_data(self):
        result = self.db.fetch_one("SELECT COUNT(*) as count FROM samples")
        if result and result['count'] > 0:
            return

        demo_samples = [
            {
                'sample_no': 'YP202401001',
                'sample_name': '饮用水样本A',
                'source_unit': '市自来水公司',
                'sender': '李工',
                'receiver': TESTERS[0],
                'receive_time': '2024-01-15 09:30:00',
                'test_items': '理化检测,微生物检测',
                'status': SAMPLE_STATUS[2],
                'description': '自来水常规检测'
            },
            {
                'sample_no': 'YP202401002',
                'sample_name': '食品样本B',
                'source_unit': DEPARTMENTS[3],
                'sender': '王经理',
                'receiver': TESTERS[1],
                'receive_time': '2024-01-16 14:20:00',
                'test_items': '重金属检测,农残检测',
                'status': SAMPLE_STATUS[1],
                'description': '蔬菜农残检测'
            },
            {
                'sample_no': 'YP202401003',
                'sample_name': '土壤样本C',
                'source_unit': DEPARTMENTS[1],
                'sender': '张工',
                'receiver': TESTERS[2],
                'receive_time': '2024-01-17 10:00:00',
                'test_items': '重金属检测',
                'status': SAMPLE_STATUS[0],
                'description': '农田土壤检测'
            },
            {
                'sample_no': 'YP202401004',
                'sample_name': '药品样本D',
                'source_unit': DEPARTMENTS[4],
                'sender': '刘主任',
                'receiver': TESTERS[3],
                'receive_time': '2024-01-18 11:30:00',
                'test_items': '理化检测,毒理检测',
                'status': SAMPLE_STATUS[3],
                'description': '新药安全性检测'
            },
            {
                'sample_no': 'YP202401005',
                'sample_name': '水质样本E',
                'source_unit': DEPARTMENTS[0],
                'sender': '陈工',
                'receiver': TESTERS[4],
                'receive_time': '2024-01-19 15:45:00',
                'test_items': '微生物检测,添加剂检测',
                'status': SAMPLE_STATUS[0],
                'description': '废水排放检测'
            }
        ]

        sample_ids = []
        for sample in demo_samples:
            sql = """
            INSERT INTO samples (sample_no, sample_name, source_unit, sender, receiver, 
                               receive_time, test_items, status, description)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """
            params = (
                sample['sample_no'],
                sample['sample_name'],
                sample['source_unit'],
                sample['sender'],
                sample['receiver'],
                sample['receive_time'],
                sample['test_items'],
                sample['status'],
                sample['description']
            )
            sample_id = self.db.execute(sql, params)
            sample_ids.append(sample_id)

        demo_test_records = [
            {
                'sample_id': sample_ids[0],
                'test_item': TEST_ITEMS[0],
                'tester': TESTERS[0],
                'test_time': '2024-01-15 14:00:00',
                'test_result': '合格',
                'result_value': 'pH=7.2, 浊度=0.5NTU',
                'standard_value': 'pH=6.5-8.5, 浊度≤1NTU',
                'is_qualified': 1,
                'remarks': '各项指标正常'
            },
            {
                'sample_id': sample_ids[0],
                'test_item': TEST_ITEMS[1],
                'tester': TESTERS[0],
                'test_time': '2024-01-16 10:00:00',
                'test_result': '合格',
                'result_value': '菌落总数=20CFU/mL',
                'standard_value': '菌落总数≤100CFU/mL',
                'is_qualified': 1,
                'remarks': '微生物指标符合标准'
            },
            {
                'sample_id': sample_ids[1],
                'test_item': TEST_ITEMS[2],
                'tester': TESTERS[1],
                'test_time': '2024-01-17 09:00:00',
                'test_result': '检测中',
                'result_value': '',
                'standard_value': '铅≤0.3mg/kg, 镉≤0.05mg/kg',
                'is_qualified': 1,
                'remarks': '正在进行检测'
            },
            {
                'sample_id': sample_ids[3],
                'test_item': TEST_ITEMS[0],
                'tester': TESTERS[3],
                'test_time': '2024-01-19 11:00:00',
                'test_result': '合格',
                'result_value': '含量=99.5%',
                'standard_value': '含量≥98.0%',
                'is_qualified': 1,
                'remarks': '含量符合标准'
            },
            {
                'sample_id': sample_ids[3],
                'test_item': TEST_ITEMS[6],
                'tester': TESTERS[3],
                'test_time': '2024-01-20 15:00:00',
                'test_result': '合格',
                'result_value': 'LD50>5000mg/kg',
                'standard_value': 'LD50>2000mg/kg',
                'is_qualified': 1,
                'remarks': '毒理测试通过'
            }
        ]

        for record in demo_test_records:
            sql = """
            INSERT INTO test_records (sample_id, test_item, tester, test_time, 
                                     test_result, result_value, standard_value, is_qualified, remarks)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """
            params = (
                record['sample_id'],
                record['test_item'],
                record['tester'],
                record['test_time'],
                record['test_result'],
                record['result_value'],
                record['standard_value'],
                record['is_qualified'],
                record['remarks']
            )
            self.db.execute(sql, params)

    def clear_all_data(self):
        tables = ['attachments', 'test_records', 'audit_logs', 'samples']
        for table in tables:
            self.db.execute(f"DELETE FROM {table}")
            self.db.execute(f"DELETE FROM sqlite_sequence WHERE name='{table}'")
