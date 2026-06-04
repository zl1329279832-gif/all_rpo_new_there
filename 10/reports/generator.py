import csv
import json
from pathlib import Path
from datetime import datetime
from typing import Optional, List, Dict, Any

from config.settings import REPORTS_DIR
from database import DatabaseConnection, CollectionRepository, RepairRecordRepository
from models import Collection


class ReportGenerator:
    def __init__(self, db: DatabaseConnection):
        self.db = db
        self.collection_repo = CollectionRepository(db)
        self.repair_repo = RepairRecordRepository(db)
        self.reports_dir = Path(REPORTS_DIR)
        self.reports_dir.mkdir(parents=True, exist_ok=True)

    def _generate_filename(self, prefix: str, ext: str) -> str:
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        return f"{prefix}_{timestamp}.{ext}"

    def export_collections_to_csv(
        self, collections: List[Collection], filename: Optional[str] = None
    ) -> str:
        if not filename:
            filename = self._generate_filename("collections", "csv")

        filepath = self.reports_dir / filename

        with open(filepath, "w", newline="", encoding="utf-8-sig") as f:
            writer = csv.writer(f)
            writer.writerow(
                [
                    "藏品编号",
                    "名称",
                    "年代",
                    "类别",
                    "来源",
                    "保存状态",
                    "入库时间",
                    "存放位置",
                    "估值",
                    "描述",
                    "创建时间",
                    "更新时间",
                ]
            )

            for item in collections:
                writer.writerow(
                    [
                        item.collection_no,
                        item.name,
                        item.era,
                        item.category,
                        item.source,
                        item.conservation_status,
                        item.entry_date or "",
                        item.location or "",
                        item.estimated_value or "",
                        item.description or "",
                        item.created_at,
                        item.updated_at,
                    ]
                )

        return str(filepath)

    def export_collections_to_json(
        self, collections: List[Collection], filename: Optional[str] = None
    ) -> str:
        if not filename:
            filename = self._generate_filename("collections", "json")

        filepath = self.reports_dir / filename

        data = [item.to_dict() for item in collections]
        with open(filepath, "w", encoding="utf-8") as f:
            json.dump(data, f, ensure_ascii=False, indent=2)

        return str(filepath)

    def generate_statistics_report(self, filename: Optional[str] = None) -> str:
        if not filename:
            filename = self._generate_filename("statistics", "txt")

        filepath = self.reports_dir / filename

        stats = self.collection_repo.get_statistics()
        repair_stats = self.repair_repo.get_statistics()

        lines = []
        lines.append("=" * 60)
        lines.append("文物藏品统计报告")
        lines.append(f"生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        lines.append("=" * 60)
        lines.append("")

        lines.append("【藏品概览】")
        lines.append(f"藏品总数: {stats['total_collections']} 件")
        lines.append("")

        lines.append("【按类别统计】")
        for category, count in stats.get("by_category", {}).items():
            lines.append(f"  {category}: {count} 件")
        lines.append("")

        lines.append("【按年代统计】")
        for era, count in stats.get("by_era", {}).items():
            lines.append(f"  {era}: {count} 件")
        lines.append("")

        lines.append("【按保存状态统计】")
        for status, count in stats.get("by_status", {}).items():
            lines.append(f"  {status}: {count} 件")
        lines.append("")

        lines.append("【按来源统计】")
        for source, count in stats.get("by_source", {}).items():
            lines.append(f"  {source}: {count} 件")
        lines.append("")

        lines.append("【修复统计】")
        lines.append(f"修复记录总数: {repair_stats.get('total_repairs', 0)} 条")
        lines.append(f"修复总费用: {repair_stats.get('total_cost', 0):.2f} 元")
        for status, count in repair_stats.get("by_status", {}).items():
            lines.append(f"  {status}: {count} 条")
        lines.append("")

        lines.append("=" * 60)
        lines.append("报告结束")
        lines.append("=" * 60)

        with open(filepath, "w", encoding="utf-8") as f:
            f.write("\n".join(lines))

        return str(filepath)

    def generate_collection_detail_report(
        self, collection: Collection, filename: Optional[str] = None
    ) -> str:
        if not filename:
            filename = self._generate_filename(f"collection_{collection.collection_no}", "txt")

        filepath = self.reports_dir / filename

        repair_records = self.repair_repo.get_all(collection.id)

        lines = []
        lines.append("=" * 60)
        lines.append("藏品详情报告")
        lines.append(f"生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        lines.append("=" * 60)
        lines.append("")

        lines.append("【基本信息】")
        lines.append(f"藏品编号: {collection.collection_no}")
        lines.append(f"藏品名称: {collection.name}")
        lines.append(f"年代: {collection.era}")
        lines.append(f"类别: {collection.category}")
        lines.append(f"来源: {collection.source}")
        lines.append(f"保存状态: {collection.conservation_status}")
        lines.append(f"入库时间: {collection.entry_date or '未填写'}")
        lines.append(f"存放位置: {collection.location or '未填写'}")
        lines.append(f"估值: {collection.estimated_value or '未评估'} 元")
        lines.append("")

        lines.append("【藏品描述】")
        lines.append(collection.description or "无描述")
        lines.append("")

        lines.append(f"【修复记录】共 {len(repair_records)} 条")
        for i, record in enumerate(repair_records, 1):
            lines.append(f"  记录 {i}:")
            lines.append(f"    修复日期: {record.repair_date}")
            lines.append(f"    修复人员: {record.repairer or '未填写'}")
            lines.append(f"    修复原因: {record.reason}")
            lines.append(f"    修复状态: {record.status}")
            lines.append(f"    修复费用: {record.cost or 0:.2f} 元")
            lines.append("")

        lines.append("=" * 60)
        lines.append("报告结束")
        lines.append("=" * 60)

        with open(filepath, "w", encoding="utf-8") as f:
            f.write("\n".join(lines))

        return str(filepath)

    def get_reports_list(self) -> List[Dict[str, Any]]:
        reports = []
        for file in self.reports_dir.iterdir():
            if file.is_file():
                reports.append(
                    {
                        "name": file.name,
                        "path": str(file),
                        "size": file.stat().st_size,
                        "created_at": datetime.fromtimestamp(file.stat().st_mtime).strftime(
                            "%Y-%m-%d %H:%M:%S"
                        ),
                    }
                )
        return sorted(reports, key=lambda x: x["created_at"], reverse=True)
