# 404-Shop

404-Shop 是一个微服务电商系统，后端以 Spring Boot / Spring Cloud 为主，前端使用 React + Vite。系统包含用户、商品、购物车、优惠券、订单、支付宝沙箱支付、后台管理等模块。

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 前端 | React 19, TypeScript, Vite, Ant Design |
| 网关 | Spring Cloud Gateway |
| 后端 | Spring Boot 2.3.3, Spring Cloud Hoxton, Java 8 |
| 注册中心 | Nacos |
| 数据库 | MySQL 8 |
| 缓存 | Redis |
| 消息队列 | RabbitMQ |
| 对象存储 | MinIO 本地演示，七牛云可选 |
| 部署 | Docker Compose |

## 快速启动

1. 启动 Docker Desktop 或 OrbStack。
2. 复制环境变量模板：

```bash
cp .env.example .env
```

3. 按需填写 `.env` 中的 `JWT_SECRET`、邮箱、支付宝沙箱、对象存储配置。真实密钥只放 `.env`，不要写入源码或文档。
4. 启动系统：

```bash
docker compose up -d --build
```

5. 打开页面：

| 入口 | 地址 |
| --- | --- |
| 前端 | http://localhost:5173 |
| 网关 | http://localhost:8090 |
| Nacos | http://localhost:8848/nacos |
| RabbitMQ | http://localhost:15672 |
| MinIO | http://localhost:9101 |
| MySQL | localhost:3307 |

演示账号见 [docs/测试账号.md](docs/测试账号.md)。

Apple Silicon 设备上，Compose 只会让 Nacos 使用 `linux/amd64` 仿真运行；MySQL、Redis、RabbitMQ、MinIO 和业务镜像仍按 Docker 自身能力选择可用架构。

如果出现 `failed to connect to the docker API`，先启动 Docker Desktop 或 OrbStack，再用 `docker ps` 验证 Docker daemon 已运行。

## 重要安全说明

- 不要提交 `.env`，真实 JWT、SMTP、支付宝、对象存储密钥只能保存在本机。
- 当前仓库已将源码中的真实密钥替换为环境变量占位，运行时从 `.env` 注入。
- 如果后续要把已有 Git 历史推到远端，需要先确认历史提交中没有旧密钥；最稳妥做法是使用清理后的工作区重新初始化一个干净仓库。

## 当前限制

- 本机直接 `mvn package` 需要 Java 8；Java 17/25 与当前 Lombok 版本不兼容，推荐使用 Docker 构建。
- 邮箱验证码、支付宝回调属于外部集成，需要在 `.env` 中配置真实沙箱或服务参数；对象存储默认使用本地 MinIO。
- Elasticsearch 搜索不是一键启动必需项，商品搜索保留 MySQL 降级说明。
