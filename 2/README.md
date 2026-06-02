# 医疗设备管理系统

## 项目简介

医疗设备管理系统是一套全栈设备生命周期管理平台，基于 Spring Boot + Vue 3 技术栈构建，实现设备档案、巡检计划、维修工单、质控记录、风险等级等全流程管理。

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.x
- **数据库**: MySQL 8.0
- **缓存**: Redis 7.x
- **ORM**: MyBatis Plus 3.5.x
- **安全**: Spring Security + JWT
- **接口文档**: Knife4j (OpenAPI 3)
- **任务调度**: Spring Scheduler

### 前端
- **框架**: Vue 3 + Composition API
- **UI组件**: Element Plus
- **状态管理**: Pinia
- **图表**: ECharts 5.x
- **路由**: Vue Router 4
- **HTTP**: Axios
- **构建工具**: Vite

## 功能模块

### 系统管理
- 用户管理、角色权限、菜单配置
- JWT认证、权限拦截

### 设备管理
- 设备档案管理（台账）
- 设备状态机控制
- 风险等级评估

### 巡检管理
- 巡检计划配置（日/周/月/季/年）
- 巡检任务自动生成
- 日历视图展示
- 巡检执行记录

### 维修管理
- 维修工单流程（报修→派单→维修→验收）
- 配件更换记录
- 停机时长统计
- 维修成本核算

### 质控管理
- 质控计划
- 质控记录
- 质控不合格设备锁定

### 校准管理
- 校准记录
- 校准证书管理

### 合同管理
- 维保合同
- 到期提醒

### 报表统计
- 设备状态统计
- 维修工单趋势
- 风险看板
- 高风险设备预警

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 7.0+

### 数据库初始化

```bash
# 创建数据库并执行初始化脚本
mysql -u root -p < backend/src/main/resources/sql/init.sql
mysql -u root -p medical_device < backend/src/main/resources/sql/data.sql
```

### 后端启动

```bash
cd backend

# 修改数据库配置
# 编辑 src/main/resources/application.yml

# 启动服务
mvn spring-boot:run
```

后端服务地址: http://localhost:8080/api
接口文档地址: http://localhost:8080/api/doc.html

### 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端地址: http://localhost:5173

### 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 系统管理员 |
| device_admin | 123456 | 设备管理员 |
| engineer1 | 123456 | 维修工程师 |
| qc_staff | 123456 | 质控人员 |

## 项目结构

```
medical-device-management/
├── backend/                          # 后端项目
│   ├── src/main/java/com/medical/device/
│   │   ├── config/                   # 配置类
│   │   ├── controller/               # 控制器
│   │   ├── service/                  # 业务逻辑
│   │   ├── mapper/                   # 数据访问
│   │   ├── entity/                   # 实体类
│   │   ├── dto/                      # 数据传输对象
│   │   ├── enums/                    # 枚举类
│   │   ├── security/                 # 安全模块
│   │   ├── statemachine/             # 状态机
│   │   ├── schedule/                 # 任务调度
│   │   ├── common/                   # 通用类
│   │   ├── exception/                # 异常处理
│   │   └── util/                     # 工具类
│   └── src/main/resources/
│       ├── sql/                      # 数据库脚本
│       └── application.yml           # 配置文件
├── frontend/                         # 前端项目
│   ├── src/
│   │   ├── views/                    # 页面组件
│   │   ├── components/               # 公共组件
│   │   ├── router/                   # 路由配置
│   │   ├── stores/                   # Pinia状态
│   │   ├── api/                      # API接口
│   │   └── utils/                    # 工具函数
│   └── package.json
└── README.md
```

## 业务规则

### 设备状态流转
```
正常使用 → 维护中 → 正常使用
        ↘ 维修中 ↗
        ↘ 校准中 ↗
        ↘ 闲置
        ↘ 已报废
```

### 重要约束
1. **质控不合格设备禁止标记为正常使用** - 必须先通过质控复检
2. **高风险设备重点提醒** - 首页风险看板展示
3. **维修流程** - 报修→派单→开始维修→完成维修→验收
4. **配件更换记录** - 维修完成时记录，自动扣减库存
5. **停机时长统计** - 维修验收后自动累计

## 许可证

MIT License
