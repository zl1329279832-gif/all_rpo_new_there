import os
from pathlib import Path

BASE_DIR = Path(__file__).parent.parent

APP_NAME = "实验室样品登记与检测系统"
APP_VERSION = "2.0.0"

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
    "已归档",
    "已作废"
]

STATUS_TRANSITION_RULES = {
    "待检测": ["检测中", "已作废"],
    "检测中": ["待检测", "检测完成", "已作废"],
    "检测完成": ["检测中", "报告已生成", "已作废"],
    "报告已生成": ["检测完成", "已归档", "已作废"],
    "已归档": ["报告已生成"],
    "已作废": []
}

STATUS_COLORS = {
    "待检测": "#FFF3CD",
    "检测中": "#CCE5FF",
    "检测完成": "#D4EDDA",
    "报告已生成": "#E2E3E5",
    "已归档": "#D6D8DB",
    "已作废": "#F8D7DA"
}

STATUS_DESCRIPTIONS = {
    "待检测": "样品已登记，等待开始检测",
    "检测中": "检测人员正在进行检测",
    "检测完成": "所有检测项目已完成，可以生成报告",
    "报告已生成": "检测报告已生成",
    "已归档": "样品已归档，流程结束",
    "已作废": "样品已作废，流程终止"
}

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
