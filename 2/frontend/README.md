# 前端运行说明

## 技术栈说明

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.x | 渐进式 JavaScript 框架 |
| Vite | 5.0.x | 下一代前端构建工具 |
| Element Plus | 2.4.x | 基于 Vue 3 的组件库 |
| Pinia | 2.1.x | Vue 官方状态管理 |
| Vue Router | 4.2.x | Vue.js 官方路由 |
| ECharts | 5.4.x | 数据可视化图表库 |
| Axios | 1.6.x | HTTP 客户端 |
| Sass | 1.69.x | CSS 预处理器 |
| @element-plus/icons-vue | 2.3.x | Element Plus 图标库 |

## 环境要求

- **Node.js**: 18 或更高版本
- **npm**: 9 或更高版本（或使用 pnpm / yarn）

### 环境验证

```bash
# 检查 Node.js 版本
node -v

# 检查 npm 版本
npm -v
```

### 推荐版本

```
Node.js >= 18.17.0
npm >= 9.6.0
```

## 安装依赖

### 方式一：使用 npm（推荐）

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install
```

### 方式二：使用国内镜像（推荐国内用户）

```bash
# 配置淘宝镜像
npm config set registry https://registry.npmmirror.com

# 安装依赖
npm install
```

### 方式三：使用 pnpm

```bash
# 安装 pnpm（如果未安装）
npm install -g pnpm

# 安装依赖
pnpm install
```

### 依赖安装验证

安装完成后，检查 `node_modules` 目录是否存在：

```bash
ls node_modules
```

## 配置修改

### 环境配置文件

项目使用 `.env` 文件进行环境配置：

| 文件名 | 说明 |
|--------|------|
| `.env.development` | 开发环境配置 |
| `.env.production` | 生产环境配置 |

### 开发环境配置

编辑 `.env.development`：

```env
VITE_APP_TITLE=医疗设备管理系统
VITE_APP_BASE_API=/api
VITE_APP_BASE_URL=http://localhost:5173
```

**配置说明**：

- `VITE_APP_TITLE`: 系统标题
- `VITE_APP_BASE_API`: 后端 API 基础路径
- `VITE_APP_BASE_URL`: 前端访问地址

### 修改后端 API 地址

如果后端地址不是默认的 `http://localhost:8080/api`，需要修改 `vite.config.js` 中的代理配置：

```javascript
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  // 修改为实际的后端地址
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '/api')
    }
  }
}
```

**修改说明**：

1. 将 `target` 修改为实际的后端服务地址（如 `http://192.168.1.100:8080`）
2. 如果后端端口不是 8080，修改为实际端口
3. 如果后端上下文路径不是 `/api`，同时修改 `rewrite` 规则

### 生产环境配置

编辑 `.env.production`：

```env
VITE_APP_TITLE=医疗设备管理系统
VITE_APP_BASE_API=/api
VITE_APP_BASE_URL=http://your-domain.com
```

## 启动命令

### 启动开发服务器

```bash
# 进入前端目录
cd frontend

# 启动开发服务器
npm run dev
```

启动成功后，控制台会输出类似信息：

```
  VITE v5.0.x  ready in xxx ms

  ➜  Local:   http://localhost:5173/
  ➜  Network: use --host to expose
  ➜  press h + enter to show help
```

### 访问地址

- **本地访问**: http://localhost:5173
- **局域网访问**: 需要添加 `--host` 参数

```bash
# 允许局域网访问
npm run dev -- --host
```

### 启动参数说明

```bash
# 指定端口
npm run dev -- --port 5174

# 同时指定主机和端口
npm run dev -- --host 0.0.0.0 --port 5174

# 强制开启 https
npm run dev -- --https
```

## 构建命令

### 构建生产版本

```bash
# 进入前端目录
cd frontend

# 构建生产环境
npm run build
```

构建完成后，文件将输出到 `dist` 目录。

### 预览生产构建

```bash
# 预览构建结果
npm run preview
```

### 构建参数说明

```bash
# 构建时指定模式
npm run build -- --mode production

# 查看构建分析（需额外配置）
npm run build -- --report
```

### 构建产物说明

构建完成后，`dist` 目录结构：

```
dist/
├── index.html
├── assets/
│   ├── index-xxxx.js
│   ├── index-xxxx.css
│   └── [其他资源文件]
└── favicon.ico
```

## 部署说明

### Nginx 部署示例

将 `dist` 目录内容上传到服务器，配置 Nginx：

```nginx
server {
    listen 80;
    server_name your-domain.com;

    root /path/to/dist;
    index index.html;

    # 前端路由 fallback
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

### Docker 部署示例

创建 `Dockerfile`：

```dockerfile
FROM nginx:alpine

COPY dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

## 项目结构

