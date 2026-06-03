import plotly.express as px
import plotly.graph_objects as go
from plotly.subplots import make_subplots
import pandas as pd
from typing import Dict, Optional, Any


class ChartGenerator:
    COLORS = {
        'primary': '#1f77b4',
        'secondary': '#ff7f0e',
        'success': '#2ca02c',
        'danger': '#d62728',
        'warning': '#ffc107',
        'info': '#17a2b8'
    }

    @staticmethod
    def create_daily_trend_chart(daily_data: pd.DataFrame) -> Optional[go.Figure]:
        if daily_data is None or len(daily_data) == 0:
            return None

        fig = go.Figure()

        fig.add_trace(go.Scatter(
            x=daily_data['date'],
            y=daily_data['registrations'],
            mode='lines+markers',
            name='挂号量',
            line=dict(color=ChartGenerator.COLORS['primary'], width=2),
            marker=dict(size=6)
        ))

        fig.add_trace(go.Scatter(
            x=daily_data['date'],
            y=daily_data['unique_patients'],
            mode='lines+markers',
            name='患者数',
            line=dict(color=ChartGenerator.COLORS['secondary'], width=2),
            marker=dict(size=6)
        ))

        fig.update_layout(
            title='每日门诊量趋势',
            xaxis_title='日期',
            yaxis_title='人数',
            hovermode='x unified',
            legend=dict(orientation='h', yanchor='bottom', y=1.02, xanchor='right', x=1),
            height=400
        )

        return fig

    @staticmethod
    def create_department_bar_chart(dept_data: pd.DataFrame, metric: str = 'total_registrations') -> Optional[go.Figure]:
        if dept_data is None or len(dept_data) == 0:
            return None

        metric_labels = {
            'total_registrations': '挂号量',
            'total_visits': '就诊量',
            'avg_wait_time': '平均候诊时间(分钟)',
            'avg_satisfaction': '平均满意度'
        }

        fig = px.bar(
            dept_data.sort_values(metric, ascending=True),
            y='department_name',
            x=metric,
            orientation='h',
            color=metric,
            color_continuous_scale='Blues',
            title=f'各科室{metric_labels.get(metric, metric)}对比'
        )

        fig.update_layout(
            xaxis_title=metric_labels.get(metric, metric),
            yaxis_title='科室',
            height=400 + len(dept_data) * 20,
            showlegend=False
        )

        return fig

    @staticmethod
    def create_doctor_ranking_chart(doc_data: pd.DataFrame, top_n: int = 10) -> Optional[go.Figure]:
        if doc_data is None or len(doc_data) == 0:
            return None

        top_docs = doc_data.head(top_n).sort_values('total_visits', ascending=True)

        fig = go.Figure()

        fig.add_trace(go.Bar(
            y=top_docs['doctor_name'] + ' (' + top_docs['department_name'] + ')',
            x=top_docs['total_visits'],
            orientation='h',
            marker_color=ChartGenerator.COLORS['primary'],
            name='就诊量'
        ))

        fig.update_layout(
            title=f'医生接诊量排名 (Top {top_n})',
            xaxis_title='接诊量',
            yaxis_title='医生',
            height=400,
            showlegend=False
        )

        return fig

    @staticmethod
    def create_fee_pie_chart(fee_data: Dict[str, Any]) -> Optional[go.Figure]:
        if not fee_data or 'exam_revenue' not in fee_data:
            return None

        labels = ['检查费用', '药品费用']
        values = [fee_data.get('exam_revenue', 0), fee_data.get('drug_revenue', 0)]

        fig = go.Figure(data=[go.Pie(
            labels=labels,
            values=values,
            hole=0.4,
            marker_colors=[ChartGenerator.COLORS['primary'], ChartGenerator.COLORS['secondary']],
            textinfo='label+percent',
            textposition='outside'
        )])

        fig.update_layout(
            title='费用结构分布',
            height=400,
            annotations=[dict(text='总收入', x=0.5, y=0.5, font_size=20, showarrow=False)]
        )

        return fig

    @staticmethod
    def create_waiting_time_histogram(wt_data: pd.DataFrame) -> Optional[go.Figure]:
        if wt_data is None or len(wt_data) == 0 or 'wait_minutes' not in wt_data.columns:
            return None

        fig = px.histogram(
            wt_data,
            x='wait_minutes',
            nbins=20,
            color_discrete_sequence=[ChartGenerator.COLORS['primary']],
            title='候诊时间分布'
        )

        fig.update_layout(
            xaxis_title='候诊时间 (分钟)',
            yaxis_title='患者数量',
            height=400,
            bargap=0.1
        )

        fig.add_vline(
            x=wt_data['wait_minutes'].mean(),
            line_dash='dash',
            line_color=ChartGenerator.COLORS['danger'],
            annotation_text=f'平均值: {wt_data["wait_minutes"].mean():.1f}分钟'
        )

        return fig

    @staticmethod
    def create_satisfaction_radar_chart(sat_data: Dict[str, Any]) -> Optional[go.Figure]:
        if not sat_data or 'avg_scores' not in sat_data:
            return None

        categories = {
            'overall_score': '整体满意度',
            'wait_score': '候诊满意度',
            'service_score': '服务满意度'
        }

        scores = sat_data['avg_scores']
        theta = [categories.get(k, k) for k in scores.keys()]
        r = list(scores.values())

        fig = go.Figure(data=go.Scatterpolar(
            r=r + [r[0]],
            theta=theta + [theta[0]],
            fill='toself',
            fillcolor=ChartGenerator.COLORS['primary'],
            line_color=ChartGenerator.COLORS['primary'],
            opacity=0.6
        ))

        fig.update_layout(
            polar=dict(
                radialaxis=dict(
                    visible=True,
                    range=[0, 5]
                )
            ),
            title='满意度雷达图',
            height=400,
            showlegend=False
        )

        return fig

    @staticmethod
    def create_conversion_rate_chart(conv_data: Dict[str, Any]) -> Optional[go.Figure]:
        if not conv_data or 'by_department' not in conv_data:
            return None

        dept_conv = conv_data['by_department']

        fig = go.Figure()

        fig.add_trace(go.Bar(
            x=dept_conv['department_name'],
            y=dept_conv['conversion_rate'],
            marker_color=ChartGenerator.COLORS['success'],
            text=dept_conv['conversion_rate'].apply(lambda x: f'{x}%'),
            textposition='auto'
        ))

        fig.update_layout(
            title='各科室检查转化率',
            xaxis_title='科室',
            yaxis_title='转化率 (%)',
            height=400,
            yaxis=dict(range=[0, 100])
        )

        return fig

    @staticmethod
    def create_monthly_comparison_chart(monthly_data: pd.DataFrame) -> Optional[go.Figure]:
        if monthly_data is None or len(monthly_data) == 0:
            return None

        fig = make_subplots(specs=[[{"secondary_y": True}]])

        fig.add_trace(
            go.Bar(
                x=monthly_data['month_label'],
                y=monthly_data['registrations'],
                name='挂号量',
                marker_color=ChartGenerator.COLORS['primary']
            ),
            secondary_y=False
        )

        fig.add_trace(
            go.Scatter(
                x=monthly_data['month_label'],
                y=monthly_data['unique_patients'],
                name='患者数',
                mode='lines+markers',
                line=dict(color=ChartGenerator.COLORS['secondary'], width=3)
            ),
            secondary_y=True
        )

        fig.update_layout(
            title='月度运营对比',
            height=400,
            legend=dict(orientation='h', yanchor='bottom', y=1.02, xanchor='right', x=1)
        )

        fig.update_yaxes(title_text='挂号量', secondary_y=False)
        fig.update_yaxes(title_text='患者数', secondary_y=True)

        return fig

    @staticmethod
    def create_exam_item_chart(exam_data: pd.DataFrame, top_n: int = 10) -> Optional[go.Figure]:
        if exam_data is None or len(exam_data) == 0:
            return None

        top_exams = exam_data.head(top_n)

        fig = px.bar(
            top_exams,
            x='exam_item',
            y='total_fee',
            color='count',
            color_continuous_scale='Viridis',
            title=f'检查项目收入排行 (Top {top_n})',
            text='total_fee'
        )

        fig.update_layout(
            xaxis_title='检查项目',
            yaxis_title='收入 (元)',
            height=400
        )

        return fig

    @staticmethod
    def create_satisfaction_distribution_chart(sat_dist: Dict[int, int]) -> Optional[go.Figure]:
        if not sat_dist:
            return None

        scores = list(sat_dist.keys())
        counts = list(sat_dist.values())

        colors = ['#dc3545', '#fd7e14', '#ffc107', '#28a745', '#20c997']

        fig = go.Figure(data=[go.Bar(
            x=scores,
            y=counts,
            marker_color=[colors[s-1] for s in scores],
            text=counts,
            textposition='auto'
        )])

        fig.update_layout(
            title='整体满意度分布',
            xaxis_title='评分 (1-5分)',
            yaxis_title='人数',
            height=350,
            xaxis=dict(tickmode='array', tickvals=[1, 2, 3, 4, 5])
        )

        return fig

    @staticmethod
    def create_kpi_card(title: str, value: str, subtitle: str = '') -> go.Figure:
        fig = go.Figure()

        fig.add_annotation(
            text=value,
            x=0.5, y=0.6,
            font=dict(size=36, color=ChartGenerator.COLORS['primary']),
            showarrow=False
        )

        fig.add_annotation(
            text=title,
            x=0.5, y=0.2,
            font=dict(size=14, color='#666'),
            showarrow=False
        )

        if subtitle:
            fig.add_annotation(
                text=subtitle,
                x=0.5, y=0.05,
                font=dict(size=12, color='#999'),
                showarrow=False
            )

        fig.update_layout(
            height=150,
            plot_bgcolor='white',
            paper_bgcolor='#f8f9fa',
            xaxis=dict(showticklabels=False, showgrid=False),
            yaxis=dict(showticklabels=False, showgrid=False),
            margin=dict(l=10, r=10, t=10, b=10)
        )

        return fig
