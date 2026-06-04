from PySide6.QtWidgets import (
    QWidget, QVBoxLayout, QHBoxLayout, QLineEdit, QComboBox,
    QPushButton, QTextEdit, QTabWidget, QLabel, QFormLayout,
    QTableWidget, QTableWidgetItem, QHeaderView, QGroupBox,
    QDoubleSpinBox, QDateEdit, QFileDialog, QListWidget, QListWidgetItem,
)
from PySide6.QtCore import Signal, Qt, QDate
from PySide6.QtGui import QPixmap

from config.settings import CATEGORIES, ERAS, SOURCES, CONSERVATION_STATUSES, REPAIR_STATUSES
from services import CollectionService, RepairService, ExhibitionService
from attachments import AttachmentManager, AttachmentError
from reports import ReportGenerator
from models import Collection, RepairRecord, Exhibition
from .dialogs import confirm_action, show_error, show_info, select_files


class CollectionDetailWidget(QWidget):
    data_changed = Signal()

    def __init__(
        self,
        collection_service: CollectionService,
        repair_service: RepairService,
        exhibition_service: ExhibitionService,
        attachment_manager: AttachmentManager,
        report_generator: ReportGenerator,
    ):
        super().__init__()
        self.collection_service = collection_service
        self.repair_service = repair_service
        self.exhibition_service = exhibition_service
        self.attachment_manager = attachment_manager
        self.report_generator = report_generator
        self.current_collection = None
        self.init_ui()

    def init_ui(self):
        layout = QVBoxLayout(self)

        self.tab_widget = QTabWidget()

        self.basic_tab = QWidget()
        self.repair_tab = QWidget()
        self.attachment_tab = QWidget()
        self.exhibition_tab = QWidget()

        self.tab_widget.addTab(self.basic_tab, "基本信息")
        self.tab_widget.addTab(self.repair_tab, "修复记录")
        self.tab_widget.addTab(self.attachment_tab, "附件管理")
        self.tab_widget.addTab(self.exhibition_tab, "展出历史")

        layout.addWidget(self.tab_widget)

        self.init_basic_tab()
        self.init_repair_tab()
        self.init_attachment_tab()
        self.init_exhibition_tab()

        btn_layout = QHBoxLayout()

        self.new_btn = QPushButton("新建藏品")
        self.new_btn.clicked.connect(self.new_collection)
        btn_layout.addWidget(self.new_btn)

        self.save_btn = QPushButton("保存")
        self.save_btn.clicked.connect(self.save_collection)
        btn_layout.addWidget(self.save_btn)

        self.report_btn = QPushButton("生成详情报告")
        self.report_btn.clicked.connect(self.generate_report)
        btn_layout.addWidget(self.report_btn)

        btn_layout.addStretch()
        layout.addLayout(btn_layout)

        self.set_edit_enabled(False)

    def init_basic_tab(self):
        layout = QVBoxLayout(self.basic_tab)

        form_group = QGroupBox("藏品信息")
        form_layout = QFormLayout(form_group)

        self.collection_no_edit = QLineEdit()
        form_layout.addRow("藏品编号:", self.collection_no_edit)

        self.name_edit = QLineEdit()
        form_layout.addRow("藏品名称:", self.name_edit)

        self.era_combo = QComboBox()
        self.era_combo.addItems(ERAS)
        form_layout.addRow("年代:", self.era_combo)

        self.category_combo = QComboBox()
        self.category_combo.addItems(CATEGORIES)
        form_layout.addRow("类别:", self.category_combo)

        self.source_combo = QComboBox()
        self.source_combo.addItems(SOURCES)
        form_layout.addRow("来源:", self.source_combo)

        self.conservation_combo = QComboBox()
        self.conservation_combo.addItems(CONSERVATION_STATUSES)
        form_layout.addRow("保存状态:", self.conservation_combo)

        self.entry_date_edit = QDateEdit()
        self.entry_date_edit.setCalendarPopup(True)
        self.entry_date_edit.setDate(QDate.currentDate())
        self.entry_date_edit.setDisplayFormat("yyyy-MM-dd")
        form_layout.addRow("入库时间:", self.entry_date_edit)

        self.location_edit = QLineEdit()
        form_layout.addRow("存放位置:", self.location_edit)

        self.value_spin = QDoubleSpinBox()
        self.value_spin.setRange(0, 999999999)
        self.value_spin.setDecimals(2)
        self.value_spin.setPrefix("¥ ")
        form_layout.addRow("估值:", self.value_spin)

        self.description_edit = QTextEdit()
        self.description_edit.setMaximumHeight(150)
        form_layout.addRow("描述:", self.description_edit)

        layout.addWidget(form_group)

        info_group = QGroupBox("系统信息")
        info_layout = QFormLayout(info_group)

        self.created_at_label = QLabel("-")
        info_layout.addRow("创建时间:", self.created_at_label)

        self.updated_at_label = QLabel("-")
        info_layout.addRow("更新时间:", self.updated_at_label)

        layout.addWidget(info_group)
        layout.addStretch()

    def init_repair_tab(self):
        layout = QVBoxLayout(self.repair_tab)

        btn_layout = QHBoxLayout()
        self.add_repair_btn = QPushButton("添加修复记录")
        self.add_repair_btn.clicked.connect(self.add_repair_record)
        btn_layout.addWidget(self.add_repair_btn)

        self.delete_repair_btn = QPushButton("删除选中")
        self.delete_repair_btn.clicked.connect(self.delete_repair_record)
        btn_layout.addWidget(self.delete_repair_btn)
        layout.addLayout(btn_layout)

        self.repair_table = QTableWidget()
        self.repair_table.setColumnCount(6)
        self.repair_table.setHorizontalHeaderLabels(
            ["修复日期", "修复人员", "修复原因", "状态", "费用", "备注"]
        )
        self.repair_table.horizontalHeader().setSectionResizeMode(QHeaderView.Stretch)
        layout.addWidget(self.repair_table)

        form_group = QGroupBox("修复记录表单")
        form_layout = QFormLayout(form_group)

        self.repair_date_edit = QDateEdit()
        self.repair_date_edit.setCalendarPopup(True)
        self.repair_date_edit.setDate(QDate.currentDate())
        self.repair_date_edit.setDisplayFormat("yyyy-MM-dd")
        form_layout.addRow("修复日期:", self.repair_date_edit)

        self.repairer_edit = QLineEdit()
        form_layout.addRow("修复人员:", self.repairer_edit)

        self.repair_reason_edit = QLineEdit()
        form_layout.addRow("修复原因:", self.repair_reason_edit)

        self.repair_status_combo = QComboBox()
        self.repair_status_combo.addItems(REPAIR_STATUSES)
        form_layout.addRow("修复状态:", self.repair_status_combo)

        self.repair_cost_spin = QDoubleSpinBox()
        self.repair_cost_spin.setRange(0, 999999)
        self.repair_cost_spin.setDecimals(2)
        self.repair_cost_spin.setPrefix("¥ ")
        form_layout.addRow("修复费用:", self.repair_cost_spin)

        self.repair_notes_edit = QTextEdit()
        self.repair_notes_edit.setMaximumHeight(80)
        form_layout.addRow("备注:", self.repair_notes_edit)

        self.save_repair_btn = QPushButton("保存修复记录")
        self.save_repair_btn.clicked.connect(self.save_repair)
        form_layout.addRow(self.save_repair_btn)

        layout.addWidget(form_group)

    def init_attachment_tab(self):
        layout = QVBoxLayout(self.attachment_tab)

        btn_layout = QHBoxLayout()
        self.add_attachment_btn = QPushButton("添加附件")
        self.add_attachment_btn.clicked.connect(self.add_attachment)
        btn_layout.addWidget(self.add_attachment_btn)

        self.delete_attachment_btn = QPushButton("删除选中")
        self.delete_attachment_btn.clicked.connect(self.delete_attachment)
        btn_layout.addWidget(self.delete_attachment_btn)

        self.open_attachment_btn = QPushButton("打开文件")
        self.open_attachment_btn.clicked.connect(self.open_attachment)
        btn_layout.addWidget(self.open_attachment_btn)
        layout.addLayout(btn_layout)

        self.attachment_list = QListWidget()
        layout.addWidget(self.attachment_list, 1)

        self.image_preview = QLabel("图片预览")
        self.image_preview.setAlignment(Qt.AlignCenter)
        self.image_preview.setMinimumHeight(200)
        self.image_preview.setStyleSheet("background-color: #f0f0f0;")
        layout.addWidget(self.image_preview)

    def init_exhibition_tab(self):
        layout = QVBoxLayout(self.exhibition_tab)

        btn_layout = QHBoxLayout()
        self.add_exhibition_btn = QPushButton("添加展出记录")
        self.add_exhibition_btn.clicked.connect(self.add_exhibition)
        btn_layout.addWidget(self.add_exhibition_btn)

        self.delete_exhibition_btn = QPushButton("删除选中")
        self.delete_exhibition_btn.clicked.connect(self.delete_exhibition)
        btn_layout.addWidget(self.delete_exhibition_btn)
        layout.addLayout(btn_layout)

        self.exhibition_table = QTableWidget()
        self.exhibition_table.setColumnCount(5)
        self.exhibition_table.setHorizontalHeaderLabels(
            ["展览名称", "地点", "开始日期", "结束日期", "主办方"]
        )
        self.exhibition_table.horizontalHeader().setSectionResizeMode(QHeaderView.Stretch)
        layout.addWidget(self.exhibition_table)

    def set_edit_enabled(self, enabled: bool):
        self.save_btn.setEnabled(enabled)
        self.report_btn.setEnabled(enabled)
        self.add_repair_btn.setEnabled(enabled)
        self.delete_repair_btn.setEnabled(enabled)
        self.save_repair_btn.setEnabled(enabled)
        self.add_attachment_btn.setEnabled(enabled)
        self.delete_attachment_btn.setEnabled(enabled)
        self.open_attachment_btn.setEnabled(enabled)
        self.add_exhibition_btn.setEnabled(enabled)
        self.delete_exhibition_btn.setEnabled(enabled)

        widgets = [
            self.collection_no_edit, self.name_edit, self.era_combo,
            self.category_combo, self.source_combo, self.conservation_combo,
            self.entry_date_edit, self.location_edit, self.value_spin,
            self.description_edit,
        ]
        for w in widgets:
            w.setEnabled(enabled)

    def new_collection(self):
        self.current_collection = Collection()
        new_no = self.collection_service.generate_collection_no()
        self.collection_no_edit.setText(new_no)
        self.name_edit.clear()
        self.era_combo.setCurrentIndex(0)
        self.category_combo.setCurrentIndex(0)
        self.source_combo.setCurrentIndex(0)
        self.conservation_combo.setCurrentIndex(0)
        self.entry_date_edit.setDate(QDate.currentDate())
        self.location_edit.clear()
        self.value_spin.setValue(0)
        self.description_edit.clear()
        self.created_at_label.setText("-")
        self.updated_at_label.setText("-")
        self.repair_table.setRowCount(0)
        self.attachment_list.clear()
        self.exhibition_table.setRowCount(0)
        self.image_preview.clear()
        self.image_preview.setText("图片预览")
        self.set_edit_enabled(True)
        self.tab_widget.setCurrentIndex(0)

    def load_collection(self, collection_id: int):
        try:
            collection = self.collection_service.get_collection(collection_id)
            if not collection:
                show_error(self, "错误", "找不到该藏品")
                return

            self.current_collection = collection
            self.collection_no_edit.setText(collection.collection_no)
            self.name_edit.setText(collection.name)
            self.era_combo.setCurrentText(collection.era)
            self.category_combo.setCurrentText(collection.category)
            self.source_combo.setCurrentText(collection.source)
            self.conservation_combo.setCurrentText(collection.conservation_status)

            if collection.entry_date:
                date = QDate.fromString(collection.entry_date.split(" ")[0], "yyyy-MM-dd")
                self.entry_date_edit.setDate(date)

            self.location_edit.setText(collection.location or "")
            self.value_spin.setValue(collection.estimated_value or 0)
            self.description_edit.setText(collection.description or "")
            self.created_at_label.setText(collection.created_at)
            self.updated_at_label.setText(collection.updated_at)

            self.load_repair_records(collection_id)
            self.load_attachments(collection_id)
            self.load_exhibitions(collection_id)

            self.set_edit_enabled(True)

        except Exception as e:
            show_error(self, "加载失败", str(e))

    def save_collection(self):
        if not self.current_collection:
            self.new_collection()
            return

        try:
            self.current_collection.collection_no = self.collection_no_edit.text().strip()
            self.current_collection.name = self.name_edit.text().strip()
            self.current_collection.era = self.era_combo.currentText()
            self.current_collection.category = self.category_combo.currentText()
            self.current_collection.source = self.source_combo.currentText()
            self.current_collection.conservation_status = self.conservation_combo.currentText()
            self.current_collection.entry_date = self.entry_date_edit.date().toString("yyyy-MM-dd")
            self.current_collection.location = self.location_edit.text().strip()
            self.current_collection.estimated_value = self.value_spin.value()
            self.current_collection.description = self.description_edit.toPlainText().strip()

            if self.current_collection.id:
                if self.collection_service.update_collection(self.current_collection):
                    show_info(self, "保存成功", "藏品信息已更新")
                else:
                    show_error(self, "保存失败", "无法更新藏品信息")
            else:
                new_id = self.collection_service.create_collection(self.current_collection)
                self.current_collection.id = new_id
                show_info(self, "保存成功", "新藏品已创建")

            self.data_changed.emit()
            self.load_collection(new_id if self.current_collection.id else self.current_collection.id)

        except ValueError as e:
            show_error(self, "验证错误", str(e))
        except Exception as e:
            show_error(self, "保存失败", str(e))

    def load_repair_records(self, collection_id: int):
        records = self.repair_service.get_repair_records(collection_id)
        self.repair_table.setRowCount(len(records))
        for row, record in enumerate(records):
            self.repair_table.setItem(row, 0, QTableWidgetItem(record.repair_date))
            self.repair_table.setItem(row, 1, QTableWidgetItem(record.repairer or ""))
            self.repair_table.setItem(row, 2, QTableWidgetItem(record.reason))
            self.repair_table.setItem(row, 3, QTableWidgetItem(record.status))
            self.repair_table.setItem(row, 4, QTableWidgetItem(str(record.cost or 0)))
            self.repair_table.setItem(row, 5, QTableWidgetItem(record.notes or ""))
            item = QTableWidgetItem()
            item.setData(Qt.UserRole, record.id)
            self.repair_table.setVerticalHeaderItem(row, item)

    def add_repair_record(self):
        self.repair_date_edit.setDate(QDate.currentDate())
        self.repairer_edit.clear()
        self.repair_reason_edit.clear()
        self.repair_status_combo.setCurrentIndex(0)
        self.repair_cost_spin.setValue(0)
        self.repair_notes_edit.clear()

    def save_repair(self):
        if not self.current_collection or not self.current_collection.id:
            show_info(self, "提示", "请先保存藏品信息")
            return

        try:
            record = RepairRecord(
                collection_id=self.current_collection.id,
                repair_date=self.repair_date_edit.date().toString("yyyy-MM-dd"),
                repairer=self.repairer_edit.text().strip(),
                reason=self.repair_reason_edit.text().strip(),
                status=self.repair_status_combo.currentText(),
                cost=self.repair_cost_spin.value(),
                notes=self.repair_notes_edit.toPlainText().strip(),
            )
            self.repair_service.create_repair_record(record)
            self.load_repair_records(self.current_collection.id)
            show_info(self, "保存成功", "修复记录已添加")
        except ValueError as e:
            show_error(self, "验证错误", str(e))
        except Exception as e:
            show_error(self, "保存失败", str(e))

    def delete_repair_record(self):
        current_row = self.repair_table.currentRow()
        if current_row < 0:
            show_info(self, "提示", "请先选择要删除的记录")
            return

        item = self.repair_table.verticalHeaderItem(current_row)
        record_id = item.data(Qt.UserRole)

        if not confirm_action(self, "删除确认", "确定要删除这条修复记录吗？"):
            return

        if self.repair_service.delete_repair_record(record_id):
            self.load_repair_records(self.current_collection.id)
            show_info(self, "删除成功", "修复记录已删除")

    def load_attachments(self, collection_id: int):
        attachments = self.attachment_manager.get_attachments(collection_id)
        self.attachment_list.clear()
        for att in attachments:
            status = "✓" if att.file_exists() else "✗"
            item_text = f"{status} {att.file_name} ({att.format_file_size()})"
            if att.description:
                item_text += f" - {att.description}"
            item = QListWidgetItem(item_text)
            item.setData(Qt.UserRole, att.id)
            self.attachment_list.addItem(item)

    def add_attachment(self):
        if not self.current_collection or not self.current_collection.id:
            show_info(self, "提示", "请先保存藏品信息")
            return

        file_paths = select_files(
            self, "选择附件文件",
            "图片文件 (*.jpg *.jpeg *.png *.gif *.bmp *.webp);;文档文件 (*.pdf *.doc *.docx *.txt);;所有文件 (*.*)"
        )

        success_count = 0
        for file_path in file_paths:
            try:
                self.attachment_manager.add_attachment(
                    self.current_collection.id, file_path
                )
                success_count += 1
            except AttachmentError as e:
                show_error(self, "添加失败", f"{file_path}: {str(e)}")

        if success_count > 0:
            self.load_attachments(self.current_collection.id)
            show_info(self, "完成", f"成功添加 {success_count} 个附件")

    def delete_attachment(self):
        current_item = self.attachment_list.currentItem()
        if not current_item:
            show_info(self, "提示", "请先选择要删除的附件")
            return

        attachment_id = current_item.data(Qt.UserRole)
        if not confirm_action(self, "删除确认", "确定要删除这个附件吗？"):
            return

        if self.attachment_manager.delete_attachment(attachment_id, delete_file=True):
            self.load_attachments(self.current_collection.id)
            show_info(self, "删除成功", "附件已删除")

    def open_attachment(self):
        current_item = self.attachment_list.currentItem()
        if not current_item:
            show_info(self, "提示", "请先选择要打开的附件")
            return

        attachment_id = current_item.data(Qt.UserRole)
        try:
            self.attachment_manager.open_file(attachment_id)
        except AttachmentError as e:
            show_error(self, "打开失败", str(e))

    def load_exhibitions(self, collection_id: int):
        exhibitions = self.exhibition_service.get_exhibitions(collection_id)
        self.exhibition_table.setRowCount(len(exhibitions))
        for row, exh in enumerate(exhibitions):
            self.exhibition_table.setItem(row, 0, QTableWidgetItem(exh.exhibition_name))
            self.exhibition_table.setItem(row, 1, QTableWidgetItem(exh.location or ""))
            self.exhibition_table.setItem(row, 2, QTableWidgetItem(exh.start_date or ""))
            self.exhibition_table.setItem(row, 3, QTableWidgetItem(exh.end_date or ""))
            self.exhibition_table.setItem(row, 4, QTableWidgetItem(exh.organizer or ""))
            item = QTableWidgetItem()
            item.setData(Qt.UserRole, exh.id)
            self.exhibition_table.setVerticalHeaderItem(row, item)

    def add_exhibition(self):
        from .exhibition_dialog import ExhibitionDialog
        if not self.current_collection or not self.current_collection.id:
            show_info(self, "提示", "请先保存藏品信息")
            return

        dialog = ExhibitionDialog(self)
        if dialog.exec():
            exhibition = dialog.get_exhibition()
            exhibition.collection_id = self.current_collection.id
            try:
                self.exhibition_service.create_exhibition(exhibition)
                self.load_exhibitions(self.current_collection.id)
                show_info(self, "保存成功", "展出记录已添加")
            except ValueError as e:
                show_error(self, "验证错误", str(e))

    def delete_exhibition(self):
        current_row = self.exhibition_table.currentRow()
        if current_row < 0:
            show_info(self, "提示", "请先选择要删除的记录")
            return

        item = self.exhibition_table.verticalHeaderItem(current_row)
        record_id = item.data(Qt.UserRole)

        if not confirm_action(self, "删除确认", "确定要删除这条展出记录吗？"):
            return

        if self.exhibition_service.delete_exhibition(record_id):
            self.load_exhibitions(self.current_collection.id)
            show_info(self, "删除成功", "展出记录已删除")

    def generate_report(self):
        if not self.current_collection:
            show_info(self, "提示", "请先选择一个藏品")
            return

        try:
            filepath = self.report_generator.generate_collection_detail_report(self.current_collection)
            show_info(self, "生成成功", f"报告已生成到: {filepath}")
        except Exception as e:
            show_error(self, "生成失败", str(e))
