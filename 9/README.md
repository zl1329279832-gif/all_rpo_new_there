# 连锁烘焙门店生产与临期管理系统

基于前后端分离架构的连锁烘焙门店每日生产和临期管理系统。

## 技术栈

### 后端
- **Java 1.8**
- **Spring Boot 2.7.18**
- **MyBatis-Plus 3.5.5**
- **Redis 6.0+**
- **MySQL 8.0**
- **Druid 连接池**
- **Redisson 分布式锁**
- **Hutool 工具库**
- **Knife4j API 文档**

### 前端
- **Vue 3**
- **Element Plus**
- **ECharts 5**
- **Vite**
- **Vue Router 4**
- **Pinia**

## 核心业务规则

1. **生产数量受原料库存限制** - 创建生产计划时自动校验原料库存是否充足
2. **成品出库遵循效期优先** - 出库时按 `expire_time` 升序排序，先过期先出
3. **临期商品自动预警** - 根据配方 `warning_hours` 自动识别临期商品并进入预警列表
4. **事务处理** - 生产完成、调拨出入库、报损审核等多表操作均带事务
5. **Redis 缓存** - 临期预警统计（5分钟）、经营分析数据（30分钟）

## 快速开始

### 环境要求

| 软件 | 版本要求 |
|------|----------|
| JDK | 1.8+ |
| MySQL | 8.0+ |
| Redis | 6.0+ |
| Node.js | 16+ |
| Maven | 3.6+ |

### 1. 数据库初始化

```sql
-- 创建数据库
CREATE DATABASE bakery_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 执行表结构脚本
source backend/src/main/resources/sql/schema.sql;

-- 执行演示数据脚本
source backend/src/main/resources/sql/data.sql;
```

### 2. 修改配置文件

编辑 `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bakery_management?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password  # 修改为你的MySQL密码
  redis:
    host: localhost
    port: 6379
    password: your_redis_password  # 如无密码可删除
```

### 3. 启动后端服务

```bash
cd backend

# 方法一：Maven 命令启动
mvn spring-boot:run

# 方法二：打包后运行
mvn clean package -DskipTests
java -jar target/bakery-management-1.0.0.jar
```

后端服务启动后访问：
- API 文档：http://localhost:8080/doc.html
- 健康检查：http://localhost:8080/actuator/health

### 4. 启动前端服务

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务启动后访问：http://localhost:5173

### 5. 打包部署

```bash
# 后端打包
cd backend
mvn clean package -DskipTests

# 前端打包
cd frontend
npm run build
```

## 目录结构

```
├── backend/                    # 后端项目
│   ├── src/main/java/com/bakery/
│   │   ├── common/            # 通用类（Result、异常处理）
│   │   ├── config/            # 配置类（Redis、CORS等）
│   │   ├── controller/        # Controller层（9个）
│   │   ├── dto/               # DTO/VO类（6个）
│   │   ├── entity/            # 实体类（16个）
│   │   ├── mapper/            # Mapper接口（16个）
│   │   ├── service/           # Service层（8个）
│   │   └── BakeryApplication.java
│   └── src/main/resources/
│       ├── sql/               # 数据库脚本
│       │   ├── schema.sql     # 表结构（16张表）
│       │   └── data.sql       # 演示数据
│       └── application.yml    # 应用配置
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── api/                # API封装
│   │   ├── router/             # 路由配置
│   │   ├── utils/              # 工具函数
│   │   ├── views/              # 页面组件（6个）
│   │   ├── assets/styles/      # 全局样式
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   └── vite.config.js
├── .gitignore
└── README.md
```

## 数据库表结构

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| sys_store | 门店 | id, store_name, store_code, type |
| base_material | 原料 | id, material_name, unit, spec |
| base_material_stock | 原料库存 | material_id, store_id, quantity |
| base_recipe | 配方 | id, product_name, category, shelf_life_hours, warning_hours |
| base_recipe_detail | 配方明细 | recipe_id, material_id, dosage |
| prod_plan | 生产计划 | id, plan_no, plan_name, produce_date, status |
| prod_plan_detail | 计划明细 | plan_id, recipe_id, plan_qty, actual_qty |
| store_demand | 门店需求 | id, demand_no, store_id, status |
| store_demand_detail | 需求明细 | demand_id, recipe_id, demand_qty |
| prod_batch | 成品批次 | id, batch_no, recipe_id, total_qty, remain_qty, produce_time, expire_time |
| stock_transfer | 门店调拨 | id, transfer_no, from_store_id, to_store_id, transfer_qty, status |
| stock_damage | 报损记录 | id, damage_no, batch_id, damage_qty, damage_type, status |
| sales_stat | 销售统计 | id, stat_date, recipe_id, store_id, sales_qty, sales_amount |
| stock_log | 库存日志 | id, batch_id, operate_type, qty, operate_time |

## 功能模块

### 1. 生产计划管理
- 创建生产计划（自动校验原料库存）
- 审核、开始生产、完成生产
- 完成生产时自动扣减原料库存（FIFO），生成成品批次
- 支持取消计划

### 2. 批次台账
- 成品批次全生命周期管理
- 批次状态跟踪（在库/部分出库/已售罄/已报损）
- 效期状态自动识别（正常/临期/已过期）
- 预警统计卡片展示

