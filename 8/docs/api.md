# API 接口文档

## 接口列表

### 认证接口

- POST /api/auth/login - 用户登录
- POST /api/auth/refresh - 刷新 Token
- GET /api/auth/userinfo - 获取用户信息
- GET /api/auth/menus - 获取用户菜单
- GET /api/auth/permissions - 获取用户权限
- POST /api/auth/logout - 退出登录

### 用户接口

- GET /api/users - 获取用户列表
- GET /api/users/:id - 获取用户详情
- POST /api/users - 创建用户
- PUT /api/users/:id - 更新用户
- DELETE /api/users/:id - 删除用户

### 订单接口

- GET /api/orders - 获取订单列表
- GET /api/orders/:id - 获取订单详情
- POST /api/orders - 创建订单
- PUT /api/orders/:id - 更新订单
- DELETE /api/orders/:id - 删除订单

### 工单接口

- GET /api/tickets - 获取工单列表
- GET /api/tickets/:id - 获取工单详情
- POST /api/tickets - 创建工单
- PUT /api/tickets/:id - 更新工单
- DELETE /api/tickets/:id - 删除工单

### 消息接口

- GET /api/messages - 获取消息列表
- GET /api/messages/unread-count - 获取未读消息数
- GET /api/messages/:id - 获取消息详情
- POST /api/messages - 发送消息
- PUT /api/messages/:id/read - 标记已读
- PUT /api/messages/read-all - 全部已读
- DELETE /api/messages/:id - 删除消息

### 日志接口

- GET /api/logs - 获取操作日志
- POST /api/logs/export - 导出日志

### 看板接口

- GET /api/dashboard/stats - 获取统计数据
- GET /api/dashboard/chart/trend - 获取趋势图数据
- GET /api/dashboard/chart/pie - 获取饼图数据
- GET /api/dashboard/chart/bar - 获取柱状图数据
- GET /api/dashboard/activities - 获取最近活动

### 聚合接口

- GET /api/aggregate/home - 首页聚合数据
- GET /api/aggregate/user-detail - 用户详情聚合
- GET /api/aggregate/order-detail - 订单详情聚合

## 统一响应格式

`json
{
  "code": 200,
  "message": "success",
  "data": {}
}
`

## 错误码说明

- 200: 成功
- 401: 未授权
- 403: 无权限
- 404: 资源不存在
- 500: 服务器错误
- 503: 服务不可用
- 504: 服务超时
