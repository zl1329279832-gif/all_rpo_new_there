const fs = require("fs");

const pkg = {
  name: "group-operation-platform",
  version: "1.0.0",
  description: "面向集团业务的多应用运营中台",
  private: true,
  workspaces: ["apps/*", "packages/*"],
  scripts: {
    "dev:web": "npm run dev -w apps/web",
    "dev:bff": "npm run start:dev -w apps/bff",
    "dev:user": "npm run dev -w apps/user-service",
    "dev:order": "npm run dev -w apps/order-service",
    "dev:ticket": "npm run dev -w apps/ticket-service",
    "dev:notification": "npm run dev -w apps/notification-service",
    "dev:services": "concurrently \"npm run dev:user\" \"npm run dev:order\" \"npm run dev:ticket\" \"npm run dev:notification\"",
    "dev:all": "concurrently \"npm run dev:web\" \"npm run dev:bff\" \"npm run dev:services\"",
    "build:web": "npm run build -w apps/web",
    "build:bff": "npm run build -w apps/bff",
    "build": "npm run build:web && npm run build:bff",
    "start:bff": "npm run start:prod -w apps/bff",
    "start:services": "concurrently \"npm start -w apps/user-service\" \"npm start -w apps/order-service\" \"npm start -w apps/ticket-service\" \"npm start -w apps/notification-service\"",
    "mock": "node scripts/init-mock.js",
    "init:data": "node scripts/init-mock.js",
    "test": "node scripts/init-mock.js"
  },
  devDependencies: {
    "concurrently": "^8.2.2",
    "nodemon": "^3.0.2",
    "typescript": "^5.3.3"
  }
};

fs.writeFileSync("package.json", JSON.stringify(pkg, null, 2));
console.log("package.json updated successfully!");