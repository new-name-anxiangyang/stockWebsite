# Stock Website Demo

这是一个面向 Java 初学者的股票行情查询 Demo。项目从单体 Spring Boot 应用逐步演进为包含业务服务、登录鉴权服务、Gateway 网关、Nacos 和 Nginx 的简单微服务项目。

## 1. 项目功能

- 查询股票历史行情；
- 从 Marketstack 获取外部行情数据；
- 使用 MySQL 保存股票数据；
- 使用 Redis 缓存查询结果；
- 使用 Nacos 进行服务注册和配置管理；
- 使用 Gateway 统一路由；
- 使用 JWT 登录鉴权；
- 使用 Nginx 提供前端页面并转发 `/api/**` 请求。

## 2. 系统架构

```text
浏览器
   │
   ▼
Nginx:80
   ├── /              前端静态页面
   └── /api/**        转发到 Gateway
                         │
                         ├── /auth/**   → auth-service:8083
                         └── /stocks/** → stock-service:8081

Nacos:8848
   ├── 服务注册中心
   └── 配置中心

stock-service
   ├── MySQL
   ├── Redis
   └── Marketstack API
```

推荐从前端访问：

```text
http://localhost/
```

前端请求会使用：

```text
/api/auth/login
/api/stocks/AAPL
```

## 3. 目录结构

```text
stockWebsite/
├── pom.xml                         # Maven 根项目
├── stockDemo/                      # stock-service 股票业务服务
│   ├── src/main/java/com/stock/
│   │   ├── Controller/             # HTTP 接口层
│   │   ├── Service/                # 业务逻辑层
│   │   ├── Repository/             # JPA 数据访问层
│   │   ├── Entity/                 # 数据库实体
│   │   ├── Dto/                    # 外部数据对象
│   │   ├── Vo/                     # 接口返回对象
│   │   └── Exception/              # 全局异常处理
│   └── src/main/resources/
│       └── application.yml         # Nacos 连接配置
├── GateWay/                        # Gateway 网关和 JWT 校验
├── auth-service/                   # 登录、用户和 JWT 签发
├── nginx-1.31.6/
│   ├── conf/nginx.conf             # Nginx 配置
│   └── html/index.html             # 前端页面
└── README.md
```

## 4. 运行环境

建议使用：

```text
JDK 17
Maven 3.9+
MySQL 8+
Redis 6+
Nacos 3.x
Nginx 1.31.6
```

当前 Maven 项目主要使用：

```text
Spring Boot 4.1.1
Spring Cloud 2025.1.x
Spring Cloud Alibaba 2025.1.0.0
```

Spring Boot、Spring Cloud 和 Spring Cloud Alibaba 必须保持兼容。升级其中一个版本时，应一起检查依赖版本。

## 5. 创建数据库

```sql
CREATE DATABASE stock_db
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE DATABASE auth_db
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```

`stock-service` 使用 `stock_db`，`auth-service` 使用 `auth_db`。开发环境中的 JPA 配置会自动创建或更新表。

## 6. 配置 Nacos

启动 Nacos 后打开：

```text
http://localhost:8848/nacos
```

创建命名空间：

```text
名称：dev
ID：dev
```

如果 Nacos 使用了其他 Namespace ID，项目配置中的 `namespace` 必须填写实际 ID。

### 6.1 stock-service.yml

Data ID：

```text
stock-service.yml
```

Group：

```text
DEFAULT_GROUP
```

示例：

```yaml
server:
  port: 8081

marketstack:
  base-url: https://api.marketstack.com/v2
  api: ${MARKETSTACK_API}

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/stock_db?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
```

### 6.2 auth-service.yml

Data ID：

```text
auth-service.yml
```

示例：

```yaml
server:
  port: 8083

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/auth_db?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update

jwt:
  private-key-location: file:D:/secure/stockWebsite/jwt/private.pem
  issuer: auth-service
  expire-seconds: 1800
```

### 6.3 gateway.yml

建议统一使用以下 Data ID：

```text
gateway.yml
```

项目历史配置中可能使用过 `gateWay.yml`，Data ID、`spring.application.name` 和 `spring.config.import` 必须保持一致。

示例：

```yaml
server:
  port: 8082

spring:
  cloud:
    gateway:
      server:
        webflux:
          routes:
            - id: auth-service
              uri: lb://auth-service
              predicates:
                - Path=/auth/**
            - id: stock-service
              uri: lb://stock-service
              predicates:
                - Path=/stocks/**
    nacos:
      server-addr: 127.0.0.1:8848
  security:
    oauth2:
      resourceserver:
        jwt:
          public-key-location: classpath:JWT/public.pem
```

## 7. JWT 密钥

认证服务使用私钥签发 JWT，Gateway 使用公钥验证 JWT：

```text
auth-service → private.pem
Gateway      → public.pem
```

生成密钥：

```powershell
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private.pem
openssl rsa -pubout -in private.pem -out public.pem
```

不要将 `private.pem` 提交到 Git。生产环境应使用环境变量、密钥管理系统或加密配置。

## 8. 启动顺序

```text
1. MySQL
2. Redis
3. Nacos
4. stock-service
5. auth-service
6. Gateway
7. Nginx
```

