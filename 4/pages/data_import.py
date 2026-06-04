import streamlit as st
import pandas as pd
from typing import Dict, Any, List
from utils.error_handler import ErrorHandler


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
                try:
                    with st.spinner("正在导入数据..."):
                        load_status = self.data_loader.load_from_uploaded_files(uploaded_files)

                        if self.data_loader.has_errors():
                            for error_msg in self.data_loader.get_error_messages():
                                if error_msg.startswith('❌'):
                                    ErrorHandler.display_error(error_msg)
                                elif error_msg.startswith('⚠️'):
                                    ErrorHandler.display_warning(error_msg)

                        loaded_count = len(self.data_loader.get_loaded_files())
                        if loaded_count > 0:
                            st.session_state.data_loaded = True
                            st.session_state.data = self.data_loader.get_all_data()
                            ErrorHandler.display_success(f"成功导入 {loaded_count} 份数据文件！")
                        else:
                            ErrorHandler.display_error("未成功导入任何数据文件，请检查文件格式后重试")

                except Exception as e:
                    error_msg = ErrorHandler.translate_error(e, "导入数据")
                    ErrorHandler.display_error(f"❌ 数据导入失败：{error_msg}")

        with col2:
            st.subheader("使用演示数据")
            st.info("点击下方按钮可直接加载内置的演示数据，包含1200条就诊记录")

            if st.button("加载演示数据", type="secondary"):
                try:
                    with st.spinner("正在加载演示数据..."):
                        self.data_loader.load_from_directory('data/demo')

                        if self.data_loader.has_errors():
                            for error_msg in self.data_loader.get_error_messages():
                                if error_msg.startswith('❌'):
                                    ErrorHandler.display_error(error_msg)
                                elif error_msg.startswith('⚠️'):
                                    ErrorHandler.display_warning(error_msg)

                        loaded_count = len(self.data_loader.get_loaded_files())
                        if loaded_count > 0:
                            st.session_state.data_loaded = True
                            st.session_state.data = self.data_loader.get_all_data()
                            ErrorHandler.display_success(f"成功加载 {loaded_count} 份演示数据！")
                        else:
                            ErrorHandler.display_warning("""
                                演示数据文件尚未生成，请先在命令行运行：
                                ```
                                python data/generate_demo_data.py
                                ```
                                或者手动上传CSV数据文件。
                            """)

                except Exception as e:
                    error_msg = ErrorHandler.translate_error(e, "加载演示数据")
                    ErrorHandler.display_error(f"❌ 加载演示数据失败：{error_msg}")

        if st.session_state.get('data_loaded', False):
            try:
                self._render_data_status()
                self._render_validation_results()
                self._render_data_preview()
            except Exception as e:
                error_msg = ErrorHandler.translate_error(e, "显示数据结果")
                ErrorHandler.display_error(f"❌ 显示数据结果失败：{error_msg}")

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

        try:
            with st.spinner("正在加载数据，请稍候..."):
                validator = self.validator(st.session_state.data)
                validation_results = validator.validate_all()
                summary = validator.get_summary()
                detailed_issues = validator.get_detailed_issues()

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

            with st.expander("📋 详细问题清单", expanded=False):
                if not detailed_issues:
                    st.info("暂无详细校验问题")
                else:
                    error_issues = [issue for issue in detailed_issues if issue.get('severity') == 'error']
                    warning_issues = [issue for issue in detailed_issues if issue.get('severity') == 'warning']

                    if error_issues:
                        st.markdown("### ❌ 错误问题")
                        error_df = pd.DataFrame([{
                            '文件': self.FILE_LABELS.get(issue.get('file_key'), issue.get('file_key')),
                            '行号': issue.get('row_number', '-') if issue.get('row_number', 0) != 0 else '-',
                            '字段': issue.get('field', '-'),
                            '问题描述': issue.get('message', ''),
                            '处理建议': issue.get('suggestion', '')
                        } for issue in error_issues])
                        st.dataframe(error_df.style.applymap(lambda x: 'background-color: #ffcccc', subset=['文件']),
                                     use_container_width=True, hide_index=True)

                    if warning_issues:
                        st.markdown("### ⚠️ 警告问题")
                        warning_df = pd.DataFrame([{
                            '文件': self.FILE_LABELS.get(issue.get('file_key'), issue.get('file_key')),
                            '行号': issue.get('row_number', '-') if issue.get('row_number', 0) != 0 else '-',
                            '字段': issue.get('field', '-'),
                            '问题描述': issue.get('message', ''),
                            '处理建议': issue.get('suggestion', '')
                        } for issue in warning_issues])
                        st.dataframe(warning_df.style.applymap(lambda x: 'background-color: #fff2cc', subset=['文件']),
                                     use_container_width=True, hide_index=True)

            if summary['is_valid']:
                st.success("✅ 数据校验通过！")

        except Exception as e:
            error_msg = ErrorHandler.translate_error(e, "数据校验")
            ErrorHandler.display_error(f"❌ 数据校验失败：{error_msg}")

    def _render_data_preview(self):
        if not st.session_state.get('data_loaded', False):
            return

        st.divider()
        st.subheader("👁️ 数据预览")

        try:
            data = st.session_state.data
            available_data = [(k, v) for k, v in data.items() if v is not None and len(v) > 0]

            if not available_data:
                st.info("暂无可用数据进行预览")
                return

            tabs = st.tabs([self.FILE_LABELS.get(k, k) for k, v in available_data])

            for idx, (key, df) in enumerate(available_data):
                with tabs[idx]:
                    try:
                        st.write(f"**{self.FILE_LABELS.get(key, key)} - 共 {len(df)} 条记录**")
                        st.dataframe(df.head(10), use_container_width=True)
                        st.caption(f"字段: {', '.join(df.columns.tolist())}")
                    except Exception as e:
                        error_msg = ErrorHandler.translate_error(e, f"预览{self.FILE_LABELS.get(key, key)}数据")
                        ErrorHandler.display_error(f"❌ 预览数据失败：{error_msg}")

        except Exception as e:
            error_msg = ErrorHandler.translate_error(e, "显示数据预览")
            ErrorHandler.display_error(f"❌ 显示数据预览失败：{error_msg}")
