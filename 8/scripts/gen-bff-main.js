const fs = require("fs");
const path = require("path");

function wp(f, c) {
  const d = path.dirname(f);
  if (!fs.existsSync(d)) fs.mkdirSync(d, { recursive: true });
  fs.writeFileSync(f, c, "utf8");
  console.log("OK:", f);
}

wp("apps/bff/src/main.ts",
  "import { NestFactory } from '@nestjs/core';\n" +
  "import { ValidationPipe } from '@nestjs/common';\n" +
  "import { AppModule } from './app.module';\n" +
  "import { AllExceptionsFilter } from './common/filters/exception.filter';\n" +
  "import { TransformInterceptor } from './common/interceptors/transform.interceptor';\n" +
  "import { TimeoutInterceptor } from './common/interceptors/timeout.interceptor';\n" +
  "async function bootstrap() {\n" +
  "  const app = await NestFactory.create(AppModule, { cors: true });\n" +
  "  app.setGlobalPrefix('api');\n" +
  "  app.useGlobalPipes(new ValidationPipe({ transform: true }));\n" +
  "  app.useGlobalFilters(new AllExceptionsFilter());\n" +
  "  app.useGlobalInterceptors(new TransformInterceptor(), new TimeoutInterceptor());\n" +
  "  const port = process.env.BFF_PORT || 3000;\n" +
  "  await app.listen(port);\n" +
  "  console.log('BFF Server running on http://localhost:' + port);\n" +
  "}\n" +
  "bootstrap();\n"
);

wp("apps/bff/src/app.module.ts",
  "import { Module } from '@nestjs/common';\n" +
  "import { ConfigModule } from '@nestjs/config';\n" +
  "import { CacheModule } from '@nestjs/cache-manager';\n" +
  "import { AuthModule } from './modules/auth/auth.module';\n" +
  "import { UserModule } from './modules/user/user.module';\n" +
  "import { OrderModule } from './modules/order/order.module';\n" +
  "import { TicketModule } from './modules/ticket/ticket.module';\n" +
  "import { MessageModule } from './modules/message/message.module';\n" +
  "import { LogModule } from './modules/log/log.module';\n" +
  "import { DashboardModule } from './modules/dashboard/dashboard.module';\n" +
  "import { AggregateModule } from './modules/aggregate/aggregate.module';\n" +
  "import { HttpService } from './common/services/http.service';\n" +
  "@Module({\n" +
  "  imports: [\n" +
  "    ConfigModule.forRoot({ isGlobal: true }),\n" +
  "    CacheModule.register({ isGlobal: true, ttl: 300 }),\n" +
  "    AuthModule, UserModule, OrderModule, TicketModule, MessageModule, LogModule, DashboardModule, AggregateModule\n" +
  "  ],\n" +
  "  providers: [HttpService]\n" +
  "})\n" +
  "export class AppModule {}\n"
);

wp("apps/bff/.env",
  "BFF_PORT=3000\n" +
  "JWT_SECRET=platform-secret-key-2024\n" +
  "REDIS_HOST=localhost\n" +
  "REDIS_PORT=6379\n" +
  "MYSQL_HOST=localhost\n" +
  "MYSQL_PORT=3306\n" +
  "MYSQL_USER=root\n" +
  "MYSQL_PASSWORD=123456\n" +
  "MYSQL_DATABASE=platform\n" +
  "USER_SERVICE_URL=http://localhost:3001\n" +
  "ORDER_SERVICE_URL=http://localhost:3002\n" +
  "TICKET_SERVICE_URL=http://localhost:3003\n" +
  "NOTIFICATION_SERVICE_URL=http://localhost:3004\n" +
  "CACHE_TTL=300000\n" +
  "REQUEST_TIMEOUT=5000\n"
);

console.log('=== BFF main and env generated! ===');