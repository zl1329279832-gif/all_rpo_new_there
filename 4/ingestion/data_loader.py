import pandas as pd
import os
from typing import Dict, Optional, List, Tuple
from utils.error_handler import ErrorHandler


class DataLoader:
    REQUIRED_FILES = {
        'departments': 'departments.csv',
        'doctors': 'doctors.csv',
        'registrations': 'registrations.csv',
        'visits': 'visits.csv',
        'examinations': 'examinations.csv',
        'medications': 'medications.csv',
        'waiting_times': 'waiting_times.csv',
        'satisfaction': 'satisfaction.csv'
    }

    def __init__(self):
        self.data: Dict[str, Optional[pd.DataFrame]] = {k: None for k in self.REQUIRED_FILES.keys()}
        self.load_status: Dict[str, bool] = {k: False for k in self.REQUIRED_FILES.keys()}
        self.error_messages: List[str] = []
        self.load_errors: Dict[str, str] = {}

    def load_from_directory(self, directory_path: str) -> Dict[str, bool]:
        self.error_messages = []
        self.load_errors = {}

        if not os.path.exists(directory_path):
            error_msg = f"❌ 数据目录不存在：{directory_path}，请先运行 `python data/generate_demo_data.py` 生成演示数据"
            self.error_messages.append(error_msg)
            return self.load_status

        for key, filename in self.REQUIRED_FILES.items():
            file_path = os.path.join(directory_path, filename)
            if os.path.exists(file_path):
                try:
                    self.data[key] = pd.read_csv(file_path, encoding='utf-8-sig')
                    self.load_status[key] = True
                except Exception as e:
                    self.load_status[key] = False
                    error_msg = ErrorHandler.handle_file_upload_error(e, key)
                    self.error_messages.append(error_msg)
                    self.load_errors[key] = error_msg
            else:
                self.load_status[key] = False
                file_label = ErrorHandler.get_file_label(key)
                warning_msg = f"⚠️ 未找到【{file_label}】数据文件：{filename}"
                self.error_messages.append(warning_msg)
                self.load_errors[key] = warning_msg
        return self.load_status

    def load_from_uploaded_files(self, uploaded_files: Dict[str, any]) -> Dict[str, bool]:
        self.error_messages = []
        self.load_errors = {}

        for key, file_obj in uploaded_files.items():
            if file_obj is not None:
                try:
                    file_obj.seek(0)
                    self.data[key] = pd.read_csv(file_obj, encoding='utf-8')
                    self.load_status[key] = True
                except Exception as e:
                    self.load_status[key] = False
                    error_msg = ErrorHandler.handle_file_upload_error(e, key)
                    self.error_messages.append(error_msg)
                    self.load_errors[key] = error_msg
            else:
                self.load_status[key] = False
                file_label = ErrorHandler.get_file_label(key)
                info_msg = f"ℹ️ 未上传【{file_label}】数据文件"
                self.load_errors[key] = info_msg
        return self.load_status

    def get_error_messages(self) -> List[str]:
        return self.error_messages

    def get_load_errors(self) -> Dict[str, str]:
        return self.load_errors

    def has_errors(self) -> bool:
        return len(self.error_messages) > 0

    def get_data(self, key: str) -> Optional[pd.DataFrame]:
        return self.data.get(key)

    def get_all_data(self) -> Dict[str, Optional[pd.DataFrame]]:
        return self.data

    def is_complete(self) -> bool:
        return all(self.load_status.values())

    def get_loaded_files(self) -> list:
        return [k for k, v in self.load_status.items() if v]

    def get_missing_files(self) -> list:
        return [k for k, v in self.load_status.items() if not v]

    def get_row_count(self, key: str) -> int:
        if self.data[key] is not None:
            return len(self.data[key])
        return 0
