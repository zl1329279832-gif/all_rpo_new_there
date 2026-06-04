from datetime import datetime

from PySide6.QtWidgets import (
    QMainWindow, QWidget, QVBoxLayout, QHBoxLayout, QPushButton,
    QLineEdit, QComboBox, QDateEdit, QTableWidget, QTableWidgetItem,
    QHeaderView, QMessageBox, QTabWidget, QSplitter, QGroupBox,
    QLabel, QFrame, QDialog, QTextEdit, QFileDialog, QSpinBox,
    QCheckBox, QListWidget, QListWidgetItem, QProgressDialog,
    QAbstractItemView, QScrollArea, QGridLayout, QFormLayout
)
from PySide6.QtCore import Qt, QDate
from PySide6.QtGui import QColor, QFont, QBrush

from config.settings import (
    SAMPLE_STATUS, TESTERS, DEPARTMENTS, TEST_ITEMS,
    STATUS_COLORS, STATUS_DESCRIPTIONS, STATUS_TRANSITION_RULES
)
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
        self.setWindowTitle("实验室样品登记与检测系统 v2.0")
        self.setMinimumSize(1400, 900)

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

        self.change_history_tab = QWidget()
        self.tabs.addTab(self.change_history_tab, "修改记录")
        self.setup_change_history_tab()

    def setup_sample_list_tab(self):
        layout = QVBoxLayout(self.sample_list_tab)

        search_group = QGroupBox("搜索筛选")
        search_layout = QHBoxLayout(search_group)

        search_layout.addWidget(QLabel("关键词:"))
        self.search_keyword = QLineEdit()
        self.search_keyword.setPlaceholderText("样品编号/名称/来源单位")
        self.search_keyword.setMinimumWidth(200)
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
        self.sample_table.horizontalHeader().setSectionResizeMode(0, QHeaderView.ResizeToContents)
        self.sample_table.setSelectionBehavior(QAbstractItemView.SelectRows)
        self.sample_table.setSelectionMode(QAbstractItemView.SingleSelection)
        self.sample_table.setEditTriggers(QAbstractItemView.NoEditTriggers)
        self.sample_table.setAlternatingRowColors(True)
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
        splitter.setSizes([800, 600])

    def setup_detail_panel(self):
        self.detail_info = QLabel("请选择一个样品查看详情")
        self.detail_info.setAlignment(Qt.AlignCenter)
        self.detail_info.setWordWrap(True)
        self.detail_info.setStyleSheet("padding: 10px;")
        self.detail_layout.addWidget(self.detail_info)

        btn_detail_layout = QHBoxLayout()

        btn_test_record = QPushButton("检测记录")
        btn_test_record.clicked.connect(self.show_test_records)
        btn_detail_layout.addWidget(btn_test_record)

        btn_change_history = QPushButton("修改历史")
        btn_change_history.clicked.connect(self.show_change_history)
        btn_detail_layout.addWidget(btn_change_history)

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
        self.test_record_table.setEditTriggers(QAbstractItemView.NoEditTriggers)
        self.test_record_table.setAlternatingRowColors(True)
        self.detail_layout.addWidget(self.test_record_table)

    def setup_statistics_tab(self):
        scroll = QScrollArea()
        scroll.setWidgetResizable(True)
        scroll_content = QWidget()
        layout = QVBoxLayout(scroll_content)

        cards_layout = QHBoxLayout()

        self.stat_total = self.create_stat_card("样品总数", "0", "#4472C4")
        cards_layout.addWidget(self.stat_total)

        self.stat_pending = self.create_stat_card("待检测", "0", "#FFC107")
        cards_layout.addWidget(self.stat_pending)

        self.stat_testing = self.create_stat_card("检测中", "0", "#17A2B8")
        cards_layout.addWidget(self.stat_testing)

        self.stat_completed = self.create_stat_card("检测完成", "0", "#28A745")
        cards_layout.addWidget(self.stat_completed)

        self.stat_today = self.create_stat_card("今日新增", "0", "#6F42C1")
        cards_layout.addWidget(self.stat_today)

        layout.addLayout(cards_layout)

        row2_layout = QHBoxLayout()

        self.stat_completion_rate = self.create_stat_card("检测完成率", "0%", "#20C997")
        row2_layout.addWidget(self.stat_completion_rate)

        self.stat_abnormal = self.create_stat_card("异常样品", "0", "#DC3545")
        row2_layout.addWidget(self.stat_abnormal)

        self.stat_attachments = self.create_stat_card("附件总数", "0", "#FD7E14")
        row2_layout.addWidget(self.stat_attachments)

        self.stat_reported = self.create_stat_card("已报告", "0", "#6610F2")
        row2_layout.addWidget(self.stat_reported)

        layout.addLayout(row2_layout)

        status_group = QGroupBox("按状态分布")
        status_layout = QVBoxLayout(status_group)
        self.status_list = QListWidget()
        self.status_list.setAlternatingRowColors(True)
        status_layout.addWidget(self.status_list)
        layout.addWidget(status_group)

        workload_group = QGroupBox("检测人员工作量")
        workload_layout = QVBoxLayout(workload_group)
        self.workload_list = QListWidget()
        self.workload_list.setAlternatingRowColors(True)
        workload_layout.addWidget(self.workload_list)
        layout.addWidget(workload_group)

        scroll.setWidget(scroll_content)
        main_layout = QVBoxLayout(self.statistics_tab)
        main_layout.addWidget(scroll)

    def create_stat_card(self, title: str, value: str, color: str) -> QFrame:
        card = QFrame()
        card.setFrameShape(QFrame.StyledPanel)
        card.setStyleSheet(f"""
            QFrame {{
                background-color: #f8f9fa;
                border: 2px solid {color};
                border-radius: 12px;
                padding: 16px;
            }}
            QFrame:hover {{
                background-color: #e9ecef;
            }}
        """)
        card_layout = QVBoxLayout(card)

        title_label = QLabel(title)
        title_label.setAlignment(Qt.AlignCenter)
        title_label.setStyleSheet(f"color: {color}; font-weight: bold; font-size: 12px;")
        card_layout.addWidget(title_label)

        value_label = QLabel(value)
        value_font = QFont()
        value_font.setPointSize(28)
        value_font.setBold(True)
        value_label.setFont(value_font)
        value_label.setAlignment(Qt.AlignCenter)
        value_label.setStyleSheet(f"color: {color};")
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
        self.audit_table.setColumnCount(9)
        self.audit_table.setHorizontalHeaderLabels([
            "ID", "样品ID", "操作类型", "字段", "旧值", "新值", "操作人", "操作时间", "备注"
        ])
        self.audit_table.horizontalHeader().setSectionResizeMode(QHeaderView.Stretch)
        self.audit_table.horizontalHeader().setSectionResizeMode(0, QHeaderView.ResizeToContents)
        self.audit_table.setEditTriggers(QAbstractItemView.NoEditTriggers)
        self.audit_table.setAlternatingRowColors(True)
        layout.addWidget(self.audit_table)

    def setup_change_history_tab(self):
        layout = QVBoxLayout(self.change_history_tab)

        filter_layout = QHBoxLayout()
        filter_layout.addWidget(QLabel("检测人员:"))
        self.change_operator = QLineEdit()
        self.change_operator.setPlaceholderText("输入修改人姓名")
        filter_layout.addWidget(self.change_operator)

        btn_load = QPushButton("查询")
        btn_load.clicked.connect(self.load_all_changes)
        filter_layout.addWidget(btn_load)

        filter_layout.addStretch()

        layout.addLayout(filter_layout)

        self.change_table = QTableWidget()
        self.change_table.setColumnCount(8)
        self.change_table.setHorizontalHeaderLabels([
            "ID", "样品ID", "修改字段", "原值", "新值", "修改原因", "修改人", "修改时间"
        ])
        self.change_table.horizontalHeader().setSectionResizeMode(QHeaderView.Stretch)
        self.change_table.setEditTriggers(QAbstractItemView.NoEditTriggers)
        self.change_table.setAlternatingRowColors(True)
        layout.addWidget(self.change_table)

        self.load_all_changes()

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
            color = STATUS_COLORS.get(sample.status, "#FFFFFF")
            status_item.setBackground(QBrush(QColor(color)))
            status_item.setToolTip(STATUS_DESCRIPTIONS.get(sample.status, ""))
            self.sample_table.setItem(row, 5, status_item)

            self.sample_table.setItem(row, 6, QTableWidgetItem(sample.test_items))

        if len(samples) == 0:
            QMessageBox.information(self, "提示", "没有找到符合条件的样品数据")

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
            status_desc = STATUS_DESCRIPTIONS.get(sample.status, "")
            allowed_transitions = self.sample_service.get_allowed_transitions(sample.status)
            transition_text = ", ".join(allowed_transitions) if allowed_transitions else "无"

            self.detail_info.setText(
                f"<h3 style='color: #4472C4;'>{sample.sample_name}</h3>"
                f"<p><b>样品编号:</b> {sample.sample_no}</p>"
                f"<p><b>来源单位:</b> {sample.source_unit}</p>"
                f"<p><b>送检人:</b> {sample.sender}</p>"
                f"<p><b>接收人:</b> {sample.receiver}</p>"
                f"<p><b>接收时间:</b> {sample.receive_time}</p>"
                f"<p><b>检测项目:</b> {sample.test_items}</p>"
                f"<p><b>当前状态:</b> <span style='background-color: {STATUS_COLORS.get(sample.status, '#FFFFFF')}; padding: 3px 8px; border-radius: 3px;'>{sample.status}</span></p>"
                f"<p style='color: #666;'><i>{status_desc}</i></p>"
                f"<p><b>可切换状态:</b> {transition_text}</p>"
                f"<p><b>备注:</b> {sample.description}</p>"
                f"<p style='color: #999; font-size: 10px;'>创建时间: {sample.created_at} | 更新时间: {sample.updated_at}</p>"
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
                qualified_item.setBackground(QBrush(QColor("#F8D7DA")))
                qualified_item.setForeground(QBrush(QColor("#721C24")))
            self.test_record_table.setItem(row, 5, qualified_item)

    def update_statistics(self):
        stats = self.sample_service.get_statistics()
        att_stats = self.attachment_service.get_attachment_statistics()

        self.stat_total.findChild(QLabel, "stat_value").setText(str(stats['total']))
        self.stat_pending.findChild(QLabel, "stat_value").setText(str(stats['待检测']))
        self.stat_testing.findChild(QLabel, "stat_value").setText(str(stats['检测中']))
        self.stat_completed.findChild(QLabel, "stat_value").setText(str(stats['检测完成']))
        self.stat_today.findChild(QLabel, "stat_value").setText(str(stats['today']))

        self.stat_completion_rate.findChild(QLabel, "stat_value").setText(f"{stats['completion_rate']}%")
        self.stat_abnormal.findChild(QLabel, "stat_value").setText(str(stats['abnormal']))
        self.stat_attachments.findChild(QLabel, "stat_value").setText(str(att_stats['total_count']))
        self.stat_reported.findChild(QLabel, "stat_value").setText(str(stats['报告已生成']))

        self.status_list.clear()
        for status in SAMPLE_STATUS:
            count = stats.get(status, 0)
            rate = stats.get(f'{status}_rate', 0)
            item = QListWidgetItem(f"{status}: {count} 个 ({rate}%)")
            item.setToolTip(f"状态: {status}\n数量: {count}\n占比: {rate}%")
            self.status_list.addItem(item)

        self.workload_list.clear()
        workload = stats.get('tester_workload', [])
        if workload:
            for item in workload:
                list_item = QListWidgetItem(f"{item['tester']}: {item['count']} 条检测记录")
                self.workload_list.addItem(list_item)
        else:
            self.workload_list.addItem("暂无数据")

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
            self.audit_table.setItem(row, 1, QTableWidgetItem(str(log.sample_id) if log.sample_id else "-"))
            self.audit_table.setItem(row, 2, QTableWidgetItem(log.operation_type))
            self.audit_table.setItem(row, 3, QTableWidgetItem(log.field_name if log.field_name else "-"))
            self.audit_table.setItem(row, 4, QTableWidgetItem(log.old_value if log.old_value else "-"))
            self.audit_table.setItem(row, 5, QTableWidgetItem(log.new_value if log.new_value else "-"))
            self.audit_table.setItem(row, 6, QTableWidgetItem(log.operator))
            self.audit_table.setItem(row, 7, QTableWidgetItem(log.operation_time))
            self.audit_table.setItem(row, 8, QTableWidgetItem(log.remarks if log.remarks else "-"))

    def load_all_changes(self):
        operator = getattr(self, 'change_operator', None)
        operator_text = operator.text().strip() if operator else ""

        changes = self.test_record_service.get_all_changes()

        if operator_text:
            changes = [c for c in changes if operator_text in c.operator]

        self.change_table.setRowCount(len(changes))
        for row, change in enumerate(changes):
            self.change_table.setItem(row, 0, QTableWidgetItem(str(change.id)))
            self.change_table.setItem(row, 1, QTableWidgetItem(str(change.sample_id)))
            self.change_table.setItem(row, 2, QTableWidgetItem(change.field_name))
            self.change_table.setItem(row, 3, QTableWidgetItem(change.old_value if change.old_value else "-"))
            self.change_table.setItem(row, 4, QTableWidgetItem(change.new_value if change.new_value else "-"))
            self.change_table.setItem(row, 5, QTableWidgetItem(change.change_reason))
            self.change_table.setItem(row, 6, QTableWidgetItem(change.operator))
            self.change_table.setItem(row, 7, QTableWidgetItem(change.change_time))

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
                QMessageBox.warning(self, "输入错误", str(e))
            except Exception as e:
                QMessageBox.critical(self, "系统错误", f"添加样品失败: {str(e)}")

    def edit_sample(self):
        if not self.current_sample_id:
            QMessageBox.warning(self, "提示", "请先选择一个样品")
            return

        sample = self.sample_service.get_sample_by_id(self.current_sample_id)
        if not sample:
            QMessageBox.warning(self, "提示", "样品不存在")
            return

        if sample.status == "已归档" or sample.status == "已作废":
            QMessageBox.warning(self, "提示", f"当前状态为'{sample.status}'，不允许编辑")
            return

        dialog = SampleDialog(self, sample)
        if dialog.exec() == QDialog.Accepted:
            updated_sample = dialog.get_sample()
            updated_sample.id = self.current_sample_id
            try:
                self.sample_service.update_sample(updated_sample, operator="当前用户")
                self.load_samples()
                self.update_statistics()
                self.on_sample_selected()
                QMessageBox.information(self, "成功", "样品更新成功")
            except ValueError as e:
                QMessageBox.warning(self, "输入错误", str(e))
            except Exception as e:
                QMessageBox.critical(self, "系统错误", f"更新样品失败: {str(e)}")

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
            f"<h3>确定要删除样品吗？</h3>"
            f"<p><b>样品编号:</b> {sample.sample_no}</p>"
            f"<p><b>样品名称:</b> {sample.sample_name}</p>"
            f"<p style='color: #DC3545;'>此操作将同时删除相关的检测记录、附件和操作日志！</p>",
            QMessageBox.Yes | QMessageBox.No,
            QMessageBox.No
        )

        if reply == QMessageBox.Yes:
            confirm = QMessageBox.question(
                self,
                "再次确认",
                f"请再次确认：您确定要永久删除样品 '{sample.sample_no}' 吗？\n此操作无法撤销！",
                QMessageBox.Yes | QMessageBox.No,
                QMessageBox.No
            )

            if confirm == QMessageBox.Yes:
                try:
                    self.sample_service.delete_sample(self.current_sample_id, operator="当前用户")
                    self.current_sample_id = None
                    self.load_samples()
                    self.update_statistics()
                    self.detail_info.setText("请选择一个样品查看详情")
                    self.test_record_table.setRowCount(0)
                    QMessageBox.information(self, "成功", "样品删除成功")
                except Exception as e:
                    QMessageBox.critical(self, "系统错误", f"删除样品失败: {str(e)}")

    def show_test_records(self):
        if not self.current_sample_id:
            QMessageBox.warning(self, "提示", "请先选择一个样品")
            return

        sample = self.sample_service.get_sample_by_id(self.current_sample_id)
        if sample and (sample.status == "已归档" or sample.status == "已作废"):
            QMessageBox.warning(self, "提示", f"当前状态为'{sample.status}'，不允许编辑检测记录")

        dialog = TestRecordDialog(self, self.current_sample_id)
        dialog.exec()
        self.load_test_records(self.current_sample_id)
        self.update_statistics()

    def show_change_history(self):
        if not self.current_sample_id:
            QMessageBox.warning(self, "提示", "请先选择一个样品")
            return

        changes = self.test_record_service.get_changes_by_sample_id(self.current_sample_id)

        if not changes:
            QMessageBox.information(self, "提示", "该样品暂无检测结果修改记录")
            return

        dialog = ChangeHistoryDialog(self, changes)
        dialog.exec()

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
            QMessageBox.warning(self, "无法生成报告", f"{message}")
            return

        reply = QMessageBox.question(
            self,
            "生成报告",
            "选择报告格式:\n\nYes: Excel (.xlsx)\nNo: PDF (.pdf)",
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
            self.on_sample_selected()

            open_reply = QMessageBox.question(
                self,
                "报告生成成功",
                f"报告已成功生成！\n\n保存路径:\n{file_path}\n\n是否打开文件？",
                QMessageBox.Yes | QMessageBox.No
            )

            if open_reply == QMessageBox.Yes:
                import os
                os.startfile(file_path)

        except Exception as e:
            QMessageBox.critical(self, "生成失败", f"生成报告时发生错误:\n{str(e)}")

    def change_status(self):
        if not self.current_sample_id:
            QMessageBox.warning(self, "提示", "请先选择一个样品")
            return

        sample = self.sample_service.get_sample_by_id(self.current_sample_id)
        if not sample:
            return

        dialog = StatusChangeDialog(self, sample)
        if dialog.exec() == QDialog.Accepted:
            new_status, reason = dialog.get_result()
            try:
                self.sample_service.update_status(
                    self.current_sample_id,
                    new_status,
                    operator="当前用户",
                    reason=reason
                )
                self.load_samples()
                self.update_statistics()
                self.on_sample_selected()
                QMessageBox.information(self, "成功", f"状态已更新为: {new_status}")
            except ValueError as e:
                QMessageBox.warning(self, "状态变更失败", str(e))
            except Exception as e:
                QMessageBox.critical(self, "系统错误", f"更新状态失败: {str(e)}")

    def show_backup_dialog(self):
        dialog = BackupDialog(self)
        dialog.exec()

    def refresh_all(self):
        self.load_samples()
        self.update_statistics()
        self.load_audit_logs()
        self.load_all_changes()
        QMessageBox.information(self, "成功", "所有数据已刷新")


class SampleDialog(QDialog):
    def __init__(self, parent=None, sample: Sample = None):
        super().__init__(parent)
        self.sample = sample
        self.setWindowTitle("样品信息" if sample else "新增样品")
        self.setMinimumWidth(550)
        self.init_ui()
        if sample:
            self.load_sample_data()

    def init_ui(self):
        layout = QVBoxLayout(self)

        scroll = QScrollArea()
        scroll.setWidgetResizable(True)
        scroll_content = QWidget()
        form_layout = QFormLayout(scroll_content)
        form_layout.setRowWrapPolicy(QFormLayout.WrapAllRows)

        self.sample_no = QLineEdit()
        self.sample_no.setPlaceholderText("例如: YP202401001")
        self.sample_no.setStyleSheet("padding: 6px;")
        form_layout.addRow("<span style='color: red;'>*</span> 样品编号:", self.sample_no)

        self.sample_name = QLineEdit()
        self.sample_name.setPlaceholderText("请输入样品名称")
        self.sample_name.setStyleSheet("padding: 6px;")
        form_layout.addRow("<span style='color: red;'>*</span> 样品名称:", self.sample_name)

        self.source_unit = QComboBox()
        self.source_unit.addItems(DEPARTMENTS)
        self.source_unit.setEditable(True)
        self.source_unit.setStyleSheet("padding: 6px;")
        form_layout.addRow("<span style='color: red;'>*</span> 来源单位:", self.source_unit)

        self.sender = QLineEdit()
        self.sender.setPlaceholderText("请输入送检人姓名")
        self.sender.setStyleSheet("padding: 6px;")
        form_layout.addRow("送检人:", self.sender)

        self.receiver = QComboBox()
        self.receiver.addItems(TESTERS)
        self.receiver.setEditable(True)
        self.receiver.setStyleSheet("padding: 6px;")
        form_layout.addRow("<span style='color: red;'>*</span> 接收人:", self.receiver)

        self.receive_time = QDateEdit()
        self.receive_time.setDisplayFormat("yyyy-MM-dd HH:mm:ss")
        self.receive_time.setCalendarPopup(True)
        self.receive_time.setDateTime(datetime.now())
        self.receive_time.setStyleSheet("padding: 6px;")
        form_layout.addRow("接收时间:", self.receive_time)

        self.test_items = QLineEdit()
        self.test_items.setPlaceholderText("多个项目用逗号分隔，例如: 理化检测,微生物检测")
        self.test_items.setStyleSheet("padding: 6px;")
        form_layout.addRow("<span style='color: red;'>*</span> 检测项目:", self.test_items)

        self.status = QComboBox()
        self.status.addItems(SAMPLE_STATUS)
        self.status.setStyleSheet("padding: 6px;")
        form_layout.addRow("状态:", self.status)

        self.description = QTextEdit()
        self.description.setMaximumHeight(80)
        self.description.setPlaceholderText("请输入备注信息（可选）")
        form_layout.addRow("备注:", self.description)

        hint_label = QLabel("<span style='color: #666; font-size: 11px;'>带 <span style='color: red;'>*</span> 的字段为必填项</span>")
        form_layout.addRow("", hint_label)

        scroll.setWidget(scroll_content)
        layout.addWidget(scroll)

        btn_layout = QHBoxLayout()
        btn_ok = QPushButton("确定")
        btn_ok.setStyleSheet("padding: 8px 24px; background-color: #4472C4; color: white; border-radius: 4px;")
        btn_ok.clicked.connect(self.validate_and_accept)
        btn_layout.addWidget(btn_ok)

        btn_cancel = QPushButton("取消")
        btn_cancel.setStyleSheet("padding: 8px 24px;")
        btn_cancel.clicked.connect(self.reject)
        btn_layout.addWidget(btn_cancel)

        layout.addLayout(btn_layout)

    def validate_and_accept(self):
        if not self.sample_no.text().strip():
            QMessageBox.warning(self, "验证失败", "样品编号不能为空")
            self.sample_no.setFocus()
            return

        if not self.sample_name.text().strip():
            QMessageBox.warning(self, "验证失败", "样品名称不能为空")
            self.sample_name.setFocus()
            return

        if not self.source_unit.currentText().strip():
            QMessageBox.warning(self, "验证失败", "来源单位不能为空")
            self.source_unit.setFocus()
            return

        if not self.test_items.text().strip():
            QMessageBox.warning(self, "验证失败", "检测项目不能为空")
            self.test_items.setFocus()
            return

        self.accept()

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
        self.setWindowTitle("检测记录管理")
        self.setMinimumSize(900, 600)
        self.init_ui()
        self.load_records()

    def init_ui(self):
        layout = QVBoxLayout(self)

        btn_layout = QHBoxLayout()
        btn_add = QPushButton("添加记录")
        btn_add.setStyleSheet("padding: 8px 16px; background-color: #28A745; color: white; border-radius: 4px;")
        btn_add.clicked.connect(self.add_record)
        btn_layout.addWidget(btn_add)

        btn_edit = QPushButton("编辑记录")
        btn_edit.setStyleSheet("padding: 8px 16px; background-color: #FFC107; color: #333; border-radius: 4px;")
        btn_edit.clicked.connect(self.edit_record)
        btn_layout.addWidget(btn_edit)

        btn_view_changes = QPushButton("查看修改历史")
        btn_view_changes.setStyleSheet("padding: 8px 16px; background-color: #17A2B8; color: white; border-radius: 4px;")
        btn_view_changes.clicked.connect(self.view_changes)
        btn_layout.addWidget(btn_view_changes)

        btn_delete = QPushButton("删除记录")
        btn_delete.setStyleSheet("padding: 8px 16px; background-color: #DC3545; color: white; border-radius: 4px;")
        btn_delete.clicked.connect(self.delete_record)
        btn_layout.addWidget(btn_delete)

        btn_layout.addStretch()
        btn_close = QPushButton("关闭")
        btn_close.setStyleSheet("padding: 8px 16px;")
        btn_close.clicked.connect(self.accept)
        btn_layout.addWidget(btn_close)

        layout.addLayout(btn_layout)

        self.table = QTableWidget()
        self.table.setColumnCount(8)
        self.table.setHorizontalHeaderLabels([
            "ID", "检测项目", "检测人员", "检测时间", "检测结果", "结果值", "标准值", "是否合格"
        ])
        self.table.horizontalHeader().setSectionResizeMode(QHeaderView.Stretch)
        self.table.horizontalHeader().setSectionResizeMode(0, QHeaderView.ResizeToContents)
        self.table.setSelectionBehavior(QAbstractItemView.SelectRows)
        self.table.setSelectionMode(QAbstractItemView.SingleSelection)
        self.table.setEditTriggers(QAbstractItemView.NoEditTriggers)
        self.table.setAlternatingRowColors(True)
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
                qualified_item.setBackground(QBrush(QColor("#F8D7DA")))
            self.table.setItem(row, 7, qualified_item)

        if len(records) == 0:
            QMessageBox.information(self, "提示", "该样品暂无检测记录，请添加检测记录")

    def add_record(self):
        dialog = TestRecordEditDialog(self)
        if dialog.exec() == QDialog.Accepted:
            record = dialog.get_record()
            record.sample_id = self.sample_id
            try:
                self.test_record_service.create_test_record(record)
                self.load_records()
                QMessageBox.information(self, "成功", "检测记录添加成功")
            except ValueError as e:
                QMessageBox.warning(self, "输入错误", str(e))

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
            updated_record, change_reason, change_remark = dialog.get_result()
            updated_record.id = record_id
            updated_record.sample_id = self.sample_id
            try:
                self.test_record_service.update_test_record(
                    updated_record,
                    operator="当前用户",
                    change_reason=change_reason,
                    change_remark=change_remark
                )
                self.load_records()
                QMessageBox.information(self, "成功", "检测记录更新成功，修改已留痕")
            except ValueError as e:
                QMessageBox.warning(self, "输入错误", str(e))

    def view_changes(self):
        selected_items = self.table.selectedItems()
        if not selected_items:
            QMessageBox.warning(self, "提示", "请先选择一条记录")
            return

        row = selected_items[0].row()
        record_id = int(self.table.item(row, 0).text())
        changes = self.test_record_service.get_changes_by_test_record_id(record_id)

        if not changes:
            QMessageBox.information(self, "提示", "该检测记录暂无修改历史")
            return

        dialog = ChangeHistoryDialog(self, changes)
        dialog.exec()

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
            QMessageBox.information(self, "成功", "检测记录已删除")


class TestRecordEditDialog(QDialog):
    def __init__(self, parent=None, record: TestRecord = None):
        super().__init__(parent)
        self.record = record
        self.is_edit = record is not None
        self.setWindowTitle("编辑检测记录" if record else "添加检测记录")
        self.setMinimumWidth(450)
        self.init_ui()
        if record:
            self.load_record_data()

    def init_ui(self):
        layout = QVBoxLayout(self)

        scroll = QScrollArea()
        scroll.setWidgetResizable(True)
        scroll_content = QWidget()
        form_layout = QFormLayout(scroll_content)

        self.test_item = QComboBox()
        self.test_item.addItems(TEST_ITEMS)
        self.test_item.setEditable(True)
        self.test_item.setStyleSheet("padding: 6px;")
        form_layout.addRow("<span style='color: red;'>*</span> 检测项目:", self.test_item)

        self.tester = QComboBox()
        self.tester.addItems(TESTERS)
        self.tester.setEditable(True)
        self.tester.setStyleSheet("padding: 6px;")
        form_layout.addRow("<span style='color: red;'>*</span> 检测人员:", self.tester)

        self.test_time = QDateEdit()
        self.test_time.setDisplayFormat("yyyy-MM-dd HH:mm:ss")
        self.test_time.setCalendarPopup(True)
        self.test_time.setDateTime(datetime.now())
        self.test_time.setStyleSheet("padding: 6px;")
        form_layout.addRow("检测时间:", self.test_time)

        self.test_result = QComboBox()
        self.test_result.addItems(["", "合格", "不合格", "检测中"])
        self.test_result.setStyleSheet("padding: 6px;")
        form_layout.addRow("检测结果:", self.test_result)

        self.result_value = QLineEdit()
        self.result_value.setPlaceholderText("请输入具体检测数值")
        self.result_value.setStyleSheet("padding: 6px;")
        form_layout.addRow("结果值:", self.result_value)

        self.standard_value = QLineEdit()
        self.standard_value.setPlaceholderText("请输入标准限值")
        self.standard_value.setStyleSheet("padding: 6px;")
        form_layout.addRow("标准值:", self.standard_value)

        self.is_qualified = QCheckBox("检测结果合格")
        self.is_qualified.setChecked(True)
        self.is_qualified.setStyleSheet("padding: 6px;")
        form_layout.addRow("是否合格:", self.is_qualified)

        self.remarks = QTextEdit()
        self.remarks.setMaximumHeight(80)
        self.remarks.setPlaceholderText("请输入备注信息（可选）")
        form_layout.addRow("备注:", self.remarks)

        if self.is_edit:
            self.change_reason = QLineEdit()
            self.change_reason.setPlaceholderText("请输入修改原因（必填）")
            self.change_reason.setStyleSheet("padding: 6px;")
            form_layout.addRow("<span style='color: red;'>*</span> 修改原因:", self.change_reason)

            self.change_remark = QLineEdit()
            self.change_remark.setPlaceholderText("请输入修改备注（可选）")
            self.change_remark.setStyleSheet("padding: 6px;")
            form_layout.addRow("修改备注:", self.change_remark)

        hint_label = QLabel("<span style='color: #666; font-size: 11px;'>带 <span style='color: red;'>*</span> 的字段为必填项</span>")
        form_layout.addRow("", hint_label)

        scroll.setWidget(scroll_content)
        layout.addWidget(scroll)

        btn_layout = QHBoxLayout()
        btn_ok = QPushButton("确定")
        btn_ok.setStyleSheet("padding: 8px 24px; background-color: #4472C4; color: white; border-radius: 4px;")
        btn_ok.clicked.connect(self.validate_and_accept)
        btn_layout.addWidget(btn_ok)

        btn_cancel = QPushButton("取消")
        btn_cancel.setStyleSheet("padding: 8px 24px;")
        btn_cancel.clicked.connect(self.reject)
        btn_layout.addWidget(btn_cancel)

        layout.addLayout(btn_layout)

    def validate_and_accept(self):
        if not self.test_item.currentText().strip():
            QMessageBox.warning(self, "验证失败", "检测项目不能为空")
            self.test_item.setFocus()
            return

        if not self.tester.currentText().strip():
            QMessageBox.warning(self, "验证失败", "检测人员不能为空")
            self.tester.setFocus()
            return

        if self.is_edit and not self.change_reason.text().strip():
            QMessageBox.warning(self, "验证失败", "修改原因不能为空")
            self.change_reason.setFocus()
            return

        self.accept()

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

    def get_result(self):
        record = self.get_record()
        reason = self.change_reason.text().strip() if self.is_edit else ""
        remark = self.change_remark.text().strip() if self.is_edit else ""
        return record, reason, remark


class ChangeHistoryDialog(QDialog):
    def __init__(self, parent=None, changes=None):
        super().__init__(parent)
        self.changes = changes or []
        self.setWindowTitle("修改历史记录")
        self.setMinimumSize(900, 500)
        self.init_ui()

    def init_ui(self):
        layout = QVBoxLayout(self)

        info_label = QLabel(f"共找到 <b>{len(self.changes)}</b> 条修改记录")
        info_label.setStyleSheet("padding: 8px; font-size: 12px;")
        layout.addWidget(info_label)

        self.table = QTableWidget()
        self.table.setColumnCount(7)
        self.table.setHorizontalHeaderLabels([
            "修改时间", "修改人", "修改字段", "原值", "新值", "修改原因", "备注"
        ])
        self.table.horizontalHeader().setSectionResizeMode(QHeaderView.Stretch)
        self.table.setEditTriggers(QAbstractItemView.NoEditTriggers)
        self.table.setAlternatingRowColors(True)

        self.table.setRowCount(len(self.changes))
        for row, change in enumerate(self.changes):
            self.table.setItem(row, 0, QTableWidgetItem(change.change_time))
            self.table.setItem(row, 1, QTableWidgetItem(change.operator))
            self.table.setItem(row, 2, QTableWidgetItem(change.field_name))
            self.table.setItem(row, 3, QTableWidgetItem(change.old_value if change.old_value else "-"))
            self.table.setItem(row, 4, QTableWidgetItem(change.new_value if change.new_value else "-"))
            self.table.setItem(row, 5, QTableWidgetItem(change.change_reason))
            self.table.setItem(row, 6, QTableWidgetItem(change.remarks if change.remarks else "-"))

        layout.addWidget(self.table)

        btn_layout = QHBoxLayout()
        btn_close = QPushButton("关闭")
        btn_close.setStyleSheet("padding: 8px 24px;")
        btn_close.clicked.connect(self.accept)
        btn_layout.addStretch()
        btn_layout.addWidget(btn_close)
        layout.addLayout(btn_layout)


class AttachmentDialog(QDialog):
    def __init__(self, parent=None, sample_id: int = 0):
        super().__init__(parent)
        self.sample_id = sample_id
        self.attachment_service = AttachmentService()
        self.setWindowTitle("附件管理")
        self.setMinimumSize(800, 600)
        self.init_ui()
        self.load_attachments()

    def init_ui(self):
        layout = QVBoxLayout(self)

        btn_layout = QHBoxLayout()
        btn_upload = QPushButton("上传附件")
        btn_upload.setStyleSheet("padding: 8px 16px; background-color: #28A745; color: white; border-radius: 4px;")
        btn_upload.clicked.connect(self.upload_attachment)
        btn_layout.addWidget(btn_upload)

        btn_preview = QPushButton("预览附件")
        btn_preview.setStyleSheet("padding: 8px 16px; background-color: #17A2B8; color: white; border-radius: 4px;")
        btn_preview.clicked.connect(self.preview_attachment)
        btn_layout.addWidget(btn_preview)

        btn_download = QPushButton("下载附件")
        btn_download.setStyleSheet("padding: 8px 16px; background-color: #6F42C1; color: white; border-radius: 4px;")
        btn_download.clicked.connect(self.download_attachment)
        btn_layout.addWidget(btn_download)

        btn_rebind = QPushButton("重新绑定")
        btn_rebind.setStyleSheet("padding: 8px 16px; background-color: #FD7E14; color: white; border-radius: 4px;")
        btn_rebind.clicked.connect(self.rebind_attachment)
        btn_layout.addWidget(btn_rebind)

        btn_delete = QPushButton("删除附件")
        btn_delete.setStyleSheet("padding: 8px 16px; background-color: #DC3545; color: white; border-radius: 4px;")
        btn_delete.clicked.connect(self.delete_attachment)
        btn_layout.addWidget(btn_delete)

        btn_layout.addStretch()
        btn_close = QPushButton("关闭")
        btn_close.setStyleSheet("padding: 8px 16px;")
        btn_close.clicked.connect(self.accept)
        btn_layout.addWidget(btn_close)

        layout.addLayout(btn_layout)

        self.list_widget = QListWidget()
        self.list_widget.setAlternatingRowColors(True)
        self.list_widget.setSelectionMode(QAbstractItemView.SingleSelection)
        layout.addWidget(self.list_widget)

    def load_attachments(self):
        self.list_widget.clear()
        existing, missing = self.attachment_service.check_attachments_exist(self.sample_id)

        for att in existing:
            preview_info = self.attachment_service.get_preview_info(att)
            can_preview = preview_info['can_preview']
            preview_text = " [可预览]" if can_preview else " [不可预览]"
            item = QListWidgetItem(
                f"📄 {att.file_name} ({att.format_file_size()}) - 上传于 {att.uploaded_at} by {att.uploaded_by}{preview_text}"
            )
            item.setData(Qt.UserRole, att)
            if att.description:
                item.setToolTip(f"描述: {att.description}")
            self.list_widget.addItem(item)

        for att in missing:
            item = QListWidgetItem(
                f"⚠️ [文件丢失] {att.file_name} - 上传于 {att.uploaded_at} (点击重新绑定)"
            )
            item.setData(Qt.UserRole, att)
            item.setForeground(QBrush(QColor("#DC3545")))
            self.list_widget.addItem(item)

        if missing:
            QMessageBox.warning(
                self,
                "附件丢失提示",
                f"发现 {len(missing)} 个附件文件已丢失！\n\n请选中丢失的附件，点击「重新绑定」按钮选择新的文件。"
            )

        if len(existing) + len(missing) == 0:
            empty_item = QListWidgetItem("📭 暂无附件，点击「上传附件」添加")
            empty_item.setTextAlignment(Qt.AlignCenter)
            empty_item.setForeground(QBrush(QColor("#6C757D")))
            empty_item.setFlags(empty_item.flags() & ~Qt.ItemIsSelectable)
            self.list_widget.addItem(empty_item)

    def get_selected_attachment(self):
        current_item = self.list_widget.currentItem()
        if not current_item:
            return None
        return current_item.data(Qt.UserRole)

    def upload_attachment(self):
        file_path, _ = QFileDialog.getOpenFileName(
            self,
            "选择文件上传",
            "",
            "所有文件 (*.*);;图片文件 (*.jpg *.jpeg *.png *.gif *.bmp);;PDF文件 (*.pdf);;文本文件 (*.txt)"
        )

        if file_path:
            is_valid, message = self.attachment_service.validate_attachment_path(file_path)
            if not is_valid:
                QMessageBox.warning(self, "文件验证失败", message)
                return

            try:
                self.attachment_service.upload_attachment(
                    self.sample_id,
                    file_path,
                    uploaded_by="当前用户"
                )
                self.load_attachments()
                QMessageBox.information(self, "成功", "附件上传成功")
            except Exception as e:
                QMessageBox.critical(self, "上传失败", f"附件上传失败: {str(e)}")

    def preview_attachment(self):
        attachment = self.get_selected_attachment()
        if not attachment:
            QMessageBox.warning(self, "提示", "请先选择一个附件")
            return

        if not attachment.file_exists():
            QMessageBox.warning(self, "错误", "文件已丢失，请先重新绑定")
            return

        can_preview, message = self.attachment_service.can_preview(attachment)
        if not can_preview:
            QMessageBox.information(self, "无法预览", message)
            return

        try:
            self.attachment_service.open_preview(attachment)
        except Exception as e:
            QMessageBox.critical(self, "预览失败", f"打开预览失败: {str(e)}")

    def download_attachment(self):
        attachment = self.get_selected_attachment()
        if not attachment:
            QMessageBox.warning(self, "提示", "请先选择一个附件")
            return

        if not attachment.file_exists():
            QMessageBox.warning(self, "错误", "文件已丢失，请先重新绑定")
            return

        save_path, _ = QFileDialog.getSaveFileName(
            self,
            "保存文件",
            attachment.file_name,
            "所有文件 (*.*)"
        )

        if save_path:
            import shutil
            try:
                shutil.copy2(attachment.file_path, save_path)
                QMessageBox.information(self, "成功", f"文件已保存到:\n{save_path}")
            except Exception as e:
                QMessageBox.critical(self, "下载失败", f"保存文件失败: {str(e)}")

    def rebind_attachment(self):
        attachment = self.get_selected_attachment()
        if not attachment:
            QMessageBox.warning(self, "提示", "请先选择一个附件")
            return

        file_path, _ = QFileDialog.getOpenFileName(
            self,
            "选择新文件重新绑定",
            "",
            "所有文件 (*.*)"
        )

        if file_path:
            is_valid, message = self.attachment_service.validate_attachment_path(file_path)
            if not is_valid:
                QMessageBox.warning(self, "文件验证失败", message)
                return

            try:
                self.attachment_service.rebind_attachment(attachment.id, file_path)
                self.load_attachments()
                QMessageBox.information(self, "成功", "附件重新绑定成功")
            except Exception as e:
                QMessageBox.critical(self, "绑定失败", f"重新绑定失败: {str(e)}")

    def delete_attachment(self):
        attachment = self.get_selected_attachment()
        if not attachment:
            QMessageBox.warning(self, "提示", "请先选择一个附件")
            return

        reply = QMessageBox.question(
            self,
            "确认删除",
            f"确定要删除附件 '{attachment.file_name}' 吗？",
            QMessageBox.Yes | QMessageBox.No,
            QMessageBox.No
        )

        if reply == QMessageBox.Yes:
            try:
                self.attachment_service.delete_attachment(attachment.id)
                self.load_attachments()
                QMessageBox.information(self, "成功", "附件已删除")
            except Exception as e:
                QMessageBox.critical(self, "删除失败", f"删除附件失败: {str(e)}")


class StatusChangeDialog(QDialog):
    def __init__(self, parent=None, sample: Sample = None):
        super().__init__(parent)
        self.sample = sample
        self.current_status = sample.status if sample else ""
        self.sample_service = SampleService()
        self.setWindowTitle("状态流转")
        self.setMinimumWidth(450)
        self.init_ui()

    def init_ui(self):
        layout = QVBoxLayout(self)

        current_desc = STATUS_DESCRIPTIONS.get(self.current_status, "")
        current_color = STATUS_COLORS.get(self.current_status, "#FFFFFF")

        info_box = QGroupBox("当前状态")
        info_layout = QVBoxLayout(info_box)
        current_label = QLabel(
            f"<h2 style='color: #333; margin: 0;'>{self.current_status}</h2>"
            f"<p style='color: #666; margin: 5px 0 0 0;'>{current_desc}</p>"
        )
        current_label.setStyleSheet(f"background-color: {current_color}; padding: 15px; border-radius: 8px;")
        info_layout.addWidget(current_label)
        layout.addWidget(info_box)

        allowed = self.sample_service.get_allowed_transitions(self.current_status)

        if allowed:
            allowed_box = QGroupBox("可切换状态")
            allowed_layout = QVBoxLayout(allowed_box)

            for status in allowed:
                desc = STATUS_DESCRIPTIONS.get(status, "")
                color = STATUS_COLORS.get(status, "#FFFFFF")
                label = QLabel(f"• <b>{status}</b> - {desc}")
                label.setStyleSheet(f"padding: 8px; background-color: {color}; border-radius: 4px; margin: 3px 0;")
                allowed_layout.addWidget(label)

            layout.addWidget(allowed_box)

            form_layout = QFormLayout()
            self.status_combo = QComboBox()
            self.status_combo.addItems(allowed)
            self.status_combo.setStyleSheet("padding: 8px;")
            form_layout.addRow("选择新状态:", self.status_combo)

            self.reason = QLineEdit()
            self.reason.setPlaceholderText("请输入状态变更原因（可选）")
            self.reason.setStyleSheet("padding: 8px;")
            form_layout.addRow("变更原因:", self.reason)

            layout.addLayout(form_layout)
        else:
            msg_label = QLabel("<p style='color: #DC3545; text-align: center; padding: 20px;'>"
                              "当前状态不允许切换到任何其他状态</p>")
            layout.addWidget(msg_label)

        btn_layout = QHBoxLayout()
        if allowed:
            btn_ok = QPushButton("确认变更")
            btn_ok.setStyleSheet("padding: 8px 24px; background-color: #4472C4; color: white; border-radius: 4px;")
            btn_ok.clicked.connect(self.validate_and_accept)
            btn_layout.addWidget(btn_ok)

        btn_cancel = QPushButton("取消")
        btn_cancel.setStyleSheet("padding: 8px 24px;")
        btn_cancel.clicked.connect(self.reject)
        btn_layout.addStretch()
        btn_layout.addWidget(btn_cancel)

        layout.addLayout(btn_layout)

    def validate_and_accept(self):
        new_status = self.status_combo.currentText()
        can_transition, message = self.sample_service.can_transition_status(self.current_status, new_status)
        if not can_transition:
            QMessageBox.warning(self, "状态变更不允许", message)
            return
        self.accept()

    def get_result(self):
        new_status = self.status_combo.currentText()
        reason = self.reason.text().strip()
        return new_status, reason


class BackupDialog(QDialog):
    def __init__(self, parent=None):
        super().__init__(parent)
        self.backup_manager = BackupManager()
        self.setWindowTitle("数据备份与恢复")
        self.setMinimumSize(700, 500)
        self.init_ui()
        self.load_backups()

    def init_ui(self):
        layout = QVBoxLayout(self)

        btn_layout = QHBoxLayout()
        btn_create = QPushButton("创建备份")
        btn_create.setStyleSheet("padding: 8px 16px; background-color: #28A745; color: white; border-radius: 4px;")
        btn_create.clicked.connect(self.create_backup)
        btn_layout.addWidget(btn_create)

        self.include_attachments = QCheckBox("包含附件")
        self.include_attachments.setStyleSheet("padding: 8px;")
        btn_layout.addWidget(self.include_attachments)

        btn_restore = QPushButton("恢复备份")
        btn_restore.setStyleSheet("padding: 8px 16px; background-color: #FFC107; color: #333; border-radius: 4px;")
        btn_restore.clicked.connect(self.restore_backup)
        btn_layout.addWidget(btn_restore)

        btn_delete = QPushButton("删除备份")
        btn_delete.setStyleSheet("padding: 8px 16px; background-color: #DC3545; color: white; border-radius: 4px;")
        btn_delete.clicked.connect(self.delete_backup)
        btn_layout.addWidget(btn_delete)

        btn_layout.addStretch()
        btn_close = QPushButton("关闭")
        btn_close.setStyleSheet("padding: 8px 16px;")
        btn_close.clicked.connect(self.accept)
        btn_layout.addWidget(btn_close)

        layout.addLayout(btn_layout)

        info_label = QLabel("💡 提示: 建议定期备份数据，防止数据丢失。恢复备份将覆盖当前所有数据，请谨慎操作。")
        info_label.setStyleSheet("padding: 10px; background-color: #FFF3CD; border-radius: 4px; color: #856404;")
        info_label.setWordWrap(True)
        layout.addWidget(info_label)

        self.backup_list = QListWidget()
        self.backup_list.setAlternatingRowColors(True)
        self.backup_list.setSelectionMode(QAbstractItemView.SingleSelection)
        layout.addWidget(self.backup_list)

    def load_backups(self):
        self.backup_list.clear()
        backups = self.backup_manager.list_backups()

        for backup in backups:
            size_str = self.backup_manager.format_size(backup['size'])
            has_att = " (含附件)" if backup['has_attachments'] else ""
            item = QListWidgetItem(
                f"📅 {backup['time']} - {size_str}{has_att}"
            )
            item.setData(Qt.UserRole, backup)
            self.backup_list.addItem(item)

        if len(backups) == 0:
            empty_item = QListWidgetItem("📭 暂无备份，点击「创建备份」添加")
            empty_item.setTextAlignment(Qt.AlignCenter)
            empty_item.setForeground(QBrush(QColor("#6C757D")))
            empty_item.setFlags(empty_item.flags() & ~Qt.ItemIsSelectable)
            self.backup_list.addItem(empty_item)

    def get_selected_backup(self):
        current_item = self.backup_list.currentItem()
        if not current_item:
            return None
        return current_item.data(Qt.UserRole)

    def create_backup(self):
        try:
            include_att = self.include_attachments.isChecked()
            backup_path = self.backup_manager.create_backup(include_att)
            self.load_backups()
            QMessageBox.information(self, "成功", f"备份创建成功！\n\n保存路径:\n{backup_path}")
        except Exception as e:
            QMessageBox.critical(self, "备份失败", f"创建备份时发生错误:\n{str(e)}")

    def restore_backup(self):
        backup = self.get_selected_backup()
        if not backup:
            QMessageBox.warning(self, "提示", "请先选择一个备份")
            return

        reply = QMessageBox.question(
            self,
            "确认恢复",
            f"<h3>确定要恢复备份吗？</h3>"
            f"<p><b>备份时间:</b> {backup['time']}</p>"
            f"<p><b>备份大小:</b> {self.backup_manager.format_size(backup['size'])}</p>"
            f"<p style='color: #DC3545;'>此操作将覆盖当前所有数据，且无法撤销！</p>",
            QMessageBox.Yes | QMessageBox.No,
            QMessageBox.No
        )

        if reply == QMessageBox.Yes:
            confirm = QMessageBox.question(
                self,
                "再次确认",
                "请再次确认：您确定要从该备份恢复所有数据吗？\n\n这将永久删除当前所有数据！",
                QMessageBox.Yes | QMessageBox.No,
                QMessageBox.No
            )

            if confirm == QMessageBox.Yes:
                try:
                    self.backup_manager.restore_backup(backup['path'])
                    QMessageBox.information(self, "成功", "数据恢复成功！\n\n请重启应用以加载恢复后的数据。")
                    self.accept()
                except Exception as e:
                    QMessageBox.critical(self, "恢复失败", f"恢复备份时发生错误:\n{str(e)}")

    def delete_backup(self):
        backup = self.get_selected_backup()
        if not backup:
            QMessageBox.warning(self, "提示", "请先选择一个备份")
            return

        reply = QMessageBox.question(
            self,
            "确认删除",
            f"确定要删除备份 '{backup['time']}' 吗？\n此操作无法撤销！",
            QMessageBox.Yes | QMessageBox.No,
            QMessageBox.No
        )

        if reply == QMessageBox.Yes:
            try:
                self.backup_manager.delete_backup(backup['path'])
                self.load_backups()
                QMessageBox.information(self, "成功", "备份已删除")
            except Exception as e:
                QMessageBox.critical(self, "删除失败", f"删除备份时发生错误:\n{str(e)}")