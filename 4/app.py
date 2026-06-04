import streamlit as st
import sys
import os
import traceback
import pandas as pd

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
from utils.error_handler import ErrorHandler


def initialize_session_state():
    if 'data_loaded' not in st.session_state:
        st.session_state.data_loaded = False
    if 'data' not in st.session_state:
        st.session_state.data = None
    if 'filters' not in st.session_state:
        st.session_state.filters = {
            'date_range': None,
            'departments': [],
            'doctors': [],
            'patient_types': []
        }
    if 'filtered_data' not in st.session_state:
        st.session_state.filtered_data = None


def get_department_options(data):
    if data.get('departments') is not None and 'department_name' in data['departments'].columns:
        return sorted(data['departments']['department_name'].tolist())
    return []


def get_doctor_options(data):
    if data.get('doctors') is not None and 'doctor_name' in data['doctors'].columns:
        return sorted(data['doctors']['doctor_name'].tolist())
    return []


def get_patient_type_options(data):
    if data.get('registrations') is not None and 'patient_type' in data['registrations'].columns:
        return sorted(data['registrations']['patient_type'].unique().tolist())
    return []


def get_date_range(data):
    if data.get('registrations') is not None and 'reg_date' in data['registrations'].columns:
        dates = pd.to_datetime(data['registrations']['reg_date'])
        return dates.min(), dates.max()
    return None, None


@st.cache_data
def apply_filters_cached(_data, filters):
    return apply_filters(_data, filters)


def apply_filters(data, filters):
    if data is None:
        return None

    filtered = {}
    for key, df in data.items():
        if df is not None:
            filtered[key] = df.copy()
        else:
            filtered[key] = None

    if filters.get('date_range') and filtered.get('registrations') is not None:
        start_date, end_date = filters['date_range']
        reg_df = filtered['registrations']
        reg_dates = pd.to_datetime(reg_df['reg_date'])
        mask = (reg_dates >= pd.to_datetime(start_date)) & (reg_dates <= pd.to_datetime(end_date))
        filtered['registrations'] = reg_df[mask].copy()

        filtered_reg_ids = set(filtered['registrations']['reg_id'].tolist())

        if filtered.get('visits') is not None:
            filtered['visits'] = filtered['visits'][filtered['visits']['reg_id'].isin(filtered_reg_ids)].copy()
            filtered_visit_ids = set(filtered['visits']['visit_id'].tolist())

            if filtered.get('examinations') is not None:
                filtered['examinations'] = filtered['examinations'][
                    filtered['examinations']['visit_id'].isin(filtered_visit_ids)
                ].copy()

            if filtered.get('medications') is not None:
                filtered['medications'] = filtered['medications'][
                    filtered['medications']['visit_id'].isin(filtered_visit_ids)
                ].copy()

            if filtered.get('satisfaction') is not None:
                filtered['satisfaction'] = filtered['satisfaction'][
                    filtered['satisfaction']['visit_id'].isin(filtered_visit_ids)
                ].copy()

        if filtered.get('waiting_times') is not None:
            filtered['waiting_times'] = filtered['waiting_times'][
                filtered['waiting_times']['reg_id'].isin(filtered_reg_ids)
            ].copy()

    if filters.get('departments') and filtered.get('departments') is not None:
        dept_df = filtered['departments']
        dept_ids = dept_df[dept_df['department_name'].isin(filters['departments'])]['department_id'].tolist()

        if filtered.get('registrations') is not None:
            filtered['registrations'] = filtered['registrations'][
                filtered['registrations']['department_id'].isin(dept_ids)
            ].copy()

        if filtered.get('visits') is not None:
            filtered['visits'] = filtered['visits'][
                filtered['visits']['department_id'].isin(dept_ids)
            ].copy()

        if filtered.get('doctors') is not None:
            filtered['doctors'] = filtered['doctors'][
                filtered['doctors']['department_id'].isin(dept_ids)
            ].copy()

    if filters.get('patient_types') and filtered.get('registrations') is not None:
        filtered['registrations'] = filtered['registrations'][
            filtered['registrations']['patient_type'].isin(filters['patient_types'])
        ].copy()

    return filtered


