# 测试说明

## 测试账号

| 用户名 | 密码 | 角色 | 权限说明 |
|--------|------|------|----------|
| admin | 123456 | 超级管理员 | 所有权限 |
| manager | 123456 | 运营经理 | 用户/订单/工单管理、消息发送、日志导出 |
| operator | 123456 | 运营专员 | 用户查看、订单/工单创建、消息查看 |
| viewer | 123456 | 访客 | 只读权限 |

## 功能测试

### 1. 登录功能

`ash
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
`

预期返回：
`json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "realName": "管理员",
      "role": "admin",
      "roleName": "超级管理员"
    }
  }
}
`

### 2. 获取用户信息

`ash
curl http://localhost:3000/api/auth/userinfo \
  -H "Authorization: Bearer <token>"
`

### 3. 获取用户列表

`ash
curl "http://localhost:3000/api/users?page=1&pageSize=10" \
  -H "Authorization: Bearer <token>"
`

### 4. 获取订单列表

`ash
curl "http://localhost:3000/api/orders?page=1&pageSize=10" \
  -H "Authorization: Bearer <token>"
`

### 5. 获取工单列表

`ash
curl "http://localhost:3000/api/tickets?page=1&pageSize=10" \
  -H "Authorization: Bearer <token>"
`

### 6. 获取消息列表

`ash
curl "http://localhost:3000/api/messages?page=1&pageSize=20" \
  -H "Authorization: Bearer <token>"
`

### 7. 获取操作日志

`ash
curl "http://localhost:3000/api/logs?page=1&pageSize=10" \
  -H "Authorization: Bearer <token>"
`

### 8. 获取看板数据

`ash
curl http://localhost:3000/api/dashboard/stats \
  -H "Authorization: Bearer <token>"
`

## 权限测试

### 测试按钮权限

1. 使用 dmin 登录，应该可以看到所有按钮（新增、编辑、删除、导出）
2. 使用 iewer 登录，应该只能看到查看按钮，新增/编辑/删除按钮隐藏

### 测试菜单权限

1. 使用 dmin 登录，应该可以看到所有菜单（看板、用户、订单、工单、消息、日志）
2. 使用 iewer 登录，应该看不到操作日志菜单

## BFF 层特性测试

### 测试超时处理

`ash
# 模拟超时请求，应该在 5 秒内返回 504 错误
curl -m 6 http://localhost:3000/api/timeout-test
`

### 测试降级处理

当下游服务不可用时，应该返回降级数据而不是 500 错误

### 测试缓存

相同的 GET 请求在缓存有效期内（默认 5 分钟）应该从缓存返回

### 测试统一错误处理

所有错误响应都应该包含统一的格式：
`json
{
  "code": 500,
  "message": "错误信息",
  "data": null,
  "timestamp": "2024-01-01T00:00:00.000Z",
  "path": "/api/users"
}
`

## 前端测试

1. 访问 http://localhost:5173 打开前端应用
2. 使用不同账号登录，验证功能权限
3. 测试页面间切换，验证全局筛选条件共享
4. 测试全局消息中心，验证消息通知功能
5. 测试个人中心，验证修改密码功能
6. 测试错误页面，访问 /404、/403、/500

## 性能测试

使用 Apache Bench 进行并发测试：

`ash
# 测试登录接口
ab -n 1000 -c 100 -p login.json -T application/json \
  http://localhost:3000/api/auth/login

# 测试用户列表接口
ab -n 1000 -c 100 -H "Authorization: Bearer <token>" \
  "http://localhost:3000/api/users?page=1&pageSize=10"
`
