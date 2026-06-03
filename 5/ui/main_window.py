from datetime import datetime

from PySide6.QtWidgets import (
    QMainWindow, QWidget, QVBoxLayout, QHBoxLayout, QPushButton,
    QLineEdit, QComboBox, QDateEdit, QTableWidget, QTableWidgetItem,
    QHeaderView, QMessageBox, QTabWidget, QSplitter, QGroupBox,
    QLabel, QFrame, QDialog, QTextEdit, QFileDialog, QSpinBox,
    QCheckBox, QListWidget, QListWidgetItem, QProgressDialog,
    QAbstractItemView
)
from PySide6.QtCore import Qt, QDate, Signal
from PySide6.QtGui import QColor, QFont

from config.settings import SAMPLE_STATUS, TESTERS, DEPARTMENTS, TEST_ITEMS
from services.sample_service import SampleService
from services.test_record_service import TestRecordService
from services.attachment_service import AttachmentService
from services.audit_service import AuditService
from models.sample import Sample
from models.test_record import TestRecord
from reports.report_generator import ReportGenerator
from backup.backup_manager import BackupManager


class MainWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.sample_service = SampleService()
        self.test_record_service = TestRecordService()
        self.attachment_service = AttachmentService()
        self.audit_service = AuditService()
        self.report_generator = ReportGenerator()
        self.backup_manager = BackupManager()
        self.current_sample_id = None
        self.init_ui()
        self.load_samples()
        self.update_statistics()

    def init_ui(self):
        self.setWindowTitle("实验室样品登记与检测系统")
        self.setMinimumSize(1200, 800)

        central_widget = QWidget()
        self.setCentralWidget(central_widget)

        main_layout = QVBoxLayout(central_widget)

        top_bar = QHBoxLayout()

        title_label = QLabel("实验室样品登记与检测系统")
        title_font = QFont()
        title_font.setPointSize(16)
        title_font.setBold(True)
        title_label.setFont(title_font)
        top_bar.addWidget(title_label)

        top_bar.addStretch()

        btn_backup = QPushButton("数据备份")
        btn_backup.clicked.connect(self.show_backup_dialog)
        top_bar.addWidget(btn_backup)

        btn_refresh = QPushButton("刷新数据")
        btn_refresh.clicked.connect(self.refresh_all)
        top_bar.addWidget(btn_refresh)

        main_layout.addLayout(top_bar)

        self.tabs = QTabWidget()
        main_layout.addWidget(self.tabs)

        self.sample_list_tab = QWidget()
        self.tabs.addTab(self.sample_list_tab, "样品管理")
        self.setup_sample_list_tab()

        self.statistics_tab = QWidget()
        self.tabs.addTab(self.statistics_tab, "统计概览")
        self.setup_statistics_tab()

        self.audit_tab = QWidget()
        self.tabs.addTab(self.audit_tab, "操作日志")
        self.setup_audit_tab()

    def setup_sample_list_tab(self):
        layout = QVBoxLayout(self.sample_list_tab)

        search_group = QGroupBox("搜索筛选")
        search_layout = QHBoxLayout(search_group)

        search_layout.addWidget(QLabel("关键词:"))
        self.search_keyword = QLineEdit()
        self.search_keyword.setPlaceholderText("样品编号/名称/来源单位")
        search_layout.addWidget(self.search_keyword)

        search_layout.addWidget(QLabel("状态:"))
        self.search_status = QComboBox()
        self.search_status.addItem("全部", "")
        for status in SAMPLE_STATUS:
            self.search_status.addItem(status, status)
        search_layout.addWidget(self.search_status)

        search_layout.addWidget(QLabel("开始日期:"))
        self.search_start_date = QDateEdit()
        self.search_start_date.setDisplayFormat("yyyy-MM-dd")
        self.search_start_date.setCalendarPopup(True)
        self.search_start_date.setDate(QDate.currentDate().addDays(-30))
        search_layout.addWidget(self.search_start_date)

        search_layout.addWidget(QLabel("结束日期:"))
        self.search_end_date = QDateEdit()
        self.search_end_date.setDisplayFormat("yyyy-MM-dd")
        self.search_end_date.setCalendarPopup(True)
        self.search_end_date.setDate(QDate.currentDate())
        search_layout.addWidget(self.search_end_date)

        btn_search = QPushButton("搜索")
        btn_search.clicked.connect(self.load_samples)
        search_layout.addWidget(btn_search)

        btn_reset = QPushButton("重置")
        btn_reset.clicked.connect(self.reset_search)
        search_layout.addWidget(btn_reset)

        layout.addWidget(search_group)

        splitter = QSplitter(Qt.Horizontal)
        layout.addWidget(splitter)

        left_widget = QWidget()
        left_layout = QVBoxLayout(left_widget)

        btn_layout = QHBoxLayout()
        btn_add = QPushButton("新增样品")
        btn_add.clicked.connect(self.add_sample)
        btn_layout.addWidget(btn_add)

        btn_edit = QPushButton("编辑样品")
        btn_edit.clicked.connect(self.edit_sample)
        btn_layout.addWidget(btn_edit)

        btn_delete = QPushButton("删除样品")
        btn_delete.clicked.connect(self.delete_sample)
        btn_layout.addWidget(btn_delete)

        left_layout.addLayout(btn_layout)

        self.sample_table = QTableWidget()
        self.sample_table.setColumnCount(7)
        self.sample_table.setHorizontalHeaderLabels([
            "ID", "样品编号", "样品名称", "来源单位", "接收时间", "状态", "检测项目"
        ])
        self.sample_table.horizontalHeader().setSectionResizeMode(QHeaderView.Stretch)
        self.sample_table.setSelectionBehavior(QAbstractItemView.SelectRows)
        self.sample_table.setSelectionMode(QAbstractItemView.SingleSelection)
        self.sample_table.itemSelectionChanged.connect(self.on_sample_selected)
        left_layout.addWidget(self.sample_table)

        splitter.addWidget(left_widget)

        right_widget = QWidget()
        right_layout = QVBoxLayout(right_widget)

        self.detail_group = QGroupBox("样品详情")
        self.detail_layout = QVBoxLayout(self.detail_group)
        self.setup_detail_panel()
        right_layout.addWidget(self.detail_group)

        splitter.addWidget(right_widget)
        splitter.setSizes([700, 500])

    def setup_detail_panel(self):
        self.detail_info = QLabel("请选择一个样品查看详情")
        self.detail_info.setAlignment(Qt.AlignCenter)
        self.detail_layout.addWidget(self.detail_info)

        btn_detail_layout = QHBoxLayout()

        btn_test_record = QPushButton("检测记录")
        btn_test_record.clicked.connect(self.show_test_records)
        btn_detail_layout.addWidget(btn_test_record)

        btn_attachment = QPushButton("附件管理")
        btn_attachment.clicked.connect(self.show_attachments)
        btn_detail_layout.addWidget(btn_attachment)

        btn_report = QPushButton("生成报告")
        btn_report.clicked.connect(self.generate_report)
        btn_detail_layout.addWidget(btn_report)

        btn_status = QPushButton("状态流转")
        btn_status.clicked.connect(self.change_status)
        btn_detail_layout.addWidget(btn_status)

        self.detail_layout.addLayout(btn_detail_layout)

        self.test_record_table = QTableWidget()
        self.test_record_table.setColumnCount(6)
        self.test_record_table.setHorizontalHeaderLabels([
            "检测项目", "检测人员", "检测时间", "检测结果", "结果值", "是否合格"
        ])
        self.test_record_table.horizontalHeader().setSectionResizeMode(QHeaderView.Stretch)
        self.detail_layout.addWidget(self.test_record_table)

    def setup_statistics_tab(self):
        layout = QVBoxLayout(self.statistics_tab)

        cards_layout = QHBoxLayout()

        self.stat_total = self.create_stat_card("样品总数", "0")
        cards_layout.addWidget(self.stat_total)

        self.stat_pending = self.create_stat_card("待检测", "0")
        cards_layout.addWidget(self.stat_pending)

        self.stat_testing = self.create_stat_card("检测中", "0")
        cards_layout.addWidget(self.stat_testing)

        self.stat_completed = self.create_stat_card("检测完成", "0")
        cards_layout.addWidget(self.stat_completed)

        self.stat_today = self.create_stat_card("今日新增", "0")
        cards_layout.addWidget(self.stat_today)

        layout.addLayout(cards_layout)

        status_group = QGroupBox("按状态分布")
        status_layout = QVBoxLayout(status_group)
        self.status_list = QListWidget()
        status_layout.addWidget(self.status_list)
        layout.addWidget(status_group)

    def create_stat_card(self, title: str, value: str) -> QFrame:
        card = QFrame()
        card.setFrameShape(QFrame.StyledPanel)
        card.setStyleSheet("""
            QFrame {
                background-color: #f0f0f0;
                border-radius: 8px;
                padding: 16px;
            }
        """)
        card_layout = QVBoxLayout(card)

        title_label = QLabel(title)
        title_label.setAlignment(Qt.AlignCenter)
        card_layout.addWidget(title_label)

        value_label = QLabel(value)
        value_font = QFont()
        value_font.setPointSize(24)
        value_font.setBold(True)
        value_label.setFont(value_font)
        value_label.setAlignment(Qt.AlignCenter)
        value_label.setObjectName("stat_value")
        card_layout.addWidget(value_label)

        return card

    def setup_audit_tab(self):
        layout = QVBoxLayout(self.audit_tab)

        filter_layout = QHBoxLayout()
        filter_layout.addWidget(QLabel("操作类型:"))
        self.audit_operation = QComboBox()
        self.audit_operation.addItem("全部", "")
        self.audit_operation.addItem("新增", "新增")
        self.audit_operation.addItem("修改", "修改")
        self.audit_operation.addItem("删除", "删除")
        self.audit_operation.addItem("状态变更", "状态变更")
        filter_layout.addWidget(self.audit_operation)

        filter_layout.addWidget(QLabel("操作人:"))
        self.audit_operator = QLineEdit()
        self.audit_operator.setPlaceholderText("输入操作人姓名")
        filter_layout.addWidget(self.audit_operator)

        btn_load_audit = QPushButton("查询")
        btn_load_audit.clicked.connect(self.load_audit_logs)
        filter_layout.addWidget(btn_load_audit)

        layout.addLayout(filter_layout)

        self.audit_table = QTableWidget()
        self.audit_table.setColumnCount(7)
        self.audit_table.setHorizontalHeaderLabels([
            "ID", "样品ID", "操作类型", "字段", "旧值", "新值", "操作人", "操作时间", "备注"
        ])
        self.audit_table.setColumnCount(9)
        self.audit_table.setHorizontalHeaderLabels([
            "ID", "样品ID", "操作类型", "字段", "旧值", "新值", "操作人", "操作时间", "备注"
        ])
        self.audit_table.horizontalHeader().setSectionResizeMode(QHeaderView.Stretch)
        layout.addWidget(self.audit_table)

    def load_samples(self):
        keyword = self.search_keyword.text().strip()
        status = self.search_status.currentData()
        start_date = self.search_start_date.date().toString("yyyy-MM-dd")
        end_date = self.search_end_date.date().toString("yyyy-MM-dd")

        samples = self.sample_service.get_all_samples(
            keyword=keyword,
            status=status,
            start_date=start_date,
            end_date=end_date
        )

        self.sample_table.setRowCount(len(samples))
        for row, sample in enumerate(samples):
            self.sample_table.setItem(row, 0, QTableWidgetItem(str(sample.id)))
            self.sample_table.setItem(row, 1, QTableWidgetItem(sample.sample_no))
            self.sample_table.setItem(row, 2, QTableWidgetItem(sample.sample_name))
            self.sample_table.setItem(row, 3, QTableWidgetItem(sample.source_unit))
            self.sample_table.setItem(row, 4, QTableWidgetItem(sample.receive_time))

            status_item = QTableWidgetItem(sample.status)
            if sample.status == "待检测":
                status_item.setBackground(QColor("#FFF3CD"))
            elif sample.status == "检测中":
                status_item.setBackground(QColor("#CCE5FF"))
            elif sample.status == "检测完成":
                status_item.setBackground(QColor("#D4EDDA"))
            elif sample.status == "报告已生成":
                status_item.setBackground(QColor("#E2E3E5"))
            elif sample.status == "已归档":
                status_item.setBackground(QColor("#F8D7DA"))
            self.sample_table.setItem(row, 5, status_item)

            self.sample_table.setItem(row, 6, QTableWidgetItem(sample.test_items))

    def reset_search(self):
        self.search_keyword.clear()
        self.search_status.setCurrentIndex(0)
        self.search_start_date.setDate(QDate.currentDate().addDays(-30))
        self.search_end_date.setDate(QDate.currentDate())
        self.load_samples()

    def on_sample_selected(self):
        selected_items = self.sample_table.selectedItems()
        if not selected_items:
            self.current_sample_id = None
            return

        row = selected_items[0].row()
        sample_id = int(self.sample_table.item(row, 0).text())
        self.current_sample_id = sample_id

        sample = self.sample_service.get_sample_by_id(sample_id)
        if sample:
            self.detail_info.setText(
                f"样品编号: {sample.sample_no}\n"
                f"样品名称: {sample.sample_name}\n"
                f"来源单位: {sample.source_unit}\n"
                f"送检人: {sample.sender}\n"
                f"接收人: {sample.receiver}\n"
                f"接收时间: {sample.receive_time}\n"
                f"检测项目: {sample.test_items}\n"
                f"状态: {sample.status}\n"
                f"备注: {sample.description}"
            )
            self.load_test_records(sample_id)

    def load_test_records(self, sample_id: int):
        records = self.test_record_service.get_test_records_by_sample_id(sample_id)
        self.test_record_table.setRowCount(len(records))
        for row, record in enumerate(records):
            self.test_record_table.setItem(row, 0, QTableWidgetItem(record.test_item))
            self.test_record_table.setItem(row, 1, QTableWidgetItem(record.tester))
            self.test_record_table.setItem(row, 2, QTableWidgetItem(record.test_time))
            self.test_record_table.setItem(row, 3, QTableWidgetItem(record.test_result))
            self.test_record_table.setItem(row, 4, QTableWidgetItem(record.result_value))

            qualified_item = QTableWidgetItem("合格" if record.is_qualified else "不合格")
            if not record.is_qualified:
                qualified_item.setBackground(QColor("#F8D7DA"))
            self.test_record_table.setItem(row, 5, qualified_item)

    def update_statistics(self):
        stats = self.sample_service.get_statistics()

        self.stat_total.findChild(QLabel, "stat_value").setText(str(stats['total']))
        self.stat_pending.findChild(QLabel, "stat_value").setText(str(stats['待检测']))
        self.stat_testing.findChild(QLabel, "stat_value").setText(str(stats['检测中']))
        self.stat_completed.findChild(QLabel, "stat_value").setText(str(stats['检测完成']))
        self.stat_today.findChild(QLabel, "stat_value").setText(str(stats['today']))

        self.status_list.clear()
        for status in SAMPLE_STATUS:
            count = stats.get(status, 0)
            item = QListWidgetItem(f"{status}: {count} 个")
            self.status_list.addItem(item)

    def load_audit_logs(self):
        operation = self.audit_operation.currentData()
        operator = self.audit_operator.text().strip()

        logs = self.audit_service.get_all_audit_logs(
            operation_type=operation,
            operator=operator
        )

        self.audit_table.setRowCount(len(logs))
        for row, log in enumerate(logs):
            self.audit_table.setItem(row, 0, QTableWidgetItem(str(log.id)))
            self.audit_table.setItem(row, 1, QTableWidgetItem(str(log.sample_id) if log.sample_id else ""))
            self.audit_table.setItem(row, 2, QTableWidgetItem(log.operation_type))
            self.audit_table.setItem(row, 3, QTableWidgetItem(log.field_name))
            self.audit_table.setItem(row, 4, QTableWidgetItem(log.old_value))
            self.audit_table.setItem(row, 5, QTableWidgetItem(log.new_value))
            self.audit_table.setItem(row, 6, QTableWidgetItem(log.operator))
            self.audit_table.setItem(row, 7, QTableWidgetItem(log.operation_time))
            self.audit_table.setItem(row, 8, QTableWidgetItem(log.remarks))

    def add_sample(self):
        dialog = SampleDialog(self)
        if dialog.exec() == QDialog.Accepted:
            sample = dialog.get_sample()
            try:
                self.sample_service.create_sample(sample, operator="当前用户")
                self.load_samples()
                self.update_statistics()
                QMessageBox.information(self, "成功", "样品添加成功")
            except ValueError as e:
                QMessageBox.warning(self, "错误", str(e))

    def edit_sample(self):
        if not self.current_sample_id:
            QMessageBox.warning(self, "提示", "请先选择一个样品")
            return

        sample = self.sample_service.get_sample_by_id(self.current_sample_id)
        if not sample:
            return

        dialog = SampleDialog(self, sample)
        if dialog.exec() == QDialog.Accepted:
            updated_sample = dialog.get_sample()
            updated_sample.id = self.current_sample_id
            try:
                self.sample_service.update_sample(updated_sample, operator="当前用户")
                self.load_samples()
                self.update_statistics()
                QMessageBox.information(self, "成功", "样品更新成功")
            except ValueError as e:
                QMessageBox.warning(self, "错误", str(e))

    def delete_sample(self):
        if not self.current_sample_id:
            QMessageBox.warning(self, "提示", "请先选择一个样品")
            return

        sample = self.sample_service.get_sample_by_id(self.current_sample_id)
        if not sample:
            return

        reply = QMessageBox.question(
            self,
            "确认删除",
            f"确定要删除样品 '{sample.sample_no}' 吗？\n此操作将同时删除相关的检测记录和附件。",
            QMessageBox.Yes | QMessageBox.No,
            QMessageBox.No
        )

        if reply == QMessageBox.Yes:
            try:
                self.sample_service.delete_sample(self.current_sample_id, operator="当前用户")
                self.current_sample_id = None
                self.load_samples()
                self.update_statistics()
                self.detail_info.setText("请选择一个样品查看详情")
                self.test_record_table.setRowCount(0)
                QMessageBox.information(self, "成功", "样品删除成功")
            except Exception as e:
                QMessageBox.warning(self, "错误", str(e))

    def show_test_records(self):
        if not self.current_sample_id:
            QMessageBox.warning(self, "提示", "请先选择一个样品")
            return

        dialog = TestRecordDialog(self, self.current_sample_id)
        if dialog.exec() == QDialog.Accepted:
            self.load_test_records(self.current_sample_id)

    def show_attachments(self):
        if not self.current_sample_id:
            QMessageBox.warning(self, "提示", "请先选择一个样品")
            return

        dialog = AttachmentDialog(self, self.current_sample_id)
        dialog.exec()

    def generate_report(self):
        if not self.current_sample_id:
            QMessageBox.warning(self, "提示", "请先选择一个样品")
            return

        sample = self.sample_service.get_sample_by_id(self.current_sample_id)
        if not sample:
            return

        can_generate, message = self.test_record_service.can_generate_report(self.current_sample_id)
        if not can_generate:
            QMessageBox.warning(self, "提示", f"无法生成报告: {message}")
            return

        reply = QMessageBox.question(
            self,
            "生成报告",
            "选择报告格式:\nYes: Excel\nNo: PDF",
            QMessageBox.Yes | QMessageBox.No | QMessageBox.Cancel
        )

        if reply == QMessageBox.Cancel:
            return

        try:
            if reply == QMessageBox.Yes:
                file_path = self.report_generator.generate_excel_report(self.current_sample_id)
            else:
                file_path = self.report_generator.generate_pdf_report(self.current_sample_id)

            self.load_samples()
            self.update_statistics()

            open_reply = QMessageBox.question(
                self,
                "报告生成成功",
                f"报告已保存至:\n{file_path}\n\n是否打开文件?",
                QMessageBox.Yes | QMessageBox.No
            )

            if open_reply == QMessageBox.Yes:
                import os
                os.startfile(file_path)

        except Exception as e:
            QMessageBox.warning(self, "错误", f"生成报告失败: {str(e)}")

    def change_status(self):
        if not self.current_sample_id:
            QMessageBox.warning(self, "提示", "请先选择一个样品")
            return

        sample = self.sample_service.get_sample_by_id(self.current_sample_id)
        if not sample:
            return

        dialog = StatusChangeDialog(self, sample.status)
        if dialog.exec() == QDialog.Accepted:
            new_status = dialog.get_new_status()
            try:
                self.sample_service.update_status(
                    self.current_sample_id,
                    new_status,
                    operator="当前用户"
                )
                self.load_samples()
                self.update_statistics()
                self.on_sample_selected()
                QMessageBox.information(self, "成功", "状态更新成功")
            except Exception as e:
                QMessageBox.warning(self, "错误", str(e))

    def show_backup_dialog(self):
        dialog = BackupDialog(self)
        dialog.exec()

    def refresh_all(self):
        self.load_samples()
        self.update_statistics()
        self.load_audit_logs()
        QMessageBox.information(self, "成功", "数据已刷新")


