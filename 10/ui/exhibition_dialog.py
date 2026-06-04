from PySide6.QtWidgets import (
    QDialog, QVBoxLayout, QFormLayout, QLineEdit, QDateEdit,
    QTextEdit, QDialogButtonBox,
)
from PySide6.QtCore import QDate

from models import Exhibition


class ExhibitionDialog(QDialog):
    def __init__(self, parent=None):
        super().__init__(parent)
        self.setWindowTitle("添加展出记录")
        self.setMinimumWidth(400)
        self.init_ui()

    def init_ui(self):
        layout = QVBoxLayout(self)
        form_layout = QFormLayout()

        self.name_edit = QLineEdit()
        form_layout.addRow("展览名称:", self.name_edit)

        self.location_edit = QLineEdit()
        form_layout.addRow("展览地点:", self.location_edit)

        self.start_date_edit = QDateEdit()
        self.start_date_edit.setCalendarPopup(True)
        self.start_date_edit.setDate(QDate.currentDate())
        self.start_date_edit.setDisplayFormat("yyyy-MM-dd")
        form_layout.addRow("开始日期:", self.start_date_edit)

        self.end_date_edit = QDateEdit()
        self.end_date_edit.setCalendarPopup(True)
        self.end_date_edit.setDate(QDate.currentDate())
        self.end_date_edit.setDisplayFormat("yyyy-MM-dd")
        form_layout.addRow("结束日期:", self.end_date_edit)

        self.organizer_edit = QLineEdit()
        form_layout.addRow("主办方:", self.organizer_edit)

        self.notes_edit = QTextEdit()
        self.notes_edit.setMaximumHeight(100)
        form_layout.addRow("备注:", self.notes_edit)

        layout.addLayout(form_layout)

        buttons = QDialogButtonBox(QDialogButtonBox.Ok | QDialogButtonBox.Cancel)
        buttons.accepted.connect(self.accept)
        buttons.rejected.connect(self.reject)
        layout.addWidget(buttons)

    def get_exhibition(self) -> Exhibition:
        return Exhibition(
            exhibition_name=self.name_edit.text().strip(),
            location=self.location_edit.text().strip(),
            start_date=self.start_date_edit.date().toString("yyyy-MM-dd"),
            end_date=self.end_date_edit.date().toString("yyyy-MM-dd"),
            organizer=self.organizer_edit.text().strip(),
            notes=self.notes_edit.toPlainText().strip(),
        )
