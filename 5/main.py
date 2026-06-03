import sys
import os
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))

from PySide6.QtWidgets import QApplication
from PySide6.QtCore import Qt
from ui.main_window import MainWindow
from config.settings import APP_NAME, APP_VERSION
from services.database_service import DatabaseService


def main():
    os.environ['QT_AUTO_SCREEN_SCALE_FACTOR'] = '1'

    app = QApplication(sys.argv)
    app.setApplicationName(APP_NAME)
    app.setApplicationVersion(APP_VERSION)
    app.setStyle('Fusion')

    try:
        db_service = DatabaseService()
        db_service.initialize_database()
        db_service.insert_demo_data()
    except Exception as e:
        from PySide6.QtWidgets import QMessageBox
        QMessageBox.critical(None, "数据库初始化失败", f"无法初始化数据库：\n{str(e)}")
        sys.exit(1)

    window = MainWindow()
    window.show()

    sys.exit(app.exec())


if __name__ == '__main__':
    main()