在 Nacos 服务列表中应看到：

```text
stock-service
auth-service
gateway
```

启动服务：

```powershell
mvn -f stockDemo/pom.xml spring-boot:run
mvn -f auth-service/pom.xml spring-boot:run
mvn -f GateWay/pom.xml spring-boot:run
```

也可以使用 IntelliJ IDEA 分别运行：

```text
StockDemoApplication
AuthServiceApplication
GateWayApplication
```

## 9. Nginx 配置

核心配置：

```nginx
location / {
    root html;
    index index.html;
}

location /api/ {
    proxy_pass http://127.0.0.1:8082/;
}
```

末尾的 `/` 会去掉 `/api` 前缀：

```text
/api/stocks/AAPL → /stocks/AAPL
```

检查和启动：

```powershell
cd nginx-1.31.6
.\nginx.exe -t
.\nginx.exe
```

修改配置后重新加载：

```powershell
.\nginx.exe -t
.\nginx.exe -s reload
```

Windows 下如果提示 `Access is denied`，请使用管理员 PowerShell，并确认当前操作的是正在运行的 Nginx 主进程。

## 10. 登录流程

### 10.1 登录

```http
POST /api/auth/login
Content-Type: application/json
```

请求体：

```json
{
  "username": "admin",
  "password": "123"
}
```

当前开发测试账号为：

```text
用户名：admin
密码：123
```

成功后返回 JWT：

```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 1800
}
```

### 10.2 调用业务接口

之后的请求需要携带：

```http
Authorization: Bearer eyJ...
```

请求流程：

```text
浏览器 → Nginx → Gateway 验证 JWT → stock-service
```

## 11. 接口列表

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| POST | `/api/auth/login` | 否 | 登录并获取 JWT |
| GET | `/api/stocks/ping` | 是 | 检查股票服务 |
| GET | `/api/stocks/{symbol}` | 是 | 查询股票行情 |
| GET | `/api/stocks/db/{symbol}` | 是 | 查询数据库数据 |
| GET | `/api/stocks/history/{symbol}` | 是 | 分页查询历史行情 |
| POST | `/api/stocks/{symbol}/refresh` | 是 | 刷新行情 |

## 12. PowerShell 测试

获取 Token：

```powershell
$body = @{
    username = "admin"
    password = "123"
} | ConvertTo-Json

$login = Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost/api/auth/login" `
    -ContentType "application/json" `
    -Body $body

$token = $login.accessToken
```

不带 Token：

```powershell
Invoke-WebRequest "http://localhost/api/stocks/AAPL"
```

预期返回：

```text
401 Unauthorized
```

带 Token：

```powershell
Invoke-RestMethod `
    -Uri "http://localhost/api/stocks/AAPL" `
    -Headers @{ Authorization = "Bearer $token" }
```

预期结果是股票 JSON 数据。
也可以使用postman或knife4j进行接口测试，其中knife4j的依赖已经添加在pom中了
## 13. 常见问题

### 401 Unauthorized

通常表示没有携带 JWT、JWT 已过期，或 Gateway 公钥和 auth-service 私钥不匹配。

### 403 Forbidden

通常表示用户名或密码错误、用户被禁用，或者用户没有对应权限。

### 404 Not Found

检查 Gateway 路由前缀、Nginx 是否去掉 `/api`、请求路径和 Nginx 配置是否已重新加载。

### 503 Service Unavailable

通常表示 Nacos 没有找到目标服务。检查服务名、Namespace、Nacos 地址和服务注册状态。

### 页面可以打开但接口失败

按以下顺序检查：

```text
stock-service 直连 → Gateway 直连 → Nginx 转发 → 前端调用
```

## 14. 适合初学者的学习路线

### Java 基础

- 类和对象；
- 接口和实现；
- 泛型；
- Record；
- 异常处理；
- Lambda 和 Stream。

### Spring Boot

- `@SpringBootApplication`；
- `@RestController`；
- `@RequestMapping`；
- 依赖注入；
- YAML 配置；
- 全局异常处理。

### 数据库

- Entity；
- Repository；
- JPA 查询方法；
- 分页查询；
- MySQL 表结构。

### 微服务

- Nacos 服务注册；
- Nacos 配置中心；
- Gateway 路由；
- `lb://service-name`；
- Nginx 反向代理。

### 安全

- BCrypt 密码加密；
- JWT 的 Header、Payload 和 Signature；
- 私钥签发 Token；
- 公钥验证 Token；
- 401 和 403 的区别。

## 15. 后续扩展

- 用户注册；
- Refresh Token；
- 退出登录和 Token 黑名单；
- 角色权限控制；
- OpenFeign 服务间调用；
- Docker Compose；
- 健康检查；
- 统一日志和链路追踪；
- HTTPS、限流和防暴力破解。

## 16. 安全说明

这是学习 Demo，不应直接用于生产环境。当前仍需要进一步加强：

- 数据库密码和 Marketstack API Key 管理；
- JWT 私钥保护；
- HTTPS；
- 认证失败限流；
- 账号锁定；
- Token 刷新和注销；
- 禁止外部直接访问 `stock-service:8081` 和 `auth-service:8083`。

