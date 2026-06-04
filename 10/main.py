import sys
from PySide6.QtWidgets import QApplication, QMessageBox

from database import DatabaseConnection, DatabaseInitializationError
from ui import MainWindow
from demo_data import generate_demo_data, has_demo_data
from config.settings import APP_NAME


def main():
    app = QApplication(sys.argv)
    app.setApplicationName(APP_NAME)

    try:
        db = DatabaseConnection()
        db.initialize_database()

        if not has_demo_data(db):
            reply = QMessageBox.question(
                None,
                "初始化数据",
                "检测到数据库为空，是否加载演示数据？\n\n"
                "选择\"是\"将自动生成20条示例藏品记录及相关数据。",
                QMessageBox.Yes | QMessageBox.No,
                QMessageBox.Yes,
            )
            if reply == QMessageBox.Yes:
                generate_demo_data(db, 20)
                QMessageBox.information(None, "完成", "演示数据已加载！")

    except DatabaseInitializationError as e:
        QMessageBox.critical(
            None,
            "数据库初始化失败",
            f"无法初始化数据库：\n{str(e)}\n\n"
            "请检查数据库目录权限或磁盘空间。",
        )
        sys.exit(1)
    except Exception as e:
        QMessageBox.critical(
            None,
            "启动失败",
            f"应用启动时发生错误：\n{str(e)}",
        )
        sys.exit(1)

    window = MainWindow(db)
    window.show()

    sys.exit(app.exec())


if __name__ == "__main__":
    main()
