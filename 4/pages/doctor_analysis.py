import streamlit as st
import pandas as pd
from utils.error_handler import ErrorHandler


class DoctorAnalysisPage:
    def __init__(self, metrics_calculator, chart_generator):
        self.metrics = metrics_calculator
        self.charts = chart_generator

    def render(self):
        if not st.session_state.get('data_loaded', False):
            st.warning("⚠️ 请先导入数据")
            return

        st.header("👨‍⚕️ 医生分析")

        try:
            doc_metrics = self.metrics.get_doctor_metrics()
            if doc_metrics is None or len(doc_metrics) == 0:
                st.warning("⚠️ 暂无医生分析数据，请检查是否已上传医生信息、就诊记录等必要数据")
                return

            col1, col2 = st.columns([1, 2])
            with col1:
                self._render_doctor_selector(doc_metrics)

            with col2:
                self._render_doctor_ranking(doc_metrics)

            st.divider()
            self._render_doctor_details(doc_metrics)

            st.divider()
            self._render_workload_analysis(doc_metrics)

        except Exception as e:
            error_msg = ErrorHandler.translate_error(e, "医生分析")
            ErrorHandler.display_error(f"❌ 医生分析失败：{error_msg}")

    def _render_doctor_selector(self, doc_metrics):
        st.subheader("医生筛选")

        departments = ['全部'] + sorted(doc_metrics['department_name'].unique().tolist())
        selected_dept = st.selectbox("选择科室", departments, key='doc_dept_filter')

        if selected_dept != '全部':
            filtered_docs = doc_metrics[doc_metrics['department_name'] == selected_dept]
        else:
            filtered_docs = doc_metrics

        doctors = ['全部'] + filtered_docs['doctor_name'].tolist()
        selected_doc = st.selectbox("选择医生", doctors, key='doc_name_filter')

        if selected_doc != '全部':
            doc_data = filtered_docs[filtered_docs['doctor_name'] == selected_doc]
            if not doc_data.empty:
                doc = doc_data.iloc[0]
                st.info(f"""
                **{doc['doctor_name']}** ({doc.get('title', '未知')})

                - 科室: {doc['department_name']}
                - 接诊量: {doc['total_visits']}
                - 收入贡献: ¥{doc['total_revenue']:,.0f}
                - 满意度: {doc.get('avg_satisfaction', 0):.2f}分
                """)

    def _render_doctor_ranking(self, doc_metrics):
        st.subheader("医生接诊量排行")

        top_n = st.slider("显示医生数量", 5, 20, 10, key='doc_top_n')
        fig = self.charts.create_doctor_ranking_chart(doc_metrics, top_n)
        if fig:
            st.plotly_chart(fig, use_container_width=True)

    def _render_doctor_details(self, doc_metrics):
        st.subheader("医生详细数据")

        sort_by = st.selectbox(
            "排序方式",
            ['接诊量', '收入贡献', '满意度'],
            key='doc_sort'
        )

        sort_map = {
            '接诊量': 'total_visits',
            '收入贡献': 'total_revenue',
            '满意度': 'avg_satisfaction'
        }

        display_df = doc_metrics.sort_values(sort_map[sort_by], ascending=False).copy()

        display_cols = ['doctor_name', 'department_name', 'title', 'total_visits', 'total_revenue', 'avg_satisfaction']
        available_cols = [c for c in display_cols if c in display_df.columns]

        display_df = display_df[available_cols]
        display_df.columns = ['医生姓名', '科室', '职称', '接诊量', '收入贡献', '满意度']

        st.dataframe(display_df, use_container_width=True, hide_index=True)

    def _render_workload_analysis(self, doc_metrics):
        st.subheader("工作负荷分析")

        col1, col2, col3 = st.columns(3)

        with col1:
            avg_visits = doc_metrics['total_visits'].mean()
            st.metric("人均接诊量", f"{avg_visits:.1f}")

        with col2:
            max_visits = doc_metrics['total_visits'].max()
            st.metric("最高接诊量", f"{max_visits}")

        with col3:
            high_workload = len(doc_metrics[doc_metrics['total_visits'] > avg_visits * 1.2])
            st.metric("高负荷医生数", f"{high_workload}人")

        st.caption("*高负荷定义: 接诊量超过平均值的120%")
