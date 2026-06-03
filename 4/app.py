import streamlit as st
import sys
import os

st.set_page_config(
    page_title="医院门诊运营分析平台",
    page_icon="🏥",
    layout="wide",
    initial_sidebar_state="expanded"
)

from ingestion import DataLoader
from validation import DataValidator
from transform import DataTransformer
from metrics import MetricsCalculator
from visualization import ChartGenerator
from export import ReportExporter
from pages import (
    DataImportPage,
    OverviewPage,
    DepartmentAnalysisPage,
    DoctorAnalysisPage,
    FeeStructurePage,
    PatientExperiencePage,
    ReportExportPage
)


def initialize_session_state():
    if 'data_loaded' not in st.session_state:
        st.session_state.data_loaded = False
    if 'data' not in st.session_state:
        st.session_state.data = None


def main():
    initialize_session_state()

    st.sidebar.title("🏥 医院门诊运营分析平台")
    st.sidebar.markdown("---")

    data_loader = DataLoader()

    page = st.sidebar.radio(
        "导航菜单",
        [
            "📂 数据导入",
            "📊 运营总览",
            "🏥 科室分析",
            "👨‍⚕️ 医生分析",
            "💰 费用结构",
            "😊 患者体验",
            "📄 报告导出"
        ],
        key="navigation"
    )

    st.sidebar.markdown("---")

    if st.session_state.data_loaded and st.session_state.data is not None:
        data = st.session_state.data.copy()

        transformer = DataTransformer(data)
        transformed_data = transformer.transform_all()
        st.session_state.transformed_data = transformed_data

        metrics = MetricsCalculator(transformed_data)
        charts = ChartGenerator()
        exporter = ReportExporter(metrics)

        if page == "📂 数据导入":
            DataImportPage(data_loader, DataValidator).render()

        elif page == "📊 运营总览":
            OverviewPage(metrics, charts).render()

        elif page == "🏥 科室分析":
            DepartmentAnalysisPage(metrics, charts).render()

        elif page == "👨‍⚕️ 医生分析":
            DoctorAnalysisPage(metrics, charts).render()

        elif page == "💰 费用结构":
            FeeStructurePage(metrics, charts).render()

        elif page == "😊 患者体验":
            PatientExperiencePage(transformed_data, metrics, charts).render()

        elif page == "📄 报告导出":
            ReportExportPage(exporter).render()

    else:
        if page == "📂 数据导入":
            DataImportPage(data_loader, DataValidator).render()
        else:
            st.warning("⚠️ 请先在「数据导入」页面上传或加载数据")
            st.info("""
            **使用说明:**

            1. 点击左侧「📂 数据导入」菜单

            2. 选择以下方式之一加载数据:
               - 上传各CSV文件
               - 使用演示数据

            3. 完成数据校验后即可查看分析结果
            """)

    st.sidebar.markdown("---")
    st.sidebar.info("""
    **支持功能:

    - 多维度运营数据分析

    - 科室/医生联动筛选

    - 智能异常识别

    - 多格式报告导出
    """)


if __name__ == "__main__":
    main()
