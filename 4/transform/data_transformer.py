import pandas as pd
import numpy as np
from typing import Dict, Optional, List


class DataTransformer:
    def __init__(self, data: Dict[str, pd.DataFrame]):
        self.data = data
        self.transformed_data = {}

    def transform_all(self) -> Dict[str, pd.DataFrame]:
        self._parse_dates()
        self._merge_department_names()
        self._merge_doctor_names()
        self._calculate_visit_fees()
        self._create_time_dimensions()
        return self.data

    def _parse_dates(self):
        date_columns = {
            'registrations': 'reg_date',
            'visits': 'visit_date'
        }
        for table, col in date_columns.items():
            if self.data.get(table) is not None and col in self.data[table].columns:
                self.data[table][col] = pd.to_datetime(self.data[table][col], errors='coerce')

    def _merge_department_names(self):
        if self.data.get('departments') is not None:
            dept_map = dict(zip(
                self.data['departments']['department_id'],
                self.data['departments']['department_name']
            ))
            tables_to_merge = ['registrations', 'visits', 'doctors']
            for table in tables_to_merge:
                if self.data.get(table) is not None and 'department_id' in self.data[table].columns:
                    self.data[table]['department_name'] = self.data[table]['department_id'].map(dept_map)

    def _merge_doctor_names(self):
        if self.data.get('doctors') is not None:
            doc_map = dict(zip(
                self.data['doctors']['doctor_id'],
                self.data['doctors']['doctor_name']
            ))
            tables_to_merge = ['registrations', 'visits']
            for table in tables_to_merge:
                if self.data.get(table) is not None and 'doctor_id' in self.data[table].columns:
                    self.data[table]['doctor_name'] = self.data[table]['doctor_id'].map(doc_map)

    def _calculate_visit_fees(self):
        if self.data.get('visits') is not None:
            if self.data.get('examinations') is not None:
                exam_fees = self.data['examinations'].groupby('visit_id')['exam_fee'].sum().reset_index()
                exam_fees.columns = ['visit_id', 'total_exam_fee']
                self.data['visits'] = self.data['visits'].merge(exam_fees, on='visit_id', how='left')
                self.data['visits']['total_exam_fee'] = self.data['visits']['total_exam_fee'].fillna(0)

            if self.data.get('medications') is not None:
                med_fees = self.data['medications'].groupby('visit_id')['drug_fee'].sum().reset_index()
                med_fees.columns = ['visit_id', 'total_drug_fee']
                self.data['visits'] = self.data['visits'].merge(med_fees, on='visit_id', how='left')
                self.data['visits']['total_drug_fee'] = self.data['visits']['total_drug_fee'].fillna(0)

            if 'total_exam_fee' in self.data['visits'].columns and 'total_drug_fee' in self.data['visits'].columns:
                self.data['visits']['total_fee'] = self.data['visits']['total_exam_fee'] + self.data['visits']['total_drug_fee']

    def _create_time_dimensions(self):
        if self.data.get('registrations') is not None and 'reg_date' in self.data['registrations'].columns:
            self.data['registrations']['year'] = self.data['registrations']['reg_date'].dt.year
            self.data['registrations']['month'] = self.data['registrations']['reg_date'].dt.month
            self.data['registrations']['week'] = self.data['registrations']['reg_date'].dt.isocalendar().week
            self.data['registrations']['day_of_week'] = self.data['registrations']['reg_date'].dt.dayofweek
            self.data['registrations']['day_name'] = self.data['registrations']['reg_date'].dt.day_name()
            self.data['registrations']['is_weekend'] = self.data['registrations']['day_of_week'].isin([5, 6]).astype(int)

        if self.data.get('visits') is not None and 'visit_date' in self.data['visits'].columns:
            self.data['visits']['year'] = self.data['visits']['visit_date'].dt.year
            self.data['visits']['month'] = self.data['visits']['visit_date'].dt.month
            self.data['visits']['week'] = self.data['visits']['visit_date'].dt.isocalendar().week

    def filter_by_date_range(self, table_name: str, date_col: str, start_date: str, end_date: str) -> Optional[pd.DataFrame]:
        if self.data.get(table_name) is not None:
            df = self.data[table_name]
            if date_col in df.columns:
                mask = (df[date_col] >= pd.to_datetime(start_date)) & (df[date_col] <= pd.to_datetime(end_date))
                return df[mask]
        return None

    def filter_by_department(self, table_name: str, department_ids: List[str]) -> Optional[pd.DataFrame]:
        if self.data.get(table_name) is not None:
            df = self.data[table_name]
            if 'department_id' in df.columns:
                return df[df['department_id'].isin(department_ids)]
        return None

    def filter_by_doctor(self, table_name: str, doctor_ids: List[str]) -> Optional[pd.DataFrame]:
        if self.data.get(table_name) is not None:
            df = self.data[table_name]
            if 'doctor_id' in df.columns:
                return df[df['doctor_id'].isin(doctor_ids)]
        return None

    def filter_by_patient_type(self, patient_types: List[str]) -> Optional[pd.DataFrame]:
        if self.data.get('registrations') is not None:
            return self.data['registrations'][self.data['registrations']['patient_type'].isin(patient_types)]
        return None

    def get_transformed_data(self) -> Dict[str, pd.DataFrame]:
        return self.data
