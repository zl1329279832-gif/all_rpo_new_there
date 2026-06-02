FROM node:20-alpine AS builder

WORKDIR /app

COPY package*.json ./

RUN npm ci --registry=https://registry.npmmirror.com

COPY . .

RUN npm run build

FROM nginx:alpine

LABEL maintainer="Warehouse Robot Viewer"
LABEL description="智能仓储机器人三维模型展示系统"
LABEL version="1.0.0"

COPY --from=builder /app/dist /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

RUN sed -i 's/#gzip  on;/gzip  on;/' /etc/nginx/nginx.conf && \
    echo 'gzip_vary on;' >> /etc/nginx/nginx.conf && \
    echo 'gzip_min_length 1024;' >> /etc/nginx/nginx.conf && \
    echo 'gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript image/svg+xml;' >> /etc/nginx/nginx.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
