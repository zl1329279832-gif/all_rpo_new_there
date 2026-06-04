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

            st.divider()
            self._render_advanced_analysis()

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
        if daily_data is not None and len(daily_data) > 0:
            fig = self.charts.create_daily_trend_chart(daily_data)
            if fig:
                st.plotly_chart(fig, use_container_width=True)
        else:
            st.info("暂无数据")

    def _render_monthly_trend(self):
        st.subheader("月度运营对比")
        monthly_data = self.metrics.get_monthly_trends()
        if monthly_data is not None and len(monthly_data) > 0:
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
        else:
            st.info("暂无患者类型数据")

    def _render_advanced_analysis(self):
        st.subheader("🔬 深度分析")

        tab1, tab2, tab3, tab4 = st.tabs([
            "📈 趋势分析",
            "⏰ 时间分析",
            "🏥 资源分析",
            "🔍 异常分析"
        ])

        with tab1:
            self._render_mom_changes()

        with tab2:
            col1, col2 = st.columns(2)
            with col1:
                self._render_weekday_weekend()
            with col2:
                self._render_peak_hours()

        with tab3:
            col1, col2 = st.columns(2)
            with col1:
                self._render_capacity_utilization()
            with col2:
                self._render_doctor_workload_balance()
            st.divider()
            self._render_wait_time_stratification()

        with tab4:
            self._render_anomaly_cause_analysis()

    def _render_mom_changes(self):
        st.markdown("### 📈 月环比变化")
        mom_data = self.metrics.get_mom_changes()

        if not mom_data:
            st.info("暂无月环比数据")
            return

        metrics_order = ['挂号量环比', '就诊量环比', '收入环比', '候诊时间环比', '满意度环比']
        metric_labels = {
            '挂号量环比': '挂号量',
            '就诊量环比': '就诊量',
            '收入环比': '收入',
            '候诊时间环比': '候诊时间',
            '满意度环比': '满意度'
        }

        cols = st.columns(5)
        for idx, metric_key in enumerate(metrics_order):
            if metric_key in mom_data and len(mom_data[metric_key]) >= 2:
                latest = mom_data[metric_key][-1]
                mom_rate = latest.get('环比变化率')

                with cols[idx]:
                    label = metric_labels.get(metric_key, metric_key)
                    if mom_rate is not None:
                        delta_color = "normal"
                        if label in ['挂号量', '就诊量', '收入', '满意度']:
                            delta_color = "normal" if mom_rate >= 0 else "inverse"
                        else:
                            delta_color = "inverse" if mom_rate >= 0 else "normal"

                        st.metric(
                            label,
                            f"{mom_rate:+.1f}%",
                            delta=None,
                            delta_color=delta_color
                        )
                    else:
                        st.metric(label, "无数据")
            else:
                with cols[idx]:
                    st.metric(metric_labels.get(metric_key, metric_key), "无数据")

        if '挂号量环比' in mom_data and len(mom_data['挂号量环比']) > 1:
            mom_df = pd.DataFrame([
                {
                    'month_label': item['月份'],
                    'mom_rate': item['环比变化率'] if item['环比变化率'] is not None else 0
                }
                for item in mom_data['挂号量环比'][1:]
            ])
            fig = self.charts.create_mom_chart(mom_df)
            if fig:
                st.plotly_chart(fig, use_container_width=True)

    def _render_weekday_weekend(self):
        st.markdown("### 📅 工作日vs周末")
        ww_data = self.metrics.get_weekday_weekend_comparison()

        if not ww_data:
            st.info("暂无工作日周末对比数据")
            return

        display_data = []
        for key, value in ww_data.items():
            if isinstance(value, dict):
                row = {'指标': key}
                row.update(value)
                display_data.append(row)

        if display_data:
            ww_df = pd.DataFrame(display_data)
            st.dataframe(ww_df, use_container_width=True, hide_index=True)

            chart_data = pd.DataFrame([
                {'day_type': '工作日', 'avg_registrations': ww_data.get('挂号量', {}).get('工作日日均', 0)},
                {'day_type': '周末', 'avg_registrations': ww_data.get('挂号量', {}).get('周末日均', 0)}
            ])
            fig = self.charts.create_weekday_weekend_chart(chart_data)
            if fig:
                st.plotly_chart(fig, use_container_width=True)

    def _render_peak_hours(self):
        st.markdown("### 🚦 就诊高峰时段")
        hours_data = self.metrics.get_peak_hours()

        if hours_data is None or len(hours_data) == 0:
            st.info("暂无高峰时段数据")
            return

        fig = self.charts.create_peak_hours_chart(hours_data)
        if fig:
            st.plotly_chart(fig, use_container_width=True)

        peak_hours = hours_data[hours_data['is_peak'] == True]['hour'].tolist()
        if peak_hours:
            st.success(f"**高峰时段**：{', '.join([f'{h}:00' for h in sorted(peak_hours)])}")

    def _render_capacity_utilization(self):
        st.markdown("### 🏥 科室容量利用率")
        cap_data = self.metrics.get_department_capacity_utilization()

        if cap_data is None or len(cap_data) == 0:
            st.info("暂无科室容量数据")
            return

        fig = self.charts.create_capacity_utilization_chart(cap_data)
        if fig:
            st.plotly_chart(fig, use_container_width=True)

        high_util = cap_data[cap_data['utilization_rate'] >= 80]
        if len(high_util) > 0:
            st.warning(f"⚠️ **高利用率预警**：{len(high_util)} 个科室利用率超过 80%")

    def _render_doctor_workload_balance(self):
        st.markdown("### 👨‍⚕️ 医生负荷均衡度")
        wl_data = self.metrics.get_doctor_workload_balance()

        if not wl_data:
            st.info("暂无医生负荷数据")
            return

        cols = st.columns(4)
        metrics = [
            ("基尼系数", wl_data.get('基尼系数', 0), ""),
            ("变异系数", wl_data.get('变异系数', 0), ""),
            ("最高最低比", wl_data.get('最高负荷最低负荷比', 0), ""),
            ("超负荷占比", wl_data.get('超负荷医生占比', 0), "%")
        ]

        for i, (label, value, unit) in enumerate(metrics):
            with cols[i]:
                if unit:
                    st.metric(label, f"{value}{unit}")
                else:
                    st.metric(label, f"{value}")

        overload_count = wl_data.get('超负荷医生人数', 0)
        if overload_count > 0:
            st.warning(f"⚠️ 共有 {overload_count} 位医生工作负荷超过平均值的 120%")

    def _render_wait_time_stratification(self):
        st.markdown("### ⏱️ 候诊时间分层")
        wts_data = self.metrics.get_wait_time_stratification()

        if not wts_data:
            st.info("暂无候诊时间数据")
            return

        col1, col2 = st.columns([1, 1])

        with col1:
            wts_df = pd.DataFrame([
                {'时间区间': k, '人数': v.get('人数', 0), '占比(%)': v.get('占比', 0)}
                for k, v in wts_data.items()
            ])
            st.dataframe(wts_df, use_container_width=True, hide_index=True)

        with col2:
            pie_data = {k: v.get('人数', 0) for k, v in wts_data.items()}
            fig = self.charts.create_wait_time_stratification_chart(pie_data)
            if fig:
                st.plotly_chart(fig, use_container_width=True)

        over_30 = wts_data.get('30至60分钟', {}).get('占比', 0) + wts_data.get('大于60分钟', {}).get('占比', 0)
        if over_30 > 30:
            st.warning(f"⚠️ 候诊时间超过 30 分钟的患者占比达 {over_30:.1f}%，需关注候诊效率")

    def _render_anomaly_cause_analysis(self):
        st.markdown("### 🔍 异常指标原因分析")
        anomaly_data = self.metrics.get_anomaly_cause_analysis()

        if not anomaly_data:
            st.success("✅ 未发现明显异常指标")
            return

        for idx, anomaly in enumerate(anomaly_data):
            with st.expander(f"🚨 {anomaly.get('科室名称', '未知科室')} - {anomaly.get('异常类型', '未知异常')}", expanded=(idx == 0)):
                st.markdown("**可能原因：**")
                for cause in anomaly.get('可能原因', []):
                    st.markdown(f"- {cause}")

                st.markdown("**关键指标：**")
                key_metrics = anomaly.get('关键指标', {})
                if key_metrics:
                    metrics_df = pd.DataFrame([
                        {'指标': k, '数值': v}
                        for k, v in key_metrics.items()
                        if k != 'anomaly_type'
                    ])
                    st.dataframe(metrics_df, use_container_width=True, hide_index=True)