### 3. 库存效期
- 临期预警列表（按预警级别：已过期/严重/一般）
- 库存效期总览（按产品+门店汇总）
- 效期优先批次明细查看
- 快捷跳转到报损/调拨

### 4. 门店调拨
- 创建调拨单（展示效期优先的可用批次）
- 确认出库（自动按效期优先扣减库存）
- 确认入库（生成新批次，继承剩余保质期）
- 支持正常调拨和临期调拨

### 5. 报损管理
- 创建报损单（选择批次，限制最大可报损数量）
- 审核流程（通过/驳回，填写意见）
- 审核通过后自动扣减库存并记录
- 报损类型：临期过期、质量问题、破损、其他

### 6. 经营分析
- **核心指标**：销售总额、销售总量、报损金额、库存总价值
- **销售趋势**：近7/30/90天销售趋势折线图
- **报损趋势**：同期报损金额趋势
- **分类销售占比**：环形饼图
- **门店销售对比**：柱状图
- **产品销量排行**：TOP 10 横向柱状图
- **临期预警统计**：分产品预警堆叠柱状图

## 演示数据说明

数据库脚本已预置完整演示数据：

- **门店**：1个中心工厂 + 4个连锁门店
- **原料**：10种常用烘焙原料
- **配方**：8种成品配方（面包、蛋糕、点心等）
- **生产计划**：最近8天的生产计划（各种状态都有）
- **门店需求**：9张门店需求单据
- **成品批次**：14个批次（含正常、临期、过期）
- **调拨记录**：8条调拨记录
- **报损记录**：5条报损记录
- **销售统计**：最近3天的销售数据

## API 接口示例

### 生产计划相关
```
POST   /api/prod-plan           # 创建生产计划
GET    /api/prod-plan/page      # 分页查询
PUT    /api/prod-plan/audit/{id}     # 审核
PUT    /api/prod-plan/start/{id}     # 开始生产
PUT    /api/prod-plan/complete       # 完成生产
PUT    /api/prod-plan/cancel/{id}    # 取消
```

### 成品批次相关
```
GET    /api/prod-batch/page               # 分页查询
GET    /api/prod-batch/available          # 查询可用批次（效期优先）
GET    /api/prod-batch/warning/stats      # 预警统计（Redis缓存5分钟）
GET    /api/prod-batch/warning/list       # 预警列表
```

### 经营分析
```
GET    /api/analysis              # 获取分析数据（Redis缓存30分钟）
POST   /api/analysis/refresh      # 刷新缓存
```

完整 API 文档请启动后端后访问：http://localhost:8080/doc.html

## 业务校验说明

| 场景 | 校验规则 |
|------|----------|
| 创建生产计划 | 校验原料库存是否充足，不足则抛出异常 |
| 完成生产 | 校验计划状态必须为「生产中」，按实际产量扣原料 |
| 调拨出库 | 校验批次未过期、库存充足，按效期优先扣减 |
| 调拨入库 | 校验调拨单状态为「已出库」，生成新批次 |
| 报损审核 | 校验报损单状态为「待审核」，通过则扣库存 |
| 批次出库 | 校验批次状态、过期时间、剩余数量 |

## 事务处理说明

核心操作均使用 `@Transactional(rollbackFor = Exception.class)` 保证原子性：

- **生产完成**：更新计划状态 → 扣原料库存 → 生成成品批次 → 记录库存日志
- **调拨出库**：更新调拨单状态 → 扣减调出方库存 → 记录库存日志
- **调拨入库**：更新调拨单状态 → 生成调入方新批次 → 记录库存日志
- **报损审核**：更新报损单状态 → 扣减批次库存 → 记录库存日志

## 开发说明

### 后端开发规范

1. 所有接口返回统一格式 `Result<T>`
2. 业务异常抛出 `BusinessException`，由全局异常处理器处理
3. Mapper 中自定义 SQL 使用 `@Select` 注解或 XML
4. 时间字段统一使用 `LocalDateTime`
5. 金额字段统一使用 `BigDecimal`，保留2位小数

### 前端开发规范

1. API 调用统一封装在 `src/api/index.js`
2. 日期/金额/状态格式化使用 `src/utils/format.js`
3. HTTP 请求统一使用 `src/utils/request.js`（含拦截器）
4. 页面组件使用 `<script setup>` 语法糖

## .gitignore 说明

已忽略以下内容：

```
# Java/Maven 构建产物
target/
*.class
*.jar

# 日志文件
logs/
*.log

# 附件与导出
attachments/
exports/
*.xlsx
*.csv

# 本地配置
application-local.yml
.env.local

# IDE
.idea/
.vscode/
*.iml

# 前端构建
dist/
node_modules/

# 系统文件
.DS_Store
Thumbs.db
```

## 常见问题

### Q: 启动报数据库连接错误
A: 检查 `application.yml` 中的数据库地址、用户名、密码是否正确

### Q: Redis 连接失败
A: 确认 Redis 服务已启动，检查配置中的 host 和 port

### Q: 前端无法调用后端 API
A: 检查后端端口是否为 8080，或修改 `vite.config.js` 中的代理配置

### Q: 临期预警不更新
A: 预警数据有 5 分钟 Redis 缓存，可调用 `/api/prod-batch/warning/list` 强制刷新

## License

MIT License
