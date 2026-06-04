import streamlit as st
import pandas as pd
from utils.error_handler import ErrorHandler
import plotly.express as px
import plotly.graph_objects as go


@st.cache_data
def filter_data_by_criteria(data, department, date_range, patient_type):
    filtered = {}
    
    reg_df = data.get('registrations', pd.DataFrame()).copy()
    if len(reg_df) > 0:
        if department and department != '全部':
            dept_name_map = data.get('departments', pd.DataFrame())
            if len(dept_name_map) > 0 and 'department_id' in dept_name_map.columns:
                dept_id = dept_name_map[dept_name_map['department_name'] == department]['department_id'].values
                if len(dept_id) > 0:
                    reg_df = reg_df[reg_df['department_id'] == dept_id[0]]
        
        if date_range and len(date_range) == 2:
            reg_df = reg_df[
                (reg_df['reg_date'] >= date_range[0].strftime('%Y-%m-%d')) &
                (reg_df['reg_date'] <= date_range[1].strftime('%Y-%m-%d'))
            ]
        
        if patient_type and patient_type != '全部':
            reg_df = reg_df[reg_df['patient_type'] == patient_type]
        
        filtered['registrations'] = reg_df
    
    if data.get('visits') is not None and len(reg_df) > 0:
        visit_df = data['visits'].copy()
        valid_reg_ids = reg_df['reg_id'].tolist()
        filtered['visits'] = visit_df[visit_df['reg_id'].isin(valid_reg_ids)]
    
    if data.get('waiting_times') is not None and len(reg_df) > 0:
        wt_df = data['waiting_times'].copy()
        valid_reg_ids = reg_df['reg_id'].tolist()
        filtered['waiting_times'] = wt_df[wt_df['reg_id'].isin(valid_reg_ids)]
    
    if data.get('satisfaction') is not None and data.get('visits') is not None:
        sat_df = data['satisfaction'].copy()
        visit_df = filtered.get('visits', pd.DataFrame())
        if len(visit_df) > 0:
            valid_visit_ids = visit_df['visit_id'].tolist()
            filtered['satisfaction'] = sat_df[sat_df['visit_id'].isin(valid_visit_ids)]
        else:
            filtered['satisfaction'] = sat_df.head(0)
    
    return filtered


@st.cache_data
def get_filtered_wait_time_stratification(filtered_data):
    result = {}
    wt_df = filtered_data.get('waiting_times')
    if wt_df is not None and len(wt_df) > 0 and 'wait_minutes' in wt_df.columns:
        total = len(wt_df)
        if total > 0:
            lt_15 = wt_df[wt_df['wait_minutes'] < 15]
            between_15_30 = wt_df[(wt_df['wait_minutes'] >= 15) & (wt_df['wait_minutes'] < 30)]
            between_30_60 = wt_df[(wt_df['wait_minutes'] >= 30) & (wt_df['wait_minutes'] < 60)]
            gt_60 = wt_df[wt_df['wait_minutes'] >= 60]
            
            result['小于15分钟'] = {'人数': len(lt_15), '占比': round((len(lt_15) / total) * 100, 2)}
            result['15至30分钟'] = {'人数': len(between_15_30), '占比': round((len(between_15_30) / total) * 100, 2)}
            result['30至60分钟'] = {'人数': len(between_30_60), '占比': round((len(between_30_60) / total) * 100, 2)}
            result['大于60分钟'] = {'人数': len(gt_60), '占比': round((len(gt_60) / total) * 100, 2)}
    return result


@st.cache_data
def get_filtered_weekday_weekend_comparison(filtered_data):
    result = {}
    reg_df = filtered_data.get('registrations')
    wt_df = filtered_data.get('waiting_times')
    
    if reg_df is not None and wt_df is not None and len(reg_df) > 0 and len(wt_df) > 0:
        if 'is_weekend' in reg_df.columns and 'wait_minutes' in wt_df.columns:
            reg_wt = reg_df.merge(wt_df, on='reg_id', how='inner')
            weekday_wt = reg_wt[reg_wt['is_weekend'] == 0]['wait_minutes']
            weekend_wt = reg_wt[reg_wt['is_weekend'] == 1]['wait_minutes']
            
            result['候诊时间'] = {
                '工作日平均': round(weekday_wt.mean(), 1) if len(weekday_wt) > 0 else 0,
                '周末平均': round(weekend_wt.mean(), 1) if len(weekend_wt) > 0 else 0,
                '工作日中位数': round(weekday_wt.median(), 1) if len(weekday_wt) > 0 else 0,
                '周末中位数': round(weekend_wt.median(), 1) if len(weekend_wt) > 0 else 0,
                '工作日标准差': round(weekday_wt.std(), 1) if len(weekday_wt) > 0 else 0,
                '周末标准差': round(weekend_wt.std(), 1) if len(weekend_wt) > 0 else 0
            }
    
    return result


