#!/bin/bash

set -e

echo "========================================"
echo "实验室样品管理系统 - Docker启动"
echo "========================================"

VNC_PASSWORD=${VNC_PASSWORD:-lab123456}
RESOLUTION=${RESOLUTION:-1280x800x24}

echo "VNC端口: 5900"
echo "VNC密码: $VNC_PASSWORD"
echo "分辨率: $RESOLUTION"
echo ""

mkdir -p /root/.vnc
x11vnc -storepasswd "$VNC_PASSWORD" /root/.vnc/passwd

mkdir -p /var/log/supervisor

cd /app
python -c "
from config.settings import ensure_directories
ensure_directories()
print('目录初始化完成')
"

python -c "
from services.database_service import DatabaseService
db = DatabaseService()
db.initialize_database()
try:
    db.insert_demo_data()
except Exception as e:
    pass
print('数据库初始化完成')
"

echo ""
echo "========================================"
echo "启动完成！"
echo "VNC连接地址: localhost:5900"
echo "浏览器访问: http://localhost:8080 (需要noVNC)"
echo "========================================"
echo ""

exec supervisord -c /etc/supervisor/conf.d/supervisord.conf