def render_sidebar_filters(data):
    with st.sidebar:
        st.markdown("---")
        st.subheader("🔍 全局筛选")

        with st.expander("筛选条件", expanded=False):
            min_date, max_date = get_date_range(data)
            if min_date is not None and max_date is not None:
                date_range = st.date_input(
                    "日期范围",
                    value=(min_date, max_date),
                    min_value=min_date,
                    max_value=max_date,
                    key="filter_date_range"
                )
                st.session_state.filters['date_range'] = date_range

            dept_options = get_department_options(data)
            if dept_options:
                selected_depts = st.multiselect(
                    "选择科室",
                    options=dept_options,
                    default=dept_options,
                    key="filter_departments"
                )
                st.session_state.filters['departments'] = selected_depts

            pt_options = get_patient_type_options(data)
            if pt_options:
                selected_pts = st.multiselect(
                    "患者类型",
                    options=pt_options,
                    default=pt_options,
                    key="filter_patient_types"
                )
                st.session_state.filters['patient_types'] = selected_pts

            if st.button("重置筛选", type="secondary", use_container_width=True):
                st.session_state.filters = {
                    'date_range': (min_date, max_date) if min_date else None,
                    'departments': dept_options,
                    'doctors': [],
                    'patient_types': pt_options
                }
                st.rerun()

        has_filters = (
            (st.session_state.filters['departments'] and len(st.session_state.filters['departments']) < len(get_department_options(data)))
            or (st.session_state.filters['patient_types'] and len(st.session_state.filters['patient_types']) < len(get_patient_type_options(data)))
        )
        if has_filters:
            st.success("✅ 筛选已应用")
        else:
            st.info("📊 显示全部数据")


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

    try:
        if st.session_state.data_loaded and st.session_state.data is not None:
            try:
                render_sidebar_filters(st.session_state.data)

                with st.spinner("正在处理数据..."):
                    filtered_data = apply_filters_cached(
                        st.session_state.data,
                        st.session_state.filters
                    )
                    st.session_state.filtered_data = filtered_data

                    transformer = DataTransformer(filtered_data)
                    transformed_data = transformer.transform_all()
                    st.session_state.transformed_data = transformed_data

                    metrics = MetricsCalculator(transformed_data)
                    charts = ChartGenerator()

                    validator = DataValidator(st.session_state.data)
                    exporter = ReportExporter(
                        data=st.session_state.data,
                        transformed_data=transformed_data,
                        metrics_calculator=metrics,
                        validator=validator
                    )

                if page == "📂 数据导入":
                    DataImportPage(data_loader, DataValidator).render()

                elif page == "📊 运营总览":
                    try:
                        OverviewPage(metrics, charts).render()
                    except Exception as e:
                        ErrorHandler.display_error(
                            ErrorHandler.handle_analysis_error(e, "运营总览分析")
                        )

                elif page == "🏥 科室分析":
                    try:
                        DepartmentAnalysisPage(metrics, charts).render()
                    except Exception as e:
                        ErrorHandler.display_error(
                            ErrorHandler.handle_analysis_error(e, "科室分析")
                        )

                elif page == "👨‍⚕️ 医生分析":
                    try:
                        DoctorAnalysisPage(metrics, charts).render()
                    except Exception as e:
                        ErrorHandler.display_error(
                            ErrorHandler.handle_analysis_error(e, "医生分析")
                        )

                elif page == "💰 费用结构":
                    try:
                        FeeStructurePage(metrics, charts).render()
                    except Exception as e:
                        ErrorHandler.display_error(
                            ErrorHandler.handle_analysis_error(e, "费用结构分析")
                        )

                elif page == "😊 患者体验":
                    try:
                        PatientExperiencePage(transformed_data, metrics, charts).render()
                    except Exception as e:
                        ErrorHandler.display_error(
                            ErrorHandler.handle_analysis_error(e, "患者体验分析")
                        )

                elif page == "📄 报告导出":
                    try:
                        ReportExportPage(exporter).render()
                    except Exception as e:
                        ErrorHandler.display_error(
                            ErrorHandler.handle_analysis_error(e, "报告导出")
                        )

            except Exception as e:
                error_msg = ErrorHandler.translate_error(e, "数据处理")
                ErrorHandler.display_error(f"❌ 数据处理失败：{error_msg}")

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

    except Exception as e:
        error_type = type(e).__name__
        error_msg = str(e)

        st.error(f"""
        🚨 **系统运行出错**

        **错误类型：** {error_type}

        **错误信息：** {error_msg}

        **请尝试以下操作：**
        1. 刷新页面重新加载
        2. 检查数据文件格式是否正确
        3. 确保已上传所有必要的数据文件

        如问题持续存在，请联系技术支持。
        """, icon="🚨")

        with st.expander("查看详细错误信息"):
            st.code(traceback.format_exc())

    st.sidebar.markdown("---")
    st.sidebar.info("""
    **支持功能:**

    - 多维度运营数据分析
    - 科室/医生联动筛选
    - 智能异常识别
    - 多格式报告导出
    """)


if __name__ == "__main__":
    main()
