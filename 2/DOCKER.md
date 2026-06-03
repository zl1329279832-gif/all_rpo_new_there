# Docker 一键部署说明

## 快速启动

### 一键启动所有服务（推荐）：

```bash
docker-compose up -d
```

启动后访问：http://localhost

---

## 服务说明

| 服务 | 容器名称 | 端口 | 说明 |
|------|---------|------|------|
| 前端 | medical-device-frontend | 80 | Nginx + Vue 3 构建产物 |
| 后端 | medical-device-backend | 8080 | Spring Boot 应用 |
| MySQL | medical-device-mysql | 3306 | 数据库（自动初始化表结构和数据） |
| Redis | medical-device-redis | 6379 | 缓存服务 |

---

## 详细部署步骤

### 1. 环境要求

- Docker 20.10+
- Docker Compose 2.0+
- 至少 4GB 可用内存
- 至少 10GB 可用磁盘空间

### 2. 一键启动

```bash
# 进入项目根目录
cd medical-device-management

# 构建并启动所有服务（后台运行）
docker-compose up -d
```

### 3. 查看启动日志

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看单个服务日志
docker-compose logs -f backend
docker-compose logs -f mysql
```

### 4. 停止服务

```bash
# 停止并保留数据
docker-compose stop

# 停止并删除容器（保留数据卷）
docker-compose down

# 停止并删除所有（包括数据卷，⚠️ 数据会丢失）
docker-compose down -v
```

### 5. 重新构建

```bash
# 代码更新后重新构建并启动
docker-compose up -d --build
```

---

## 默认账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | 123456 | 系统管理员 | 全部权限 |
| device_admin | 123456 | 设备管理员 | 设备管理权限 |
| engineer1 | 123456 | 维修工程师 | 维修工单权限 |
| qc_staff | 123456 | 质控人员 | 质控记录权限 |

---

## 访问地址

- **系统首页：http://localhost
- **接口文档：http://localhost:8080/api/doc.html
- **MySQL连接：localhost:3306/medical_device
  - 用户名：root
  - 密码：root123

---

## 数据持久化

MySQL和Redis的数据通过Docker Volume持久化，即使删除容器，数据也不会丢失。

数据卷名称：
- `medical-device-management_mysql-data` - MySQL数据
- `medical-device-management_redis-data` - Redis数据

---

## 常见问题

### 1. 端口被占用

如果80端口被占用，修改 `docker-compose.yml` 中的端口映射：

```yaml
frontend:
  ports:
    - "8088:80  # 将左边的8088改为可用端口
```

然后访问：http://localhost:8088

### 2. 后端启动失败

检查MySQL是否启动完成：

```bash
docker-compose logs mysql
```

如果MySQL未就绪，后端会自动重试。后端会自动等待MySQL健康检查通过后才启动。

### 3. 前端无法访问后端API

检查后端服务是否正常：

```bash
docker-compose ps
docker-compose logs backend
```

### 4. 清除所有数据重新初始化

```bash
docker-compose down -v
docker-compose up -d
```

---

## 服务依赖关系

```
frontend (Nginx:80)
    ↓
backend (Spring Boot:8080)
    ↓   ↓
mysql (MySQL:3306)   redis (Redis:6379)
```

启动顺序：
1. MySQL 和 Redis 并行启动
2. 等待 MySQL 和 Redis 健康检查通过
3. 启动后端服务
4. 启动前端服务