class SampleDialog(QDialog):
    def __init__(self, parent=None, sample: Sample = None):
        super().__init__(parent)
        self.sample = sample
        self.setWindowTitle("样品信息" if sample else "新增样品")
        self.setMinimumWidth(500)
        self.init_ui()
        if sample:
            self.load_sample_data()

    def init_ui(self):
        layout = QVBoxLayout(self)

        form_layout = QVBoxLayout()

        layout.addWidget(QLabel("样品编号:"))
        self.sample_no = QLineEdit()
        self.sample_no.setPlaceholderText("例如: YP202401001")
        layout.addWidget(self.sample_no)

        layout.addWidget(QLabel("样品名称:"))
        self.sample_name = QLineEdit()
        layout.addWidget(self.sample_name)

        layout.addWidget(QLabel("来源单位:"))
        self.source_unit = QComboBox()
        self.source_unit.addItems(DEPARTMENTS)
        self.source_unit.setEditable(True)
        layout.addWidget(self.source_unit)

        layout.addWidget(QLabel("送检人:"))
        self.sender = QLineEdit()
        layout.addWidget(self.sender)

        layout.addWidget(QLabel("接收人:"))
        self.receiver = QComboBox()
        self.receiver.addItems(TESTERS)
        self.receiver.setEditable(True)
        layout.addWidget(self.receiver)

        layout.addWidget(QLabel("接收时间:"))
        self.receive_time = QDateEdit()
        self.receive_time.setDisplayFormat("yyyy-MM-dd HH:mm:ss")
        self.receive_time.setCalendarPopup(True)
        self.receive_time.setDateTime(datetime.now())
        layout.addWidget(self.receive_time)

        layout.addWidget(QLabel("检测项目:"))
        self.test_items = QLineEdit()
        self.test_items.setPlaceholderText("多个项目用逗号分隔")
        layout.addWidget(self.test_items)

        layout.addWidget(QLabel("状态:"))
        self.status = QComboBox()
        self.status.addItems(SAMPLE_STATUS)
        layout.addWidget(self.status)

        layout.addWidget(QLabel("备注:"))
        self.description = QTextEdit()
        self.description.setMaximumHeight(100)
        layout.addWidget(self.description)

        btn_layout = QHBoxLayout()
        btn_ok = QPushButton("确定")
        btn_ok.clicked.connect(self.accept)
        btn_layout.addWidget(btn_ok)

        btn_cancel = QPushButton("取消")
        btn_cancel.clicked.connect(self.reject)
        btn_layout.addWidget(btn_cancel)

        layout.addLayout(btn_layout)

    def load_sample_data(self):
        self.sample_no.setText(self.sample.sample_no)
        self.sample_name.setText(self.sample.sample_name)
        self.source_unit.setCurrentText(self.sample.source_unit)
        self.sender.setText(self.sample.sender)
        self.receiver.setCurrentText(self.sample.receiver)
        self.test_items.setText(self.sample.test_items)
        self.status.setCurrentText(self.sample.status)
        self.description.setPlainText(self.sample.description)

    def get_sample(self) -> Sample:
        return Sample(
            sample_no=self.sample_no.text().strip(),
            sample_name=self.sample_name.text().strip(),
            source_unit=self.source_unit.currentText().strip(),
            sender=self.sender.text().strip(),
            receiver=self.receiver.currentText().strip(),
            receive_time=self.receive_time.dateTime().toString("yyyy-MM-dd HH:mm:ss"),
            test_items=self.test_items.text().strip(),
            status=self.status.currentText(),
            description=self.description.toPlainText().strip()
        )


