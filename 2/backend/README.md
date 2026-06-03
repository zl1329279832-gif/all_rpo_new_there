# 后端运行说明

## 技术栈说明

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.1.8 | 核心框架 |
| MyBatis Plus | 3.5.8 | ORM 框架 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 6.0+ | 缓存数据库 |
| Spring Security | 6.x | 安全框架 |
| JWT | 0.12.6 | Token 认证 |
| Knife4j | 4.5.0 | 接口文档 |
| EasyExcel | 3.3.4 | Excel 处理 |
| Lombok | - | 代码简化工具 |

## 环境要求

- **JDK**: 17 或更高版本
- **Maven**: 3.8 或更高版本
- **MySQL**: 8.0 或更高版本
- **Redis**: 6.0 或更高版本

### 环境验证

```bash
# 检查 JDK 版本
java -version

# 检查 Maven 版本
mvn -version

# 检查 MySQL 版本（登录后执行）
SELECT VERSION();

# 检查 Redis 版本
redis-server --version
```

## 数据库初始化步骤

### 1. 创建数据库并执行初始化脚本

```bash
# 方式一：使用 MySQL 命令行
mysql -u root -p

# 登录后执行
source backend/src/main/resources/sql/init.sql
source backend/src/main/resources/sql/data.sql

# 方式二：直接执行脚本文件
mysql -u root -p < src/main/resources/sql/init.sql
mysql -u root -p medical_device < src/main/resources/sql/data.sql
```

### 2. 脚本说明

- `init.sql`：创建数据库、所有数据表结构和索引
- `data.sql`：插入初始化数据（用户、角色、科室、设备、巡检计划等测试数据）

### 3. 验证数据库

登录 MySQL 后执行：

```sql
USE medical_device;
SHOW TABLES;
SELECT * FROM sys_user;
```

## 配置修改说明

配置文件位置：`src/main/resources/application.yml`

### 1. 数据库配置

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3307/medical_device?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: root123
```

**修改说明**：
- 修改 `url` 中的主机地址、端口（默认 3306）和数据库名
- 修改 `username` 和 `password` 为实际的数据库账号密码

### 2. Redis 配置

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:
      database: 0
      timeout: 10000ms
```

**修改说明**：
- 修改 `host` 和 `port` 为实际的 Redis 地址和端口
- 如果 Redis 设置了密码，填写 `password` 字段
- 根据需要修改 `database` 序号（0-15）

### 3. JWT 配置

```yaml
jwt:
  secret: medical-device-management-system-secret-key-2024
  expiration: 86400000
  header: Authorization
  prefix: Bearer
```

**修改说明**：
- `secret`：JWT 签名密钥，生产环境建议修改为复杂随机字符串
- `expiration`：Token 过期时间，单位毫秒（默认 24 小时）

### 4. 服务端口配置

```yaml
server:
  port: 8080
  servlet:
    context-path: /api
```

**修改说明**：
- `port`：服务端口，默认 8080
- `context-path`：应用上下文路径，默认 `/api`

## 启动命令

### 方式一：使用 Maven 启动（推荐开发环境）

```bash
# 进入后端目录
cd backend

# 编译项目
mvn clean compile

# 启动应用
mvn spring-boot:run
```

### 方式二：打包后运行（推荐生产环境）

```bash
# 进入后端目录
cd backend

# 打包项目（跳过测试）
mvn clean package -DskipTests

# 运行 jar 包
java -jar target/device-management-1.0.0.jar

# 带内存参数运行
java -Xms512m -Xmx1024m -jar target/device-management-1.0.0.jar
```

### 方式三：使用 IDE 启动

1. 导入项目到 IntelliJ IDEA 或 Eclipse
2. 找到主类 `DeviceManagementApplication.java`
3. 右键点击 `Run 'DeviceManagementApplication'`

## 验证启动成功

启动成功后，控制台会输出类似信息：

```
Started DeviceManagementApplication in x.xxx seconds
```

## 接口文档访问地址

项目集成了 Knife4j 接口文档，启动后可通过以下地址访问：

- **Knife4j 文档地址**: http://localhost:8080/api/doc.html
- **OpenAPI JSON**: http://localhost:8080/api/v3/api-docs

### 接口文档使用说明

1. 访问 http://localhost:8080/api/doc.html
2. 点击左侧「登录接口」→ `POST /api/auth/login`
3. 点击「调试」，输入用户名密码获取 Token
4. 点击「授权」按钮，输入 `Bearer {Token}`
5. 现在可以调试所有需要认证的接口

## 默认账号密码

以下为系统预置的测试账号，密码均为 `123456`：

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | 123456 | 系统管理员 | 拥有所有权限 |
| device_admin | 123456 | 设备管理员 | 负责设备档案管理和日常维护 |
| engineer1 | 123456 | 维修工程师 | 负责设备维修和保养 |
| engineer2 | 123456 | 维修工程师 | 负责设备维修和保养 |
| qc_staff | 123456 | 质控人员 | 负责设备质量控制和校准 |

### 登录接口示例

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456"
  }'
```

响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员"
    }
  }
}
```

## 主要接口列表

| 模块 | 接口路径 | 说明 |
|------|----------|------|
| 认证 | `POST /api/auth/login` | 用户登录 |
| 认证 | `POST /api/auth/logout` | 用户登出 |
| 设备 | `GET /api/devices` | 设备列表 |
| 设备 | `POST /api/devices` | 创建设备 |
| 设备 | `PUT /api/devices/{id}` | 更新设备 |
| 设备 | `DELETE /api/devices/{id}` | 删除设备 |
| 巡检 | `GET /api/inspection/tasks` | 巡检任务列表 |
| 巡检 | `POST /api/inspection/tasks/{id}/execute` | 执行巡检任务 |
| 维修 | `GET /api/repair-orders` | 维修工单列表 |
| 维修 | `POST /api/repair-orders` | 创建维修工单 |
| 质控 | `GET /api/qc-records` | 质控记录列表 |
| 统计 | `GET /api/statistics/overview` | 统计概览数据 |

## 常见问题

### 1. 启动报错：数据库连接失败

```
检查 MySQL 服务是否启动
检查 application.yml 中的数据库连接配置
确认数据库 medical_device 是否已创建
确认用户名密码是否正确
```

### 2. 启动报错：Redis 连接失败

```
检查 Redis 服务是否启动
检查 application.yml 中的 Redis 连接配置
如果 Redis 有密码，确认密码是否配置正确
```

### 3. Maven 下载依赖慢

建议配置 Maven 阿里云镜像，修改 `settings.xml`：

```xml
<mirror>
  <id>aliyunmaven</id>
  <mirrorOf>*</mirrorOf>
  <name>阿里云公共仓库</name>
  <url>https://maven.aliyun.com/repository/public</url>
</mirror>
```

### 4. 端口被占用

修改 `application.yml` 中的 `server.port` 为其他端口。
