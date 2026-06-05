const fs = require("fs");
const path = require("path");

function wp(f, c) {
  const d = path.dirname(f);
  if (!fs.existsSync(d)) fs.mkdirSync(d, { recursive: true });
  fs.writeFileSync(f, c, "utf8");
  console.log("OK:", f);
}

function createService(name, port, data) {
  const dir = "apps/" + name + "-service";
  wp(dir + "/package.json", JSON.stringify({
    name: "@platform/" + name + "-service",
    version: "1.0.0",
    private: true,
    main: "src/index.js",
    scripts: { start: "node src/index.js", dev: "nodemon src/index.js" },
    dependencies: { express: "^4.18.2", cors: "^2.8.5" }
  }, null, 2));
  
  wp(dir + "/src/index.js",
    "const express = require('express');\n" +
    "const cors = require('cors');\n" +
    "const app = express();\n" +
    "app.use(cors());\n" +
    "app.use(express.json());\n" +
    "const PORT = process.env.PORT || " + port + ";\n" +
    "let data = " + JSON.stringify(data) + ";\n" +
    "app.get('/health', (req, res) => res.json({ status: 'ok', service: '" + name + "' }));\n" +
    "app.get('/api/" + name + "s', (req, res) => {\n" +
    "  const page = parseInt(req.query.page || 1);\n" +
    "  const pageSize = parseInt(req.query.pageSize || 10);\n" +
    "  const start = (page - 1) * pageSize;\n" +
    "  res.json({ code: 200, message: 'success', data: { list: data.slice(start, start + pageSize), total: data.length } });\n" +
    "});\n" +
    "app.get('/api/" + name + "s/:id', (req, res) => {\n" +
    "  const item = data.find(d => d.id === parseInt(req.params.id));\n" +
    "  res.json({ code: 200, message: 'success', data: item });\n" +
    "});\n" +
    "app.post('/api/" + name + "s', (req, res) => {\n" +
    "  const newItem = { id: data.length + 1, ...req.body, createTime: new Date().toISOString() };\n" +
    "  data.push(newItem);\n" +
    "  res.json({ code: 200, message: 'success', data: newItem });\n" +
    "});\n" +
    "app.put('/api/" + name + "s/:id', (req, res) => {\n" +
    "  const idx = data.findIndex(d => d.id === parseInt(req.params.id));\n" +
    "  if (idx > -1) data[idx] = { ...data[idx], ...req.body };\n" +
    "  res.json({ code: 200, message: 'success', data: data[idx] });\n" +
    "});\n" +
    "app.delete('/api/" + name + "s/:id', (req, res) => {\n" +
    "  const idx = data.findIndex(d => d.id === parseInt(req.params.id));\n" +
    "  if (idx > -1) data.splice(idx, 1);\n" +
    "  res.json({ code: 200, message: 'success', data: null });\n" +
    "});\n" +
    "app.listen(PORT, () => console.log('" + name + " service running on http://localhost:' + PORT));\n"
  );
}

// Create user service
const users = [];
for (let i = 1; i <= 50; i++) {
  users.push({ id: i, username: 'user' + i, realName: '用户' + i, email: 'user' + i + '@example.com', phone: '13800' + String(i).padStart(5, '0'), role: i % 4 === 1 ? 'admin' : i % 4 === 2 ? 'manager' : i % 4 === 3 ? 'operator' : 'viewer', status: i % 5 === 0 ? 0 : 1, createTime: '2024-01-01 10:00:00' });
}
createService("user", 3001, users);

// Create order service
const orders = [];
const statuses = ['待支付', '已支付', '已发货', '已完成', '已取消'];
for (let i = 1; i <= 100; i++) {
  orders.push({ id: i, orderNo: 'ORD' + String(i).padStart(8, '0'), amount: (Math.random() * 10000 + 100).toFixed(2), status: i % 5, statusName: statuses[i % 5], createTime: '2024-01-01 10:00:00' });
}
createService("order", 3002, orders);

// Create ticket service
const tickets = [];
const types = ['咨询', '投诉', '建议', '故障'];
const tStatuses = ['待处理', '处理中', '已解决', '已关闭'];
for (let i = 1; i <= 80; i++) {
  tickets.push({ id: i, ticketNo: 'TK' + String(i).padStart(8, '0'), title: types[i % 4] + '工单' + i, content: '这是一个' + types[i % 4] + '工单的内容...', type: i % 4, typeName: types[i % 4], status: i % 4, statusName: tStatuses[i % 4], createTime: '2024-01-01 10:00:00' });
}
createService("ticket", 3003, tickets);

// Create notification service
const notifications = [];
for (let i = 1; i <= 50; i++) {
  notifications.push({ id: i, title: '系统通知' + i, content: '这是一条系统通知内容...', type: i % 3, isRead: i % 3 === 0 ? 1 : 0, createTime: '2024-01-01 10:00:00' });
}
createService("notification", 3004, notifications);

console.log('=== Service modules generated! ===');