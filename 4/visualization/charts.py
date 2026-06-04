import plotly.express as px
import plotly.graph_objects as go
from plotly.subplots import make_subplots
import pandas as pd
from typing import Dict, Optional, Any, List


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
    def _create_empty_figure(title: str = '') -> go.Figure:
        fig = go.Figure()
        fig.add_annotation(
            text='暂无数据',
            x=0.5,
            y=0.5,
            font=dict(size=24, color='#999'),
            showarrow=False,
            xref='paper',
            yref='paper'
        )
        fig.update_layout(
            title=title,
            height=400,
            xaxis=dict(showticklabels=False, showgrid=False, zeroline=False),
            yaxis=dict(showticklabels=False, showgrid=False, zeroline=False),
            plot_bgcolor='white'
        )
        return fig

    @staticmethod
    def create_daily_trend_chart(daily_data: pd.DataFrame) -> go.Figure:
        if daily_data is None or len(daily_data) == 0:
            return ChartGenerator._create_empty_figure('每日门诊量趋势')

        fig = go.Figure()

        fig.add_trace(go.Scatter(
            x=daily_data['date'],
            y=daily_data['registrations'],
            mode='lines+markers',
            name='挂号量',
            line=dict(color=ChartGenerator.COLORS['primary'], width=2),
            marker=dict(size=6),
            hovertemplate='<b>日期</b>: %{x}<br><b>挂号量</b>: %{y} 人次<br><extra></extra>'
        ))

        fig.add_trace(go.Scatter(
            x=daily_data['date'],
            y=daily_data['unique_patients'],
            mode='lines+markers',
            name='患者数',
            line=dict(color=ChartGenerator.COLORS['secondary'], width=2),
            marker=dict(size=6),
            hovertemplate='<b>日期</b>: %{x}<br><b>患者数</b>: %{y} 人<br><extra></extra>'
        ))

        fig.update_layout(
            title='每日门诊量趋势',
            xaxis_title='日期',
            yaxis_title='人数',
            hovermode='x unified',
            legend=dict(orientation='h', yanchor='bottom', y=1.02, xanchor='right', x=1, title='图例'),
            height=400,
            autosize=True
        )

        return fig

    @staticmethod
    def create_department_bar_chart(dept_data: pd.DataFrame, metric: str = 'total_registrations') -> go.Figure:
        if dept_data is None or len(dept_data) == 0:
            return ChartGenerator._create_empty_figure('各科室指标对比')

        metric_labels = {
            'total_registrations': '挂号量',
            'total_visits': '就诊量',
            'avg_wait_time': '平均候诊时间',
            'avg_satisfaction': '平均满意度'
        }

        metric_units = {
            'total_registrations': '人次',
            'total_visits': '人次',
            'avg_wait_time': '分钟',
            'avg_satisfaction': '分'
        }

        sorted_data = dept_data.sort_values(metric, ascending=True)
        label = metric_labels.get(metric, metric)
        unit = metric_units.get(metric, '')

        fig = px.bar(
            sorted_data,
            y='department_name',
            x=metric,
            orientation='h',
            color=metric,
            color_continuous_scale='Blues',
            title=f'各科室{label}对比'
        )

        fig.update_traces(
            hovertemplate=f'<b>科室</b>: %{{y}}<br><b>{label}</b>: %{{x}} {unit}<br><extra></extra>'
        )

        fig.update_layout(
            xaxis_title=f'{label} ({unit})' if unit else label,
            yaxis_title='科室',
            height=max(400, 300 + len(dept_data) * 25),
            showlegend=False,
            coloraxis_colorbar=dict(title=label),
            autosize=True
        )

        return fig

    @staticmethod
    def create_doctor_ranking_chart(doc_data: pd.DataFrame, top_n: int = 10) -> go.Figure:
        if doc_data is None or len(doc_data) == 0:
            return ChartGenerator._create_empty_figure(f'医生接诊量排名 (Top {top_n})')

        top_docs = doc_data.head(top_n).sort_values('total_visits', ascending=True)

        fig = go.Figure()

        fig.add_trace(go.Bar(
            y=top_docs['doctor_name'] + ' (' + top_docs['department_name'] + ')',
            x=top_docs['total_visits'],
            orientation='h',
            marker_color=ChartGenerator.COLORS['primary'],
            name='就诊量',
            hovertemplate='<b>医生</b>: %{y}<br><b>接诊量</b>: %{x} 人次<br><extra></extra>'
        ))

        fig.update_layout(
            title=f'医生接诊量排名 (Top {top_n})',
            xaxis_title='接诊量 (人次)',
            yaxis_title='医生',
            height=max(400, 300 + top_n * 25),
            showlegend=False,
            autosize=True
        )

        return fig

    @staticmethod
    def create_fee_pie_chart(fee_data: Dict[str, Any]) -> go.Figure:
        if not fee_data or 'exam_revenue' not in fee_data:
            return ChartGenerator._create_empty_figure('费用结构分布')

        labels = ['检查费用', '药品费用']
        values = [fee_data.get('exam_revenue', 0), fee_data.get('drug_revenue', 0)]

        fig = go.Figure(data=[go.Pie(
            labels=labels,
            values=values,
            hole=0.4,
            marker_colors=[ChartGenerator.COLORS['primary'], ChartGenerator.COLORS['secondary']],
            textinfo='label+percent',
            textposition='outside',
            hovertemplate='<b>%{label}</b><br>金额: ¥%{value:,.2f}<br>占比: %{percent}<br><extra></extra>'
        )])

        fig.update_layout(
            title='费用结构分布',
            height=400,
            autosize=True,
            legend=dict(title='费用类型'),
            annotations=[dict(text='总收入', x=0.5, y=0.5, font_size=20, showarrow=False)]
        )

        return fig

    @staticmethod
    def create_waiting_time_histogram(wt_data: pd.DataFrame) -> go.Figure:
        if wt_data is None or len(wt_data) == 0 or 'wait_minutes' not in wt_data.columns:
            return ChartGenerator._create_empty_figure('候诊时间分布')

        fig = px.histogram(
            wt_data,
            x='wait_minutes',
            nbins=20,
            color_discrete_sequence=[ChartGenerator.COLORS['primary']],
            title='候诊时间分布'
        )

        fig.update_traces(
            hovertemplate='<b>候诊时间</b>: %{x} 分钟<br><b>患者数量</b>: %{y} 人<br><extra></extra>'
        )

        fig.update_layout(
            xaxis_title='候诊时间 (分钟)',
            yaxis_title='患者数量 (人)',
            height=400,
            bargap=0.1,
            autosize=True
        )

        mean_val = wt_data['wait_minutes'].mean()
        fig.add_vline(
            x=mean_val,
            line_dash='dash',
            line_color=ChartGenerator.COLORS['danger'],
            annotation_text=f'平均值: {mean_val:.1f}分钟',
            annotation_position='top right'
        )

        return fig

    @staticmethod
    def create_satisfaction_radar_chart(sat_data: Dict[str, Any]) -> go.Figure:
        if not sat_data or 'avg_scores' not in sat_data:
            return ChartGenerator._create_empty_figure('满意度雷达图')

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
            opacity=0.6,
            hovertemplate='<b>%{theta}</b><br>评分: %{r:.1f} 分<br><extra></extra>'
        ))

        fig.update_layout(
            polar=dict(
                radialaxis=dict(
                    visible=True,
                    range=[0, 5],
                    title='评分'
                )
            ),
            title='满意度雷达图',
            height=400,
            showlegend=False,
            autosize=True
        )

        return fig

    @staticmethod
    def create_conversion_rate_chart(conv_data: Dict[str, Any]) -> go.Figure:
        if not conv_data or 'by_department' not in conv_data:
            return ChartGenerator._create_empty_figure('各科室检查转化率')

        dept_conv = conv_data['by_department']

        fig = go.Figure()

        fig.add_trace(go.Bar(
            x=dept_conv['department_name'],
            y=dept_conv['conversion_rate'],
            marker_color=ChartGenerator.COLORS['success'],
            text=dept_conv['conversion_rate'].apply(lambda x: f'{x}%'),
            textposition='auto',
            hovertemplate='<b>科室</b>: %{x}<br><b>转化率</b>: %{y}%<br><extra></extra>'
        ))

        fig.update_layout(
            title='各科室检查转化率',
            xaxis_title='科室',
            yaxis_title='转化率 (%)',
            height=400,
            yaxis=dict(range=[0, 100]),
            autosize=True
        )

        return fig

    @staticmethod
    def create_monthly_comparison_chart(monthly_data: pd.DataFrame) -> go.Figure:
        if monthly_data is None or len(monthly_data) == 0:
            return ChartGenerator._create_empty_figure('月度运营对比')

        fig = make_subplots(specs=[[{"secondary_y": True}]])

        fig.add_trace(
            go.Bar(
                x=monthly_data['month_label'],
                y=monthly_data['registrations'],
                name='挂号量',
                marker_color=ChartGenerator.COLORS['primary'],
                hovertemplate='<b>月份</b>: %{x}<br><b>挂号量</b>: %{y} 人次<br><extra></extra>'
            ),
            secondary_y=False
        )

        fig.add_trace(
            go.Scatter(
                x=monthly_data['month_label'],
                y=monthly_data['unique_patients'],
                name='患者数',
                mode='lines+markers',
                line=dict(color=ChartGenerator.COLORS['secondary'], width=3),
                hovertemplate='<b>月份</b>: %{x}<br><b>患者数</b>: %{y} 人<br><extra></extra>'
            ),
            secondary_y=True
        )

        fig.update_layout(
            title='月度运营对比',
            height=400,
            legend=dict(orientation='h', yanchor='bottom', y=1.02, xanchor='right', x=1, title='图例'),
            autosize=True
        )

        fig.update_yaxes(title_text='挂号量 (人次)', secondary_y=False)
        fig.update_yaxes(title_text='患者数 (人)', secondary_y=True)
        fig.update_xaxes(title_text='月份')

        return fig

    @staticmethod
    def create_exam_item_chart(exam_data: pd.DataFrame, top_n: int = 10) -> go.Figure:
        if exam_data is None or len(exam_data) == 0:
            return ChartGenerator._create_empty_figure(f'检查项目收入排行 (Top {top_n})')

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

        fig.update_traces(
            texttemplate='¥%{text:,.0f}',
            hovertemplate='<b>检查项目</b>: %{x}<br><b>收入</b>: ¥%{y:,.2f}<br><b>次数</b>: %{marker.color} 次<br><extra></extra>'
        )

        fig.update_layout(
            xaxis_title='检查项目',
            yaxis_title='收入 (元)',
            height=400,
            coloraxis_colorbar=dict(title='检查次数'),
            autosize=True
        )

        return fig

    @staticmethod
    def create_satisfaction_distribution_chart(sat_dist: Dict[int, int]) -> go.Figure:
        if not sat_dist:
            return ChartGenerator._create_empty_figure('整体满意度分布')

        scores = list(sat_dist.keys())
        counts = list(sat_dist.values())

        colors = ['#dc3545', '#fd7e14', '#ffc107', '#28a745', '#20c997']

        fig = go.Figure(data=[go.Bar(
            x=scores,
            y=counts,
            marker_color=[colors[s-1] for s in scores],
            text=counts,
            textposition='auto',
            hovertemplate='<b>评分</b>: %{x} 分<br><b>人数</b>: %{y} 人<br><extra></extra>'
        )])

        fig.update_layout(
            title='整体满意度分布',
            xaxis_title='评分 (1-5分)',
            yaxis_title='人数 (人)',
            height=350,
            xaxis=dict(tickmode='array', tickvals=[1, 2, 3, 4, 5]),
            autosize=True
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
            margin=dict(l=10, r=10, t=10, b=10),
            autosize=True
        )

        return fig

    @staticmethod
    def create_mom_chart(mom_data: pd.DataFrame) -> go.Figure:
        if mom_data is None or len(mom_data) == 0:
            return ChartGenerator._create_empty_figure('月环比变化分析')

        colors = [ChartGenerator.COLORS['success'] if x >= 0 else ChartGenerator.COLORS['danger'] 
                  for x in mom_data['mom_rate']]

        fig = go.Figure()

        fig.add_trace(go.Bar(
            x=mom_data['month_label'],
            y=mom_data['mom_rate'],
            marker_color=colors,
            text=mom_data['mom_rate'].apply(lambda x: f'+{x}%' if x >= 0 else f'{x}%'),
            textposition='outside',
            hovertemplate='<b>月份</b>: %{x}<br><b>环比变化</b>: %{y}%<br><extra></extra>'
        ))

        fig.add_hline(y=0, line_color='#666', line_width=1)

        fig.update_layout(
            title='月环比变化分析',
            xaxis_title='月份',
            yaxis_title='环比变化率 (%)',
            height=400,
            autosize=True
        )

        return fig

    @staticmethod
    def create_weekday_weekend_chart(ww_data: pd.DataFrame) -> go.Figure:
        if ww_data is None or len(ww_data) == 0:
            return ChartGenerator._create_empty_figure('工作日vs周末对比分析')

        fig = go.Figure()

        fig.add_trace(go.Bar(
            x=ww_data['day_type'],
            y=ww_data['avg_registrations'],
            name='平均挂号量',
            marker_color=ChartGenerator.COLORS['primary'],
            hovertemplate='<b>日期类型</b>: %{x}<br><b>平均挂号量</b>: %{y} 人次<br><extra></extra>'
        ))

        fig.add_trace(go.Bar(
            x=ww_data['day_type'],
            y=ww_data['avg_visits'],
            name='平均就诊量',
            marker_color=ChartGenerator.COLORS['secondary'],
            hovertemplate='<b>日期类型</b>: %{x}<br><b>平均就诊量</b>: %{y} 人次<br><extra></extra>'
        ))

        fig.update_layout(
            title='工作日vs周末对比分析',
            xaxis_title='日期类型',
            yaxis_title='人次',
            barmode='group',
            height=400,
            legend=dict(title='指标'),
            autosize=True
        )

        return fig

    @staticmethod
    def create_peak_hours_chart(hours_data: pd.DataFrame) -> go.Figure:
        if hours_data is None or len(hours_data) == 0:
            return ChartGenerator._create_empty_figure('小时挂号量分布')

        peak_threshold = hours_data['registrations'].quantile(0.75)
        
        colors = [ChartGenerator.COLORS['danger'] if x >= peak_threshold else ChartGenerator.COLORS['primary'] 
                  for x in hours_data['registrations']]

        fig = go.Figure()

        fig.add_trace(go.Bar(
            x=hours_data['hour'],
            y=hours_data['registrations'],
            marker_color=colors,
            hovertemplate='<b>时间</b>: %{x}:00<br><b>挂号量</b>: %{y} 人次<br><extra></extra>'
        ))

        fig.add_hline(
            y=peak_threshold,
            line_dash='dash',
            line_color=ChartGenerator.COLORS['danger'],
            annotation_text=f'高峰阈值: {peak_threshold:.0f}人次',
            annotation_position='top right'
        )

        fig.update_layout(
            title='小时挂号量分布',
            xaxis_title='时间 (时)',
            yaxis_title='挂号量 (人次)',
            height=400,
            xaxis=dict(tickmode='array', tickvals=list(range(8, 19))),
            autosize=True
        )

        return fig

    @staticmethod
    def create_capacity_utilization_chart(cap_data: pd.DataFrame) -> go.Figure:
        if cap_data is None or len(cap_data) == 0:
            return ChartGenerator._create_empty_figure('科室容量利用率')

        sorted_data = cap_data.sort_values('utilization_rate', ascending=True)

        fig = go.Figure()

        fig.add_trace(go.Bar(
            y=sorted_data['department_name'],
            x=sorted_data['utilization_rate'],
            orientation='h',
            marker=dict(
                color=sorted_data['utilization_rate'],
                colorscale='Reds',
                showscale=True,
                colorbar=dict(title='利用率 (%)')
            ),
            text=sorted_data['utilization_rate'].apply(lambda x: f'{x}%'),
            textposition='outside',
            hovertemplate='<b>科室</b>: %{y}<br><b>容量利用率</b>: %{x}%<br><extra></extra>'
        ))

        fig.update_layout(
            title='科室容量利用率',
            xaxis_title='利用率 (%)',
            yaxis_title='科室',
            height=max(400, 300 + len(cap_data) * 25),
            xaxis=dict(range=[0, 100]),
            autosize=True
        )

        return fig

    @staticmethod
    def create_workload_balance_chart(wl_data: pd.DataFrame) -> go.Figure:
        if wl_data is None or len(wl_data) == 0:
            return ChartGenerator._create_empty_figure('医生负荷分布')

        departments = wl_data['department_name'].unique()

        fig = go.Figure()

        for dept in departments:
            dept_data = wl_data[wl_data['department_name'] == dept]
            fig.add_trace(go.Box(
                y=dept_data['daily_visits'],
                name=dept,
                boxpoints='outliers',
                jitter=0.5,
                whiskerwidth=0.2,
                marker_size=4,
                line_width=2,
                hovertemplate='<b>科室</b>: %{x}<br><b>日接诊量</b>: %{y} 人次<br><extra></extra>'
            ))

        fig.update_layout(
            title='医生负荷分布',
            xaxis_title='科室',
            yaxis_title='日接诊量 (人次)',
            height=450,
            showlegend=False,
            autosize=True
        )

        return fig

    @staticmethod
    def create_wait_time_stratification_chart(wts_data: Dict[str, Any]) -> go.Figure:
        if not wts_data:
            return ChartGenerator._create_empty_figure('候诊时间分层分析')

        labels_map = {
            '小于15分钟': '<15分钟(快速)',
            '15至30分钟': '15-30分钟(正常)',
            '30至60分钟': '30-60分钟(偏长)',
            '大于60分钟': '>60分钟(过长)'
        }

        labels = []
        values = []
        for key in ['小于15分钟', '15至30分钟', '30至60分钟', '大于60分钟']:
            if key in wts_data:
                labels.append(labels_map.get(key, key))
                values.append(wts_data[key].get('人数', 0))

        colors = ['#28a745', '#ffc107', '#fd7e14', '#dc3545']

        fig = go.Figure(data=[go.Pie(
            labels=labels,
            values=values,
            marker_colors=colors,
            textinfo='label+percent',
            textposition='outside',
            pull=[0, 0, 0, 0.1],
            hovertemplate='<b>%{label}</b><br>患者数: %{value} 人<br>占比: %{percent}<br><extra></extra>'
        )])

        fig.update_layout(
            title='候诊时间分层分析',
            height=400,
            legend=dict(title='候诊时间层级'),
            autosize=True
        )

        return fig

    @staticmethod
    def create_weekday_weekend_waiting_chart(ww_data: Dict[str, Any]) -> go.Figure:
        if not ww_data or '候诊时间' not in ww_data:
            return ChartGenerator._create_empty_figure('工作日vs周末候诊对比')

        wt_data = ww_data['候诊时间']
        categories = ['平均候诊时间', '中位数候诊时间']
        weekday_values = [wt_data.get('工作日平均', 0), wt_data.get('工作日中位数', 0)]
        weekend_values = [wt_data.get('周末平均', 0), wt_data.get('周末中位数', 0)]

        fig = go.Figure()

        fig.add_trace(go.Bar(
            name='工作日',
            x=categories,
            y=weekday_values,
            marker_color=ChartGenerator.COLORS['primary'],
            text=weekday_values,
            textposition='auto',
            hovertemplate='<b>工作日</b><br>%{x}: %{y} 分钟<br><extra></extra>'
        ))

        fig.add_trace(go.Bar(
            name='周末',
            x=categories,
            y=weekend_values,
            marker_color=ChartGenerator.COLORS['secondary'],
            text=weekend_values,
            textposition='auto',
            hovertemplate='<b>周末</b><br>%{x}: %{y} 分钟<br><extra></extra>'
        ))

        fig.update_layout(
            title='工作日vs周末候诊对比',
            xaxis_title='指标',
            yaxis_title='时间 (分钟)',
            barmode='group',
            height=400,
            legend=dict(title='日期类型'),
            autosize=True
        )

        return fig

    @staticmethod
    def create_peak_hours_waiting_chart(ph_data: pd.DataFrame) -> go.Figure:
        if ph_data is None or len(ph_data) == 0:
            return ChartGenerator._create_empty_figure('高峰时段候诊压力')

        colors = [ChartGenerator.COLORS['danger'] if is_peak else ChartGenerator.COLORS['primary']
                  for is_peak in ph_data.get('is_peak', [False] * len(ph_data))]

        fig = go.Figure()

        fig.add_trace(go.Bar(
            x=ph_data['hour'],
            y=ph_data.get('avg_wait', ph_data.get('registrations', [0] * len(ph_data))),
            marker_color=colors,
            hovertemplate='<b>时间</b>: %{x}:00<br><b>平均候诊</b>: %{y} 分钟<br><extra></extra>'
        ))

        peak_threshold = ph_data.get('avg_wait', ph_data.get('registrations', [0])).quantile(0.75) if len(ph_data) > 0 else 0
        fig.add_hline(
            y=peak_threshold,
            line_dash='dash',
            line_color=ChartGenerator.COLORS['danger'],
            annotation_text=f'高峰阈值: {peak_threshold:.0f}分钟',
            annotation_position='top right'
        )

        fig.update_layout(
            title='高峰时段候诊压力',
            xaxis_title='时间 (时)',
            yaxis_title='平均候诊时间 (分钟)',
            height=400,
            xaxis=dict(tickmode='array', tickvals=list(range(8, 19))),
            autosize=True
        )

        return fig

    @staticmethod
    def create_satisfaction_mom_chart(sat_mom_data: List[Dict[str, Any]]) -> go.Figure:
        if not sat_mom_data:
            return ChartGenerator._create_empty_figure('满意度环比变化')

        months = [item.get('月份', '') for item in sat_mom_data]
        scores = [item.get('平均满意度', 0) for item in sat_mom_data]
        mom_rates = [item.get('环比变化率', 0) if item.get('环比变化率') is not None else 0 for item in sat_mom_data]

        fig = make_subplots(specs=[[{"secondary_y": True}]])

        fig.add_trace(
            go.Scatter(
                x=months,
                y=scores,
                mode='lines+markers',
                name='平均满意度',
                line=dict(color=ChartGenerator.COLORS['primary'], width=3),
                marker=dict(size=10),
                hovertemplate='<b>月份</b>: %{x}<br><b>平均满意度</b>: %{y:.2f} 分<br><extra></extra>'
            ),
            secondary_y=False
        )

        bar_colors = [ChartGenerator.COLORS['success'] if r >= 0 else ChartGenerator.COLORS['danger'] for r in mom_rates]
        fig.add_trace(
            go.Bar(
                x=months,
                y=mom_rates,
                name='环比变化率',
                marker_color=bar_colors,
                opacity=0.6,
                hovertemplate='<b>月份</b>: %{x}<br><b>环比变化</b>: %{y:.2f}%<br><extra></extra>'
            ),
            secondary_y=True
        )

        fig.add_hline(
            y=-5,
            line_dash='dash',
            line_color=ChartGenerator.COLORS['danger'],
            annotation_text='告警阈值 (-5%)',
            annotation_position='bottom right',
            secondary_y=True
        )

        fig.update_layout(
            title='满意度环比变化',
            height=400,
            legend=dict(orientation='h', yanchor='bottom', y=1.02, xanchor='right', x=1, title='图例'),
            autosize=True
        )

        fig.update_yaxes(title_text='平均满意度 (分)', range=[0, 5.5], secondary_y=False)
        fig.update_yaxes(title_text='环比变化率 (%)', secondary_y=True)
        fig.update_xaxes(title_text='月份')

        return fig

    @staticmethod
    def create_low_satisfaction_analysis_chart(ls_data: pd.DataFrame, metric: str = 'count') -> go.Figure:
        if ls_data is None or len(ls_data) == 0:
            return ChartGenerator._create_empty_figure('低分满意度特征分析')

        sorted_data = ls_data.sort_values(metric, ascending=True)

        fig = px.bar(
            sorted_data,
            y='category',
            x=metric,
            orientation='h',
            color=metric,
            color_continuous_scale='Reds',
            title=f'低分满意度{metric}分布'
        )

        fig.update_layout(
            xaxis_title=metric,
            yaxis_title='分类',
            height=max(400, 300 + len(ls_data) * 25),
            showlegend=False,
            autosize=True
        )

        return fig
