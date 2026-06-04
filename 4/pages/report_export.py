import streamlit as st
import pandas as pd
from datetime import datetime
from utils.error_handler import ErrorHandler


class ReportExportPage:
    def __init__(self, report_exporter):
        self.exporter = report_exporter

    def render(self):
        if not st.session_state.get('data_loaded', False):
            st.warning("⚠️ 请先导入数据")
            return

        st.header("📄 报告导出")

        try:
            col1, col2 = st.columns([1, 1])

            with col1:
                st.subheader("导出设置")

                report_type = st.radio(
                    "报告格式",
                    ['Excel 报告', 'HTML 报告'],
                    horizontal=True,
                    key='report_format'
                )

                include_charts = st.checkbox("包含图表", value=True, key='include_charts')
                include_raw_data = st.checkbox("包含原始数据摘要", value=False, key='include_raw')

                st.info(f"""
                **报告内容说明:**

                - 运营概览: 关键指标汇总
                - 科室分析: 各科室运营数据
                - 医生分析: 医生绩效数据
                - 费用分析: 收入结构分析
                - 满意度分析: 患者体验数据
                """)

            with col2:
                st.subheader("预览报告")

                if st.button("生成预览", type="primary", key='preview_btn'):
                    try:
                        with st.spinner("正在生成报告..."):
                            html_preview = self.exporter.generate_html_report()
                            st.session_state.report_preview = html_preview
                            ErrorHandler.display_success("报告生成成功！")
                    except Exception as e:
                        error_msg = ErrorHandler.translate_error(e, "生成HTML报告")
                        ErrorHandler.display_error(f"❌ 生成报告预览失败：{error_msg}")

                if 'report_preview' in st.session_state:
                    with st.expander("查看HTML报告预览", expanded=True):
                        st.components.v1.html(
                            st.session_state.report_preview,
                            height=600,
                            scrolling=True
                        )

            st.divider()
            self._render_download_buttons(report_type)

        except Exception as e:
            error_msg = ErrorHandler.translate_error(e, "报告导出")
            ErrorHandler.display_error(f"❌ 报告导出失败：{error_msg}")

    def _render_download_buttons(self, report_type):
        st.subheader("下载报告")

        col1, col2 = st.columns(2)

        with col1:
            if st.button("下载 Excel 报告", type="secondary", key='download_excel'):
                try:
                    with st.spinner("正在生成Excel报告..."):
                        excel_data = self.exporter.generate_excel_report()
                        filename = self.exporter.get_report_filename('excel')

                        st.download_button(
                            label="点击下载 Excel 文件",
                            data=excel_data,
                            file_name=filename,
                            mime="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            type="primary"
                        )
                except Exception as e:
                    error_msg = ErrorHandler.translate_error(e, "生成Excel报告")
                    ErrorHandler.display_error(f"❌ 生成Excel报告失败：{error_msg}")

        with col2:
            if st.button("下载 HTML 报告", type="secondary", key='download_html'):
                try:
                    with st.spinner("正在生成HTML报告..."):
                        html_content = self.exporter.generate_html_report()
                        filename = self.exporter.get_report_filename('html')

                        st.download_button(
                            label="点击下载 HTML 文件",
                            data=html_content,
                            file_name=filename,
                            mime="text/html",
                            type="primary"
                        )
                except Exception as e:
                    error_msg = ErrorHandler.translate_error(e, "生成HTML报告")
                    ErrorHandler.display_error(f"❌ 生成HTML报告失败：{error_msg}")

        st.caption(f"报告生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
