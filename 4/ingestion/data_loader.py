import pandas as pd
import os
from typing import Dict, Optional


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

    def load_from_directory(self, directory_path: str) -> Dict[str, bool]:
        for key, filename in self.REQUIRED_FILES.items():
            file_path = os.path.join(directory_path, filename)
            if os.path.exists(file_path):
                try:
                    self.data[key] = pd.read_csv(file_path, encoding='utf-8-sig')
                    self.load_status[key] = True
                except Exception as e:
                    self.load_status[key] = False
                    print(f"Error loading {filename}: {str(e)}")
            else:
                self.load_status[key] = False
        return self.load_status

    def load_from_uploaded_files(self, uploaded_files: Dict[str, any]) -> Dict[str, bool]:
        for key, file_obj in uploaded_files.items():
            if file_obj is not None:
                try:
                    self.data[key] = pd.read_csv(file_obj, encoding='utf-8')
                    self.load_status[key] = True
                except Exception as e:
                    self.load_status[key] = False
                    print(f"Error loading {key}: {str(e)}")
        return self.load_status

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
