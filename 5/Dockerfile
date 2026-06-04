FROM python:3.11-slim

LABEL maintainer="实验室样品管理系统"
LABEL description="实验室样品登记与检测记录系统 - Docker版"

ENV DEBIAN_FRONTEND=noninteractive
ENV PYTHONUNBUFFERED=1
ENV QT_QPA_PLATFORM=xcb
ENV DISPLAY=:0

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    libxcb-cursor0 \
    libxcb-icccm4 \
    libxcb-image0 \
    libxcb-keysyms1 \
    libxcb-randr0 \
    libxcb-render-util0 \
    libxcb-shape0 \
    libxcb-sync1 \
    libxcb-xfixes0 \
    libxcb-xinerama0 \
    libxcb-xkb1 \
    libxkbcommon-x11-0 \
    libxkbcommon0 \
    libfontconfig1 \
    libfreetype6 \
    libx11-xcb1 \
    libglib2.0-0 \
    libgl1-mesa-glx \
    libegl1 \
    fonts-wqy-zenhei \
    fonts-wqy-microhei \
    x11vnc \
    xvfb \
    supervisor \
    net-tools \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

RUN mkdir -p /app/data /app/attachments /app/reports /app/backups /app/logs

COPY docker/supervisord.conf /etc/supervisor/conf.d/supervisord.conf
COPY docker/start.sh /start.sh
RUN chmod +x /start.sh

EXPOSE 5900 8080

VOLUME ["/app/data", "/app/attachments", "/app/reports", "/app/backups", "/app/logs"]

CMD ["/start.sh"]
