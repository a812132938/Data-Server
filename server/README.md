# Data Server

Data Server 是一个基于 Spring Boot 的数据服务平台，用于把受管理的数据源查询发布为 HTTP API，并通过网关统一提供认证、鉴权、限流、执行、脱敏、日志和监控能力。

## 项目结构

| 路径 | 说明 |
| --- | --- |
| `pom.xml` | Maven 父工程，聚合所有模块 |
| `data-server-runtime/` | 共享运行时模块，包含实体、DTO、VO、Mapper、Service、通用配置和共享资源 |
| `data-server-admin-app/` | 管理端 Spring Boot 应用，包含管理端 Controller 和启动配置 |
| `data-server-gateway-app/` | 网关 Spring Boot 应用，包含网关入口、认证、过滤链、路由、限流和调用日志 |

## 技术栈

| 类型 | 技术 |
| --- | --- |
| JDK | Java 8 |
| 构建工具 | Maven |
| 后端框架 | Spring Boot 2.5.15 |
| Web | Spring MVC |
| 参数校验 | Spring Validation |
| ORM | MyBatis Plus 3.5.3.1 |
| 数据库 | MySQL，PostgreSQL 驱动已引入 |
| 数据库连接池 | HikariCP |
| 数据库迁移 | Flyway |
| 缓存/限流/路由通知 | Redis |
| 认证 | Sa-Token 1.37.0，Sa-Token JWT |
| API 文档 | Knife4j OpenAPI2 |
| 工具库 | Hutool |
| 加密 | Bouncy Castle + 项目 AES 工具 |
| 监控 | Spring Boot Actuator，Micrometer Prometheus |
| 日志 | SLF4J + Logback |
| 代码简化 | Lombok |

## 环境要求

| 依赖 | 要求/默认值 |
| --- | --- |
| JDK | 8 |
| Maven | 3.x |
| MySQL | 5.7+/8.x，默认 `localhost:3306` |
| 数据库 | 默认 `data_server` |
| Redis | 5+，默认 `localhost:6379` |
| Admin 端口 | `8080` |
| Gateway 端口 | `8081` |
| 编码 | UTF-8 |

数据库迁移脚本：

```text
data-server-runtime/src/main/resources/db/migration/V1__init.sql
```

Admin 服务启动时默认执行 Flyway 迁移；Gateway 服务默认不执行 Flyway。

默认登录账号：

| 用户名 | 默认密码 | 说明 |
| --- | --- | --- |
| `admin` | `admin123` | 管理员账号 |
| `system` | `admin123` | 系统内置账号 |

初始化脚本中密码字段使用 `v1:CHANGE_ME` 占位，Admin 服务启动后会按当前 `ENCRYPT_KEY` 自动加密更新。

## 模块说明

| 模块 | 是否可启动 | 默认端口 | 配置文件 | 主要职责 |
| --- | --- | --- | --- | --- |
| `data-server-runtime` | 否 | 无 | 无默认 `application.yml` | 共享实体、DTO、VO、Mapper、Service、通用配置、SQL 执行、数据脱敏、Flyway/Lua 资源 |
| `data-server-admin-app` | 是 | `8080` | `data-server-admin-app/src/main/resources/application.yml` | 管理端 API、数据源管理、API 定义、测试、文档、授权、日志、监控、告警、多表查询向导 |
| `data-server-gateway-app` | 是 | `8081` | `data-server-gateway-app/src/main/resources/application.yml` | `/gateway/**` 对外调用、路由表、认证、鉴权、限流、参数校验、执行编排、脱敏、调用日志 |

## 访问地址

| 类型 | 方法 | 地址 | 说明 |
| --- | --- | --- | --- |
| Knife4j | GET | `http://localhost:8080/doc.html` | Admin API 文档 |
| 网关调用 | GET/POST | `http://localhost:8081/gateway/{api-path}` | 对外 API 调用入口 |
| 路由状态 | GET | `http://localhost:8081/internal/gateway/routes/status` | 查看网关路由表状态 |
| 路由列表 | GET | `http://localhost:8081/internal/gateway/routes` | 查看已加载路由 |
| 路由刷新 | POST | `http://localhost:8081/internal/gateway/routes/reload` | 手动刷新网关路由 |

## 配置说明