class TestRecordDialog(QDialog):
    def __init__(self, parent=None, sample_id: int = 0):
        super().__init__(parent)
        self.sample_id = sample_id
        self.test_record_service = TestRecordService()
        self.setWindowTitle("检测记录")
        self.setMinimumSize(800, 600)
        self.init_ui()
        self.load_records()

    def init_ui(self):
        layout = QVBoxLayout(self)

        btn_layout = QHBoxLayout()
        btn_add = QPushButton("添加记录")
        btn_add.clicked.connect(self.add_record)
        btn_layout.addWidget(btn_add)

        btn_edit = QPushButton("编辑记录")
        btn_edit.clicked.connect(self.edit_record)
        btn_layout.addWidget(btn_edit)

        btn_delete = QPushButton("删除记录")
        btn_delete.clicked.connect(self.delete_record)
        btn_layout.addWidget(btn_delete)

        btn_layout.addStretch()
        btn_close = QPushButton("关闭")
        btn_close.clicked.connect(self.accept)
        btn_layout.addWidget(btn_close)

        layout.addLayout(btn_layout)

        self.table = QTableWidget()
        self.table.setColumnCount(8)
        self.table.setHorizontalHeaderLabels([
            "ID", "检测项目", "检测人员", "检测时间", "检测结果", "结果值", "标准值", "是否合格"
        ])
        self.table.horizontalHeader().setSectionResizeMode(QHeaderView.Stretch)
        self.table.setSelectionBehavior(QAbstractItemView.SelectRows)
        layout.addWidget(self.table)

    def load_records(self):
        records = self.test_record_service.get_test_records_by_sample_id(self.sample_id)
        self.table.setRowCount(len(records))
        for row, record in enumerate(records):
            self.table.setItem(row, 0, QTableWidgetItem(str(record.id)))
            self.table.setItem(row, 1, QTableWidgetItem(record.test_item))
            self.table.setItem(row, 2, QTableWidgetItem(record.tester))
            self.table.setItem(row, 3, QTableWidgetItem(record.test_time))
            self.table.setItem(row, 4, QTableWidgetItem(record.test_result))
            self.table.setItem(row, 5, QTableWidgetItem(record.result_value))
            self.table.setItem(row, 6, QTableWidgetItem(record.standard_value))

            qualified_item = QTableWidgetItem("合格" if record.is_qualified else "不合格")
            if not record.is_qualified:
                qualified_item.setBackground(QColor("#F8D7DA"))
            self.table.setItem(row, 7, qualified_item)

    def add_record(self):
        dialog = TestRecordEditDialog(self)
        if dialog.exec() == QDialog.Accepted:
            record = dialog.get_record()
            record.sample_id = self.sample_id
            try:
                self.test_record_service.create_test_record(record)
                self.load_records()
            except ValueError as e:
                QMessageBox.warning(self, "错误", str(e))

    def edit_record(self):
        selected_items = self.table.selectedItems()
        if not selected_items:
            QMessageBox.warning(self, "提示", "请先选择一条记录")
            return

        row = selected_items[0].row()
        record_id = int(self.table.item(row, 0).text())
        record = self.test_record_service.get_test_record_by_id(record_id)
        if not record:
            return

        dialog = TestRecordEditDialog(self, record)
        if dialog.exec() == QDialog.Accepted:
            updated_record = dialog.get_record()
            updated_record.id = record_id
            updated_record.sample_id = self.sample_id
            try:
                self.test_record_service.update_test_record(updated_record)
                self.load_records()
            except ValueError as e:
                QMessageBox.warning(self, "错误", str(e))

    def delete_record(self):
        selected_items = self.table.selectedItems()
        if not selected_items:
            QMessageBox.warning(self, "提示", "请先选择一条记录")
            return

        row = selected_items[0].row()
        record_id = int(self.table.item(row, 0).text())

        reply = QMessageBox.question(
            self,
            "确认删除",
            "确定要删除这条检测记录吗？",
            QMessageBox.Yes | QMessageBox.No,
            QMessageBox.No
        )

        if reply == QMessageBox.Yes:
            self.test_record_service.delete_test_record(record_id)
            self.load_records()


