from PySide6.QtWidgets import (
    QWidget, QVBoxLayout, QHBoxLayout, QLineEdit, QComboBox,
    QPushButton, QListWidget, QListWidgetItem, QLabel, QGroupBox,
)
from PySide6.QtCore import Signal, Qt

from config.settings import CATEGORIES, ERAS, SOURCES, CONSERVATION_STATUSES
from services import CollectionService
from .dialogs import confirm_action, show_error, show_info


class CollectionListWidget(QWidget):
    collection_selected = Signal(int)
    collection_deleted = Signal()

    def __init__(self, collection_service: CollectionService):
        super().__init__()
        self.collection_service = collection_service
        self.collections = []
        self.init_ui()
        self.load_collections()

    def init_ui(self):
        layout = QVBoxLayout(self)

        filter_group = QGroupBox("筛选条件")
        filter_layout = QVBoxLayout(filter_group)

        search_layout = QHBoxLayout()
        self.search_input = QLineEdit()
        self.search_input.setPlaceholderText("搜索藏品编号或名称...")
        self.search_input.textChanged.connect(self.on_search_changed)
        search_layout.addWidget(self.search_input)
        filter_layout.addLayout(search_layout)

        row1_layout = QHBoxLayout()
        self.category_combo = QComboBox()
        self.category_combo.addItem("全部类别")
        self.category_combo.addItems(CATEGORIES)
        self.category_combo.currentIndexChanged.connect(self.apply_filters)
        row1_layout.addWidget(self.category_combo)

        self.era_combo = QComboBox()
        self.era_combo.addItem("全部年代")
        self.era_combo.addItems(ERAS)
        self.era_combo.currentIndexChanged.connect(self.apply_filters)
        row1_layout.addWidget(self.era_combo)
        filter_layout.addLayout(row1_layout)

        row2_layout = QHBoxLayout()
        self.source_combo = QComboBox()
        self.source_combo.addItem("全部来源")
        self.source_combo.addItems(SOURCES)
        self.source_combo.currentIndexChanged.connect(self.apply_filters)
        row2_layout.addWidget(self.source_combo)

        self.status_combo = QComboBox()
        self.status_combo.addItem("全部状态")
        self.status_combo.addItems(CONSERVATION_STATUSES)
        self.status_combo.currentIndexChanged.connect(self.apply_filters)
        row2_layout.addWidget(self.status_combo)
        filter_layout.addLayout(row2_layout)

        btn_layout = QHBoxLayout()
        self.reset_btn = QPushButton("重置筛选")
        self.reset_btn.clicked.connect(self.reset_filters)
        btn_layout.addWidget(self.reset_btn)

        self.refresh_btn = QPushButton("刷新")
        self.refresh_btn.clicked.connect(self.load_collections)
        btn_layout.addWidget(self.refresh_btn)
        filter_layout.addLayout(btn_layout)

        layout.addWidget(filter_group)

        list_label = QLabel("藏品列表")
        layout.addWidget(list_label)

        self.collection_list = QListWidget()
        self.collection_list.itemClicked.connect(self.on_item_clicked)
        layout.addWidget(self.collection_list, 1)

        action_layout = QHBoxLayout()
        self.delete_btn = QPushButton("删除选中")
        self.delete_btn.clicked.connect(self.delete_selected)
        action_layout.addWidget(self.delete_btn)

        self.count_label = QLabel("共 0 件")
        action_layout.addStretch()
        action_layout.addWidget(self.count_label)
        layout.addLayout(action_layout)

    def load_collections(self):
        try:
            self.collections = self.collection_service.get_collections()
            self.update_list_display()
        except Exception as e:
            show_error(self, "加载失败", f"无法加载藏品列表: {str(e)}")

    def on_search_changed(self):
        self.apply_filters()

    def apply_filters(self):
        filters = {}
        search_text = self.search_input.text().strip()
        if search_text:
            filters["collection_no"] = search_text
            filters["name"] = search_text

        if self.category_combo.currentIndex() > 0:
            filters["category"] = self.category_combo.currentText()

        if self.era_combo.currentIndex() > 0:
            filters["era"] = self.era_combo.currentText()

        if self.source_combo.currentIndex() > 0:
            filters["source"] = self.source_combo.currentText()

        if self.status_combo.currentIndex() > 0:
            filters["conservation_status"] = self.status_combo.currentText()

        try:
            self.collections = self.collection_service.get_collections(filters if filters else None)
            self.update_list_display()
        except Exception as e:
            show_error(self, "筛选失败", str(e))

    def reset_filters(self):
        self.search_input.clear()
        self.category_combo.setCurrentIndex(0)
        self.era_combo.setCurrentIndex(0)
        self.source_combo.setCurrentIndex(0)
        self.status_combo.setCurrentIndex(0)
        self.load_collections()

    def update_list_display(self):
        self.collection_list.clear()
        for collection in self.collections:
            item = QListWidgetItem(
                f"[{collection.collection_no}] {collection.name}\n"
                f"{collection.era} · {collection.category} · {collection.conservation_status}"
            )
            item.setData(Qt.UserRole, collection.id)
            self.collection_list.addItem(item)

        self.count_label.setText(f"共 {len(self.collections)} 件")

    def on_item_clicked(self, item: QListWidgetItem):
        collection_id = item.data(Qt.UserRole)
        self.collection_selected.emit(collection_id)

    def delete_selected(self):
        current_item = self.collection_list.currentItem()
        if not current_item:
            show_info(self, "提示", "请先选择要删除的藏品")
            return

        collection_id = current_item.data(Qt.UserRole)
        collection_name = current_item.text().split("\n")[0]

        if not confirm_action(
            self, "删除确认", f"确定要删除藏品\n{collection_name}\n吗？\n此操作不可恢复！"
        ):
            return

        try:
            if self.collection_service.delete_collection(collection_id):
                self.collections = [c for c in self.collections if c.id != collection_id]
                self.update_list_display()
                self.collection_deleted.emit()
                show_info(self, "删除成功", "藏品已删除")
            else:
                show_error(self, "删除失败", "无法删除藏品")
        except Exception as e:
            show_error(self, "删除失败", str(e))