@st.cache_data
def get_filtered_peak_hours(filtered_data):
    reg_df = filtered_data.get('registrations')
    wt_df = filtered_data.get('waiting_times')
    
    if reg_df is not None and wt_df is not None and len(reg_df) > 0 and len(wt_df) > 0:
        reg_wt = reg_df.merge(wt_df, on='reg_id', how='inner')
        if 'reg_time' in reg_wt.columns and 'wait_minutes' in reg_wt.columns:
            reg_wt['hour'] = reg_wt['reg_time'].apply(
                lambda x: int(str(x).split(':')[0]) if pd.notna(x) else None
            )
            reg_wt = reg_wt.dropna(subset=['hour'])
            reg_wt['hour'] = reg_wt['hour'].astype(int)
            
            hourly = reg_wt.groupby('hour')['wait_minutes'].agg(['mean', 'count']).reset_index()
            hourly.columns = ['hour', 'avg_wait', 'count']
            hourly = hourly.sort_values('hour').reset_index(drop=True)
            
            if len(hourly) > 0:
                peak_threshold = hourly['avg_wait'].quantile(0.75)
                hourly['is_peak'] = hourly['avg_wait'] >= peak_threshold
            
            return hourly
    return None


@st.cache_data
def get_satisfaction_cause_analysis(filtered_data):
    result = {}
    sat_df = filtered_data.get('satisfaction')
    wt_df = filtered_data.get('waiting_times')
    reg_df = filtered_data.get('registrations')
    visit_df = filtered_data.get('visits')
    dept_df = filtered_data.get('departments')
    
    if sat_df is not None and len(sat_df) > 0:
        avg_wait_score = sat_df['wait_score'].mean() if 'wait_score' in sat_df.columns else 0
        avg_service_score = sat_df['service_score'].mean() if 'service_score' in sat_df.columns else 0
        
        result['候诊评分'] = round(avg_wait_score, 2)
        result['服务评分'] = round(avg_service_score, 2)
        result['评分差距'] = round(avg_service_score - avg_wait_score, 2)
        result['候诊为主要不满'] = avg_wait_score < avg_service_score - 0.5
        
        low_sat = sat_df[sat_df['overall_score'] < 3] if 'overall_score' in sat_df.columns else sat_df.head(0)
        result['低分满意度记录数'] = len(low_sat)
        result['低分满意度占比'] = round((len(low_sat) / len(sat_df)) * 100, 2) if len(sat_df) > 0 else 0
        
        if len(low_sat) > 0 and visit_df is not None and wt_df is not None and reg_df is not None:
            visit_reg = reg_df.merge(visit_df, on='reg_id', how='inner')
            low_sat_visits = low_sat.merge(visit_reg, on='visit_id', how='inner')
            low_sat_with_wt = low_sat_visits.merge(wt_df, on='reg_id', how='inner')
            
            if 'wait_minutes' in low_sat_with_wt.columns:
                result['低分平均候诊时间'] = round(low_sat_with_wt['wait_minutes'].mean(), 1)
            
            if 'department_name' in low_sat_visits.columns:
                dept_dist = low_sat_visits['department_name'].value_counts().reset_index()
                dept_dist.columns = ['category', 'count']
                result['低分科室分布'] = dept_dist.to_dict('records')
            
            if 'hour' not in low_sat_visits.columns and 'reg_time' in low_sat_visits.columns:
                low_sat_visits['hour'] = low_sat_visits['reg_time'].apply(
                    lambda x: int(str(x).split(':')[0]) if pd.notna(x) else None
                )
            if 'hour' in low_sat_visits.columns:
                hour_dist = low_sat_visits['hour'].value_counts().reset_index()
                hour_dist.columns = ['category', 'count']
                hour_dist['category'] = hour_dist['category'].apply(lambda x: f'{int(x)}:00' if pd.notna(x) else '未知')
                result['低分时段分布'] = hour_dist.to_dict('records')
    
    return result


