import streamlit as st
import pandas as pd
import plotly.express as px
import plotly.graph_objects as go
from utils.error_handler import ErrorHandler


class DepartmentAnalysisPage:
    def __init__(self, metrics_calculator, chart_generator):
        self.metrics = metrics_calculator
        self.charts = chart_generator

    def render(self):
        if not st.session_state.get('data_loaded', False):
            st.warning("⚠️ 请先导入数据")
            return

        st.header("🏥 科室分析")

        try:
            self._init_filters()
            self._render_filters()

            filtered_data = self._get_filtered_data()

            tab1, tab2, tab3, tab4 = st.tabs([
                "📊 基础分析",
                "📈 容量利用率分析",
                "⏰ 高峰时段分析",
                "🚩 异常科室深度分析"
            ])

            with tab1:
                self._render_basic_analysis(filtered_data)

            with tab2:
                self._render_capacity_analysis(filtered_data)

            with tab3:
                self._render_peak_hours_analysis(filtered_data)

            with tab4:
                self._render_anomaly_deep_analysis()

        except Exception as e:
            error_msg = ErrorHandler.translate_error(e, "科室分析")
            ErrorHandler.display_error(f"❌ 科室分析失败：{error_msg}")

    def _init_filters(self):
        if 'dept_filter_date_start' not in st.session_state:
            st.session_state.dept_filter_date_start = None
        if 'dept_filter_date_end' not in st.session_state:
            st.session_state.dept_filter_date_end = None
        if 'dept_filter_departments' not in st.session_state:
            st.session_state.dept_filter_departments = []
        if 'dept_filter_patient_types' not in st.session_state:
            st.session_state.dept_filter_patient_types = []

    def _render_filters(self):
        st.subheader("🔍 筛选器")

        col1, col2 = st.columns(2)

        with col1:
            self._render_date_filters()
            self._render_patient_type_filters()

        with col2:
            self._render_department_filters()

        st.divider()

    def _render_date_filters(self):
        reg_df = st.session_state.data.get('registrations')
        if reg_df is not None and 'reg_date' in reg_df.columns:
            all_dates = pd.to_datetime(reg_df['reg_date']).sort_values()
            min_date = all_dates.min().date()
            max_date = all_dates.max().date()

            if st.session_state.dept_filter_date_start is None:
                st.session_state.dept_filter_date_start = min_date
            if st.session_state.dept_filter_date_end is None:
                st.session_state.dept_filter_date_end = max_date

            date_range = st.date_input(
                "日期范围",
                value=[st.session_state.dept_filter_date_start, st.session_state.dept_filter_date_end],
                min_value=min_date,
                max_value=max_date,
                key='dept_date_range'
            )
            if len(date_range) == 2:
                st.session_state.dept_filter_date_start = date_range[0]
                st.session_state.dept_filter_date_end = date_range[1]

    def _render_department_filters(self):
        dept_df = st.session_state.data.get('departments')
        if dept_df is not None and 'department_name' in dept_df.columns:
            all_depts = sorted(dept_df['department_name'].unique().tolist())

            if not st.session_state.dept_filter_departments:
                st.session_state.dept_filter_departments = all_depts

            selected_depts = st.multiselect(
                "科室选择（默认全选）",
                options=all_depts,
                default=st.session_state.dept_filter_departments,
                key='dept_multiselect'
            )
            st.session_state.dept_filter_departments = selected_depts

    def _render_patient_type_filters(self):
        reg_df = st.session_state.data.get('registrations')
        if reg_df is not None and 'patient_type' in reg_df.columns:
            all_types = sorted(reg_df['patient_type'].unique().tolist())

            if not st.session_state.dept_filter_patient_types:
                st.session_state.dept_filter_patient_types = all_types

            selected_types = st.multiselect(
                "患者类型",
                options=all_types,
                default=st.session_state.dept_filter_patient_types,
                key='patient_type_multiselect'
            )
            st.session_state.dept_filter_patient_types = selected_types

    @st.cache_data
    def _get_filtered_data(_self):
        filtered = {}
        data = st.session_state.data

        reg_df = data.get('registrations').copy() if data.get('registrations') is not None else None

        if reg_df is not None:
            if st.session_state.dept_filter_date_start and st.session_state.dept_filter_date_end:
                reg_df['reg_date_dt'] = pd.to_datetime(reg_df['reg_date'])
                mask = (reg_df['reg_date_dt'].dt.date >= st.session_state.dept_filter_date_start) & \
                       (reg_df['reg_date_dt'].dt.date <= st.session_state.dept_filter_date_end)
                reg_df = reg_df[mask]

            if st.session_state.dept_filter_departments:
                dept_df = data.get('departments')
                if dept_df is not None:
                    selected_dept_ids = dept_df[
                        dept_df['department_name'].isin(st.session_state.dept_filter_departments)
                    ]['department_id'].tolist()
                    reg_df = reg_df[reg_df['department_id'].isin(selected_dept_ids)]

            if st.session_state.dept_filter_patient_types:
                reg_df = reg_df[reg_df['patient_type'].isin(st.session_state.dept_filter_patient_types)]

            filtered['registrations'] = reg_df

        for key in ['visits', 'waiting_times', 'satisfaction', 'doctors', 'departments']:
            if data.get(key) is not None:
                filtered[key] = data.get(key).copy()

        return filtered

    def _render_basic_analysis(self, filtered_data):
        dept_metrics = self._get_filtered_dept_metrics(filtered_data)
        if dept_metrics is None or len(dept_metrics) == 0:
            st.warning("⚠️ 暂无科室分析数据")
            return

        self._render_dept_selector(dept_metrics)

        col1, col2 = st.columns(2)
        with col1:
            self._render_dept_bar_chart(dept_metrics)
        with col2:
            self._render_dept_satisfaction_chart(dept_metrics)

        st.divider()
        self._render_mom_changes(dept_metrics, filtered_data)

        st.divider()
        self._render_dept_details(dept_metrics)

        st.divider()
        self._render_anomalous_departments()

    @st.cache_data
    def _get_filtered_dept_metrics(_self, filtered_data):
        if filtered_data.get('registrations') is not None and filtered_data.get('departments') is not None:
            reg_df = filtered_data['registrations']
            dept_df = filtered_data['departments']

            dept_metrics = reg_df.groupby(
                ['department_id', 'department_name']
            ).agg({
                'reg_id': 'count',
                'patient_id': 'nunique'
            }).reset_index()
            dept_metrics.columns = ['department_id', 'department_name', 'total_registrations', 'unique_patients']

            if filtered_data.get('visits') is not None:
                visit_dept = filtered_data['visits'].groupby('department_id').agg({
                    'visit_id': 'count'
                }).reset_index()
                visit_dept.columns = ['department_id', 'total_visits']
                dept_metrics = dept_metrics.merge(visit_dept, on='department_id', how='left')

            if filtered_data.get('doctors') is not None:
                doc_dept = filtered_data['doctors'].groupby('department_id').size().reset_index()
                doc_dept.columns = ['department_id', 'doctor_count']
                dept_metrics = dept_metrics.merge(doc_dept, on='department_id', how='left')
                dept_metrics['visits_per_doctor'] = round(
                    dept_metrics['total_visits'] / dept_metrics['doctor_count'], 1
                )

            if filtered_data.get('waiting_times') is not None:
                reg_wt = reg_df.merge(
                    filtered_data['waiting_times'], on='reg_id', how='inner'
                )
                wt_dept = reg_wt.groupby('department_id')['wait_minutes'].agg(['mean', 'median']).reset_index()
                wt_dept.columns = ['department_id', 'avg_wait_time', 'median_wait_time']
                dept_metrics = dept_metrics.merge(wt_dept, on='department_id', how='left')

            if filtered_data.get('satisfaction') is not None and filtered_data.get('visits') is not None:
                visit_sat = filtered_data['visits'].merge(
                    filtered_data['satisfaction'], on='visit_id', how='inner'
                )
                sat_dept = visit_sat.groupby('department_id')['overall_score'].mean().reset_index()
                sat_dept.columns = ['department_id', 'avg_satisfaction']
                dept_metrics = dept_metrics.merge(sat_dept, on='department_id', how='left')

            return dept_metrics.sort_values('total_registrations', ascending=False)
        return None

    def _render_dept_selector(self, dept_metrics):
        departments = ['全部'] + dept_metrics['department_name'].tolist()
        selected_dept = st.selectbox("选择科室查看详情", departments, key='dept_selector')

        if selected_dept != '全部':
            dept_data = dept_metrics[dept_metrics['department_name'] == selected_dept]
            if not dept_data.empty:
                st.info(f"""
                **{selected_dept}** - 关键指标:
                - 挂号量: {dept_data.iloc[0]['total_registrations']}
                - 医生数: {dept_data.iloc[0]['doctor_count']}
                - 平均候诊: {dept_data.iloc[0]['avg_wait_time']:.1f}分钟
                - 满意度: {dept_data.iloc[0]['avg_satisfaction']:.2f}分
                """)

    def _render_dept_bar_chart(self, dept_metrics):
        st.subheader("科室挂号量排行")
        metric_options = {
            '挂号量': 'total_registrations',
            '就诊量': 'total_visits',
            '平均候诊时间': 'avg_wait_time'
        }
        selected_metric = st.selectbox("选择指标", list(metric_options.keys()), key='dept_metric')

        fig = self.charts.create_department_bar_chart(
            dept_metrics,
            metric_options[selected_metric]
        )
        if fig:
            st.plotly_chart(fig, use_container_width=True)

    def _render_dept_satisfaction_chart(self, dept_metrics):
        st.subheader("科室满意度对比")
        fig = self.charts.create_department_bar_chart(
            dept_metrics,
            'avg_satisfaction'
        )
        if fig:
            st.plotly_chart(fig, use_container_width=True)

    def _render_mom_changes(self, dept_metrics, filtered_data):
        st.subheader("📊 同环比变化")

        reg_df = filtered_data.get('registrations')
        if reg_df is None or 'reg_date' not in reg_df.columns:
            st.warning("暂无环比数据")
            return

        reg_df_copy = reg_df.copy()
        reg_df_copy['reg_date_dt'] = pd.to_datetime(reg_df_copy['reg_date'])
        reg_df_copy['year_month'] = reg_df_copy['reg_date_dt'].dt.to_period('M')

        monthly_reg = reg_df_copy.groupby(['year_month', 'department_name'])['reg_id'].count().reset_index()
        monthly_reg.columns = ['year_month', 'department_name', 'registrations']

        monthly_reg = monthly_reg.sort_values(['department_name', 'year_month'])
        monthly_reg['环比变化率'] = monthly_reg.groupby('department_name')['registrations'].pct_change() * 100

        latest_month = monthly_reg['year_month'].max()
        mom_data = monthly_reg[monthly_reg['year_month'] == latest_month].copy()
        mom_data = mom_data.merge(
            dept_metrics[['department_name', 'avg_wait_time', 'avg_satisfaction']],
            on='department_name',
            how='left'
        )

        display_df = mom_data[['department_name', 'registrations', '环比变化率', 'avg_wait_time', 'avg_satisfaction']].copy()
        display_df.columns = ['科室名称', '挂号量', '挂号量环比(%)', '平均候诊时间', '平均满意度']

        def color_registration_rate(val):
            if pd.isna(val):
                return ''
            color = 'color: red' if val > 0 else 'color: green' if val < 0 else ''
            return color

        styled_df = display_df.style.map(color_registration_rate, subset=['挂号量环比(%)'])
        styled_df = styled_df.format({
            '挂号量环比(%)': '{:.1f}'.format,
            '平均候诊时间': '{:.1f}'.format,
            '平均满意度': '{:.2f}'.format
        })

        st.dataframe(styled_df, use_container_width=True, hide_index=True)

    def _render_dept_details(self, dept_metrics):
        st.subheader("科室详细数据")

        display_cols = [
            'department_name', 'total_registrations', 'total_visits',
            'doctor_count', 'visits_per_doctor', 'avg_wait_time', 'avg_satisfaction'
        ]
        available_cols = [c for c in display_cols if c in dept_metrics.columns]

        display_df = dept_metrics[available_cols].copy()
        display_df.columns = [
            '科室名称', '挂号量', '就诊量', '医生数',
            '人均接诊量', '平均候诊时间', '平均满意度'
        ]

        st.dataframe(display_df, use_container_width=True, hide_index=True)

    def _render_anomalous_departments(self):
        st.subheader("⚠️ 异常科室识别")
        anomalies = self.metrics.detect_anomalous_departments()

        if anomalies is not None and len(anomalies) > 0:
            anomaly_types = anomalies['anomaly_type'].unique()
            cols = st.columns(len(anomaly_types))

            for i, atype in enumerate(anomaly_types):
                with cols[i]:
                    st.markdown(f"**{atype}**")
                    dept_list = anomalies[anomalies['anomaly_type'] == atype]['department_name'].tolist()
                    for dept in dept_list:
                        st.warning(dept)
        else:
            st.success("✅ 未发现异常科室")

    def _render_capacity_analysis(self, filtered_data):
        st.subheader("📈 容量利用率分析")

        cap_data = self._get_capacity_data(filtered_data)
        if cap_data is None or len(cap_data) == 0:
            st.warning("⚠️ 暂无容量分析数据")
            return

        sort_option = st.selectbox(
            "排序方式",
            ["按利用率降序", "按利用率升序", "按容量降序", "按容量升序"],
            key='capacity_sort'
        )

        if sort_option == "按利用率降序":
            cap_data = cap_data.sort_values('utilization_rate', ascending=False)
        elif sort_option == "按利用率升序":
            cap_data = cap_data.sort_values('utilization_rate', ascending=True)
        elif sort_option == "按容量降序":
            cap_data = cap_data.sort_values('capacity', ascending=False)
        else:
            cap_data = cap_data.sort_values('capacity', ascending=True)

        col1, col2 = st.columns([1, 1])

        with col1:
            self._render_capacity_chart(cap_data)

        with col2:
            self._render_capacity_table(cap_data)

    @st.cache_data
    def _get_capacity_data(_self, filtered_data):
        if filtered_data.get('visits') is not None and filtered_data.get('doctors') is not None and filtered_data.get('departments') is not None:
            visit_df = filtered_data['visits']
            doctor_df = filtered_data['doctors']
            dept_df = filtered_data['departments']

            actual_visits = visit_df.groupby('department_id')['visit_id'].count().reset_index()
            actual_visits.columns = ['department_id', 'actual_visits']

            doctor_count = doctor_df.groupby('department_id').size().reset_index()
            doctor_count.columns = ['department_id', 'doctor_count']

            if 'visit_date' in visit_df.columns:
                visit_dates = pd.to_datetime(visit_df['visit_date'])
                if len(visit_dates) > 0:
                    workdays = np.busday_count(
                        visit_dates.min().strftime('%Y-%m-%d'),
                        (visit_dates.max() + pd.Timedelta(days=1)).strftime('%Y-%m-%d')
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

    def _render_capacity_chart(self, cap_data):
        sorted_data = cap_data.sort_values('utilization_rate', ascending=True)

        colors = []
        for rate in sorted_data['utilization_rate']:
            if rate > 80:
                colors.append('#d62728')
            elif rate < 50:
                colors.append('#2ca02c')
            else:
                colors.append('#1f77b4')

        fig = go.Figure()
        fig.add_trace(go.Bar(
            y=sorted_data['department_name'],
            x=sorted_data['utilization_rate'],
            orientation='h',
            marker_color=colors,
            text=sorted_data['utilization_rate'].apply(lambda x: f'{x}%'),
            textposition='outside',
            hovertemplate='<b>科室</b>: %{y}<br><b>容量利用率</b>: %{x}%<br><extra></extra>'
        ))

        fig.add_vline(x=80, line_dash='dash', line_color='#d62728', annotation_text='高负荷阈值 (80%)', annotation_position='top right')
        fig.add_vline(x=50, line_dash='dash', line_color='#2ca02c', annotation_text='低负荷阈值 (50%)', annotation_position='top left')

        fig.update_layout(
            title='科室容量利用率',
            xaxis_title='利用率 (%)',
            yaxis_title='科室',
            height=max(400, 300 + len(cap_data) * 25),
            xaxis=dict(range=[0, 100]),
            autosize=True
        )

        st.plotly_chart(fig, use_container_width=True)

    def _render_capacity_table(self, cap_data):
        st.markdown("**容量利用率详情**")

        display_df = cap_data.copy()
        display_df.columns = ['科室名称', '设计容量', '实际就诊量', '利用率(%)']

        def highlight_rows(row):
            if row['利用率(%)'] > 80:
                return ['background-color: #ffcccc'] * len(row)
            elif row['利用率(%)'] < 50:
                return ['background-color: #ccffcc'] * len(row)
            else:
                return [''] * len(row)

        styled_df = display_df.style.apply(highlight_rows, axis=1)
        st.dataframe(styled_df, use_container_width=True, hide_index=True)

        st.caption("🔴 利用率>80% 标红 | 🟢 利用率<50% 标绿")

    def _render_peak_hours_analysis(self, filtered_data):
        st.subheader("⏰ 高峰时段分析")

        reg_df = filtered_data.get('registrations')
        if reg_df is None or 'reg_time' not in reg_df.columns:
            st.warning("⚠️ 暂无高峰时段数据")
            return

        heatmap_data, peak_hours = self._get_peak_hours_data(filtered_data)

        if heatmap_data is not None and len(heatmap_data) > 0:
            self._render_peak_hours_heatmap(heatmap_data)

            st.divider()
            self._render_peak_hours_summary(peak_hours)

    @st.cache_data
    def _get_peak_hours_data(_self, filtered_data):
        reg_df = filtered_data.get('registrations')
        if reg_df is None or 'reg_time' not in reg_df.columns:
            return None, None

        reg_df_copy = reg_df.copy()
        reg_df_copy['hour'] = reg_df_copy['reg_time'].apply(
            lambda x: int(str(x).split(':')[0]) if pd.notna(x) else None
        )
        reg_df_copy = reg_df_copy.dropna(subset=['hour'])
        reg_df_copy['hour'] = reg_df_copy['hour'].astype(int)

        dept_hourly = reg_df_copy.groupby(['department_name', 'hour'])['reg_id'].count().reset_index()
        dept_hourly.columns = ['department_name', 'hour', 'registrations']

        all_hours = sorted(range(7, 19))
        all_depts = reg_df_copy['department_name'].unique()

        heatmap_data = pd.DataFrame(index=all_depts, columns=all_hours)
        for dept in all_depts:
            dept_data = dept_hourly[dept_hourly['department_name'] == dept]
            for hour in all_hours:
                hour_data = dept_data[dept_data['hour'] == hour]
                heatmap_data.loc[dept, hour] = hour_data['registrations'].values[0] if len(hour_data) > 0 else 0

        peak_hours = []
        for dept in all_depts:
            dept_regs = dept_hourly[dept_hourly['department_name'] == dept]
            if len(dept_regs) > 0:
                peak_hour = dept_regs.loc[dept_regs['registrations'].idxmax()]
                peak_hours.append({
                    'department_name': dept,
                    'peak_hour': int(peak_hour['hour']),
                    'peak_registrations': int(peak_hour['registrations'])
                })

        return heatmap_data, pd.DataFrame(peak_hours)

    def _render_peak_hours_heatmap(self, heatmap_data):
        fig = go.Figure(data=go.Heatmap(
            z=heatmap_data.values.astype(int),
            x=heatmap_data.columns,
            y=heatmap_data.index,
            colorscale='Reds',
            hovertemplate='<b>科室</b>: %{y}<br><b>时间</b>: %{x}:00<br><b>挂号量</b>: %{z} 人次<br><extra></extra>',
            colorbar=dict(title='挂号量')
        ))

        fig.update_layout(
            title='各科室高峰时段挂号量热力图',
            xaxis_title='时间 (时)',
            yaxis_title='科室',
            xaxis=dict(tickmode='array', tickvals=list(heatmap_data.columns)),
            height=max(400, 300 + len(heatmap_data) * 30),
            autosize=True
        )

        st.plotly_chart(fig, use_container_width=True)

    def _render_peak_hours_summary(self, peak_hours):
        if peak_hours is None or len(peak_hours) == 0:
            return

        st.markdown("**各科室最繁忙时段**")

        display_df = peak_hours.copy()
        display_df.columns = ['科室名称', '最繁忙时段', '该时段挂号量']
        display_df['最繁忙时段'] = display_df['最繁忙时段'].apply(lambda x: f"{x}:00 - {x+1}:00")

        st.dataframe(display_df, use_container_width=True, hide_index=True)

    def _render_anomaly_deep_analysis(self):
        st.subheader("🚩 异常科室深度分析")

        anomaly_analysis = self.metrics.get_anomaly_cause_analysis()

        if not anomaly_analysis:
            st.success("✅ 未发现异常科室，无需深度分析")
            return

        for idx, anomaly in enumerate(anomaly_analysis):
            with st.expander(f"{anomaly['科室名称']} - {anomaly['异常类型']}", expanded=False):
                col1, col2 = st.columns([1, 1])

                with col1:
                    st.markdown("**可能原因**")
                    for i, cause in enumerate(anomaly['可能原因']):
                        st.warning(f"{i+1}. {cause}")

                with col2:
                    st.markdown("**关键指标**")
                    for k, v in anomaly['关键指标'].items():
                        if k != 'department_name' and k != 'anomaly_type':
                            st.write(f"- {k}: {v}")

        st.divider()

        if st.button("🔧 一键生成改进建议", type="primary", key="generate_suggestions"):
            self._generate_improvement_suggestions(anomaly_analysis)

    def _generate_improvement_suggestions(self, anomaly_analysis):
        st.markdown("### 📋 改进建议报告")

        suggestions = []

        for anomaly in anomaly_analysis:
            dept_name = anomaly['科室名称']
            anomaly_type = anomaly['异常类型']

            if anomaly_type == '高候诊时间':
                suggestions.append({
                    '科室': dept_name,
                    '问题': '高候诊时间',
                    '建议': [
                        '增加医生排班，特别是高峰时段',
                        '优化预约系统，实施分时段就诊',
                        '增设分诊台，提前进行基础检查',
                        '考虑增加特需门诊分流高端患者'
                    ]
                })
            elif anomaly_type == '低满意度':
                suggestions.append({
                    '科室': dept_name,
                    '问题': '低满意度',
                    '建议': [
                        '加强医患沟通培训',
                        '优化就诊环境，增加便民设施',
                        '建立患者回访机制，及时处理投诉',
                        '开展服务之星评选，激励医护人员'
                    ]
                })
            elif anomaly_type == '高工作负荷':
                suggestions.append({
                    '科室': dept_name,
                    '问题': '高工作负荷',
                    '建议': [
                        '紧急招聘或调配增援医生',
                        '调整门诊时间，增加夜门诊',
                        '优化诊疗流程，提高工作效率',
                        '建立医生轮换休息机制，防止 burnout'
                    ]
                })

        for s in suggestions:
            with st.container():
                st.markdown(f"#### 🏥 {s['科室']} - {s['问题']}")
                for i, suggestion in enumerate(s['建议']):
                    st.info(f"💡 建议 {i+1}: {suggestion}")
                st.markdown("---")

        st.success("✅ 改进建议生成完成！")


import numpy as np
