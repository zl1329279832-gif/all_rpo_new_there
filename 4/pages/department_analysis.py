import streamlit as st
import pandas as pd
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
            dept_metrics = self.metrics.get_department_metrics()
            if dept_metrics is None or len(dept_metrics) == 0:
                st.warning("⚠️ 暂无科室分析数据，请检查是否已上传科室信息、挂号记录等必要数据")
                return

            self._render_dept_selector(dept_metrics)

            col1, col2 = st.columns(2)
            with col1:
                self._render_dept_bar_chart(dept_metrics)
            with col2:
                self._render_dept_satisfaction_chart(dept_metrics)

            st.divider()
            self._render_dept_details(dept_metrics)

            st.divider()
            self._render_anomalous_departments()

        except Exception as e:
            error_msg = ErrorHandler.translate_error(e, "科室分析")
            ErrorHandler.display_error(f"❌ 科室分析失败：{error_msg}")

    def _render_dept_selector(self, dept_metrics):
        departments = ['全部'] + dept_metrics['department_name'].tolist()
        selected_dept = st.selectbox("选择科室", departments, key='dept_selector')

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
