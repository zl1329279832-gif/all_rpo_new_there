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

    def get_mom_changes(self) -> Dict[str, Any]:
        result = {}

        if self.data.get('registrations') is not None:
            reg_df = self.data['registrations']
            if 'year' in reg_df.columns and 'month' in reg_df.columns:
                monthly_reg = reg_df.groupby(['year', 'month'])['reg_id'].count().reset_index()
                monthly_reg.columns = ['year', 'month', 'count']
                monthly_reg = monthly_reg.sort_values(['year', 'month']).reset_index(drop=True)
                monthly_reg['环比变化率'] = monthly_reg['count'].pct_change() * 100
                monthly_reg['year'] = monthly_reg['year'].astype(int)
                monthly_reg['month'] = monthly_reg['month'].astype(int)
                result['挂号量环比'] = [
                    {
                        '月份': f"{int(row['year'])}-{int(row['month']):02d}",
                        '数量': int(row['count']),
                        '环比变化率': round(row['环比变化率'], 2) if pd.notna(row['环比变化率']) else None
                    }
                    for _, row in monthly_reg.iterrows()
                ]

        if self.data.get('visits') is not None:
            visit_df = self.data['visits']
            if 'year' in visit_df.columns and 'month' in visit_df.columns:
                monthly_visit = visit_df.groupby(['year', 'month'])['visit_id'].count().reset_index()
                monthly_visit.columns = ['year', 'month', 'count']
                monthly_visit = monthly_visit.sort_values(['year', 'month']).reset_index(drop=True)
                monthly_visit['环比变化率'] = monthly_visit['count'].pct_change() * 100
                monthly_visit['year'] = monthly_visit['year'].astype(int)
                monthly_visit['month'] = monthly_visit['month'].astype(int)
                result['就诊量环比'] = [
                    {
                        '月份': f"{int(row['year'])}-{int(row['month']):02d}",
                        '数量': int(row['count']),
                        '环比变化率': round(row['环比变化率'], 2) if pd.notna(row['环比变化率']) else None
                    }
                    for _, row in monthly_visit.iterrows()
                ]

        if self.data.get('visits') is not None and 'total_fee' in self.data['visits'].columns:
            visit_df = self.data['visits']
            if 'year' in visit_df.columns and 'month' in visit_df.columns:
                monthly_rev = visit_df.groupby(['year', 'month'])['total_fee'].sum().reset_index()
                monthly_rev.columns = ['year', 'month', 'revenue']
                monthly_rev = monthly_rev.sort_values(['year', 'month']).reset_index(drop=True)
                monthly_rev['环比变化率'] = monthly_rev['revenue'].pct_change() * 100
                monthly_rev['year'] = monthly_rev['year'].astype(int)
                monthly_rev['month'] = monthly_rev['month'].astype(int)
                result['收入环比'] = [
                    {
                        '月份': f"{int(row['year'])}-{int(row['month']):02d}",
                        '金额': round(row['revenue'], 2),
                        '环比变化率': round(row['环比变化率'], 2) if pd.notna(row['环比变化率']) else None
                    }
                    for _, row in monthly_rev.iterrows()
                ]

        if self.data.get('waiting_times') is not None and self.data.get('registrations') is not None:
            wt_df = self.data['waiting_times']
            reg_df = self.data['registrations']
            if 'year' in reg_df.columns and 'month' in reg_df.columns and 'wait_minutes' in wt_df.columns:
                reg_wt = reg_df.merge(wt_df, on='reg_id', how='inner')
                monthly_wt = reg_wt.groupby(['year', 'month'])['wait_minutes'].mean().reset_index()
                monthly_wt.columns = ['year', 'month', 'avg_wait']
                monthly_wt = monthly_wt.sort_values(['year', 'month']).reset_index(drop=True)
                monthly_wt['环比变化率'] = monthly_wt['avg_wait'].pct_change() * 100
                monthly_wt['year'] = monthly_wt['year'].astype(int)
                monthly_wt['month'] = monthly_wt['month'].astype(int)
                result['候诊时间环比'] = [
                    {
                        '月份': f"{int(row['year'])}-{int(row['month']):02d}",
                        '平均候诊时间': round(row['avg_wait'], 1),
                        '环比变化率': round(row['环比变化率'], 2) if pd.notna(row['环比变化率']) else None
                    }
                    for _, row in monthly_wt.iterrows()
                ]

        if self.data.get('satisfaction') is not None and self.data.get('visits') is not None:
            sat_df = self.data['satisfaction']
            visit_df = self.data['visits']
            if 'year' in visit_df.columns and 'month' in visit_df.columns and 'overall_score' in sat_df.columns:
                visit_sat = visit_df.merge(sat_df, on='visit_id', how='inner')
                monthly_sat = visit_sat.groupby(['year', 'month'])['overall_score'].mean().reset_index()
                monthly_sat.columns = ['year', 'month', 'avg_score']
                monthly_sat = monthly_sat.sort_values(['year', 'month']).reset_index(drop=True)
                monthly_sat['环比变化率'] = monthly_sat['avg_score'].pct_change() * 100
                monthly_sat['year'] = monthly_sat['year'].astype(int)
                monthly_sat['month'] = monthly_sat['month'].astype(int)
                result['满意度环比'] = [
                    {
                        '月份': f"{int(row['year'])}-{int(row['month']):02d}",
                        '平均满意度': round(row['avg_score'], 2),
                        '环比变化率': round(row['环比变化率'], 2) if pd.notna(row['环比变化率']) else None
                    }
                    for _, row in monthly_sat.iterrows()
                ]

        return result

    def get_weekday_weekend_comparison(self) -> Dict[str, Any]:
        result = {}

        if self.data.get('registrations') is not None:
            reg_df = self.data['registrations']
            if 'is_weekend' in reg_df.columns:
                weekday_reg = reg_df[reg_df['is_weekend'] == 0]
                weekend_reg = reg_df[reg_df['is_weekend'] == 1]
                result['挂号量'] = {
                    '工作日': len(weekday_reg),
                    '周末': len(weekend_reg),
                    '工作日日均': round(len(weekday_reg) / weekday_reg['reg_date'].nunique(), 1) if weekday_reg['reg_date'].nunique() > 0 else 0,
                    '周末日均': round(len(weekend_reg) / weekend_reg['reg_date'].nunique(), 1) if weekend_reg['reg_date'].nunique() > 0 else 0
                }

        if self.data.get('visits') is not None and self.data.get('registrations') is not None:
            visit_df = self.data['visits']
            reg_df = self.data['registrations']
            if 'is_weekend' in reg_df.columns:
                reg_visit = reg_df.merge(visit_df, on='reg_id', how='inner')
                weekday_visits = reg_visit[reg_visit['is_weekend'] == 0]
                weekend_visits = reg_visit[reg_visit['is_weekend'] == 1]
                result['就诊量'] = {
                    '工作日': len(weekday_visits),
                    '周末': len(weekend_visits)
                }

        if self.data.get('waiting_times') is not None and self.data.get('registrations') is not None:
            wt_df = self.data['waiting_times']
            reg_df = self.data['registrations']
            if 'is_weekend' in reg_df.columns and 'wait_minutes' in wt_df.columns:
                reg_wt = reg_df.merge(wt_df, on='reg_id', how='inner')
                weekday_wt = reg_wt[reg_wt['is_weekend'] == 0]['wait_minutes']
                weekend_wt = reg_wt[reg_wt['is_weekend'] == 1]['wait_minutes']
                result['候诊时间'] = {
                    '工作日平均': round(weekday_wt.mean(), 1) if len(weekday_wt) > 0 else 0,
                    '周末平均': round(weekend_wt.mean(), 1) if len(weekend_wt) > 0 else 0,
                    '工作日中位数': round(weekday_wt.median(), 1) if len(weekday_wt) > 0 else 0,
                    '周末中位数': round(weekend_wt.median(), 1) if len(weekend_wt) > 0 else 0
                }

        if self.data.get('satisfaction') is not None and self.data.get('visits') is not None and self.data.get('registrations') is not None:
            sat_df = self.data['satisfaction']
            visit_df = self.data['visits']
            reg_df = self.data['registrations']
            if 'is_weekend' in reg_df.columns and 'overall_score' in sat_df.columns:
                reg_visit = reg_df.merge(visit_df, on='reg_id', how='inner')
                reg_visit_sat = reg_visit.merge(sat_df, on='visit_id', how='inner')
                weekday_sat = reg_visit_sat[reg_visit_sat['is_weekend'] == 0]['overall_score']
                weekend_sat = reg_visit_sat[reg_visit_sat['is_weekend'] == 1]['overall_score']
                result['满意度'] = {
                    '工作日平均': round(weekday_sat.mean(), 2) if len(weekday_sat) > 0 else 0,
                    '周末平均': round(weekend_sat.mean(), 2) if len(weekend_sat) > 0 else 0
                }

        return result

    def get_peak_hours(self) -> Optional[pd.DataFrame]:
        if self.data.get('registrations') is not None:
            reg_df = self.data['registrations']
            if 'reg_time' in reg_df.columns:
                reg_df_copy = reg_df.copy()
                reg_df_copy['hour'] = reg_df_copy['reg_time'].apply(
                    lambda x: int(str(x).split(':')[0]) if pd.notna(x) else None
                )
                reg_df_copy = reg_df_copy.dropna(subset=['hour'])
                reg_df_copy['hour'] = reg_df_copy['hour'].astype(int)
                hourly = reg_df_copy.groupby('hour')['reg_id'].count().reset_index()
                hourly.columns = ['hour', 'registrations']
                hourly = hourly.sort_values('hour').reset_index(drop=True)
                top3_hours = hourly.nlargest(3, 'registrations')['hour'].tolist()
                hourly['is_peak'] = hourly['hour'].apply(lambda x: True if x in top3_hours else False)
                return hourly
        return None

    def get_department_capacity_utilization(self) -> Optional[pd.DataFrame]:
        if self.data.get('visits') is not None and self.data.get('doctors') is not None and self.data.get('departments') is not None:
            visit_df = self.data['visits']
            doctor_df = self.data['doctors']
            dept_df = self.data['departments']

            actual_visits = visit_df.groupby('department_id')['visit_id'].count().reset_index()
            actual_visits.columns = ['department_id', 'actual_visits']

            doctor_count = doctor_df.groupby('department_id').size().reset_index()
            doctor_count.columns = ['department_id', 'doctor_count']

            if 'visit_date' in visit_df.columns:
                total_days = visit_df['visit_date'].nunique()
                date_range = pd.to_datetime(visit_df['visit_date'])
                if len(date_range) > 0:
                    workdays = np.busday_count(
                        date_range.min().strftime('%Y-%m-%d'),
                        (date_range.max() + pd.Timedelta(days=1)).strftime('%Y-%m-%d')
                    )
                    workdays = max(workdays, 1)
                else:
                    workdays = 1
            else:
                workdays = 1

            max_daily_per_doctor = 40

            result = dept_df[['department_id', 'department_name']].merge(
                doctor_count, on='department_id', how='left'
            ).merge(
                actual_visits, on='department_id', how='left'
            )

            result['doctor_count'] = result['doctor_count'].fillna(0).astype(int)
            result['actual_visits'] = result['actual_visits'].fillna(0).astype(int)
            result['capacity'] = result['doctor_count'] * max_daily_per_doctor * workdays
            result['utilization_rate'] = result.apply(
                lambda x: round((x['actual_visits'] / x['capacity']) * 100, 2) if x['capacity'] > 0 else 0,
                axis=1
            )

            return result[['department_name', 'capacity', 'actual_visits', 'utilization_rate']]
        return None

    def get_doctor_workload_balance(self) -> Dict[str, Any]:
        result = {}

        if self.data.get('visits') is not None:
            visit_df = self.data['visits']
            workload = visit_df.groupby('doctor_id')['visit_id'].count().values.astype(float)

            if len(workload) == 0:
                return result

            sorted_workload = np.sort(workload)
            n = len(sorted_workload)
            cum_values = np.cumsum(sorted_workload)
            gini = (2 * np.sum((np.arange(1, n + 1)) * sorted_workload) - (n + 1) * np.sum(sorted_workload)) / (n * np.sum(sorted_workload)) if np.sum(sorted_workload) > 0 else 0
            result['基尼系数'] = round(float(gini), 4)

            mean_wl = np.mean(workload)
            std_wl = np.std(workload)
            cv = std_wl / mean_wl if mean_wl > 0 else 0
            result['变异系数'] = round(float(cv), 4)

            max_wl = np.max(workload)
            min_wl = np.min(workload)
            max_min_ratio = max_wl / min_wl if min_wl > 0 else float('inf')
            result['最高负荷最低负荷比'] = round(float(max_min_ratio), 2)

            threshold = mean_wl * 1.2
            overload_count = int(np.sum(workload > threshold))
            overload_ratio = overload_count / n if n > 0 else 0
            result['超负荷医生占比'] = round(float(overload_ratio) * 100, 2)
            result['超负荷医生人数'] = overload_count
            result['平均负荷'] = round(float(mean_wl), 1)
            result['超负荷阈值'] = round(float(threshold), 1)

        return result

    def get_wait_time_stratification(self) -> Dict[str, Any]:
        result = {}

        if self.data.get('waiting_times') is not None:
            wt_df = self.data['waiting_times']
            if 'wait_minutes' in wt_df.columns:
                total = len(wt_df)
                if total == 0:
                    return result

                lt_15 = wt_df[wt_df['wait_minutes'] < 15]
                between_15_30 = wt_df[(wt_df['wait_minutes'] >= 15) & (wt_df['wait_minutes'] < 30)]
                between_30_60 = wt_df[(wt_df['wait_minutes'] >= 30) & (wt_df['wait_minutes'] < 60)]
                gt_60 = wt_df[wt_df['wait_minutes'] >= 60]

                result['小于15分钟'] = {
                    '人数': len(lt_15),
                    '占比': round((len(lt_15) / total) * 100, 2)
                }
                result['15至30分钟'] = {
                    '人数': len(between_15_30),
                    '占比': round((len(between_15_30) / total) * 100, 2)
                }
                result['30至60分钟'] = {
                    '人数': len(between_30_60),
                    '占比': round((len(between_30_60) / total) * 100, 2)
                }
                result['大于60分钟'] = {
                    '人数': len(gt_60),
                    '占比': round((len(gt_60) / total) * 100, 2)
                }

        return result

    def get_anomaly_cause_analysis(self) -> List[Dict[str, Any]]:
        anomalies = self.detect_anomalous_departments()
        if anomalies is None or len(anomalies) == 0:
            return []

        result = []
        dept_metrics = self.get_department_metrics()

        for _, row in anomalies.iterrows():
            dept_name = row.get('department_name', '未知科室')
            anomaly_type = row.get('anomaly_type', '未知异常')
            causes = []

            if anomaly_type == '高候诊时间':
                if 'doctor_count' in row and pd.notna(row.get('doctor_count')):
                    if row['doctor_count'] <= 3:
                        causes.append('医生数量偏少，接诊能力不足')
                if 'total_registrations' in row and pd.notna(row.get('total_registrations')):
                    overall_avg_reg = dept_metrics['total_registrations'].mean() if dept_metrics is not None and 'total_registrations' in dept_metrics.columns else 0
                    if row['total_registrations'] > overall_avg_reg * 1.3:
                        causes.append('挂号量显著高于平均水平，患者集中')
                if 'visits_per_doctor' in row and pd.notna(row.get('visits_per_doctor')):
                    overall_avg_vpd = dept_metrics['visits_per_doctor'].mean() if dept_metrics is not None and 'visits_per_doctor' in dept_metrics.columns else 0
                    if row['visits_per_doctor'] > overall_avg_vpd * 1.2:
                        causes.append('人均接诊量偏高，单医生负荷过大')
                if not causes:
                    causes.append('候诊流程可能存在效率瓶颈，需进一步排查')

            elif anomaly_type == '低满意度':
                if 'avg_wait_time' in row and pd.notna(row.get('avg_wait_time')):
                    overall_avg_wait = dept_metrics['avg_wait_time'].mean() if dept_metrics is not None and 'avg_wait_time' in dept_metrics.columns else 0
                    if row['avg_wait_time'] > overall_avg_wait * 1.2:
                        causes.append('候诊时间偏长，影响患者体验')
                if 'visits_per_doctor' in row and pd.notna(row.get('visits_per_doctor')):
                    overall_avg_vpd = dept_metrics['visits_per_doctor'].mean() if dept_metrics is not None and 'visits_per_doctor' in dept_metrics.columns else 0
                    if row['visits_per_doctor'] > overall_avg_vpd * 1.2:
                        causes.append('医生工作负荷过重，可能导致服务质量下降')
                if self.data.get('satisfaction') is not None:
                    sat_df = self.data['satisfaction']
                    visit_df = self.data.get('visits')
                    if visit_df is not None and 'department_id' in row:
                        dept_visits = visit_df[visit_df['department_id'] == row['department_id']]
                        dept_sat = dept_visits.merge(sat_df, on='visit_id', how='inner')
                        if 'wait_score' in dept_sat.columns and len(dept_sat) > 0:
                            if dept_sat['wait_score'].mean() < dept_sat['service_score'].mean():
                                causes.append('候诊评分低于服务评分，候诊环节为主要不满来源')
                if not causes:
                    causes.append('满意度偏低原因待查，建议进行患者回访调查')

            elif anomaly_type == '高工作负荷':
                if 'doctor_count' in row and pd.notna(row.get('doctor_count')):
                    if row['doctor_count'] <= 3:
                        causes.append('科室医生配置不足，无法有效分流患者')
                if 'avg_wait_time' in row and pd.notna(row.get('avg_wait_time')):
                    overall_avg_wait = dept_metrics['avg_wait_time'].mean() if dept_metrics is not None and 'avg_wait_time' in dept_metrics.columns else 0
                    if row['avg_wait_time'] > overall_avg_wait:
                        causes.append('高负荷导致候诊时间增加，可能形成恶性循环')
                if self.data.get('doctors') is not None and 'department_id' in row:
                    dept_doctors = self.data['doctors'][self.data['doctors']['department_id'] == row['department_id']]
                    if 'title' in dept_doctors.columns:
                        senior_ratio = (dept_doctors['title'].isin(['主任医师', '副主任医师'])).mean()
                        if senior_ratio < 0.3:
                            causes.append('高级职称医生占比较低，复杂病例处理效率受限')
                if not causes:
                    causes.append('工作负荷偏高，建议增加科室人员配置')

            if self.data.get('visits') is not None and 'department_id' in row:
                visit_df = self.data['visits']
                dept_visits = visit_df[visit_df['department_id'] == row['department_id']]
                if 'total_fee' in dept_visits.columns and len(dept_visits) > 0:
                    dept_avg_fee = dept_visits['total_fee'].mean()
                    overall_avg_fee = visit_df['total_fee'].mean()
                    if dept_avg_fee > overall_avg_fee * 1.5:
                        causes.append('该科室次均费用显著偏高，存在过度检查或用药可能')
                    elif dept_avg_fee < overall_avg_fee * 0.5:
                        causes.append('该科室次均费用明显偏低，可能存在服务不足')

            result.append({
                '科室名称': dept_name,
                '异常类型': anomaly_type,
                '可能原因': causes,
                '关键指标': {
                    k: round(v, 2) if isinstance(v, float) else v
                    for k, v in row.items()
                    if k not in ['department_id'] and pd.notna(v)
                }
            })

        return result
