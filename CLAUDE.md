# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

智能养老院管理系统 (Smart Elderly Care Management System) — full-stack web application for managing nursing home operations: check-in/checkout, bed management, contracts, reservations, visits, leave, nursing plans, and AI-assisted features.

## Quick Start

### Backend (`zzyl/`)
```bash
cd zzyl
# First time: copy and edit the config template (fill in your MySQL password + OSS/DashScope keys)
cp src/main/resources/application.properties.example src/main/resources/application.properties
mvn spring-boot:run        # Starts on port 8080

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=ClassName

# Package for production
mvn package -DskipTests
```
Requires JDK 17, Maven 3.8+, MySQL 8. Create database `zzyl` first, then run the SQL scripts in order:
1. `zzyl/sql/module_acd.sql` — base tables (reservation, visit, customer, contract, room_type, floor, room, bed, leave, checkout)
2. `zzyl/sql/module_checkin.sql` — checkin table

`DataInitRunner` seeds demo data on first startup and auto-adds missing columns.

### Frontend (`zzylvue/`)
```bash
cd zzylvue
npm install
npm run serve              # Dev server on port 80, proxies API to :8080 via axios baseURL
npm run build              # Production build → dist/
```

Default login: `admin` / `123456` (seeded by `DataInitRunner`).

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend framework | Spring Boot 3.5.5 (Java 17) |
| ORM | MyBatis-Plus 3.5.9 (Spring Boot 3 starter) |
| Database | MySQL 8 |
| Frontend | Vue 3 + Element Plus 2 + Vue Router 4 + Axios |
| AI | Spring AI + Alibaba DashScope (qwen model) |
| File storage | Aliyun OSS (with local fallback via `UploadWebConfig`) |
| Utilities | Hutool 5.8, Lombok |

## Backend Architecture (`com.soft`)

Standard layered structure under `zzyl/src/main/java/com/soft/`:

- **`controller/`** — REST controllers. Most inject mappers directly via `@Autowired` (no service layer indirection). Controllers that do use services: `LoginController`, `MenuController`, `NursingItemController`, `NursingLevelController`, `NursingPlainItemController`, `PlainItemController`, `UserController`.
- **`service/` + `service/impl/`** — MyBatis-Plus `IService`/`ServiceImpl` pattern. Only `Menu`, `NursingItem`, `NursingLevel`, `NursingPlain`, `PlainItem`, `User` have service layers.
- **`mapper/`** — MyBatis-Plus `BaseMapper` interfaces. Six have companion XML files in `resources/mapper/`: `MenuMapper.xml`, `NursimgItemMapper.xml`, `NursingLevelMapper.xml`, `NursingPlainMapper.xml`, `PlainItemMapper.xml`, `UserMapper.xml`. Use `LambdaQueryWrapper` for type-safe queries.
- **`pojo/`** — Entity classes mapped to `t_*` tables.
- **`dto/`** — Request/transfer objects in `com.soft.dto`. `PageQueryDto` is the standard paginated query DTO (pageNum, pageSize, keyword, plus domain-specific filter fields — status, type, elderName, etc.).
- **`common/Result<T>`** — Standard API response wrapper with `code`, `msg`, `data`, `total`. Static factories: `Result.ok(data)`, `Result.ok(data, total)`, `Result.fail(msg)`.
- **`config/`**:
  - `CrossOriginConfig` — CORS for localhost origins
  - `MybatisplusPageConfig` — pagination plugin (MySQL, max 500/page, overflow wraps to page 1)
  - `DataInitRunner` — `CommandLineRunner` that seeds demo data and auto-adds missing columns to `t_checkout` and `t_checkin` on startup
  - `UploadWebConfig` — serves `uploads/` directory as static resources
  - `JacksonConfig`, `AiliModelConfig`, `DemoDataFill`
- **`utils/AliyunOssUtils`** — Aliyun OSS file upload helper, configured via `aliyun.oss.*` properties

### Key patterns
- **Auth**: No Spring Security or login interceptors — authentication is entirely session-based (`HttpSession`). The login flow stores the authenticated user as `UserLineDto` under the `"online"` session attribute. Most controllers do not explicitly check session — auth gating is minimal in the current state.
- Controllers freely mix `@Autowired` mappers directly with service calls — there's no strict rule about always going through the service layer.
- MyBatis-Plus `LambdaQueryWrapper` is the dominant query style. The six XML mappers listed above are the exception.
- **Response format**: Most controllers return `Result<T>` (`code`, `msg`, `data`, `total`). Exception: `LoginController` returns raw `Map<String,Object>` directly.
- **Multi-step workflow**: `t_checkin` and `t_checkout` both have `step` (INT) and `step_status` (VARCHAR) fields representing a multi-step approval process. The frontend uses `CheckinSteps`/`CheckoutSteps` components to render these. `flow_status`, `approval_result`, and `approval_comment` track the overall approval state.
- **File uploads**: `FileController` provides two endpoints — `/upload` (PNG/JPG/JPEG/PDF, max 2MB) and `/upload/report` (PDF only, max 60MB). Files are stored locally in the project-relative `uploads/` directory and served via `UploadWebConfig`. Cloud uploads go through `AliyunOssUtils`.
- **Auto-DDL**: `mybatis-plus.global-config.db-config.table-auto-create=true` is set, so MyBatis-Plus will auto-create missing tables from entity classes. `DataInitRunner` also auto-adds missing columns to `t_checkout` and `t_checkin` on startup.

## Frontend Architecture (`zzylvue/`)

- **`src/router/index.js`** — Vue Router with hash history. Root `/` is `HomeView` (login). After login, routes live under `/MainIndex` (layout shell) as lazy-loaded children (23 routes total).
- **`src/config/menu.js`** — Top-level navigation menu config with module grouping. Exports `topMenus`, `findModuleByPath()`, `getSideMenus()`. Module IDs: `workbench`, `visit`, `checkinout`, `resident`, `customer`, `collab`, `profile`.
- **`src/main.js`** — App bootstrap. Configures Axios globally: `baseURL = http://localhost:8080`, `withCredentials = true` (for session cookie), 5s timeout, plus a response interceptor that auto-formats ISO date strings to `yyyy-MM-dd HH:mm:ss`.
- **`src/views/`** — Page components (one per route). Names roughly map to routes (e.g., `CheckinProcess.vue` → `/CheckinProcess`).
- **`src/components/`** — Reusable components: `CheckinSteps`, `CheckoutSteps`, `PageCard`, plus sub-folders for checkin/checkout info sections.

## Database

- Database name: `zzyl`
- All tables use `t_` prefix (e.g., `t_user`, `t_checkin`, `t_reservation`, `t_bed`, `t_room`, `t_floor`, `t_room_type`, `t_contract`, `t_customer`, `t_visit`, `t_leave`, `t_checkout`, `t_nursing_item`, `t_nursing_plain`, `t_nursing_level`, `t_plain_item`, `t_menu`)
- `DataInitRunner` uses `JdbcTemplate` to auto-add missing columns to `t_checkout` and `t_checkin` on startup (ethnicity, political_status, religion, marital_status, education_level, income_source, medical_insurance, hobbies, medical_insurance_no for `t_checkin`; flow_status, approval_result, approval_comment for `t_checkout`)

## Security Note

`application.properties.example` contains credentials (Aliyun OSS keys, DashScope API key). These should be rotated. The file `.gitignore` excludes `application.properties` but the example template itself was committed with real values.