class TestRecordEditDialog(QDialog):
    def __init__(self, parent=None, record: TestRecord = None):
        super().__init__(parent)
        self.record = record
        self.setWindowTitle("检测记录" if record else "添加检测记录")
        self.setMinimumWidth(400)
        self.init_ui()
        if record:
            self.load_record_data()

    def init_ui(self):
        layout = QVBoxLayout(self)

        layout.addWidget(QLabel("检测项目:"))
        self.test_item = QComboBox()
        self.test_item.addItems(TEST_ITEMS)
        self.test_item.setEditable(True)
        layout.addWidget(self.test_item)

        layout.addWidget(QLabel("检测人员:"))
        self.tester = QComboBox()
        self.tester.addItems(TESTERS)
        self.tester.setEditable(True)
        layout.addWidget(self.tester)

        layout.addWidget(QLabel("检测时间:"))
        self.test_time = QDateEdit()
        self.test_time.setDisplayFormat("yyyy-MM-dd HH:mm:ss")
        self.test_time.setCalendarPopup(True)
        self.test_time.setDateTime(datetime.now())
        layout.addWidget(self.test_time)

        layout.addWidget(QLabel("检测结果:"))
        self.test_result = QComboBox()
        self.test_result.addItems(["", "合格", "不合格", "检测中"])
        layout.addWidget(self.test_result)

        layout.addWidget(QLabel("结果值:"))
        self.result_value = QLineEdit()
        layout.addWidget(self.result_value)

        layout.addWidget(QLabel("标准值:"))
        self.standard_value = QLineEdit()
        layout.addWidget(self.standard_value)

        layout.addWidget(QLabel("是否合格:"))
        self.is_qualified = QCheckBox()
        self.is_qualified.setChecked(True)
        layout.addWidget(self.is_qualified)

        layout.addWidget(QLabel("备注:"))
        self.remarks = QTextEdit()
        self.remarks.setMaximumHeight(80)
        layout.addWidget(self.remarks)

        btn_layout = QHBoxLayout()
        btn_ok = QPushButton("确定")
        btn_ok.clicked.connect(self.accept)
        btn_layout.addWidget(btn_ok)

        btn_cancel = QPushButton("取消")
        btn_cancel.clicked.connect(self.reject)
        btn_layout.addWidget(btn_cancel)

        layout.addLayout(btn_layout)

    def load_record_data(self):
        self.test_item.setCurrentText(self.record.test_item)
        self.tester.setCurrentText(self.record.tester)
        self.test_result.setCurrentText(self.record.test_result)
        self.result_value.setText(self.record.result_value)
        self.standard_value.setText(self.record.standard_value)
        self.is_qualified.setChecked(self.record.is_qualified)
        self.remarks.setPlainText(self.record.remarks)

    def get_record(self) -> TestRecord:
        return TestRecord(
            test_item=self.test_item.currentText().strip(),
            tester=self.tester.currentText().strip(),
            test_time=self.test_time.dateTime().toString("yyyy-MM-dd HH:mm:ss"),
            test_result=self.test_result.currentText().strip(),
            result_value=self.result_value.text().strip(),
            standard_value=self.standard_value.text().strip(),
            is_qualified=self.is_qualified.isChecked(),
            remarks=self.remarks.toPlainText().strip()
        )


