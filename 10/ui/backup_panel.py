from PySide6.QtWidgets import (
    QWidget, QVBoxLayout, QHBoxLayout, QPushButton, QTableWidget,
    QTableWidgetItem, QHeaderView, QGroupBox, QLabel, QMessageBox,
)
from PySide6.QtCore import Qt

from backup import BackupManager, BackupError
from database import DatabaseConnection
from .dialogs import confirm_action, show_error, show_info


class BackupPanel(QWidget):
    def __init__(self, backup_manager: BackupManager, db: DatabaseConnection):
        super().__init__()
        self.backup_manager = backup_manager
        self.db = db
        self.init_ui()
        self.load_backups()

    def init_ui(self):
        layout = QVBoxLayout(self)

        btn_layout = QHBoxLayout()

        self.db_backup_btn = QPushButton("数据库备份")
        self.db_backup_btn.clicked.connect(self.create_database_backup)
        btn_layout.addWidget(self.db_backup_btn)

        self.full_backup_btn = QPushButton("完整备份")
        self.full_backup_btn.clicked.connect(self.create_full_backup)
        btn_layout.addWidget(self.full_backup_btn)

        self.restore_btn = QPushButton("恢复选中")
        self.restore_btn.clicked.connect(self.restore_backup)
        btn_layout.addWidget(self.restore_btn)

        self.delete_btn = QPushButton("删除选中")
        self.delete_btn.clicked.connect(self.delete_backup)
        btn_layout.addWidget(self.delete_btn)

        self.refresh_btn = QPushButton("刷新列表")
        self.refresh_btn.clicked.connect(self.load_backups)
        btn_layout.addWidget(self.refresh_btn)

        layout.addLayout(btn_layout)

        stats_group = QGroupBox("备份统计")
        stats_layout = QHBoxLayout(stats_group)

        self.total_count_label = QLabel("备份总数: 0")
        stats_layout.addWidget(self.total_count_label)

        self.total_size_label = QLabel("总大小: 0 B")
        stats_layout.addWidget(self.total_size_label)

        layout.addWidget(stats_group)

        self.backup_table = QTableWidget()
        self.backup_table.setColumnCount(4)
        self.backup_table.setHorizontalHeaderLabels(
            ["备份名称", "类型", "大小", "创建时间"]
        )
        self.backup_table.horizontalHeader().setSectionResizeMode(QHeaderView.Stretch)
        self.backup_table.setSelectionBehavior(QTableWidget.SelectRows)
        layout.addWidget(self.backup_table, 1)

    def load_backups(self):
        try:
            backups = self.backup_manager.get_backup_list()
            stats = self.backup_manager.get_backup_stats()

            self.backup_table.setRowCount(len(backups))
            for row, backup in enumerate(backups):
                name_item = QTableWidgetItem(backup["name"])
                name_item.setData(Qt.UserRole, backup["path"])
                self.backup_table.setItem(row, 0, name_item)
                self.backup_table.setItem(row, 1, QTableWidgetItem(
                    "数据库" if backup["type"] == "database" else "完整"
                ))
                self.backup_table.setItem(row, 2, QTableWidgetItem(
                    self.backup_manager.format_size(backup["size"])
                ))
                self.backup_table.setItem(row, 3, QTableWidgetItem(backup["created_at"]))

            self.total_count_label.setText(f"备份总数: {stats['total_count']}")
            self.total_size_label.setText(f"总大小: {stats['total_size_formatted']}")

        except Exception as e:
            show_error(self, "加载失败", f"无法加载备份列表: {str(e)}")

    def create_database_backup(self):
        try:
            filepath = self.backup_manager.create_database_backup()
            show_info(self, "备份成功", f"数据库备份已创建:\n{filepath}")
            self.load_backups()
        except BackupError as e:
            show_error(self, "备份失败", str(e))

    def create_full_backup(self):
        try:
            filepath = self.backup_manager.create_full_backup()
            show_info(self, "备份成功", f"完整备份已创建:\n{filepath}")
            self.load_backups()
        except BackupError as e:
            show_error(self, "备份失败", str(e))

    def restore_backup(self):
        current_row = self.backup_table.currentRow()
        if current_row < 0:
            show_info(self, "提示", "请先选择要恢复的备份")
            return

        item = self.backup_table.item(current_row, 0)
        backup_path = item.data(Qt.UserRole)
        backup_name = item.text()

        backup_type_item = self.backup_table.item(current_row, 1)
        backup_type = backup_type_item.text()

        if not confirm_action(
            self,
            "恢复确认",
            f"确定要恢复备份:\n{backup_name}\n\n"
            f"类型: {backup_type}\n"
            "警告: 当前数据将被覆盖！",
        ):
            return

        try:
            if backup_type == "数据库":
                if self.backup_manager.restore_database_backup(backup_path):
                    show_info(self, "恢复成功", "数据库已恢复，请重启应用")
            else:
                show_info(self, "提示", "完整备份需要手动解压恢复")
        except BackupError as e:
            show_error(self, "恢复失败", str(e))

    def delete_backup(self):
        current_row = self.backup_table.currentRow()
        if current_row < 0:
            show_info(self, "提示", "请先选择要删除的备份")
            return

        item = self.backup_table.item(current_row, 0)
        backup_path = item.data(Qt.UserRole)
        backup_name = item.text()

        if not confirm_action(
            self, "删除确认", f"确定要删除备份:\n{backup_name}？\n此操作不可恢复！"
        ):
            return

        if self.backup_manager.delete_backup(backup_path):
            self.load_backups()
            show_info(self, "删除成功", "备份已删除")
        else:
            show_error(self, "删除失败", "无法删除备份文件")