@st.cache_data
def get_satisfaction_mom(filtered_data):
    sat_df = filtered_data.get('satisfaction')
    visit_df = filtered_data.get('visits')
    reg_df = filtered_data.get('registrations')
    wt_df = filtered_data.get('waiting_times')
    
    if sat_df is None or visit_df is None or reg_df is None or wt_df is None:
        return None
    
    if len(sat_df) == 0 or len(visit_df) == 0 or len(reg_df) == 0 or len(wt_df) == 0:
        return None
    
    if 'year' not in visit_df.columns or 'month' not in visit_df.columns:
        return None
    
    visit_sat = visit_df.merge(sat_df, on='visit_id', how='inner')
    visit_reg = visit_sat.merge(reg_df, on='reg_id', how='inner')
    visit_reg_wt = visit_reg.merge(wt_df, on='reg_id', how='inner')
    
    if 'overall_score' not in visit_reg_wt.columns or 'wait_minutes' not in visit_reg_wt.columns:
        return None
    
    monthly = visit_reg_wt.groupby(['year', 'month']).agg({
        'overall_score': 'mean',
        'wait_minutes': 'mean'
    }).reset_index()
    monthly.columns = ['year', 'month', 'avg_score', 'avg_wait']
    monthly = monthly.sort_values(['year', 'month']).reset_index(drop=True)
    
    monthly['满意度环比变化率'] = monthly['avg_score'].pct_change() * 100
    monthly['候诊时间环比变化率'] = monthly['avg_wait'].pct_change() * 100
    
    result = []
    for _, row in monthly.iterrows():
        result.append({
            '月份': f"{int(row['year'])}-{int(row['month']):02d}",
            '平均满意度': round(row['avg_score'], 2),
            '平均候诊时间': round(row['avg_wait'], 1),
            '满意度环比变化率': round(row['满意度环比变化率'], 2) if pd.notna(row['满意度环比变化率']) else None,
            '候诊时间环比变化率': round(row['候诊时间环比变化率'], 2) if pd.notna(row['候诊时间环比变化率']) else None,
            '满意度告警': row['满意度环比变化率'] < -5 if pd.notna(row['满意度环比变化率']) else False
        })
    
    return result


