import streamlit as st
import pandas as pd


class PatientExperiencePage:
    def __init__(self, data, metrics_calculator, chart_generator):
        self.data = data
        self.metrics = metrics_calculator
        self.charts = chart_generator

    def render(self):
        if not st.session_state.get('data_loaded', False):
            st.warning("请先导入数据")
            return

        st.header("😊 患者体验分析")

        sat_dist = self.metrics.get_satisfaction_distribution()

        self._render_satisfaction_overview(sat_dist)

        st.divider()

        col1, col2 = st.columns(2)
        with col1:
            self._render_satisfaction_radar(sat_dist)
        with col2:
            self._render_satisfaction_distribution(sat_dist)

        st.divider()
        self._render_waiting_time_analysis()

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

    def _render_waiting_time_analysis(self):
        st.subheader("⏱️ 候诊时间分析")

        if self.data.get('waiting_times') is not None:
            wt_df = self.data['waiting_times']

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

            self._render_wait_time_by_period(wt_df)

    def _render_wait_time_by_period(self, wt_df):
        st.subheader("不同时段候诊对比")

        if self.data.get('registrations') is not None:
            reg_df = self.data['registrations'].copy()
            reg_df['hour'] = reg_df['reg_time'].str[:2].astype(int)

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

            reg_df['period'] = reg_df['hour'].apply(get_period)

            merged = reg_df.merge(wt_df, on='reg_id', how='inner')
            period_stats = merged.groupby('period')['wait_minutes'].agg(['mean', 'count']).reset_index()
            period_stats.columns = ['时段', '平均候诊时间', '人数']

            import plotly.express as px
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
