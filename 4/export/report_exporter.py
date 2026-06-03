import pandas as pd
import os
from datetime import datetime
from typing import Dict, Any, Optional
import io
import json


class ReportExporter:
    def __init__(self, metrics_calculator):
        self.metrics = metrics_calculator

    def generate_excel_report(self, output_path: Optional[str] = None) -> bytes:
        output = io.BytesIO()

        with pd.ExcelWriter(output, engine='openpyxl') as writer:
            self._write_overview_sheet(writer)
            self._write_department_sheet(writer)
            self._write_doctor_sheet(writer)
            self._write_fee_sheet(writer)
            self._write_satisfaction_sheet(writer)

        output.seek(0)

        if output_path:
            with open(output_path, 'wb') as f:
                f.write(output.getvalue())

        return output.getvalue()

    def _write_overview_sheet(self, writer):
        overview_metrics = self.metrics.get_overview_metrics()

        overview_data = []
        for key, value in overview_metrics.items():
            if isinstance(value, (int, float, str)):
                label = self._get_metric_label(key)
                overview_data.append({'指标': label, '数值': value})

        df = pd.DataFrame(overview_data)
        df.to_excel(writer, sheet_name='运营概览', index=False)

    def _write_department_sheet(self, writer):
        dept_metrics = self.metrics.get_department_metrics()
        if dept_metrics is not None:
            dept_metrics.to_excel(writer, sheet_name='科室分析', index=False)

        anomalies = self.metrics.detect_anomalous_departments()
        if anomalies is not None:
            anomalies.to_excel(writer, sheet_name='异常科室', index=False)

    def _write_doctor_sheet(self, writer):
        doc_metrics = self.metrics.get_doctor_metrics()
        if doc_metrics is not None:
            doc_metrics.to_excel(writer, sheet_name='医生分析', index=False)

    def _write_fee_sheet(self, writer):
        fee_structure = self.metrics.get_fee_structure()

        summary_data = [
            {'项目': '总收入', '金额': fee_structure.get('total_revenue', 0)},
            {'项目': '检查收入', '金额': fee_structure.get('exam_revenue', 0)},
            {'项目': '药品收入', '金额': fee_structure.get('drug_revenue', 0)},
            {'项目': '检查占比(%)', '金额': fee_structure.get('exam_ratio', 0)},
            {'项目': '药品占比(%)', '金额': fee_structure.get('drug_ratio', 0)}
        ]
        pd.DataFrame(summary_data).to_excel(writer, sheet_name='费用分析', index=False, startrow=0)

        if 'exam_by_item' in fee_structure:
            fee_structure['exam_by_item'].to_excel(
                writer, sheet_name='费用分析', index=False, startrow=8
            )

    def _write_satisfaction_sheet(self, writer):
        sat_dist = self.metrics.get_satisfaction_distribution()

        if 'avg_scores' in sat_dist:
            data = [{'维度': k, '平均分': v} for k, v in sat_dist['avg_scores'].items()]
            pd.DataFrame(data).to_excel(writer, sheet_name='满意度分析', index=False)

    def generate_html_report(self) -> str:
        overview = self.metrics.get_overview_metrics()
        dept_metrics = self.metrics.get_department_metrics()
        doc_metrics = self.metrics.get_doctor_metrics()
        fee_structure = self.metrics.get_fee_structure()
        sat_dist = self.metrics.get_satisfaction_distribution()

        html_content = f"""
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>医院门诊运营分析报告</title>
    <style>
        body {{ font-family: 'Microsoft YaHei', Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }}
        .header {{ background: linear-gradient(135deg, #1f77b4, #17a2b8); color: white; padding: 20px; border-radius: 10px; margin-bottom: 20px; }}
        .header h1 {{ margin: 0; font-size: 24px; }}
        .header p {{ margin: 5px 0 0 0; opacity: 0.9; }}
        .section {{ background: white; padding: 20px; border-radius: 10px; margin-bottom: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }}
        .section h2 {{ color: #1f77b4; border-bottom: 2px solid #1f77b4; padding-bottom: 10px; margin-top: 0; }}
        .kpi-grid {{ display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; }}
        .kpi-card {{ background: linear-gradient(135deg, #f8f9fa, #e9ecef); padding: 15px; border-radius: 8px; text-align: center; }}
        .kpi-value {{ font-size: 28px; font-weight: bold; color: #1f77b4; }}
        .kpi-label {{ font-size: 14px; color: #666; margin-top: 5px; }}
        table {{ width: 100%; border-collapse: collapse; margin-top: 10px; }}
        th, td {{ padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }}
        th {{ background-color: #f8f9fa; font-weight: bold; color: #333; }}
        tr:hover {{ background-color: #f8f9fa; }}
        .footer {{ text-align: center; color: #666; margin-top: 30px; padding: 20px; }}
        .highlight {{ background-color: #fff3cd; }}
    </style>
</head>
<body>
    <div class="header">
        <h1>🏥 医院门诊运营分析报告</h1>
        <p>生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}</p>
    </div>

    <div class="section">
        <h2>📊 运营概览</h2>
        <div class="kpi-grid">
            <div class="kpi-card">
                <div class="kpi-value">{overview.get('total_registrations', 0)}</div>
                <div class="kpi-label">总挂号量</div>
            </div>
            <div class="kpi-card">
                <div class="kpi-value">{overview.get('unique_patients', 0)}</div>
                <div class="kpi-label">就诊患者数</div>
            </div>
            <div class="kpi-card">
                <div class="kpi-value">¥{overview.get('total_revenue', 0):,.0f}</div>
                <div class="kpi-label">总收入</div>
            </div>
            <div class="kpi-card">
                <div class="kpi-value">{overview.get('avg_wait_time', 0)}分钟</div>
                <div class="kpi-label">平均候诊时间</div>
            </div>
            <div class="kpi-card">
                <div class="kpi-value">{overview.get('avg_overall_score', 0)}</div>
                <div class="kpi-label">平均满意度</div>
            </div>
            <div class="kpi-card">
                <div class="kpi-value">{overview.get('total_departments', 0)}</div>
                <div class="kpi-label">科室数量</div>
            </div>
        </div>
    </div>

    <div class="section">
        <h2>🏥 科室运营分析</h2>
        <table>
            <thead>
                <tr>
                    <th>科室名称</th>
                    <th>挂号量</th>
                    <th>就诊量</th>
                    <th>医生数</th>
                    <th>人均接诊量</th>
                    <th>平均候诊时间</th>
                    <th>平均满意度</th>
                </tr>
            </thead>
            <tbody>
                {self._generate_dept_rows(dept_metrics)}
            </tbody>
        </table>
    </div>

    <div class="section">
        <h2>👨‍⚕️ 医生绩效Top 10</h2>
        <table>
            <thead>
                <tr>
                    <th>排名</th>
                    <th>医生姓名</th>
                    <th>科室</th>
                    <th>职称</th>
                    <th>接诊量</th>
                    <th>收入贡献</th>
                    <th>满意度</th>
                </tr>
            </thead>
            <tbody>
                {self._generate_doctor_rows(doc_metrics)}
            </tbody>
        </table>
    </div>

    <div class="section">
        <h2>💰 费用结构分析</h2>
        <div class="kpi-grid">
            <div class="kpi-card">
                <div class="kpi-value">¥{fee_structure.get('exam_revenue', 0):,.0f}</div>
                <div class="kpi-label">检查收入 ({fee_structure.get('exam_ratio', 0)}%)</div>
            </div>
            <div class="kpi-card">
                <div class="kpi-value">¥{fee_structure.get('drug_revenue', 0):,.0f}</div>
                <div class="kpi-label">药品收入 ({fee_structure.get('drug_ratio', 0)}%)</div>
            </div>
        </div>
    </div>

    <div class="footer">
        <p>本报告由医院门诊运营分析平台自动生成</p>
    </div>
</body>
</html>
        """
        return html_content

    def _generate_dept_rows(self, dept_metrics):
        if dept_metrics is None or len(dept_metrics) == 0:
            return '<tr><td colspan="7" style="text-align:center;">暂无数据</td></tr>'

        rows = []
        for _, row in dept_metrics.iterrows():
            rows.append(f"""
                <tr>
                    <td>{row.get('department_name', '')}</td>
                    <td>{row.get('total_registrations', 0)}</td>
                    <td>{row.get('total_visits', 0)}</td>
                    <td>{row.get('doctor_count', 0)}</td>
                    <td>{row.get('visits_per_doctor', 0)}</td>
                    <td>{row.get('avg_wait_time', 0):.1f}分钟</td>
                    <td>{row.get('avg_satisfaction', 0):.2f}</td>
                </tr>
            """)
        return ''.join(rows)

    def _generate_doctor_rows(self, doc_metrics):
        if doc_metrics is None or len(doc_metrics) == 0:
            return '<tr><td colspan="7" style="text-align:center;">暂无数据</td></tr>'

        rows = []
        for idx, (_, row) in enumerate(doc_metrics.head(10).iterrows(), 1):
            rows.append(f"""
                <tr>
                    <td>{idx}</td>
                    <td>{row.get('doctor_name', '')}</td>
                    <td>{row.get('department_name', '')}</td>
                    <td>{row.get('title', '')}</td>
                    <td>{row.get('total_visits', 0)}</td>
                    <td>¥{row.get('total_revenue', 0):,.0f}</td>
                    <td>{row.get('avg_satisfaction', 0):.2f}</td>
                </tr>
            """)
        return ''.join(rows)

    def _get_metric_label(self, key: str) -> str:
        labels = {
            'total_registrations': '总挂号量',
            'unique_patients': '就诊患者数',
            'avg_daily_registrations': '日均挂号量',
            'total_visits': '总就诊量',
            'total_revenue': '总收入',
            'avg_visit_fee': '次均费用',
            'avg_wait_time': '平均候诊时间(分钟)',
            'median_wait_time': '中位候诊时间(分钟)',
            'max_wait_time': '最长候诊时间(分钟)',
            'wait_over_30min_pct': '候诊超30分钟占比(%)',
            'avg_overall_score': '平均满意度',
            'satisfaction_pct': '满意度达标率(%)',
            'total_departments': '科室数量',
            'total_doctors': '医生数量'
        }
        return labels.get(key, key)

    def save_html_report(self, output_path: str) -> None:
        html_content = self.generate_html_report()
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write(html_content)

    def get_report_filename(self, report_type: str = 'excel') -> str:
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        if report_type == 'excel':
            return f'门诊运营分析报告_{timestamp}.xlsx'
        elif report_type == 'html':
            return f'门诊运营分析报告_{timestamp}.html'
        return f'门诊运营分析报告_{timestamp}'
