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

    PRIMARY_KEYS = {
        'departments': 'department_id',
        'doctors': 'doctor_id',
        'registrations': 'reg_id',
        'visits': 'visit_id',
        'examinations': 'exam_id',
        'medications': 'med_id',
        'waiting_times': 'wait_id',
        'satisfaction': 'survey_id'
    }

    FILE_LABELS = {
        'departments': '科室信息',
        'doctors': '医生信息',
        'registrations': '挂号记录',
        'visits': '就诊记录',
        'examinations': '检查项目',
        'medications': '药品费用',
        'waiting_times': '候诊时间',
        'satisfaction': '患者满意度'
    }

    def __init__(self, data: Dict[str, pd.DataFrame]):
        self.data = data
        self.validation_results = {}
        self.warnings = []
        self.errors = []
        self._detailed_issues: List[Dict[str, Any]] = []

    def validate_all(self) -> Dict[str, Any]:
        self._detailed_issues = []
        self.validation_results = {
            'field_validation': self._validate_fields(),
            'missing_values': self._check_missing_values(),
            'relationship_validation': self._validate_relationships(),
            'anomaly_detection': self._detect_anomalies(),
            'data_types': self._validate_data_types(),
            'duplicate_records': self._detect_duplicates(),
            'invalid_doctor_ids': self._detect_invalid_doctor_ids(),
            'visit_reg_mismatch': self._detect_visit_reg_mismatch(),
            'fee_anomalies': self._detect_fee_anomalies(),
            'wait_time_anomalies': self._detect_wait_time_anomalies(),
            'satisfaction_missing': self._detect_satisfaction_missing()
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

    def _detect_duplicates(self) -> Dict[str, List[Dict[str, Any]]]:
        results = {}
        for file_key, pk_field in self.PRIMARY_KEYS.items():
            df = self.data.get(file_key)
            if df is None or pk_field not in df.columns:
                continue
            duplicated = df[df.duplicated(subset=[pk_field], keep=False)]
            if len(duplicated) == 0:
                continue
            dup_info = []
            for pk_val, group in duplicated.groupby(pk_field):
                for _, row in group.iterrows():
                    row_number = int(row.name) + 2
                    issue = {
                        'file_key': file_key,
                        'row_number': row_number,
                        'field': pk_field,
                        'issue_type': 'duplicate_record',
                        'severity': 'error',
                        'message': f"{self.FILE_LABELS.get(file_key, file_key)} 第{row_number}行 {pk_field} 存在重复值({pk_val})",
                        'suggestion': '请检查数据来源，删除或修正重复记录'
                    }
                    dup_info.append(issue)
                    self._detailed_issues.append(issue)
            results[file_key] = dup_info
            self.errors.append(f"{self.FILE_LABELS.get(file_key, file_key)}: 发现 {len(duplicated)} 条主键 {pk_field} 重复记录")
        return results

    def _detect_invalid_doctor_ids(self) -> Dict[str, List[Dict[str, Any]]]:
        results = {}
        doctors_df = self.data.get('doctors')
        if doctors_df is None:
            return results
        valid_doctor_ids = set(doctors_df['doctor_id'].unique())

        for file_key in ['registrations', 'visits']:
            df = self.data.get(file_key)
            if df is None or 'doctor_id' not in df.columns:
                continue
            invalid_mask = ~df['doctor_id'].isin(valid_doctor_ids)
            invalid_rows = df[invalid_mask]
            if len(invalid_rows) == 0:
                continue
            issues = []
            for idx, row in invalid_rows.iterrows():
                row_number = int(idx) + 2
                issue = {
                    'file_key': file_key,
                    'row_number': row_number,
                    'field': 'doctor_id',
                    'issue_type': 'invalid_doctor_id',
                    'severity': 'error',
                    'message': f"{self.FILE_LABELS.get(file_key, file_key)} 第{row_number}行 doctor_id 医生编号({row['doctor_id']})在医生表中不存在",
                    'suggestion': '请核实医生编号是否正确，或补充医生信息表中的对应记录'
                }
                issues.append(issue)
                self._detailed_issues.append(issue)
            results[file_key] = issues
            self.errors.append(f"{self.FILE_LABELS.get(file_key, file_key)}: 发现 {len(invalid_rows)} 条医生编号无效记录")
        return results

    def _detect_visit_reg_mismatch(self) -> Dict[str, List[Dict[str, Any]]]:
        results = {}
        visits_df = self.data.get('visits')
        registrations_df = self.data.get('registrations')
        if visits_df is None or registrations_df is None:
            return results

        issues = []
        reg_ids = set(registrations_df['reg_id'].unique())
        reg_lookup = registrations_df.set_index('reg_id')

        missing_reg_mask = ~visits_df['reg_id'].isin(reg_ids)
        missing_reg_rows = visits_df[missing_reg_mask]
        for idx, row in missing_reg_rows.iterrows():
            row_number = int(idx) + 2
            issue = {
                'file_key': 'visits',
                'row_number': row_number,
                'field': 'reg_id',
                'issue_type': 'visit_reg_not_found',
                'severity': 'error',
                'message': f"就诊记录 第{row_number}行 reg_id 挂号编号({row['reg_id']})在挂号记录中不存在",
                'suggestion': '请核实挂号编号是否正确，或补充挂号记录'
            }
            issues.append(issue)
            self._detailed_issues.append(issue)

        if len(missing_reg_rows) > 0:
            self.errors.append(f"就诊记录: 发现 {len(missing_reg_rows)} 条挂号编号不存在的记录")

        matched_visits = visits_df[~missing_reg_mask]
        for idx, row in matched_visits.iterrows():
            row_number = int(idx) + 2
            reg_row = reg_lookup.loc[row['reg_id']]
            if isinstance(reg_row, pd.DataFrame):
                reg_row = reg_row.iloc[0]

            if 'doctor_id' in visits_df.columns and 'doctor_id' in registrations_df.columns:
                if pd.notna(row.get('doctor_id')) and pd.notna(reg_row.get('doctor_id')):
                    if str(row['doctor_id']) != str(reg_row['doctor_id']):
                        issue = {
                            'file_key': 'visits',
                            'row_number': row_number,
                            'field': 'doctor_id',
                            'issue_type': 'visit_reg_doctor_mismatch',
                            'severity': 'warning',
                            'message': f"就诊记录 第{row_number}行 doctor_id 就诊医生({row['doctor_id']})与挂号医生({reg_row['doctor_id']})不一致",
                            'suggestion': '请核实该就诊记录的医生信息，以挂号记录为准进行修正'
                        }
                        issues.append(issue)
                        self._detailed_issues.append(issue)

            if 'department_id' in visits_df.columns and 'department_id' in registrations_df.columns:
                if pd.notna(row.get('department_id')) and pd.notna(reg_row.get('department_id')):
                    if str(row['department_id']) != str(reg_row['department_id']):
                        issue = {
                            'file_key': 'visits',
                            'row_number': row_number,
                            'field': 'department_id',
                            'issue_type': 'visit_reg_dept_mismatch',
                            'severity': 'warning',
                            'message': f"就诊记录 第{row_number}行 department_id 就诊科室({row['department_id']})与挂号科室({reg_row['department_id']})不一致",
                            'suggestion': '请核实该就诊记录的科室信息，以挂号记录为准进行修正'
                        }
                        issues.append(issue)
                        self._detailed_issues.append(issue)

        mismatch_count = len(issues) - len(missing_reg_rows)
        if mismatch_count > 0:
            self.warnings.append(f"就诊记录: 发现 {mismatch_count} 条与挂号记录医生或科室不一致的记录")

        results['visits'] = issues
        return results

    def _detect_fee_anomalies(self) -> Dict[str, List[Dict[str, Any]]]:
        results = {}

        exam_df = self.data.get('examinations')
        if exam_df is not None and 'exam_fee' in exam_df.columns:
            issues = []
            invalid_mask = (exam_df['exam_fee'] <= 0) | (exam_df['exam_fee'] > 5000)
            invalid_rows = exam_df[invalid_mask]
            for idx, row in invalid_rows.iterrows():
                row_number = int(idx) + 2
                fee_val = row['exam_fee']
                if fee_val <= 0:
                    desc = f"检查费用({fee_val})小于等于0"
                else:
                    desc = f"检查费用({fee_val})超过5000"
                issue = {
                    'file_key': 'examinations',
                    'row_number': row_number,
                    'field': 'exam_fee',
                    'issue_type': 'exam_fee_anomaly',
                    'severity': 'error',
                    'message': f"检查项目 第{row_number}行 exam_fee {desc}",
                    'suggestion': '请核实检查费用是否正确，修正异常值'
                }
                issues.append(issue)
                self._detailed_issues.append(issue)
            if len(issues) > 0:
                results['examinations'] = issues
                self.errors.append(f"检查项目: 发现 {len(invalid_rows)} 条检查费用异常记录")

        med_df = self.data.get('medications')
        if med_df is not None and 'drug_fee' in med_df.columns:
            issues = []
            invalid_mask = (med_df['drug_fee'] <= 0) | (med_df['drug_fee'] > 3000)
            invalid_rows = med_df[invalid_mask]
            for idx, row in invalid_rows.iterrows():
                row_number = int(idx) + 2
                fee_val = row['drug_fee']
                if fee_val <= 0:
                    desc = f"药品费用({fee_val})小于等于0"
                else:
                    desc = f"药品费用({fee_val})超过3000"
                issue = {
                    'file_key': 'medications',
                    'row_number': row_number,
                    'field': 'drug_fee',
                    'issue_type': 'drug_fee_anomaly',
                    'severity': 'error',
                    'message': f"药品费用 第{row_number}行 drug_fee {desc}",
                    'suggestion': '请核实药品费用是否正确，修正异常值'
                }
                issues.append(issue)
                self._detailed_issues.append(issue)
            if len(issues) > 0:
                results['medications'] = issues
                self.errors.append(f"药品费用: 发现 {len(invalid_rows)} 条药品费用异常记录")

        return results

    def _detect_wait_time_anomalies(self) -> Dict[str, List[Dict[str, Any]]]:
        results = {}
        wt_df = self.data.get('waiting_times')
        if wt_df is None or 'wait_minutes' not in wt_df.columns:
            return results

        issues = []
        invalid_mask = (wt_df['wait_minutes'] < 0) | (wt_df['wait_minutes'] > 180)
        invalid_rows = wt_df[invalid_mask]
        for idx, row in invalid_rows.iterrows():
            row_number = int(idx) + 2
            wait_val = row['wait_minutes']
            if wait_val < 0:
                desc = f"候诊时间({wait_val})为负值"
                suggestion = '请核实候诊时间数据，修正为合理的非负值'
            else:
                desc = f"候诊时间({wait_val})超过180分钟"
                suggestion = '请核实候诊时间数据，排查是否存在排队系统异常'
            issue = {
                'file_key': 'waiting_times',
                'row_number': row_number,
                'field': 'wait_minutes',
                'issue_type': 'wait_time_anomaly',
                'severity': 'error' if wait_val < 0 else 'warning',
                'message': f"候诊时间 第{row_number}行 wait_minutes {desc}",
                'suggestion': suggestion
            }
            issues.append(issue)
            self._detailed_issues.append(issue)

        if len(issues) > 0:
            results['waiting_times'] = issues
            self.errors.append(f"候诊时间: 发现 {len(invalid_rows)} 条候诊时间异常记录")
        return results

    def _detect_satisfaction_missing(self) -> Dict[str, Any]:
        results = {}
        visits_df = self.data.get('visits')
        satisfaction_df = self.data.get('satisfaction')
        if visits_df is None:
            return results

        total_visits = len(visits_df)
        if total_visits == 0:
            return results

        if satisfaction_df is None or 'visit_id' not in satisfaction_df.columns:
            results = {
                'total_visits': total_visits,
                'visits_with_satisfaction': 0,
                'visits_without_satisfaction': total_visits,
                'missing_rate': 100.0,
                'missing_visit_ids': list(visits_df['visit_id'].unique()[:100])
            }
            self._detailed_issues.append({
                'file_key': 'visits',
                'row_number': 0,
                'field': 'visit_id',
                'issue_type': 'satisfaction_missing',
                'severity': 'warning',
                'message': f"就诊记录 满意度数据缺失，所有{total_visits}条就诊记录均无对应满意度数据",
                'suggestion': '请补充患者满意度调查数据'
            })
            self.warnings.append(f"满意度数据缺失: 所有{total_visits}条就诊记录均无对应满意度数据")
            return results

        sat_visit_ids = set(satisfaction_df['visit_id'].unique())
        visits_without_sat = visits_df[~visits_df['visit_id'].isin(sat_visit_ids)]
        missing_count = len(visits_without_sat)
        missing_rate = round((missing_count / total_visits) * 100, 2)

        results = {
            'total_visits': total_visits,
            'visits_with_satisfaction': total_visits - missing_count,
            'visits_without_satisfaction': missing_count,
            'missing_rate': missing_rate,
            'missing_visit_ids': list(visits_without_sat['visit_id'].unique()[:100])
        }

        if missing_count > 0:
            severity = 'warning'
            if missing_rate > 50:
                severity = 'error'
            self._detailed_issues.append({
                'file_key': 'visits',
                'row_number': 0,
                'field': 'visit_id',
                'issue_type': 'satisfaction_missing',
                'severity': severity,
                'message': f"就诊记录 有{missing_count}条就诊记录缺少满意度数据(缺失率{missing_rate}%)",
                'suggestion': '请补充缺失的满意度调查数据，提高数据完整性'
            })
            if severity == 'error':
                self.errors.append(f"满意度缺失: {missing_count}条就诊记录缺少满意度数据(缺失率{missing_rate}%)")
            else:
                self.warnings.append(f"满意度缺失: {missing_count}条就诊记录缺少满意度数据(缺失率{missing_rate}%)")

        return results

    def get_detailed_issues(self) -> List[Dict[str, Any]]:
        return self._detailed_issues

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
