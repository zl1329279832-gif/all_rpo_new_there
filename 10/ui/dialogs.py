from PySide6.QtWidgets import QMessageBox, QInputDialog, QFileDialog
from PySide6.QtCore import Qt


def show_error(parent, title: str, message: str):
    msg_box = QMessageBox(parent)
    msg_box.setIcon(QMessageBox.Critical)
    msg_box.setWindowTitle(title)
    msg_box.setText(message)
    msg_box.setStandardButtons(QMessageBox.Ok)
    msg_box.exec()


def show_info(parent, title: str, message: str):
    msg_box = QMessageBox(parent)
    msg_box.setIcon(QMessageBox.Information)
    msg_box.setWindowTitle(title)
    msg_box.setText(message)
    msg_box.setStandardButtons(QMessageBox.Ok)
    msg_box.exec()


def show_warning(parent, title: str, message: str) -> bool:
    msg_box = QMessageBox(parent)
    msg_box.setIcon(QMessageBox.Warning)
    msg_box.setWindowTitle(title)
    msg_box.setText(message)
    msg_box.setStandardButtons(QMessageBox.Ok | QMessageBox.Cancel)
    return msg_box.exec() == QMessageBox.Ok


def confirm_action(parent, title: str, message: str) -> bool:
    msg_box = QMessageBox(parent)
    msg_box.setIcon(QMessageBox.Question)
    msg_box.setWindowTitle(title)
    msg_box.setText(message)
    msg_box.setStandardButtons(QMessageBox.Yes | QMessageBox.No)
    msg_box.setDefaultButton(QMessageBox.No)
    return msg_box.exec() == QMessageBox.Yes


def input_text(parent, title: str, label: str, default_text: str = "") -> tuple[str, bool]:
    text, ok = QInputDialog.getText(parent, title, label, text=default_text)
    return text, ok


def select_file(parent, title: str, file_filter: str = "") -> str:
    file_path, _ = QFileDialog.getOpenFileName(parent, title, "", file_filter)
    return file_path


def select_files(parent, title: str, file_filter: str = "") -> list[str]:
    file_paths, _ = QFileDialog.getOpenFileNames(parent, title, "", file_filter)
    return file_paths


def select_save_file(parent, title: str, file_filter: str = "") -> str:
    file_path, _ = QFileDialog.getSaveFileName(parent, title, "", file_filter)
    return file_path


def select_directory(parent, title: str) -> str:
    directory = QFileDialog.getExistingDirectory(parent, title)
    return directory
