# 智能仓储机器人 3D 展示系统 - Docker 部署指南

## 📋 项目简介

智能仓储机器人三维模型展示系统，基于 Vue 3 + TypeScript + Three.js 构建，支持机器人模型展示、动画演示、交互操作等功能。

---

## 🚀 快速开始（推荐方式）

### 使用 Docker Compose 一键部署

```bash
# 1. 克隆或下载项目代码
git clone <repository-url>
cd <project-directory>

# 2. 一键构建并启动
docker-compose up -d

# 3. 访问应用
打开浏览器访问: http://localhost:8080
```

---

## 🔧 手动构建 Docker 镜像

### 构建镜像

```bash
# 构建镜像
docker build -t warehouse-robot-viewer:latest .

# 或使用国内镜像源加速
docker build --build-arg NODE_MIRROR=https://registry.npmmirror.com -t warehouse-robot-viewer:latest .
```

### 运行容器

```bash
# 后台运行
docker run -d \
  --name warehouse-robot-viewer \
  -p 8080:80 \
  --restart unless-stopped \
  warehouse-robot-viewer:latest
```

---

## 📁 文件说明

| 文件名 | 说明 |
|--------|------|
| `Dockerfile` | Docker 镜像构建文件（多阶段构建） |
| `docker-compose.yml` | Docker Compose 编排配置 |
| `.dockerignore` | Docker 构建排除文件列表 |
| `nginx.conf` | Nginx 服务器配置 |
| `DEPLOY.md` | 本部署指南 |

---

## 🔍 Dockerfile 详解

采用 **多阶段构建** 策略，最终镜像仅包含运行时所需的静态资源和 Nginx 服务器，镜像体积更小、更安全。

### 构建阶段（node:20-alpine）
1. 基于 Node.js 20 Alpine 镜像
2. 安装项目依赖
3. 执行生产环境构建（`npm run build`）

### 运行阶段（nginx:alpine）
1. 基于 Nginx Alpine 镜像（轻量级，约 20MB）
2. 复制构建产物到 Nginx 静态资源目录
3. 配置 Gzip 压缩和缓存策略
4. 配置健康检查
5. 暴露 80 端口

---

## ⚙️ docker-compose.yml 配置说明

### 端口映射
```yaml
ports:
  - "8080:80"   # 主机端口:容器端口
```
如需修改访问端口，只需修改 `8080` 为其他端口即可。

### 资源限制
```yaml
deploy:
  resources:
    limits:
      cpus: '1.0'      # 最多使用 1 核 CPU
      memory: 512M    # 最多使用 512MB 内存
```

### 健康检查
- 间隔 30 秒检查一次服务健康状态
- 超时 3 秒视为失败
- 连续 3 次失败标记为不健康

---

## 🔧 常用命令

### Docker Compose 命令

```bash
# 构建并启动（后台运行）
docker-compose up -d

# 仅构建镜像
docker-compose build

# 停止并删除容器
docker-compose down

# 查看日志
docker-compose logs -f

# 重启服务
docker-compose restart

# 查看服务状态
docker-compose ps
```

### Docker 常用命令

```bash
# 查看运行中的容器
docker ps

# 查看所有容器
docker ps -a

# 查看镜像
docker images

# 删除镜像
docker rmi warehouse-robot-viewer:latest

# 停止容器
docker stop warehouse-robot-viewer

# 启动容器
docker start warehouse-robot-viewer

# 重启容器
docker restart warehouse-robot-viewer

# 删除容器
docker rm warehouse-robot-viewer

# 进入容器
docker exec -it warehouse-robot-viewer sh

# 查看容器日志
docker logs -f warehouse-robot-viewer
```

---

## 🌐 访问应用

启动成功后，可以通过以下方式访问：

| 环境 | 地址 |
|------|------|
| 本地访问 | http://localhost:8080 |
| 局域网访问 | http://<服务器IP>:8080 |
| 健康检查 | http://localhost:8080/health |

---

## 🚀 生产环境部署建议

### 1. 使用 HTTPS
建议使用 Nginx 反向代理配置 SSL 证书，或使用 Traefik 作为入口网关。

### 2. 配置域名
修改 `nginx.conf` 中的 `server_name` 为实际域名：
```nginx
server_name  your-domain.com;
```

### 3. 负载均衡
如需部署多实例，可配合以下方案：
- Docker Swarm
- Kubernetes
- Nginx 负载均衡

### 4. 监控告警
- 集成 Prometheus + Grafana 监控
- 配置健康检查告警

---

## ❓ 故障排查

### 1. 端口被占用
```
Error starting userland proxy: listen tcp4 0.0.0.0:8080: bind: address already in use
```
**解决方法**：修改 `docker-compose.yml` 中的端口映射，或停止占用 8080 端口的程序。

### 2. 镜像构建失败（网络问题）
```
npm ERR! network timeout
```
**解决方法**：使用国内 npm 镜像源，修改 Dockerfile：
```dockerfile
RUN npm ci --registry=https://registry.npmmirror.com
```

### 3. 服务无法访问
检查服务状态：
```bash
# 查看容器是否运行
docker ps

# 查看容器日志
docker logs warehouse-robot-viewer

# 测试健康检查
curl http://localhost:8080/health
```

### 4. 清除所有资源（重新部署）
```bash
# 停止并删除容器
docker-compose down

# 删除旧镜像
docker rmi warehouse-robot-viewer:latest

# 重新构建并启动
docker-compose up -d --build
```

---

## 📊 资源使用

| 资源 | 推荐配置 | 最小配置 |
|------|---------|---------|
| CPU | 1 核 | 0.5 核 |
| 内存 | 512MB | 256MB |
| 磁盘 | 1GB | 500MB |

> **注意**：由于应用是纯前端静态资源，运行时资源消耗很低，大部分资源消耗在构建阶段。

---

## 📝 更新部署

```bash
# 拉取最新代码
git pull

# 重新构建并启动
docker-compose up -d --build

# 查看日志确认启动成功
docker-compose logs -f
```

---

## 📞 技术支持

如有问题，请检查：
1. Docker 版本是否 >= 20.10
2. Docker Compose 版本是否 >= 1.29
3. 服务器是否有足够的内存和磁盘空间

---

## 📄 许可证

本项目仅供学习和展示使用。
