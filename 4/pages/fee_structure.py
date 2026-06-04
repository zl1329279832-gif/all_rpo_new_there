import streamlit as st
import pandas as pd
from utils.error_handler import ErrorHandler


class FeeStructurePage:
    def __init__(self, metrics_calculator, chart_generator):
        self.metrics = metrics_calculator
        self.charts = chart_generator

    def render(self):
        if not st.session_state.get('data_loaded', False):
            st.warning("⚠️ 请先导入数据")
            return

        st.header("💰 费用结构分析")

        try:
            fee_structure = self.metrics.get_fee_structure()
            conv_data = self.metrics.get_exam_conversion_rate()

            if not fee_structure and not conv_data:
                st.warning("⚠️ 暂无费用分析数据，请检查是否已上传就诊记录、检查项目、药品费用等必要数据")
                return

            self._render_fee_overview(fee_structure)

            st.divider()

            col1, col2 = st.columns(2)
            with col1:
                self._render_fee_pie_chart(fee_structure)
            with col2:
                self._render_conversion_rate(conv_data)

            st.divider()

            col3, col4 = st.columns(2)
            with col3:
                self._render_exam_items(fee_structure)
            with col4:
                self._render_drug_categories(fee_structure)

        except Exception as e:
            error_msg = ErrorHandler.translate_error(e, "费用结构分析")
            ErrorHandler.display_error(f"❌ 费用结构分析失败：{error_msg}")

    def _render_fee_overview(self, fee_structure):
        st.subheader("费用概览")

        cols = st.columns(4)
        with cols[0]:
            st.metric("总收入", f"¥{fee_structure.get('total_revenue', 0):,.0f}")
        with cols[1]:
            st.metric("检查收入", f"¥{fee_structure.get('exam_revenue', 0):,.0f}",
                      f"{fee_structure.get('exam_ratio', 0)}%")
        with cols[2]:
            st.metric("药品收入", f"¥{fee_structure.get('drug_revenue', 0):,.0f}",
                      f"{fee_structure.get('drug_ratio', 0)}%")
        with cols[3]:
            avg_fee = fee_structure.get('total_revenue', 0) / 1200 if 1200 > 0 else 0
            st.metric("次均费用", f"¥{avg_fee:,.0f}")

    def _render_fee_pie_chart(self, fee_structure):
        st.subheader("收入结构")
        fig = self.charts.create_fee_pie_chart(fee_structure)
        if fig:
            st.plotly_chart(fig, use_container_width=True)

    def _render_conversion_rate(self, conv_data):
        st.subheader("检查转化率")

        if conv_data:
            col1, col2, col3 = st.columns(3)
            with col1:
                st.metric("总就诊量", conv_data.get('total_visits', 0))
            with col2:
                st.metric("检查人次", conv_data.get('visits_with_exam', 0))
            with col3:
                st.metric("转化率", f"{conv_data.get('conversion_rate', 0)}%")

            fig = self.charts.create_conversion_rate_chart(conv_data)
            if fig:
                st.plotly_chart(fig, use_container_width=True)

    def _render_exam_items(self, fee_structure):
        st.subheader("检查项目排行")

        if 'exam_by_item' in fee_structure:
            exam_data = fee_structure['exam_by_item']

            top_n = st.slider("显示项目数", 5, 10, 5, key='exam_top_n')
            fig = self.charts.create_exam_item_chart(exam_data, top_n)
            if fig:
                st.plotly_chart(fig, use_container_width=True)

            display_df = exam_data.copy()
            display_df.columns = ['检查项目', '收入', '次数']
            st.dataframe(display_df, use_container_width=True, hide_index=True)

    def _render_drug_categories(self, fee_structure):
        st.subheader("药品分类收入")

        if 'drug_by_category' in fee_structure:
            drug_data = fee_structure['drug_by_category']

            import plotly.express as px
            fig = px.bar(
                drug_data,
                x='drug_category',
                y='drug_fee',
                color='drug_fee',
                color_continuous_scale='Reds',
                title='药品分类收入'
            )
            fig.update_layout(height=300, xaxis_title='药品分类', yaxis_title='收入', showlegend=False)
            st.plotly_chart(fig, use_container_width=True)

            display_df = drug_data.copy()
            display_df.columns = ['药品分类', '收入']
            st.dataframe(display_df, use_container_width=True, hide_index=True)
