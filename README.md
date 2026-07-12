# 中州养老管理系统（全栈）

前后端一体仓库：

- `zzyl/` — Spring Boot 后端（端口 8080）
- `zzylvue/` — Vue 3 + Element Plus 前端

## 环境要求

- JDK 17
- Maven 3.8+
- Node.js 16+
- MySQL 8（库名 `zzyl`）

## 后端启动

```bash
cd zzyl
# 首次：复制配置模板并填写本地密码/密钥
copy src\main\resources\application.properties.example src\main\resources\application.properties
mvn spring-boot:run
```

默认账号：`admin` / `123456`（由 `DataInitRunner` 初始化）

## 前端启动

```bash
cd zzylvue
npm install
npm run serve
```

前端默认请求 `http://localhost:8080`

## 数据库

在 MySQL 中创建库 `zzyl`，执行 `zzyl/sql/module_acd.sql`（如需要）。

## 说明

- 本地真实配置写在 `zzyl/src/main/resources/application.properties`，该文件不会提交到 Git。
- 上传 GitHub 前请确认未包含数据库密码、API Key 等敏感信息。
