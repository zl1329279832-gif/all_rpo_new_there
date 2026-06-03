import os
from pathlib import Path

BASE_DIR = Path(__file__).parent.parent

APP_NAME = "实验室样品登记与检测系统"
APP_VERSION = "1.0.0"

DATABASE_PATH = BASE_DIR / "data" / "laboratory.db"
ATTACHMENT_DIR = BASE_DIR / "attachments"
REPORT_DIR = BASE_DIR / "reports"
BACKUP_DIR = BASE_DIR / "backups"
LOG_DIR = BASE_DIR / "logs"

SAMPLE_STATUS = [
    "待检测",
    "检测中",
    "检测完成",
    "报告已生成",
    "已归档"
]

TEST_ITEMS = [
    "理化检测",
    "微生物检测",
    "重金属检测",
    "农残检测",
    "兽药残留检测",
    "添加剂检测",
    "毒理检测",
    "其他"
]

DEPARTMENTS = [
    "质检部",
    "研发部",
    "生产部",
    "客户送检",
    "第三方机构",
    "其他"
]

TESTERS = [
    "张三",
    "李四",
    "王五",
    "赵六",
    "陈七"
]


def ensure_directories():
    for directory in [
        BASE_DIR / "data",
        ATTACHMENT_DIR,
        REPORT_DIR,
        BACKUP_DIR,
        LOG_DIR
    ]:
        directory.mkdir(parents=True, exist_ok=True)
