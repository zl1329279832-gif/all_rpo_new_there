from datetime import datetime, timedelta
import random

from database import DatabaseConnection
from services import CollectionService, RepairService, ExhibitionService
from models import Collection, RepairRecord, Exhibition
from config.settings import CATEGORIES, ERAS, SOURCES, CONSERVATION_STATUSES


def generate_demo_data(db: DatabaseConnection, count: int = 20):
    collection_service = CollectionService(db)
    repair_service = RepairService(db)
    exhibition_service = ExhibitionService(db)

    names = [
        "青铜鼎", "青花瓷瓶", "玉璧", "书法作品", "金缕玉衣",
        "陶俑", "瓷器罐", "古画", "铜镜", "玉佩",
        "青铜器", "象牙雕刻", "漆器", "丝绸", "印章",
        "古籍善本", "钱币", "石碑拓片", "木雕", "银器"
    ]

    collections = []
    for i in range(count):
        entry_date = (datetime.now() - timedelta(days=random.randint(1, 365 * 10))).strftime("%Y-%m-%d")

        collection = Collection(
            collection_no=f"WW{datetime.now().strftime('%Y%m%d')}{i+1:03d}",
            name=names[i % len(names)] + (f" #{i+1}" if i >= len(names) else ""),
            era=random.choice(ERAS),
            category=random.choice(CATEGORIES),
            source=random.choice(SOURCES),
            conservation_status=random.choice(CONSERVATION_STATUSES),
            entry_date=entry_date,
            description=f"这是一件珍贵的{random.choice(CATEGORIES)}文物，具有重要的历史价值。",
            location=f"展柜 {random.randint(1, 50)}",
            estimated_value=round(random.uniform(1000, 1000000), 2),
        )
        collection_id = collection_service.create_collection(collection)
        collections.append(collection_id)

        if random.random() > 0.5:
            repair_date = (datetime.now() - timedelta(days=random.randint(1, 365))).strftime("%Y-%m-%d")
            repair = RepairRecord(
                collection_id=collection_id,
                repair_date=repair_date,
                repairer=random.choice(["张师傅", "李师傅", "王师傅", "赵师傅"]),
                reason=random.choice(["表面清洁", "裂纹修复", "色彩修复", "防腐处理"]),
                description="进行了专业的修复工作",
                cost=round(random.uniform(100, 5000), 2),
                status=random.choice(["待修复", "修复中", "已完成", "已取消"]),
            )
            repair_service.create_repair_record(repair)

        if random.random() > 0.6:
            start_date = datetime.now() - timedelta(days=random.randint(30, 180))
            end_date = start_date + timedelta(days=random.randint(7, 60))
            exhibition = Exhibition(
                collection_id=collection_id,
                exhibition_name=random.choice([
                    "春季文物展", "夏季珍品展", "秋季特展", "冬季精品展",
                    "馆藏精品展", "文化交流展", "周年纪念展"
                ]),
                location=random.choice(["北京故宫", "上海博物馆", "南京博物院", "本地博物馆"]),
                start_date=start_date.strftime("%Y-%m-%d"),
                end_date=end_date.strftime("%Y-%m-%d"),
                organizer=random.choice(["国家文物局", "省博物馆", "市文化局"]),
                notes="重要展出记录",
            )
            exhibition_service.create_exhibition(exhibition)

    return collections


def has_demo_data(db: DatabaseConnection) -> bool:
    with db.get_cursor() as cursor:
        cursor.execute("SELECT COUNT(*) FROM collections")
        count = cursor.fetchone()[0]
        return count > 0
