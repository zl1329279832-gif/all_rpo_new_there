# 部署文档

## 后端部署

### 1. 打包项目

```bash
cd backend
mvn clean package -DskipTests
```

### 2. 运行JAR包

```bash
java -jar target/device-management-1.0.0.jar --spring.profiles.active=prod
```

### 3. 使用Docker部署

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/device-management-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 前端部署

### 1. 构建项目

```bash
cd frontend
npm run build
```

### 2. Nginx配置示例

```nginx
server {
    listen 80;
    server_name medical-device.example.com;

    root /var/www/medical-device/dist;
    index index.html;

    # 前端路由
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API代理
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

## Docker Compose 一键部署

创建 `docker-compose.yml`:

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: medical_device
    volumes:
      - mysql-data:/var/lib/mysql
      - ./backend/src/main/resources/sql:/docker-entrypoint-initdb.d
    ports:
      - "3306:3306"

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/medical_device
      SPRING_DATA_REDIS_HOST: redis

  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  mysql-data:
```

启动命令:
```bash
docker-compose up -d
```

## 生产环境建议

1. **数据库**: 配置主从复制，定期备份
2. **Redis**: 开启持久化，配置密码
3. **HTTPS**: 使用Let's Encrypt配置SSL证书
4. **日志**: ELK Stack 日志收集分析
5. **监控**: Prometheus + Grafana 系统监控