class AttachmentDialog(QDialog):
    def __init__(self, parent=None, sample_id: int = 0):
        super().__init__(parent)
        self.sample_id = sample_id
        self.attachment_service = AttachmentService()
        self.setWindowTitle("附件管理")
        self.setMinimumSize(700, 500)
        self.init_ui()
        self.load_attachments()

    def init_ui(self):
        layout = QVBoxLayout(self)

        btn_layout = QHBoxLayout()
        btn_upload = QPushButton("上传附件")
        btn_upload.clicked.connect(self.upload_attachment)
        btn_layout.addWidget(btn_upload)

        btn_download = QPushButton("下载附件")
        btn_download.clicked.connect(self.download_attachment)
        btn_layout.addWidget(btn_download)

        btn_delete = QPushButton("删除附件")
        btn_delete.clicked.connect(self.delete_attachment)
        btn_layout.addWidget(btn_delete)

        btn_layout.addStretch()
        btn_close = QPushButton("关闭")
        btn_close.clicked.connect(self.accept)
        btn_layout.addWidget(btn_close)

        layout.addLayout(btn_layout)

        self.list_widget = QListWidget()
        layout.addWidget(self.list_widget)

    def load_attachments(self):
        self.list_widget.clear()
        existing, missing = self.attachment_service.check_attachments_exist(self.sample_id)

        for att in existing:
            item = QListWidgetItem(
                f"{att.file_name} ({att.format_file_size()}) - 上传于 {att.uploaded_at}"
            )
            item.setData(Qt.UserRole, att)
            self.list_widget.addItem(item)

        for att in missing:
            item = QListWidgetItem(
                f"[文件丢失] {att.file_name} - 上传于 {att.uploaded_at}"
            )
            item.setData(Qt.UserRole, att)
            item.setForeground(QColor("#FF0000"))
            self.list_widget.addItem(item)

        if missing:
            QMessageBox.warning(
                self,
                "附件丢失提示",
                f"发现 {len(missing)} 个附件文件已丢失，请检查文件存储位置。"
            )

    def upload_attachment(self):
        file_path, _ = QFileDialog.getOpenFileName(
            self,
            "选择文件",
            "",
            "所有文件 (*.*)"
        )

        if file_path:
            try:
                self.attachment_service.upload_attachment(
                    self.sample_id,
                    file_path,
                    uploaded_by="当前用户"
                )
                self.load_attachments()
                QMessageBox.information(self, "成功", "附件上传成功")
            except Exception as e:
                QMessageBox.warning(self, "错误", str(e))

    def download_attachment(self):
        current_item = self.list_widget.currentItem()
        if not current_item:
            QMessageBox.warning(self, "提示", "请先选择一个附件")
            return

        attachment = current_item.data(Qt.UserRole)
        if not attachment.file_exists():
            QMessageBox.warning(self, "错误", "文件已丢失，无法下载")
            return

        save_path, _ = QFileDialog.getSaveFileName(
            self,
            "保存文件",
            attachment.file_name,
            "所有文件 (*.*)"
        )

        if save_path:
            import shutil
            shutil.copy2(attachment.file_path, save_path)
            QMessageBox.information(self, "成功", "文件已保存")

    def delete_attachment(self):
        current_item = self.list_widget.currentItem()
        if not current_item:
            QMessageBox.warning(self, "提示", "请先选择一个附件")
            return

        attachment = current_item.data(Qt.UserRole)

        reply = QMessageBox.question(
            self,
            "确认删除",
            f"确定要删除附件 '{attachment.file_name}' 吗？",
            QMessageBox.Yes | QMessageBox.No,
            QMessageBox.No
        )

        if reply == QMessageBox.Yes:
            self.attachment_service.delete_attachment(attachment.id)
            self.load_attachments()


