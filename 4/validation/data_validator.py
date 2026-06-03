import pandas as pd
import numpy as np
from typing import Dict, List, Tuple, Any
from scipy import stats


class DataValidator:
    REQUIRED_FIELDS = {
        'departments': ['department_id', 'department_name', 'type'],
        'doctors': ['doctor_id', 'doctor_name', 'department_id', 'title'],
        'registrations': ['reg_id', 'patient_id', 'department_id', 'doctor_id', 'reg_date', 'reg_time', 'patient_type'],
        'visits': ['visit_id', 'reg_id', 'doctor_id', 'department_id', 'visit_date', 'diagnosis'],
        'examinations': ['exam_id', 'visit_id', 'exam_item', 'exam_fee', 'exam_result'],
        'medications': ['med_id', 'visit_id', 'drug_name', 'drug_fee', 'quantity'],
        'waiting_times': ['wait_id', 'reg_id', 'arrival_time', 'call_time', 'wait_minutes'],
        'satisfaction': ['survey_id', 'visit_id', 'overall_score', 'wait_score', 'service_score']
    }

    def __init__(self, data: Dict[str, pd.DataFrame]):
        self.data = data
        self.validation_results = {}
        self.warnings = []
        self.errors = []

    def validate_all(self) -> Dict[str, Any]:
        self.validation_results = {
            'field_validation': self._validate_fields(),
            'missing_values': self._check_missing_values(),
            'relationship_validation': self._validate_relationships(),
            'anomaly_detection': self._detect_anomalies(),
            'data_types': self._validate_data_types()
        }
        return self.validation_results

    def _validate_fields(self) -> Dict[str, Dict[str, List[str]]]:
        results = {}
        for file_key, required_fields in self.REQUIRED_FIELDS.items():
            if self.data.get(file_key) is not None:
                df = self.data[file_key]
                actual_fields = df.columns.tolist()
                missing_fields = [f for f in required_fields if f not in actual_fields]
                extra_fields = [f for f in actual_fields if f not in required_fields]
                results[file_key] = {
                    'missing_fields': missing_fields,
                    'extra_fields': extra_fields,
                    'is_valid': len(missing_fields) == 0
                }
                if missing_fields:
                    self.errors.append(f"{file_key}: 缺少必需字段 {', '.join(missing_fields)}")
            else:
                results[file_key] = {'missing_fields': [], 'extra_fields': [], 'is_valid': False}
        return results

    def _check_missing_values(self) -> Dict[str, Dict[str, Any]]:
        results = {}
        for file_key, df in self.data.items():
            if df is not None:
                missing_info = df.isnull().sum()
                total_missing = missing_info.sum()
                missing_percent = (total_missing / (len(df) * len(df.columns))) * 100
                results[file_key] = {
                    'total_missing': total_missing,
                    'missing_percent': round(missing_percent, 2),
                    'by_column': missing_info[missing_info > 0].to_dict(),
                    'has_missing': total_missing > 0
                }
                if total_missing > 0:
                    self.warnings.append(f"{file_key}: 存在 {total_missing} 个缺失值 ({missing_percent:.2f}%)")
            else:
                results[file_key] = {'total_missing': 0, 'missing_percent': 0, 'by_column': {}, 'has_missing': False}
        return results

    def _validate_relationships(self) -> Dict[str, List[Dict[str, Any]]]:
        issues = []

        if self.data.get('doctors') is not None and self.data.get('departments') is not None:
            dept_ids = set(self.data['departments']['department_id'].unique())
            invalid_depts = self.data['doctors'][~self.data['doctors']['department_id'].isin(dept_ids)]
            if len(invalid_depts) > 0:
                issues.append({
                    'table': 'doctors',
                    'field': 'department_id',
                    'referenced_table': 'departments',
                    'invalid_count': len(invalid_depts),
                    'message': f"医生表中有 {len(invalid_depts)} 条记录的科室ID不存在"
                })
                self.errors.append(f"医生表中有 {len(invalid_depts)} 条记录的科室ID不存在")

        if self.data.get('registrations') is not None and self.data.get('doctors') is not None:
            doc_ids = set(self.data['doctors']['doctor_id'].unique())
            invalid_docs = self.data['registrations'][~self.data['registrations']['doctor_id'].isin(doc_ids)]
            if len(invalid_docs) > 0:
                issues.append({
                    'table': 'registrations',
                    'field': 'doctor_id',
                    'referenced_table': 'doctors',
                    'invalid_count': len(invalid_docs),
                    'message': f"挂号表中有 {len(invalid_docs)} 条记录的医生ID不存在"
                })
                self.warnings.append(f"挂号表中有 {len(invalid_docs)} 条记录的医生ID不存在")

        if self.data.get('visits') is not None and self.data.get('registrations') is not None:
            reg_ids = set(self.data['registrations']['reg_id'].unique())
            invalid_regs = self.data['visits'][~self.data['visits']['reg_id'].isin(reg_ids)]
            if len(invalid_regs) > 0:
                issues.append({
                    'table': 'visits',
                    'field': 'reg_id',
                    'referenced_table': 'registrations',
                    'invalid_count': len(invalid_regs),
                    'message': f"就诊表中有 {len(invalid_regs)} 条记录的挂号ID不存在"
                })
                self.warnings.append(f"就诊表中有 {len(invalid_regs)} 条记录的挂号ID不存在")

        return issues

    def _detect_anomalies(self) -> Dict[str, List[Dict[str, Any]]]:
        anomalies = {}

        if self.data.get('registrations') is not None:
            reg_df = self.data['registrations']
            daily_counts = reg_df.groupby('reg_date').size()
            z_scores = np.abs(stats.zscore(daily_counts))
            anomalous_dates = daily_counts[z_scores > 2].index.tolist()
            if anomalous_dates:
                anomalies['registrations'] = [{
                    'type': 'outlier_date',
                    'description': f"门诊量异常日期: {', '.join(anomalous_dates[:5])}",
                    'severity': 'warning'
                }]
                self.warnings.append(f"发现 {len(anomalous_dates)} 个门诊量异常日期")

        if self.data.get('waiting_times') is not None:
            wt_df = self.data['waiting_times']
            if 'wait_minutes' in wt_df.columns:
                extreme_waits = wt_df[wt_df['wait_minutes'] > 120]
                if len(extreme_waits) > 0:
                    anomalies.setdefault('waiting_times', []).append({
                        'type': 'extreme_wait',
                        'description': f"候诊时间超过2小时的记录: {len(extreme_waits)} 条",
                        'severity': 'warning'
                    })
                    self.warnings.append(f"发现 {len(extreme_waits)} 条候诊时间超过2小时的记录")

                negative_waits = wt_df[wt_df['wait_minutes'] < 0]
                if len(negative_waits) > 0:
                    anomalies.setdefault('waiting_times', []).append({
                        'type': 'negative_wait',
                        'description': f"候诊时间为负值的记录: {len(negative_waits)} 条",
                        'severity': 'error'
                    })
                    self.errors.append(f"发现 {len(negative_waits)} 条候诊时间为负值的记录")

        if self.data.get('examinations') is not None:
            exam_df = self.data['examinations']
            if 'exam_fee' in exam_df.columns:
                zero_fees = exam_df[exam_df['exam_fee'] <= 0]
                if len(zero_fees) > 0:
                    anomalies.setdefault('examinations', []).append({
                        'type': 'zero_fee',
                        'description': f"检查费用为0或负值的记录: {len(zero_fees)} 条",
                        'severity': 'error'
                    })
                    self.errors.append(f"发现 {len(zero_fees)} 条检查费用异常记录")

        if self.data.get('satisfaction') is not None:
            sat_df = self.data['satisfaction']
            score_cols = ['overall_score', 'wait_score', 'service_score']
            for col in score_cols:
                if col in sat_df.columns:
                    invalid_scores = sat_df[(sat_df[col] < 1) | (sat_df[col] > 5)]
                    if len(invalid_scores) > 0:
                        anomalies.setdefault('satisfaction', []).append({
                            'type': 'invalid_score',
                            'description': f"{col} 超出1-5范围的记录: {len(invalid_scores)} 条",
                            'severity': 'error'
                        })
                        self.errors.append(f"发现 {len(invalid_scores)} 条 {col} 分数异常记录")

        return anomalies

    def _validate_data_types(self) -> Dict[str, List[Dict[str, Any]]]:
        type_issues = {}

        numeric_fields = {
            'examinations': ['exam_fee'],
            'medications': ['drug_fee', 'quantity'],
            'waiting_times': ['wait_minutes'],
            'satisfaction': ['overall_score', 'wait_score', 'service_score']
        }

        for file_key, fields in numeric_fields.items():
            if self.data.get(file_key) is not None:
                df = self.data[file_key]
                for field in fields:
                    if field in df.columns:
                        if not pd.api.types.is_numeric_dtype(df[field]):
                            type_issues.setdefault(file_key, []).append({
                                'field': field,
                                'expected_type': 'numeric',
                                'actual_type': str(df[field].dtype)
                            })
                            self.warnings.append(f"{file_key}.{field} 应为数值类型")

        date_fields = {
            'registrations': ['reg_date'],
            'visits': ['visit_date']
        }

        for file_key, fields in date_fields.items():
            if self.data.get(file_key) is not None:
                df = self.data[file_key]
                for field in fields:
                    if field in df.columns:
                        try:
                            pd.to_datetime(df[field])
                        except:
                            type_issues.setdefault(file_key, []).append({
                                'field': field,
                                'expected_type': 'date',
                                'actual_type': str(df[field].dtype)
                            })
                            self.errors.append(f"{file_key}.{field} 日期格式不正确")

        return type_issues

    def get_summary(self) -> Dict[str, Any]:
        return {
            'total_errors': len(self.errors),
            'total_warnings': len(self.warnings),
            'errors': self.errors,
            'warnings': self.warnings,
            'is_valid': len(self.errors) == 0
        }

    def get_error_count(self) -> int:
        return len(self.errors)

    def get_warning_count(self) -> int:
        return len(self.warnings)
