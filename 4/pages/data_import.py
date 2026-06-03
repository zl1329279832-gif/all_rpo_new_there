import streamlit as st
import pandas as pd
from typing import Dict, Any


class DataImportPage:
    FILE_LABELS = {
        'departments': '科室信息',
        'doctors': '医生信息',
        'registrations': '挂号记录',
        'visits': '就诊记录',
        'examinations': '检查项目',
        'medications': '药品费用',
        'waiting_times': '候诊时间',
        'satisfaction': '患者满意度'
    }

    def __init__(self, data_loader, data_validator):
        self.data_loader = data_loader
        self.validator = data_validator

    def render(self):
        st.header("📂 数据导入")

        col1, col2 = st.columns([2, 1])

        with col1:
            st.subheader("上传CSV文件")
            uploaded_files = {}
            for key, label in self.FILE_LABELS.items():
                uploaded_files[key] = st.file_uploader(
                    f"上传 {label} CSV",
                    type=['csv'],
                    key=f"upload_{key}",
                    help=f"上传 {label} 数据文件"
                )

            if st.button("开始导入数据", type="primary"):
                with st.spinner("正在导入数据..."):
                    load_status = self.data_loader.load_from_uploaded_files(uploaded_files)
                    st.session_state.data_loaded = True
                    st.session_state.data = self.data_loader.get_all_data()
                    st.success("数据导入完成！")

        with col2:
            st.subheader("使用演示数据")
            if st.button("加载演示数据", type="secondary"):
                with st.spinner("正在加载演示数据..."):
                    self.data_loader.load_from_directory('data/demo')
                    st.session_state.data_loaded = True
                    st.session_state.data = self.data_loader.get_all_data()
                    st.success("演示数据加载完成！")

        if st.session_state.get('data_loaded', False):
            self._render_data_status()
            self._render_validation_results()
            self._render_data_preview()

    def _render_data_status(self):
        st.divider()
        st.subheader("📊 数据加载状态")

        status_cols = st.columns(4)
        loaded_count = len(self.data_loader.get_loaded_files())
        total_files = len(self.FILE_LABELS)

        with status_cols[0]:
            st.metric("已加载文件", f"{loaded_count}/{total_files}")

        with status_cols[1]:
            total_rows = sum(self.data_loader.get_row_count(k) for k in self.data_loader.get_loaded_files())
            st.metric("总记录数", f"{total_rows:,}")

        with status_cols[2]:
            missing = self.data_loader.get_missing_files()
            st.metric("缺失文件", len(missing), delta_color="inverse")

        with status_cols[3]:
            is_complete = self.data_loader.is_complete()
            st.metric("数据完整性", "完整" if is_complete else "不完整",
                      delta_color="normal" if is_complete else "inverse")

        if missing:
            st.warning(f"以下文件尚未上传: {', '.join([self.FILE_LABELS[m] for m in missing])}")

    def _render_validation_results(self):
        if not st.session_state.get('data_loaded', False):
            return

        st.divider()
        st.subheader("✅ 数据校验结果")

        validator = self.validator(st.session_state.data)
        validation_results = validator.validate_all()
        summary = validator.get_summary()

        col1, col2 = st.columns(2)
        with col1:
            st.metric("错误数量", summary['total_errors'], delta_color="inverse")
        with col2:
            st.metric("警告数量", summary['total_warnings'], delta_color="off")

        if summary['errors']:
            with st.expander("查看详细错误", expanded=True):
                for error in summary['errors']:
                    st.error(f"❌ {error}")

        if summary['warnings']:
            with st.expander("查看详细警告"):
                for warning in summary['warnings']:
                    st.warning(f"⚠️ {warning}")

        if summary['is_valid']:
            st.success("✅ 数据校验通过！")

    def _render_data_preview(self):
        if not st.session_state.get('data_loaded', False):
            return

        st.divider()
        st.subheader("👁️ 数据预览")

        data = st.session_state.data
        tabs = st.tabs([self.FILE_LABELS.get(k, k) for k in data.keys() if data[k] is not None])

        for idx, (key, df) in enumerate([(k, v) for k, v in data.items() if v is not None]):
            with tabs[idx]:
                st.write(f"**{self.FILE_LABELS.get(key, key)} - 共 {len(df)} 条记录**")
                st.dataframe(df.head(10), use_container_width=True)

                st.caption(f"字段: {', '.join(df.columns.tolist())}")
