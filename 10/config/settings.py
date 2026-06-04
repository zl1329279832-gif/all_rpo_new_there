import os
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent

DATABASE_PATH = BASE_DIR / "data" / "collections.db"

ATTACHMENTS_DIR = BASE_DIR / "data" / "attachments"
BACKUPS_DIR = BASE_DIR / "data" / "backups"
REPORTS_DIR = BASE_DIR / "data" / "reports"
LOGS_DIR = BASE_DIR / "logs"

ALLOWED_IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"}
ALLOWED_DOC_EXTENSIONS = {".pdf", ".doc", ".docx", ".txt", ".xls", ".xlsx"}
MAX_ATTACHMENT_SIZE = 50 * 1024 * 1024

CATEGORIES = ["青铜器", "陶瓷器", "玉器", "书画", "金银器", "木器", "石刻", "织绣", "其他"]
ERAS = ["新石器时代", "夏", "商", "周", "秦", "汉", "三国", "晋", "南北朝", "隋", "唐", "宋", "元", "明", "清", "民国", "现代"]
SOURCES = ["考古发掘", "征集", "捐赠", "移交", "收购", "其他"]
CONSERVATION_STATUSES = ["完好", "轻微破损", "中度破损", "严重破损", "修复中", "已修复"]
REPAIR_STATUSES = ["待修复", "修复中", "已完成", "已取消"]

APP_NAME = "文物藏品管理系统"
APP_VERSION = "1.0.0"
WINDOW_WIDTH = 1400
WINDOW_HEIGHT = 900

for directory in [ATTACHMENTS_DIR, BACKUPS_DIR, REPORTS_DIR, LOGS_DIR]:
    directory.mkdir(parents=True, exist_ok=True)
