import streamlit as st
import pandas as pd
from utils.error_handler import ErrorHandler


class OverviewPage:
    def __init__(self, metrics_calculator, chart_generator):
        self.metrics = metrics_calculator
        self.charts = chart_generator

    def render(self):
        if not st.session_state.get('data_loaded', False):
            st.warning("⚠️ 请先导入数据")
            return

        st.header("📊 运营总览")

        try:
            overview_metrics = self.metrics.get_overview_metrics()

            if not overview_metrics:
                st.warning("⚠️ 无法获取运营数据，请检查是否已上传挂号记录、就诊记录等必要数据")
                return

            self._render_kpi_cards(overview_metrics)

            st.divider()

            col1, col2 = st.columns(2)
            with col1:
                self._render_daily_trend()
            with col2:
                self._render_monthly_trend()

            st.divider()
            self._render_patient_type_distribution(overview_metrics)

        except Exception as e:
            error_msg = ErrorHandler.translate_error(e, "运营总览分析")
            ErrorHandler.display_error(f"❌ 运营总览分析失败：{error_msg}")

    def _render_kpi_cards(self, overview_metrics):
        st.subheader("关键指标")
        cols = st.columns(4)

        kpis = [
            ("总挂号量", overview_metrics.get('total_registrations', 0), ""),
            ("就诊患者", overview_metrics.get('unique_patients', 0), "人"),
            ("总收入", overview_metrics.get('total_revenue', 0), "元"),
            ("平均候诊", overview_metrics.get('avg_wait_time', 0), "分钟"),
        ]

        for i, (label, value, unit) in enumerate(kpis):
            with cols[i]:
                if unit == "元":
                    st.metric(label, f"¥{value:,.0f}")
                elif unit:
                    st.metric(label, f"{value} {unit}")
                else:
                    st.metric(label, f"{value:,}")

        cols2 = st.columns(4)
        more_kpis = [
            ("日均挂号", overview_metrics.get('avg_daily_registrations', 0), ""),
            ("次均费用", overview_metrics.get('avg_visit_fee', 0), "元"),
            ("满意度", overview_metrics.get('avg_overall_score', 0), "分"),
            ("科室数量", overview_metrics.get('total_departments', 0), "个"),
        ]

        for i, (label, value, unit) in enumerate(more_kpis):
            with cols2[i]:
                if unit == "元":
                    st.metric(label, f"¥{value:,.0f}")
                elif unit:
                    st.metric(label, f"{value} {unit}")
                else:
                    st.metric(label, f"{value}")

    def _render_daily_trend(self):
        st.subheader("每日门诊量趋势")
        daily_data = self.metrics.get_daily_trends()
        if daily_data is not None:
            fig = self.charts.create_daily_trend_chart(daily_data)
            if fig:
                st.plotly_chart(fig, use_container_width=True)
        else:
            st.info("暂无数据")

    def _render_monthly_trend(self):
        st.subheader("月度运营对比")
        monthly_data = self.metrics.get_monthly_trends()
        if monthly_data is not None:
            fig = self.charts.create_monthly_comparison_chart(monthly_data)
            if fig:
                st.plotly_chart(fig, use_container_width=True)
        else:
            st.info("暂无数据")

    def _render_patient_type_distribution(self, overview_metrics):
        st.subheader("患者类型分布")

        pt_dist = overview_metrics.get('patient_type_distribution', {})
        if pt_dist:
            col1, col2 = st.columns([1, 1])
            with col1:
                pt_df = pd.DataFrame({
                    '患者类型': list(pt_dist.keys()),
                    '数量': list(pt_dist.values())
                })
                st.dataframe(pt_df, use_container_width=True, hide_index=True)

            with col2:
                import plotly.graph_objects as go
                fig = go.Figure(data=[go.Pie(
                    labels=list(pt_dist.keys()),
                    values=list(pt_dist.values()),
                    hole=0.4,
                    textinfo='label+percent'
                )])
                fig.update_layout(height=300, showlegend=False)
                st.plotly_chart(fig, use_container_width=True)
