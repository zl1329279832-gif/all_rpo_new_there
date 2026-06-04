from PySide6.QtWidgets import (
    QMainWindow, QWidget, QVBoxLayout, QHBoxLayout, QTabWidget,
    QStatusBar, QMessageBox, QSplitter,
)
from PySide6.QtCore import Qt
from PySide6.QtGui import QAction

from config.settings import APP_NAME, APP_VERSION, WINDOW_WIDTH, WINDOW_HEIGHT
from database import DatabaseConnection
from services import CollectionService, RepairService, ExhibitionService
from attachments import AttachmentManager
from reports import ReportGenerator
from backup import BackupManager
from .collection_list import CollectionListWidget
from .collection_detail import CollectionDetailWidget
from .statistics_panel import StatisticsPanel
from .backup_panel import BackupPanel
from .dialogs import show_error, show_info, confirm_action


class MainWindow(QMainWindow):
    def __init__(self, db: DatabaseConnection):
        super().__init__()
        self.db = db
        self.collection_service = CollectionService(db)
        self.repair_service = RepairService(db)
        self.exhibition_service = ExhibitionService(db)
        self.attachment_manager = AttachmentManager(db)
        self.report_generator = ReportGenerator(db)
        self.backup_manager = BackupManager()

        self.current_collection_id = None
        self.init_ui()
        self.check_missing_attachments()

    def init_ui(self):
        self.setWindowTitle(f"{APP_NAME} v{APP_VERSION}")
        self.setGeometry(100, 100, WINDOW_WIDTH, WINDOW_HEIGHT)

        self.create_menu_bar()
        self.create_central_widget()
        self.create_status_bar()

    def create_menu_bar(self):
        menubar = self.menuBar()

        file_menu = menubar.addMenu("文件")

        new_action = QAction("新建藏品", self)
        new_action.setShortcut("Ctrl+N")
        new_action.triggered.connect(self.new_collection)
        file_menu.addAction(new_action)

        export_menu = file_menu.addMenu("导出")

        export_csv_action = QAction("导出CSV", self)
        export_csv_action.triggered.connect(self.export_to_csv)
        export_menu.addAction(export_csv_action)

        export_json_action = QAction("导出JSON", self)
        export_json_action.triggered.connect(self.export_to_json)
        export_menu.addAction(export_json_action)

        export_stat_action = QAction("导出统计报告", self)
        export_stat_action.triggered.connect(self.export_statistics)
        export_menu.addAction(export_stat_action)

        file_menu.addSeparator()

        backup_menu = file_menu.addMenu("备份")

        db_backup_action = QAction("数据库备份", self)
        db_backup_action.triggered.connect(self.create_db_backup)
        backup_menu.addAction(db_backup_action)

        full_backup_action = QAction("完整备份", self)
        full_backup_action.triggered.connect(self.create_full_backup)
        backup_menu.addAction(full_backup_action)

        file_menu.addSeparator()

        exit_action = QAction("退出", self)
        exit_action.setShortcut("Ctrl+Q")
        exit_action.triggered.connect(self.close)
        file_menu.addAction(exit_action)

        help_menu = menubar.addMenu("帮助")

        about_action = QAction("关于", self)
        about_action.triggered.connect(self.show_about)
        help_menu.addAction(about_action)

    def create_central_widget(self):
        central_widget = QWidget()
        self.setCentralWidget(central_widget)

        main_layout = QVBoxLayout(central_widget)

        splitter = QSplitter(Qt.Horizontal)

        left_widget = QWidget()
        left_layout = QVBoxLayout(left_widget)
        left_layout.setContentsMargins(0, 0, 0, 0)

        self.collection_list = CollectionListWidget(self.collection_service)
        self.collection_list.collection_selected.connect(self.on_collection_selected)
        self.collection_list.collection_deleted.connect(self.refresh_collections)
        left_layout.addWidget(self.collection_list)

        right_tab = QTabWidget()

        self.detail_tab = CollectionDetailWidget(
            self.collection_service,
            self.repair_service,
            self.exhibition_service,
            self.attachment_manager,
            self.report_generator,
        )
        self.detail_tab.data_changed.connect(self.refresh_collections)
        right_tab.addTab(self.detail_tab, "藏品详情")

        self.statistics_tab = StatisticsPanel(self.collection_service, self.repair_service)
        right_tab.addTab(self.statistics_tab, "统计概览")

        self.backup_tab = BackupPanel(self.backup_manager, self.db)
        right_tab.addTab(self.backup_tab, "备份管理")

        splitter.addWidget(left_widget)
        splitter.addWidget(right_tab)
        splitter.setSizes([400, 1000])

        main_layout.addWidget(splitter)

    def create_status_bar(self):
        self.status_bar = QStatusBar()
        self.setStatusBar(self.status_bar)
        self.update_status()

    def update_status(self):
        stats = self.collection_service.get_statistics()
        total = stats.get("total_collections", 0)
        self.status_bar.showMessage(f"藏品总数: {total} 件")

    def refresh_collections(self):
        self.collection_list.load_collections()
        self.statistics_tab.load_statistics()
        self.update_status()

    def on_collection_selected(self, collection_id: int):
        self.current_collection_id = collection_id
        self.detail_tab.load_collection(collection_id)

    def new_collection(self):
        self.detail_tab.new_collection()

    def export_to_csv(self):
        try:
            collections = self.collection_service.get_collections()
            filepath = self.report_generator.export_collections_to_csv(collections)
            show_info(self, "导出成功", f"已导出到: {filepath}")
        except Exception as e:
            show_error(self, "导出失败", str(e))

    def export_to_json(self):
        try:
            collections = self.collection_service.get_collections()
            filepath = self.report_generator.export_collections_to_json(collections)
            show_info(self, "导出成功", f"已导出到: {filepath}")
        except Exception as e:
            show_error(self, "导出失败", str(e))

    def export_statistics(self):
        try:
            filepath = self.report_generator.generate_statistics_report()
            show_info(self, "导出成功", f"已导出到: {filepath}")
        except Exception as e:
            show_error(self, "导出失败", str(e))

    def create_db_backup(self):
        try:
            filepath = self.backup_manager.create_database_backup()
            show_info(self, "备份成功", f"已备份到: {filepath}")
            self.backup_tab.load_backups()
        except Exception as e:
            show_error(self, "备份失败", str(e))

    def create_full_backup(self):
        try:
            filepath = self.backup_manager.create_full_backup()
            show_info(self, "备份成功", f"已备份到: {filepath}")
            self.backup_tab.load_backups()
        except Exception as e:
            show_error(self, "备份失败", str(e))

    def check_missing_attachments(self):
        try:
            missing = self.attachment_manager.get_missing_attachments()
            if missing:
                QMessageBox.warning(
                    self,
                    "附件丢失提示",
                    f"检测到 {len(missing)} 个附件文件已丢失或损坏。\n"
                    "请检查附件目录或重新上传相关文件。",
                )
        except Exception:
            pass

    def show_about(self):
        QMessageBox.about(
            self,
            "关于",
            f"<h3>{APP_NAME}</h3>"
            f"<p>版本: {APP_VERSION}</p>"
            f"<p>基于 Python + PySide6 + SQLite 开发</p>"
            f"<p>文物藏品管理系统 - 专业的文物收藏管理解决方案</p>",
        )

    def closeEvent(self, event):
        if confirm_action(self, "退出确认", "确定要退出系统吗？"):
            self.db.close()
            event.accept()
        else:
            event.ignore()
