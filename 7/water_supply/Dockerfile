FROM python:3.11-slim

LABEL maintainer="water-supply-analyzer"
LABEL description="城市供水管网运行分析系统"

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    fonts-wqy-zenhei \
    fonts-noto-cjk \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

RUN mkdir -p /app/simulation_results /app/output_charts /app/logs

EXPOSE 8501

HEALTHCHECK --interval=30s --timeout=10s --start-period=15s --retries=3 \
    CMD python -c "import urllib.request; urllib.request.urlopen('http://localhost:8501/_stcore/health')" || exit 1

ENTRYPOINT ["streamlit", "run", "app.py", \
    "--server.headless", "true", \
    "--server.port", "8501", \
    "--server.address", "0.0.0.0", \
    "--browser.gatherUsageStats", "false", \
    "--server.maxUploadSize", "50"]