class PatientExperiencePage:
    def __init__(self, data, metrics_calculator, chart_generator):
        self.data = data
        self.metrics = metrics_calculator
        self.charts = chart_generator

    def render(self):
        if not st.session_state.get('data_loaded', False):
            st.warning("⚠️ 请先导入数据")
            return

        st.header("😊 患者体验分析")

        try:
            self._render_filters()
            
            filtered_data = filter_data_by_criteria(
                self.data,
                st.session_state.get('pe_selected_department', '全部'),
                st.session_state.get('pe_date_range', []),
                st.session_state.get('pe_patient_type', '全部')
            )
            
            sat_dist = self._get_filtered_satisfaction_distribution(filtered_data)
            has_waiting_data = filtered_data.get('waiting_times') is not None and len(filtered_data['waiting_times']) > 0

            if not sat_dist and not has_waiting_data:
                st.warning("⚠️ 暂无患者体验数据，请检查是否已上传候诊时间、患者满意度等必要数据")
                return

            self._render_satisfaction_overview(sat_dist)

            st.divider()

            col1, col2 = st.columns(2)
            with col1:
                self._render_satisfaction_radar(sat_dist)
            with col2:
                self._render_satisfaction_distribution(sat_dist)

            st.divider()
            self._render_waiting_time_analysis(filtered_data)

            st.divider()
            self._render_wait_time_stratification(filtered_data)

            st.divider()
            col1, col2 = st.columns(2)
            with col1:
                self._render_weekday_weekend_comparison(filtered_data)
            with col2:
                self._render_peak_hours(filtered_data)

            st.divider()
            self._render_satisfaction_cause_analysis(filtered_data)

            st.divider()
            self._render_satisfaction_mom(filtered_data)

        except Exception as e:
            error_msg = ErrorHandler.translate_error(e, "患者体验分析")
            ErrorHandler.display_error(f"❌ 患者体验分析失败：{error_msg}")

    def _render_filters(self):
        st.subheader("🔍 数据筛选")
        
        col1, col2, col3 = st.columns(3)
        
        with col1:
            departments = ['全部']
            if self.data.get('departments') is not None and len(self.data['departments']) > 0:
                departments += self.data['departments']['department_name'].tolist()
            selected_dept = st.selectbox(
                "选择科室",
                departments,
                index=0,
                key='pe_selected_department'
            )
        
        with col2:
            min_date, max_date = None, None
            if self.data.get('registrations') is not None and len(self.data['registrations']) > 0:
                dates = pd.to_datetime(self.data['registrations']['reg_date'])
                min_date = dates.min().date()
                max_date = dates.max().date()
            
            if min_date and max_date:
                date_range = st.date_input(
                    "日期范围",
                    value=[min_date, max_date],
                    min_value=min_date,
                    max_value=max_date,
                    key='pe_date_range'
                )
        
        with col3:
            patient_types = ['全部']
            if self.data.get('registrations') is not None and len(self.data['registrations']) > 0:
                patient_types += self.data['registrations']['patient_type'].unique().tolist()
            selected_pt = st.selectbox(
                "患者类型",
                patient_types,
                index=0,
                key='pe_patient_type'
            )

    @st.cache_data
    def _get_filtered_satisfaction_distribution(_self, filtered_data):
        result = {}
        sat_df = filtered_data.get('satisfaction')
        if sat_df is not None and len(sat_df) > 0:
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

    def _render_satisfaction_overview(self, sat_dist):
        st.subheader("满意度概览")

        cols = st.columns(4)
        avg_scores = sat_dist.get('avg_scores', {})

        with cols[0]:
            st.metric("整体满意度", f"{avg_scores.get('overall_score', 0):.2f}分")
        with cols[1]:
            st.metric("候诊满意度", f"{avg_scores.get('wait_score', 0):.2f}分")
        with cols[2]:
            st.metric("服务满意度", f"{avg_scores.get('service_score', 0):.2f}分")
        with cols[3]:
            rec_rate = sat_dist.get('recommendation_rate', 0)
            st.metric("推荐率", f"{rec_rate}%")

    def _render_satisfaction_radar(self, sat_dist):
        st.subheader("满意度雷达图")
        fig = self.charts.create_satisfaction_radar_chart(sat_dist)
        if fig:
            st.plotly_chart(fig, use_container_width=True)

    def _render_satisfaction_distribution(self, sat_dist):
        st.subheader("满意度分布")

        if 'overall_score_distribution' in sat_dist:
            dist = sat_dist['overall_score_distribution']
            fig = self.charts.create_satisfaction_distribution_chart(dist)
            if fig:
                st.plotly_chart(fig, use_container_width=True)

    def _render_waiting_time_analysis(self, filtered_data):
        st.subheader("⏱️ 候诊时间分析")

        wt_df = filtered_data.get('waiting_times')
        if wt_df is not None and len(wt_df) > 0:
            col1, col2, col3, col4 = st.columns(4)
            with col1:
                st.metric("平均候诊", f"{wt_df['wait_minutes'].mean():.1f}分钟")
            with col2:
                st.metric("中位候诊", f"{wt_df['wait_minutes'].median():.1f}分钟")
            with col3:
                st.metric("最长候诊", f"{wt_df['wait_minutes'].max()}分钟")
            with col4:
                over_30_pct = (wt_df['wait_minutes'] > 30).mean() * 100
                st.metric("候诊超30分钟", f"{over_30_pct:.1f}%")

            fig = self.charts.create_waiting_time_histogram(wt_df)
            if fig:
                st.plotly_chart(fig, use_container_width=True)

            self._render_wait_time_by_period(filtered_data, wt_df)

    def _render_wait_time_by_period(self, filtered_data, wt_df):
        st.subheader("不同时段候诊对比")

        reg_df = filtered_data.get('registrations')
        if reg_df is not None and len(reg_df) > 0:
            reg_df_copy = reg_df.copy()
            reg_df_copy['hour'] = reg_df_copy['reg_time'].str[:2].astype(int)

            def get_period(hour):
                if 7 <= hour < 10:
                    return '早高峰 (7-10点)'
                elif 10 <= hour < 12:
                    return '上午 (10-12点)'
                elif 13 <= hour < 16:
                    return '午高峰 (13-16点)'
                elif 16 <= hour < 18:
                    return '下午 (16-18点)'
                else:
                    return '其他时段'

            reg_df_copy['period'] = reg_df_copy['hour'].apply(get_period)

            merged = reg_df_copy.merge(wt_df, on='reg_id', how='inner')
            period_stats = merged.groupby('period')['wait_minutes'].agg(['mean', 'count']).reset_index()
            period_stats.columns = ['时段', '平均候诊时间', '人数']

            fig = px.bar(
                period_stats,
                x='时段',
                y='平均候诊时间',
                color='平均候诊时间',
                color_continuous_scale='OrRd',
                text='人数'
            )
            fig.update_layout(height=350, yaxis_title='平均候诊时间(分钟)')
            st.plotly_chart(fig, use_container_width=True)

    def _render_wait_time_stratification(self, filtered_data):
        st.subheader("⏱️ 候诊时间分层分析")
        
        stratification = get_filtered_wait_time_stratification(filtered_data)
        
        if stratification:
            col1, col2 = st.columns([1, 1])
            
            with col1:
                fig = self.charts.create_wait_time_stratification_chart(stratification)
                if fig:
                    st.plotly_chart(fig, use_container_width=True)
            
            with col2:
                st.markdown("#### 分档统计")
                labels_map = {
                    '小于15分钟': ('<15分钟', '快速', '✅'),
                    '15至30分钟': ('15-30分钟', '正常', '✅'),
                    '30至60分钟': ('30-60分钟', '偏长', '⚠️'),
                    '大于60分钟': ('>60分钟', '过长', '🔴')
                }
                
                for key in ['小于15分钟', '15至30分钟', '30至60分钟', '大于60分钟']:
                    if key in stratification:
                        label, status, icon = labels_map.get(key, (key, '', ''))
                        data = stratification[key]
                        if key == '大于60分钟':
                            st.markdown(f"{icon} **{label}** ({status}): **{data['人数']}**人 ({data['占比']}%) 🔴")
                        else:
                            st.markdown(f"{icon} **{label}** ({status}): **{data['人数']}**人 ({data['占比']}%)")
                
                gt_60 = stratification.get('大于60分钟', {}).get('占比', 0)
                if gt_60 > 10:
                    st.error(f"⚠️ 过长等待占比达 {gt_60}%，建议优化候诊流程！")

    def _render_weekday_weekend_comparison(self, filtered_data):
        st.subheader("📅 工作日vs周末候诊对比")
        
        comparison = get_filtered_weekday_weekend_comparison(filtered_data)
        
        if comparison and '候诊时间' in comparison:
            fig = self.charts.create_weekday_weekend_waiting_chart(comparison)
            if fig:
                st.plotly_chart(fig, use_container_width=True)
            
            wt_data = comparison['候诊时间']
            st.markdown("#### 差异分析")
            avg_diff = wt_data.get('周末平均', 0) - wt_data.get('工作日平均', 0)
            if avg_diff > 0:
                st.info(f"📊 周末平均候诊比工作日长 **{avg_diff:.1f}** 分钟")
                st.markdown("""
                **可能原因：**
                - 🩺 周末值班医生数量相对较少
                - 👥 周末患者集中就诊，特别是专科门诊
                - ⏰ 周末辅助科室（检验、影像）人员配置减少
                - 📋 部分检查项目周末无法完成，导致等待积压
                """)
            else:
                st.success(f"📊 周末候诊情况良好，比工作日短 **{abs(avg_diff):.1f}** 分钟")

    def _render_peak_hours(self, filtered_data):
        st.subheader("🚦 高峰时段候诊压力")
        
        peak_hours = get_filtered_peak_hours(filtered_data)
        
        if peak_hours is not None and len(peak_hours) > 0:
            fig = self.charts.create_peak_hours_waiting_chart(peak_hours)
            if fig:
                st.plotly_chart(fig, use_container_width=True)
            
            peak_periods = peak_hours[peak_hours['is_peak'] == True]['hour'].tolist()
            if peak_periods:
                peak_times = ", ".join([f"{h}:00" for h in sorted(peak_periods)])
                st.warning(f"🚨 高峰时段：**{peak_times}**")
                st.markdown("""
                **就诊建议：**
                - ⏰ 避开高峰时段，建议选择 11:00-12:00 或 15:00-17:00 就诊
                - 📱 优先使用预约挂号，减少现场等待时间
                - 🏥 非急症可考虑工作日下午就诊
                """)

    def _render_satisfaction_cause_analysis(self, filtered_data):
        st.subheader("🔍 满意度下降原因分析")
        
        cause_analysis = get_satisfaction_cause_analysis(filtered_data)
        
        if cause_analysis:
            col1, col2 = st.columns([1, 1])
            
            with col1:
                st.markdown("#### 评分对比分析")
                wait_score = cause_analysis.get('候诊评分', 0)
                service_score = cause_analysis.get('服务评分', 0)
                score_gap = cause_analysis.get('评分差距', 0)
                
                fig = go.Figure()
                fig.add_trace(go.Bar(
                    x=['候诊满意度', '服务满意度'],
                    y=[wait_score, service_score],
                    marker_color=['#dc3545', '#28a745'],
                    text=[f'{wait_score:.2f}', f'{service_score:.2f}'],
                    textposition='auto'
                ))
                fig.update_layout(
                    title='候诊vs服务满意度对比',
                    yaxis_title='评分 (分)',
                    yaxis=dict(range=[0, 5.5]),
                    height=350
                )
                st.plotly_chart(fig, use_container_width=True)
                
                if cause_analysis.get('候诊为主要不满', False):
                    st.error(f"🔴 候诊评分显著低于服务评分 ({score_gap:.2f}分)，候诊是主要不满来源")
                else:
                    st.success("✅ 候诊评分与服务评分差距不大")
            
            with col2:
                st.markdown("#### 低分满意度特征")
                low_sat_count = cause_analysis.get('低分满意度记录数', 0)
                low_sat_pct = cause_analysis.get('低分满意度占比', 0)
                avg_wait = cause_analysis.get('低分平均候诊时间', 0)
                
                st.metric("低分满意度人数", f"{low_sat_count}人")
                st.metric("低分满意度占比", f"{low_sat_pct}%")
                st.metric("低分记录平均候诊时间", f"{avg_wait:.1f}分钟")
                
                dept_dist = cause_analysis.get('低分科室分布', [])
                if dept_dist and len(dept_dist) > 0:
                    st.markdown("**低分科室TOP3:**")
                    for i, item in enumerate(dept_dist[:3]):
                        st.markdown(f"{i+1}. {item['category']}: {item['count']}人")
            
            st.markdown("#### 改进建议")
            suggestions = []
            
            if cause_analysis.get('候诊为主要不满', False):
                suggestions.append("🏥 优化候诊流程，增加叫号屏幕和等候区座位")
                suggestions.append("📱 推行候诊提醒APP，让患者随时了解排队进度")
                suggestions.append("⏰ 实行分时段就诊，减少集中等候")
            
            if avg_wait > 45:
                suggestions.append("👨‍⚕️ 高峰时段增派医生，提高接诊效率")
                suggestions.append("🔄 优化科室排班，均衡各时段医生资源")
            
            if low_sat_pct > 15:
                suggestions.append("📋 对低分患者进行回访，了解具体不满原因")
                suggestions.append("🎯 开展服务质量培训，提升医患沟通能力")
            
            if suggestions:
                for s in suggestions:
                    st.markdown(f"- {s}")
            else:
                st.success("✅ 患者满意度良好，继续保持！")

    def _render_satisfaction_mom(self, filtered_data):
        st.subheader("📈 满意度环比变化")
        
        mom_data = get_satisfaction_mom(filtered_data)
        
        if mom_data and len(mom_data) > 0:
            fig = self.charts.create_satisfaction_mom_chart(mom_data)
            if fig:
                st.plotly_chart(fig, use_container_width=True)
            
            alerts = [item for item in mom_data if item.get('满意度告警', False)]
            if alerts:
                st.error("🚨 **满意度下降告警**")
                for alert in alerts:
                    st.markdown(f"- **{alert['月份']}**: 满意度环比下降 **{abs(alert['满意度环比变化率']):.2f}%**")
                    if alert.get('候诊时间环比变化率', 0) > 0:
                        st.markdown(f"  同期候诊时间环比上升 **{alert['候诊时间环比变化率']:.2f}%**，可能是满意度下降的原因")
            else:
                st.success("✅ 满意度趋势稳定，无显著下降")
            
            st.markdown("#### 月度数据详情")
            df = pd.DataFrame(mom_data)
            df_display = df[['月份', '平均满意度', '平均候诊时间', '满意度环比变化率']].copy()
            df_display.columns = ['月份', '平均满意度(分)', '平均候诊时间(分钟)', '满意度环比(%)']
            st.dataframe(df_display, use_container_width=True)
