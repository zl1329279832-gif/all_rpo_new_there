# 集团业务多应用运营中台

面向集团业务的多应用运营中台，采用前后端分离架构，支持统一登录、动态菜单、按钮权限、跨应用路由等核心功能。

## 技术栈

### 前端
- **框架**: Vue 3 + TypeScript
- **构建工具**: Vite 5
- **状态管理**: Pinia
- **UI 组件库**: Element Plus
- **路由**: Vue Router 4
- **HTTP 客户端**: Axios

### 后端
- **BFF 层**: NestJS + Node.js
- **缓存**: Redis
- **数据库**: MySQL 8.0
- **服务间通信**: HTTP REST

## 项目结构

```
.
 apps/
    web/                    # 前端应用
    bff/                    # BFF 聚合层
    user-service/           # 用户服务
    order-service/          # 订单服务
    ticket-service/         # 工单服务
    notification-service/   # 通知服务
 packages/
    components/             # 共享组件库
    shared/                 # 共享类型定义和工具
 mock/                       # 模拟数据
 docs/                       # 文档目录
 scripts/                    # 脚本目录
 package.json
```

## 核心功能

### 前端功能
- **统一登录**: 支持账号密码登录，token 无感刷新
- **动态菜单**: 根据用户权限动态渲染侧边栏菜单
- **按钮权限**: 细粒度的按钮级权限控制
- **跨应用路由**: 支持多应用间路由跳转和状态保持
- **全局消息中心**: 统一的消息通知和待办事项
- **操作日志**: 记录用户关键操作行为
- **指标看板**: 业务数据可视化展示
- **异常状态页**: 403/404/500 等错误页面
- **状态共享**: 页面间共享用户信息和筛选条件

### BFF 层功能
- **接口聚合**: 聚合多个微服务接口，减少前端请求
- **缓存策略**: Redis 缓存热点数据
- **超时控制**: 接口超时保护
- **服务降级**: 依赖服务不可用时自动降级
- **统一错误处理**: 标准化错误返回格式
- **操作日志**: 统一记录操作日志

## 快速开始

### 环境要求
- Node.js >= 18.0.0
- npm >= 9.0.0
- MySQL >= 8.0
- Redis >= 6.0

### 安装依赖
```bash
npm install
```

### 初始化数据库
```bash
npm run init:data
```

### 启动开发服务
```bash
# 仅启动前端
npm run dev:web

# 仅启动 BFF 服务
npm run dev:bff

# 同时启动前端和 BFF
npm run dev:all
```

### 生产构建
```bash
npm run build
```

## 测试账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | 123456 | 超级管理员 | 拥有所有权限 |
| manager | 123456 | 运营经理 | 订单、工单管理权限 |
| operator | 123456 | 运营专员 | 基础操作权限 |
| viewer | 123456 | 访客 | 只读权限 |

## 端口说明

| 服务 | 端口 | 说明 |
|------|------|------|
| web | 5173 | 前端应用 |
| bff | 3000 | BFF 聚合层 |
| user-service | 3001 | 用户服务 |
| order-service | 3002 | 订单服务 |
| ticket-service | 3003 | 工单服务 |
| notification-service | 3004 | 通知服务 |

## 文档

- [API 接口文档](./docs/api.md)
- [架构设计文档](./docs/architecture.md)
- [测试说明](./docs/testing.md)
- [部署指南](./docs/deploy.md)