| 配置项 | Admin 默认值 | Gateway 默认值 | 说明 |
| --- | --- | --- | --- |
| `server.port` | `${ADMIN_SERVER_PORT:8080}` | `${GATEWAY_SERVER_PORT:8081}` | 服务端口 |
| `spring.application.name` | `data-server-admin` | `data-server-gateway` | 应用名 |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/data_server?...` | `jdbc:mysql://localhost:3306/data_server?...` | 主库连接 |
| `spring.datasource.username` | `root` | `root` | 主库用户名 |
| `spring.datasource.password` | `Hcd123.@` | `Hcd123.@` | 主库密码，生产环境应覆盖 |
| `spring.redis.host` | `${REDIS_HOST:localhost}` | `${REDIS_HOST:localhost}` | Redis 地址 |
| `spring.redis.port` | `${REDIS_PORT:6379}` | `${REDIS_PORT:6379}` | Redis 端口 |
| `spring.flyway.enabled` | `true` | `false` | 是否执行数据库迁移 |
| `knife4j.enable` | `true` | `false` | 是否启用 Knife4j |
| `mybatis-plus.configuration.log-impl` | `Slf4jImpl` | `Slf4jImpl` | MyBatis 日志实现 |

常用环境变量：

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `ADMIN_SERVER_PORT` | `8080` | Admin 服务端口 |
| `GATEWAY_SERVER_PORT` | `8081` | Gateway 服务端口 |
| `REDIS_HOST` | `localhost` | Redis 地址 |
| `REDIS_PORT` | `6379` | Redis 端口 |
| `REDIS_PASSWORD` | 空 | Redis 密码 |
| `JWT_SECRET_KEY` | `default-dev-jwt-secret-key-32ch` | JWT 签名密钥 |
| `ENCRYPT_KEY` | `default-dev-encrypt-key-32ch!` | AES 加密密钥 |
| `LOG_PATH` | 按服务区分 | 日志目录覆盖 |

生产环境应通过外部配置或环境变量覆盖数据库账号、密码、JWT 密钥和加密密钥。

## 构建与运行

| 场景 | 命令 |
| --- | --- |
| 构建所有模块 | `mvn clean package` |
| 构建所有模块并跳过测试 | `mvn clean -DskipTests package` |
| 安装到本地 Maven 仓库 | `mvn clean install` |
| 启动 Admin | `mvn -pl data-server-admin-app spring-boot:run` |
| 启动 Gateway | `mvn -pl data-server-gateway-app spring-boot:run` |
| 运行 Admin JAR | `java -jar data-server-admin-app/target/data-server-admin.jar` |
| 运行 Gateway JAR | `java -jar data-server-gateway-app/target/data-server-gateway.jar` |

构建产物：

| 模块 | 产物 |
| --- | --- |
| `data-server-admin-app` | `data-server-admin-app/target/data-server-admin.jar` |
| `data-server-gateway-app` | `data-server-gateway-app/target/data-server-gateway.jar` |

本地初始化数据库：

```sql
CREATE DATABASE data_server DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

## 日志

两个服务都使用 `logback-spring.xml`，并按级别拆分日志文件。

| 服务 | 默认日志目录 |
| --- | --- |
| Admin | `logs/data-server-admin` |
| Gateway | `logs/data-server-gateway` |

| 文件 | 内容 |
| --- | --- |
| `all.log` | INFO 及以上 |
| `info.log` | 仅 INFO |
| `warn.log` | 仅 WARN |
| `error.log` | 仅 ERROR |

| 日志项 | 策略 |
| --- | --- |
| MyBatis SQL 日志 | 使用 `org.apache.ibatis.logging.slf4j.Slf4jImpl` |
| SQL 调试输出 | 默认不直接刷控制台 |
| 4xx 业务异常 | 按预期业务失败处理，不记录 ERROR |
| 未处理异常 | 记录 ERROR 和堆栈 |
| 敏感字段 | 密钥、token、签名、解密密码不得写入日志 |

## 核心能力

| 分类 | 能力 |
| --- | --- |
| 数据源 | 数据源注册、连接测试、连接池管理 |
| API 管理 | SQL API 创建、测试、发布、下线 |
| 查询设计 | 多表查询向导 SQL 生成、SQL 预览 |
| 参数响应 | API 参数管理、响应字段管理 |
| 版本 | API 版本快照、回滚 |
| 应用 | 应用注册、AppKey/AppCode 凭证 |
| 权限 | 授权、审批流程 |
| 网关 | 认证、鉴权、限流、路由表原子刷新 |
| 日志 | 网关调用日志、审计日志 |
| 监控 | 调用统计、监控看板、告警规则和通知 |
| 文档 | Knife4j API 文档 |

## 作者邮箱

`h1y2f3456@gmail.com`
