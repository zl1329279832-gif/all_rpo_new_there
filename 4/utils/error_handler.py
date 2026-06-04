import pandas as pd
import streamlit as st
from typing import Dict, Any, Optional


class ErrorHandler:
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

    ERROR_MESSAGES = {
        'FileNotFoundError': '文件未找到',
        'ParserError': 'CSV文件格式错误，无法解析',
        'UnicodeDecodeError': '文件编码格式不正确，请使用UTF-8编码',
        'EmptyDataError': '文件为空，没有数据内容',
        'KeyError': '缺少必要的字段',
        'ValueError': '数据格式不正确',
        'TypeError': '数据类型错误',
        'AttributeError': '数据属性不存在',
        'IndexError': '数据索引错误',
        'MemoryError': '内存不足，无法处理大数据文件'
    }

    @staticmethod
    def get_file_label(file_key: str) -> str:
        return ErrorHandler.FILE_LABELS.get(file_key, file_key)

    @staticmethod
    def translate_error(exception: Exception, context: str = '') -> str:
        error_type = type(exception).__name__
        error_msg = str(exception)

        base_msg = ErrorHandler.ERROR_MESSAGES.get(error_type, '未知错误')

        if context:
            return f"{context} - {base_msg}: {error_msg}"
        return f"{base_msg}: {error_msg}"

    @staticmethod
    def handle_file_upload_error(exception: Exception, file_key: str) -> str:
        file_label = ErrorHandler.get_file_label(file_key)
        error_type = type(exception).__name__

        if error_type == 'ParserError':
            return f"❌ 导入【{file_label}】失败：CSV文件格式错误，请检查文件是否损坏或格式不正确"
        elif error_type == 'UnicodeDecodeError':
            return f"❌ 导入【{file_label}】失败：文件编码格式不正确，请另存为UTF-8编码格式"
        elif error_type == 'EmptyDataError':
            return f"❌ 导入【{file_label}】失败：文件为空，没有包含任何数据"
        elif error_type == 'KeyError':
            return f"❌ 导入【{file_label}】失败：缺少必要的字段，请检查CSV文件的列名"
        elif error_type == 'ValueError':
            return f"❌ 导入【{file_label}】失败：数据内容格式不正确，请检查数据类型"
        else:
            return f"❌ 导入【{file_label}】失败：{ErrorHandler.translate_error(exception)}"

    @staticmethod
    def handle_data_validation_error(exception: Exception) -> str:
        return f"⚠️ 数据校验失败：{ErrorHandler.translate_error(exception)}"

    @staticmethod
    def handle_missing_data_error(required_files: list) -> str:
        missing_files = [ErrorHandler.get_file_label(f) for f in required_files]
        return f"⚠️ 缺少必要数据文件：{', '.join(missing_files)}，请先在数据导入页面上传这些文件"

    @staticmethod
    def handle_analysis_error(exception: Exception, analysis_type: str) -> str:
        return f"❌ {analysis_type}失败：{ErrorHandler.translate_error(exception)}"

    @staticmethod
    def display_error(message: str):
        st.error(message, icon="🚨")

    @staticmethod
    def display_warning(message: str):
        st.warning(message, icon="⚠️")

    @staticmethod
    def display_success(message: str):
        st.success(message, icon="✅")

    @staticmethod
    def check_required_data(data: Dict[str, Any], required_keys: list) -> tuple[bool, Optional[str]]:
        missing = []
        for key in required_keys:
            if data.get(key) is None or len(data[key]) == 0:
                missing.append(key)

        if missing:
            missing_labels = [ErrorHandler.get_file_label(m) for m in missing]
            return False, f"缺少必要数据：{', '.join(missing_labels)}，请先上传这些数据文件"
        return True, None

    @staticmethod
    def safe_dataframe_operation(func, *args, **kwargs):
        try:
            return func(*args, **kwargs)
        except KeyError as e:
            raise Exception(f"缺少必要的字段: {e}")
        except ValueError as e:
            raise Exception(f"数据值错误: {e}")
        except TypeError as e:
            raise Exception(f"数据类型错误: {e}")
        except Exception as e:
            raise Exception(f"数据处理错误: {e}")
