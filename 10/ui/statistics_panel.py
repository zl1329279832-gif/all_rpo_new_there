from PySide6.QtWidgets import (
    QWidget, QVBoxLayout, QHBoxLayout, QGridLayout, QLabel, QGroupBox,
    QPushButton,
)
from PySide6.QtCore import Qt

from services import CollectionService, RepairService
from .dialogs import show_error


class StatisticsPanel(QWidget):
    def __init__(
        self,
        collection_service: CollectionService,
        repair_service: RepairService,
    ):
        super().__init__()
        self.collection_service = collection_service
        self.repair_service = repair_service
        self.init_ui()
        self.load_statistics()

    def init_ui(self):
        layout = QVBoxLayout(self)

        refresh_btn = QPushButton("刷新统计")
        refresh_btn.clicked.connect(self.load_statistics)
        layout.addWidget(refresh_btn)

        overview_group = QGroupBox("藏品概览")
        overview_layout = QGridLayout(overview_group)

        self.total_label = self.create_stat_card("藏品总数", "0")
        overview_layout.addLayout(self.total_label, 0, 0)

        self.repair_count_label = self.create_stat_card("修复记录", "0")
        overview_layout.addLayout(self.repair_count_label, 0, 1)

        self.repair_cost_label = self.create_stat_card("修复总费用", "¥ 0.00")
        overview_layout.addLayout(self.repair_cost_label, 0, 2)

        layout.addWidget(overview_group)

        content_layout = QHBoxLayout()

        category_group = QGroupBox("按类别统计")
        category_layout = QVBoxLayout(category_group)
        self.category_labels = []
        content_layout.addWidget(category_group, 1)

        era_group = QGroupBox("按年代统计")
        era_layout = QVBoxLayout(era_group)
        self.era_labels = []
        content_layout.addWidget(era_group, 1)

        status_group = QGroupBox("按保存状态统计")
        status_layout = QVBoxLayout(status_group)
        self.status_labels = []
        content_layout.addWidget(status_group, 1)

        source_group = QGroupBox("按来源统计")
        source_layout = QVBoxLayout(source_group)
        self.source_labels = []
        content_layout.addWidget(source_group, 1)

        layout.addLayout(content_layout, 1)

    def create_stat_card(self, title: str, value: str) -> QVBoxLayout:
        layout = QVBoxLayout()
        title_label = QLabel(title)
        title_label.setAlignment(Qt.AlignCenter)
        title_label.setStyleSheet("font-size: 14px; color: #666;")

        value_label = QLabel(value)
        value_label.setAlignment(Qt.AlignCenter)
        value_label.setStyleSheet("font-size: 24px; font-weight: bold; color: #2c3e50;")
        value_label.setObjectName("stat_value")

        layout.addWidget(title_label)
        layout.addWidget(value_label)
        return layout

    def load_statistics(self):
        try:
            stats = self.collection_service.get_statistics()
            repair_stats = self.repair_service.get_statistics()

            self.update_stat_card(self.total_label, str(stats.get("total_collections", 0)))
            self.update_stat_card(self.repair_count_label, str(repair_stats.get("total_repairs", 0)))
            self.update_stat_card(
                self.repair_cost_label,
                f"¥ {repair_stats.get('total_cost', 0):.2f}"
            )

            self.update_stat_list(
                self.category_labels,
                stats.get("by_category", {}),
                self.findChild(QGroupBox, "category_group") or self.findChildren(QGroupBox)[1]
            )
            self.update_stat_list(
                self.era_labels,
                stats.get("by_era", {}),
                self.findChildren(QGroupBox)[2]
            )
            self.update_stat_list(
                self.status_labels,
                stats.get("by_status", {}),
                self.findChildren(QGroupBox)[3]
            )
            self.update_stat_list(
                self.source_labels,
                stats.get("by_source", {}),
                self.findChildren(QGroupBox)[4]
            )

        except Exception as e:
            show_error(self, "加载失败", f"无法加载统计数据: {str(e)}")

    def update_stat_card(self, layout: QVBoxLayout, value: str):
        for i in range(layout.count()):
            widget = layout.itemAt(i).widget()
            if widget and widget.objectName() == "stat_value":
                widget.setText(value)
                break

    def update_stat_list(self, labels: list, data: dict, group: QGroupBox):
        layout = group.layout()
        while layout.count():
            item = layout.takeAt(0)
            widget = item.widget()
            if widget:
                widget.deleteLater()
        labels.clear()

        for key, value in data.items():
            label = QLabel(f"{key}: {value} 件")
            label.setStyleSheet("padding: 5px; font-size: 13px;")
            layout.addWidget(label)
            labels.append(label)

        layout.addStretch()
