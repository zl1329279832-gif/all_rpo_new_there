# 快速开始

## 环境要求

- Node.js >= 18.x
- npm >= 9.x
- Redis >= 6.x (可选，用于缓存)
- MySQL >= 8.0 (可选，用于持久化存储)

## 端口说明

| 服务 | 端口 | 说明 |
|------|------|------|
| Web | 5173 | 前端应用 |
| BFF | 3000 | 聚合层 |
| User Service | 3001 | 用户服务 |
| Order Service | 3002 | 订单服务 |
| Ticket Service | 3003 | 工单服务 |
| Notification Service | 3004 | 通知服务 |

## 测试账号

| 用户名 | 密码 | 角色 | 权限 |
|--------|------|------|------|
| admin | 123456 | 超级管理员 | 所有权限 |
| manager | 123456 | 运营经理 | 用户/订单/工单管理、消息发送、日志导出 |
| operator | 123456 | 运营专员 | 用户查看、订单/工单创建、消息查看 |
| viewer | 123456 | 访客 | 只读权限 |

## 安装依赖

`ash
# 安装所有依赖
npm install

# 或使用 workspaces 安装
npm run init:data
`

## 启动项目

### 方式一：启动所有服务（推荐）

`ash
npm run dev:all
`

### 方式二：分别启动

`ash
# 启动所有下游服务
npm run dev:services

# 启动 BFF 层（新终端）
npm run dev:bff

# 启动前端（新终端）
npm run dev:web
`

### 方式三：仅启动前端和 BFF（使用内置模拟数据）

`ash
# 启动 BFF 层
npm run dev:bff

# 启动前端（新终端）
npm run dev:web
`

## 访问项目

- 前端地址: http://localhost:5173
- BFF API 地址: http://localhost:3000
- API 文档: http://localhost:3000/api (可查看 docs/api.md)

## 初始化演示数据

`ash
npm run init:data
`

## 项目结构

`
.
 apps/
    web/                    # 前端应用 (Vue 3 + TS + Vite)
    bff/                    # BFF 聚合层 (NestJS)
    user-service/           # 用户服务 (Express)
    order-service/          # 订单服务 (Express)
    ticket-service/         # 工单服务 (Express)
    notification-service/   # 通知服务 (Express)
 packages/
    components/             # 共享组件库
    shared/                 # 共享类型定义
 mock/                       # 模拟数据
 docs/                       # 接口文档和测试说明
 scripts/                    # 工具脚本
 package.json
 .gitignore
 README.md
`

## 功能特性

### 前端功能
- [x] 统一登录（支持 Token 无感刷新）
- [x] 动态菜单（根据用户权限渲染）
- [x] 按钮级权限控制（v-permission 指令）
- [x] 跨应用路由跳转
- [x] 全局消息中心
- [x] 操作日志记录
- [x] 指标看板（ECharts 图表）
- [x] 异常状态页（403/404/500）
- [x] 页面间状态共享（用户信息、筛选条件）

### BFF 层功能
- [x] 接口聚合
- [x] 缓存策略（Redis）
- [x] 超时控制（默认 5 秒）
- [x] 服务降级（熔断机制）
- [x] 统一错误返回格式
- [x] 操作日志自动记录
- [x] JWT 身份认证
- [x] 权限验证

## 开发说明

### 添加新页面

1. 在 pps/web/src/views/ 创建页面组件
2. 在 pps/web/src/router/index.ts 添加路由配置
3. 在 BFF 层 pps/bff/src/modules/ 添加对应模块

### 添加新接口

1. 在 BFF 层对应模块的 controller 中添加接口
2. 在 service 中实现业务逻辑
3. 在前端 pps/web/src/api/ 添加 API 调用函数

### 配置说明

- BFF 环境变量: pps/bff/.env
- 前端代理: pps/web/vite.config.ts
- 缓存配置: BFF 层 CacheModule
- JWT 配置: BFF 层 JwtModule

## 常见问题

### 端口被占用

修改对应服务的配置文件：
- 前端: pps/web/vite.config.ts
- BFF: pps/bff/.env 中的 BFF_PORT
- 业务服务: 启动时设置 PORT 环境变量

### 依赖安装失败

`ash
# 清除缓存
npm cache clean --force

# 删除 node_modules 重新安装
rm -rf node_modules package-lock.json
npm install
`

### 跨域问题

前端已配置代理，所有 /api 请求会转发到 BFF 层。如需修改，编辑 pps/web/vite.config.ts。

## 测试

详细测试说明请查看 [测试说明](docs/test-guide.md)

## 接口文档

完整接口文档请查看 [API 文档](docs/api.md)