```
frontend/
├── public/                 # 静态资源目录
├── src/
│   ├── api/                # API 接口
│   │   ├── user.js         # 用户相关接口
│   │   ├── device.js       # 设备相关接口
│   │   ├── inspection.js   # 巡检相关接口
│   │   ├── repair.js       # 维修相关接口
│   │   ├── qcRecord.js     # 质控相关接口
│   │   └── ...             # 其他接口
│   ├── assets/             # 静态资源
│   ├── components/         # 公共组件
│   ├── router/             # 路由配置
│   │   └── index.js
│   ├── stores/             # Pinia 状态管理
│   │   └── user.js
│   ├── utils/              # 工具函数
│   │   └── request.js      # Axios 封装
│   ├── views/              # 页面组件
│   │   ├── Login.vue       # 登录页
│   │   ├── Layout.vue      # 布局组件
│   │   ├── Dashboard.vue   # 首页仪表盘
│   │   ├── DeviceList.vue  # 设备列表
│   │   ├── InspectionCalendar.vue  # 巡检日历
│   │   ├── RepairOrderList.vue     # 维修工单
│   │   ├── QcRecordList.vue        # 质控记录
│   │   ├── StatisticsReport.vue    # 统计报表
│   │   └── RiskDashboard.vue       # 风险看板
│   ├── App.vue             # 根组件
│   └── main.js             # 入口文件
├── .env.development        # 开发环境配置
├── .env.production         # 生产环境配置
├── index.html              # HTML 模板
├── vite.config.js          # Vite 配置
└── package.json            # 项目配置
```

## 默认访问地址

开发环境启动后，访问以下地址：

- **前端页面**: http://localhost:5173
- **后端接口**: http://localhost:8080/api
- **接口文档**: http://localhost:8080/api/doc.html

## 默认登录账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 系统管理员 |
| device_admin | 123456 | 设备管理员 |
| engineer1 | 123456 | 维修工程师 |
| engineer2 | 123456 | 维修工程师 |
| qc_staff | 123456 | 质控人员 |

## 功能页面说明

### 1. 登录页
- 路径：`/login`
- 功能：用户登录、记住密码

### 2. 首页仪表盘
- 路径：`/dashboard`
- 功能：设备统计、工单概览、高风险设备预警、待办任务

### 3. 设备管理
- 路径：`/devices`
- 功能：设备列表、新增设备、编辑设备、删除设备、设备详情、导出Excel

### 4. 巡检管理
- 路径：`/inspection`
- 功能：巡检计划、任务日历、任务执行、巡检记录

### 5. 维修管理
- 路径：`/repair-orders`
- 功能：工单列表、报修、派单、维修、验收、配件更换

### 6. 质控管理
- 路径：`/qc-records`
- 功能：质控记录、质控计划、不合格设备管理

### 7. 统计报表
- 路径：`/statistics`
- 功能：设备状态统计、维修趋势、故障分析、成本统计

### 8. 风险看板
- 路径：`/risk-dashboard`
- 功能：高风险设备列表、风险等级分布、风险预警

## 主要 API 模块

| 模块 | 文件 | 说明 |
|------|------|------|
| 用户 | `src/api/user.js` | 登录、登出、用户信息 |
| 设备 | `src/api/device.js` | 设备 CRUD、设备状态 |
| 巡检 | `src/api/inspection.js` | 巡检计划、巡检任务 |
| 维修 | `src/api/repair.js` | 维修工单、配件更换 |
| 质控 | `src/api/qcRecord.js` | 质控记录、质控计划 |
| 配件 | `src/api/sparePart.js` | 配件管理、库存 |
| 科室 | `src/api/department.js` | 科室管理 |
| 校准 | `src/api/calibrationRecord.js` | 校准记录 |
| 合同 | `src/api/maintenanceContract.js` | 维保合同 |
| 统计 | `src/api/statistics.js` | 统计数据 |

## 常见问题

### 1. npm install 失败

```bash
# 清理缓存重新安装
npm cache clean --force
rm -rf node_modules package-lock.json
npm install

# 或使用国内镜像
npm install --registry=https://registry.npmmirror.com
```

### 2. 启动后接口请求失败

- 检查后端服务是否正常启动
- 检查 `vite.config.js` 中的代理配置是否正确
- 检查 `.env.development` 中的 `VITE_APP_BASE_API` 配置
- 打开浏览器开发者工具，查看 Network 面板的请求详情

### 3. 端口被占用

修改 `vite.config.js` 中的 `server.port` 配置，或使用命令行参数：

```bash
npm run dev -- --port 5174
```

### 4. 热更新不生效

Vite 热更新通常会自动生效，如果不生效：

1. 检查文件名大小写是否正确
2. 检查文件是否在 `src` 目录下
3. 尝试重启开发服务器

### 5. 构建后页面空白

检查 `vite.config.js` 中的 `base` 配置，如果部署在子路径下：

```javascript
export default defineConfig({
  base: '/medical-device/',  // 根据实际部署路径修改
  // ...
})
```

### 6. 跨域问题

开发环境通过 Vite 代理解决跨域，生产环境需要：

1. 后端配置 CORS
2. 或使用 Nginx 反向代理

## 开发规范建议

### 组件命名
- 页面组件使用大驼峰命名，如 `DeviceList.vue`
- 公共组件使用前缀，如 `BaseTable.vue`

### 接口命名
- API 函数使用动词开头，如 `getDeviceList`, `createDevice`
- 统一放置在 `src/api/` 目录下

### 状态管理
- 用户信息、全局配置使用 Pinia 存储
- 页面级状态优先使用组件内状态

### 样式规范
- 使用 Scoped 样式避免污染
- 公共样式提取到全局样式文件
- 优先使用 Element Plus 主题变量