class StatusChangeDialog(QDialog):
    def __init__(self, parent=None, current_status: str = ""):
        super().__init__(parent)
        self.current_status = current_status
        self.setWindowTitle("状态流转")
        self.setMinimumWidth(300)
        self.init_ui()

    def init_ui(self):
        layout = QVBoxLayout(self)

        layout.addWidget(QLabel(f"当前状态: {self.current_status}"))
        layout.addWidget(QLabel("选择新状态:"))

        self.status_combo = QComboBox()
        self.status_combo.addItems(SAMPLE_STATUS)
        self.status_combo.setCurrentText(self.current_status)
        layout.addWidget(self.status_combo)

        btn_layout = QHBoxLayout()
        btn_ok = QPushButton("确定")
        btn_ok.clicked.connect(self.accept)
        btn_layout.addWidget(btn_ok)

        btn_cancel = QPushButton("取消")
        btn_cancel.clicked.connect(self.reject)
        btn_layout.addWidget(btn_cancel)

        layout.addLayout(btn_layout)

    def get_new_status(self) -> str:
        return self.status_combo.currentText()


class BackupDialog(QDialog):
    def __init__(self, parent=None):
        super().__init__(parent)
        self.backup_manager = BackupManager()
        self.setWindowTitle("数据备份")
        self.setMinimumSize(600, 400)
        self.init_ui()
        self.load_backups()

    def init_ui(self):
        layout = QVBoxLayout(self)

        btn_layout = QHBoxLayout()
        btn_create = QPushButton("创建备份")
        btn_create.clicked.connect(self.create_backup)
        btn_layout.addWidget(btn_create)

        self.include_attachments = QCheckBox("包含附件")
        btn_layout.addWidget(self.include_attachments)

        btn_restore = QPushButton("恢复备份")
        btn_restore.clicked.connect(self.restore_backup)
        btn_layout.addWidget(btn_restore)

        btn_delete = QPushButton("删除备份")
        btn_delete.clicked.connect(self.delete_backup)
        btn_layout.addWidget(btn_delete)

        btn_layout.addStretch()
        btn_close = QPushButton("关闭")
        btn_close.clicked.connect(self.accept)
        btn_layout.addWidget(btn_close)

        layout.addLayout(btn_layout)

        self.backup_list = QListWidget()
        layout.addWidget(self.backup_list)

    def load_backups(self):
        self.backup_list.clear()
        backups = self.backup_manager.list_backups()

        for backup in backups:
            size_str = self.backup_manager.format_size(backup['size'])
            has_att = " (含附件)" if backup['has_attachments'] else ""
            item = QListWidgetItem(
                f"{backup['time']} - {size_str}{has_att}"
            )
            item.setData(Qt.UserRole, backup)
            self.backup_list.addItem(item)

    def create_backup(self):
        try:
            include_att = self.include_attachments.isChecked()
            backup_path = self.backup_manager.create_backup(include_att)
            self.load_backups()
            QMessageBox.information(self, "成功", f"备份创建成功:\n{backup_path}")
        except Exception as e:
            QMessageBox.warning(self, "错误", str(e))

    def restore_backup(self):
        current_item = self.backup_list.currentItem()
        if not current_item:
            QMessageBox.warning(self, "提示", "请先选择一个备份")
            return

        backup = current_item.data(Qt.UserRole)

        reply = QMessageBox.question(
            self,
            "确认恢复",
            f"确定要从备份 '{backup['time']}' 恢复数据吗？\n这将覆盖当前所有数据！",
            QMessageBox.Yes | QMessageBox.No,
            QMessageBox.No
        )

        if reply == QMessageBox.Yes:
            try:
                self.backup_manager.restore_backup(backup['path'])
                QMessageBox.information(self, "成功", "数据恢复成功，请重启应用。")
                self.accept()
            except Exception as e:
                QMessageBox.warning(self, "错误", str(e))

    def delete_backup(self):
        current_item = self.backup_list.currentItem()
        if not current_item:
            QMessageBox.warning(self, "提示", "请先选择一个备份")
            return

        backup = current_item.data(Qt.UserRole)

        reply = QMessageBox.question(
            self,
            "确认删除",
            f"确定要删除备份 '{backup['time']}' 吗？",
            QMessageBox.Yes | QMessageBox.No,
            QMessageBox.No
        )

        if reply == QMessageBox.Yes:
            self.backup_manager.delete_backup(backup['path'])
            self.load_backups()
