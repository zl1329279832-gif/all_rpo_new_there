import pandas as pd
import numpy as np
from typing import Dict, List, Optional, Any
from scipy import stats


class MetricsCalculator:
    def __init__(self, data: Dict[str, pd.DataFrame]):
        self.data = data

    def get_overview_metrics(self) -> Dict[str, Any]:
        metrics = {}

        if self.data.get('registrations') is not None:
            reg_df = self.data['registrations']
            metrics['total_registrations'] = len(reg_df)
            metrics['unique_patients'] = reg_df['patient_id'].nunique()
            metrics['avg_daily_registrations'] = round(reg_df.groupby('reg_date').size().mean(), 1)

            if 'patient_type' in reg_df.columns:
                pt_dist = reg_df['patient_type'].value_counts().to_dict()
                metrics['patient_type_distribution'] = pt_dist

        if self.data.get('visits') is not None:
            visit_df = self.data['visits']
            metrics['total_visits'] = len(visit_df)

            if 'total_fee' in visit_df.columns:
                metrics['total_revenue'] = round(visit_df['total_fee'].sum(), 2)
                metrics['avg_visit_fee'] = round(visit_df['total_fee'].mean(), 2)

        if self.data.get('waiting_times') is not None:
            wt_df = self.data['waiting_times']
            if 'wait_minutes' in wt_df.columns:
                metrics['avg_wait_time'] = round(wt_df['wait_minutes'].mean(), 1)
                metrics['median_wait_time'] = round(wt_df['wait_minutes'].median(), 1)
                metrics['max_wait_time'] = round(wt_df['wait_minutes'].max(), 1)

                metrics['wait_over_30min_pct'] = round(
                    (wt_df['wait_minutes'] > 30).mean() * 100, 1
                )

        if self.data.get('satisfaction') is not None:
            sat_df = self.data['satisfaction']
            if 'overall_score' in sat_df.columns:
                metrics['avg_overall_score'] = round(sat_df['overall_score'].mean(), 2)
                metrics['satisfaction_pct'] = round((sat_df['overall_score'] >= 4).mean() * 100, 1)

        if self.data.get('departments') is not None:
            metrics['total_departments'] = len(self.data['departments'])

        if self.data.get('doctors') is not None:
            metrics['total_doctors'] = len(self.data['doctors'])

        return metrics

    def get_daily_trends(self) -> Optional[pd.DataFrame]:
        if self.data.get('registrations') is not None:
            reg_df = self.data['registrations']
            daily = reg_df.groupby('reg_date').agg({
                'reg_id': 'count',
                'patient_id': 'nunique'
            }).reset_index()
            daily.columns = ['date', 'registrations', 'unique_patients']
            return daily
        return None

    def get_department_metrics(self) -> Optional[pd.DataFrame]:
        if self.data.get('registrations') is not None and self.data.get('departments') is not None:
            dept_metrics = self.data['registrations'].groupby(
                ['department_id', 'department_name']
            ).agg({
                'reg_id': 'count',
                'patient_id': 'nunique'
            }).reset_index()
            dept_metrics.columns = ['department_id', 'department_name', 'total_registrations', 'unique_patients']

            if self.data.get('visits') is not None:
                visit_dept = self.data['visits'].groupby('department_id').agg({
                    'visit_id': 'count'
                }).reset_index()
                visit_dept.columns = ['department_id', 'total_visits']
                dept_metrics = dept_metrics.merge(visit_dept, on='department_id', how='left')

            if self.data.get('doctors') is not None:
                doc_dept = self.data['doctors'].groupby('department_id').size().reset_index()
                doc_dept.columns = ['department_id', 'doctor_count']
                dept_metrics = dept_metrics.merge(doc_dept, on='department_id', how='left')
                dept_metrics['visits_per_doctor'] = round(
                    dept_metrics['total_visits'] / dept_metrics['doctor_count'], 1
                )

            if self.data.get('waiting_times') is not None:
                reg_wt = self.data['registrations'].merge(
                    self.data['waiting_times'], on='reg_id', how='inner'
                )
                wt_dept = reg_wt.groupby('department_id')['wait_minutes'].agg(['mean', 'median']).reset_index()
                wt_dept.columns = ['department_id', 'avg_wait_time', 'median_wait_time']
                dept_metrics = dept_metrics.merge(wt_dept, on='department_id', how='left')

            if self.data.get('satisfaction') is not None and self.data.get('visits') is not None:
                visit_sat = self.data['visits'].merge(
                    self.data['satisfaction'], on='visit_id', how='inner'
                )
                sat_dept = visit_sat.groupby('department_id')['overall_score'].mean().reset_index()
                sat_dept.columns = ['department_id', 'avg_satisfaction']
                dept_metrics = dept_metrics.merge(sat_dept, on='department_id', how='left')

            return dept_metrics.sort_values('total_registrations', ascending=False)
        return None

    def get_doctor_metrics(self) -> Optional[pd.DataFrame]:
        if self.data.get('visits') is not None and self.data.get('doctors') is not None:
            doc_metrics = self.data['visits'].groupby(
                ['doctor_id', 'doctor_name', 'department_name']
            ).agg({
                'visit_id': 'count',
                'total_fee': 'sum' if 'total_fee' in self.data['visits'].columns else 'count'
            }).reset_index()
            doc_metrics.columns = ['doctor_id', 'doctor_name', 'department_name', 'total_visits', 'total_revenue']

            if self.data.get('satisfaction') is not None:
                visit_sat = self.data['visits'].merge(
                    self.data['satisfaction'], on='visit_id', how='inner'
                )
                sat_doc = visit_sat.groupby('doctor_id')['overall_score'].mean().reset_index()
                sat_doc.columns = ['doctor_id', 'avg_satisfaction']
                doc_metrics = doc_metrics.merge(sat_doc, on='doctor_id', how='left')

            if 'title' in self.data['doctors'].columns:
                title_map = dict(zip(
                    self.data['doctors']['doctor_id'],
                    self.data['doctors']['title']
                ))
                doc_metrics['title'] = doc_metrics['doctor_id'].map(title_map)

            return doc_metrics.sort_values('total_visits', ascending=False)
        return None

    def get_fee_structure(self) -> Dict[str, Any]:
        result = {}

        if self.data.get('visits') is not None:
            visit_df = self.data['visits']
            if 'total_exam_fee' in visit_df.columns and 'total_drug_fee' in visit_df.columns:
                total_exam = visit_df['total_exam_fee'].sum()
                total_drug = visit_df['total_drug_fee'].sum()
                total = total_exam + total_drug

                result['total_revenue'] = round(total, 2)
                result['exam_revenue'] = round(total_exam, 2)
                result['drug_revenue'] = round(total_drug, 2)
                result['exam_ratio'] = round((total_exam / total) * 100, 1) if total > 0 else 0
                result['drug_ratio'] = round((total_drug / total) * 100, 1) if total > 0 else 0

        if self.data.get('examinations') is not None:
            exam_items = self.data['examinations'].groupby('exam_item').agg({
                'exam_fee': ['sum', 'count']
            }).reset_index()
            exam_items.columns = ['exam_item', 'total_fee', 'count']
            result['exam_by_item'] = exam_items.sort_values('total_fee', ascending=False)

        if self.data.get('medications') is not None and 'drug_category' in self.data['medications'].columns:
            drug_cat = self.data['medications'].groupby('drug_category').agg({
                'drug_fee': 'sum'
            }).reset_index()
            result['drug_by_category'] = drug_cat.sort_values('drug_fee', ascending=False)

        return result

    def get_exam_conversion_rate(self) -> Dict[str, Any]:
        result = {}

        if self.data.get('visits') is not None:
            total_visits = len(self.data['visits'])
            visits_with_exam = (self.data['visits']['has_examination'] == 1).sum() if 'has_examination' in self.data['visits'].columns else 0

            result['total_visits'] = total_visits
            result['visits_with_exam'] = int(visits_with_exam)
            result['conversion_rate'] = round((visits_with_exam / total_visits) * 100, 1) if total_visits > 0 else 0

            dept_conv = self.data['visits'].groupby('department_name').agg({
                'visit_id': 'count',
                'has_examination': 'sum'
            }).reset_index()
            dept_conv.columns = ['department_name', 'total_visits', 'visits_with_exam']
            dept_conv['conversion_rate'] = round(
                (dept_conv['visits_with_exam'] / dept_conv['total_visits']) * 100, 1
            )
            result['by_department'] = dept_conv.sort_values('conversion_rate', ascending=False)

        return result

    def get_satisfaction_distribution(self) -> Dict[str, Any]:
        result = {}

        if self.data.get('satisfaction') is not None:
            sat_df = self.data['satisfaction']

            score_cols = ['overall_score', 'wait_score', 'service_score']
            for col in score_cols:
                if col in sat_df.columns:
                    dist = sat_df[col].value_counts().sort_index()
                    result[f'{col}_distribution'] = dist.to_dict()

            result['avg_scores'] = {}
            for col in score_cols:
                if col in sat_df.columns:
                    result['avg_scores'][col] = round(sat_df[col].mean(), 2)

            if 'would_recommend' in sat_df.columns:
                result['recommendation_rate'] = round(sat_df['would_recommend'].mean() * 100, 1)

        return result

    def detect_anomalous_departments(self) -> Optional[pd.DataFrame]:
        dept_metrics = self.get_department_metrics()
        if dept_metrics is None:
            return None

        anomalies = []

        if 'avg_wait_time' in dept_metrics.columns:
            wait_threshold = dept_metrics['avg_wait_time'].quantile(0.75)
            high_wait = dept_metrics[dept_metrics['avg_wait_time'] > wait_threshold].copy()
            high_wait['anomaly_type'] = '高候诊时间'
            anomalies.append(high_wait)

        if 'avg_satisfaction' in dept_metrics.columns:
            sat_threshold = dept_metrics['avg_satisfaction'].quantile(0.25)
            low_sat = dept_metrics[dept_metrics['avg_satisfaction'] < sat_threshold].copy()
            low_sat['anomaly_type'] = '低满意度'
            anomalies.append(low_sat)

        if 'visits_per_doctor' in dept_metrics.columns:
            workload_threshold = dept_metrics['visits_per_doctor'].quantile(0.9)
            high_workload = dept_metrics[dept_metrics['visits_per_doctor'] > workload_threshold].copy()
            high_workload['anomaly_type'] = '高工作负荷'
            anomalies.append(high_workload)

        if anomalies:
            return pd.concat(anomalies, ignore_index=True)
        return None

    def get_monthly_trends(self) -> Optional[pd.DataFrame]:
        if self.data.get('registrations') is not None:
            reg_df = self.data['registrations']
            monthly = reg_df.groupby(['year', 'month']).agg({
                'reg_id': 'count',
                'patient_id': 'nunique'
            }).reset_index()
            monthly.columns = ['year', 'month', 'registrations', 'unique_patients']
            monthly['month_label'] = monthly.apply(lambda x: f"{x['year']}-{x['month']:02d}", axis=1)
            return monthly
        return None
