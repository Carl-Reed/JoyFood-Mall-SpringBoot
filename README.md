# JoyFood 食品商城管理系统后端 (JoyFood Mall Backend)

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.x-brightgreen" alt="Spring Boot">
  <img src="https://img.shields.io/badge/JDK-21-blue" alt="JDK">
  <img src="https://img.shields.io/badge/MySQL-8.0-orange" alt="MySQL">
  <img src="https://img.shields.io/badge/MyBatis--Plus-3.5.12-red" alt="MyBatis-Plus">
  <img src="https://img.shields.io/badge/Spring%20Security-6.x-yellow" alt="Spring Security">
  <img src="https://img.shields.io/badge/JWT-HS256-purple" alt="JWT">
  <img src="https://img.shields.io/badge/License-MIT-green" alt="License">
</p>

<p align="center">
  <b>基于 Spring Boot 3.5 + Vue 3 前后端分离架构的食品商城管理系统后端服务</b>
</p>

---

## 📖 项目简介

JoyFood 食品商城管理系统是一套面向中小型食品零售商的数字化经营解决方案。本后端服务基于 **Spring Boot 3.5** 构建，采用前后端分离架构，通过 RESTful API 为前端提供数据支持。系统涵盖商品管理、订单流转、用户权限控制、数据可视化等核心模块，实现食品零售业务全流程数字化管理。

---

## ✨ 核心特性

- **🔐 安全认证体系**：基于 Spring Security + JWT 实现无状态身份认证，BCrypt 强哈希加密存储密码
- **📦 订单事务一致性**：采用 `@Transactional` 注解保障订单确认与销量同步的原子性
- **🛡️ 防超卖策略**："下单预占、支付扣减" 库存管理机制，基于乐观锁防止并发超卖
- **📊 数据可视化接口**：为前端 ECharts 图表提供销售趋势、品类占比等经营数据
- **🔒 RBAC 权限控制**：基于角色的访问控制，支持 ROOT / ADMIN / MERCHANT / USER 四级角色
- **🗑️ 逻辑删除机制**：关键数据表采用逻辑删除，保障历史订单可追溯性
- **📁 文件资源管理**：异步图片上传、引用状态追踪、过期自动清理

---

## 🛠️ 技术栈

| 类别 | 技术 | 版本 |
|:---|:---|:---|
| 核心框架 | Spring Boot | 3.5.x |
| JDK | Java | 21 (LTS) |
| 数据持久层 | MyBatis-Plus | 3.5.12 |
| 安全框架 | Spring Security | 6.x |
| 认证机制 | JWT (JSON Web Token) | - |
| 密码加密 | BCrypt | - |
| 数据库 | MySQL | 8.0.41 |
| 构建工具 | Maven | - |
| 开发工具 | IntelliJ IDEA | 2022.2+ |

---

## 🚀 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+
- MySQL 8.0+
- IntelliJ IDEA（推荐）

### 1. 克隆项目

```bash
git clone https://github.com/Carl-Reed/JoyFood-Mall-SpringBoot.git
cd joyfoodmall
```

### 2. 创建数据库

```sql
CREATE DATABASE joyfood_mall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
导入[releases](https://github.com/Carl-Reed/JoyFood-Mall-SpringBoot/releases)中的sql数据

### 3. 配置数据库

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/joyfood_mall?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password

  # JWT 配置
  jwt:
    secret: your-secret-key-here

```

### 4. 运行项目

```bash
# 方式一：使用 Maven
mvn spring-boot:run

# 方式二：打包后运行
mvn clean package
java -jar target/joyfoodmall-1.0.0.jar
```

服务启动后，API 默认访问地址：`http://localhost:8080`

---

## 📦 部署

### 推荐运行环境

| 环境类型 | 最小配置 | 推荐配置 |
|:---|:---|:---|
| 服务器 | CPU 2核 / 内存 4GB / 硬盘 50GB | CPU 4核 / 内存 8GB / 硬盘 100GB |
| 数据库 | MySQL 8.0 | MySQL 8.0 |
| Web 服务器 | Nginx 1.20.0 | Nginx 1.24.0 |

### Docker 部署（可选）

```bash
# 构建镜像
docker build -t joyfoodmall

# 运行容器
docker run -d -p 8080:8080   -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/joyfood_mall   -e SPRING_DATASOURCE_USERNAME=root   -e SPRING_DATASOURCE_PASSWORD=password   joyfoodmall
```

---

## 🔗 前端项目

本项目为后端服务，配套前端项目请访问：

👉 [JoyFoodMall-Vue3](https://github.com/Carl-Reed/JoyFoodMall-Vue3) （Vue 3 + Element Plus + Vite）

---

## 📁 项目结构

```
joyfoodmall/
├── .mvn/                          # Maven Wrapper
├── src/
│   └── main/
│       ├── java/com/lpw/joyfoodmall/
│       │   ├── JoyFoodMallApplication.java    # 启动类
│       │   ├── common/              # 通用工具类、常量、枚举
│       │   ├── component/           # 自定义组件（如 JWT 过滤器）
│       │   ├── config/              # 配置类（Security、MyBatis-Plus、CORS 等）
│       │   ├── controller/          # 控制器层（REST API 接口）
│       │   │   ├── admin/           # 后台管理接口
│       │   │   └── mall/            # 前台商城接口
│       │   ├── entity/              # 实体类
│       │   │   ├── DTO/             # 数据传输对象（请求/响应参数）
│       │   │   └── VO/              # 视图对象（返回前端的数据结构）
│       │   ├── mapper/              # MyBatis-Plus 数据访问层
│       │   ├── service/             # 业务逻辑层
│       │   │   └── impl/            # 业务实现类
│       │   ├── task/                # 定时任务（如定时删除用户上传的无用文件）
│       │   └── utils/               # 工具类（文件处理、密码生成等）
│       └── resources/
│           ├── mapper/              # MyBatis XML 映射文件
│           └── application.yml      # 应用配置文件
└── test/                            # 单元测试
```

### 分层架构说明

```
┌─────────────────────────────────────────────────────────────┐
│  Controller 层  │ 接收 HTTP 请求 参数校验 调用 Service        │
├─────────────────────────────────────────────────────────────┤
│  Service 层     │ 业务逻辑处理，事务控制，数据运算             │
├─────────────────────────────────────────────────────────────┤
│  Mapper 层      │ 数据库 CRUD 操作，通过 MyBatis-Plus 实现    │
├─────────────────────────────────────────────────────────────┤
│  Entity 层      │ 数据模型:PO(实体)、DTO(传输)、VO(视图)      │
└─────────────────────────────────────────────────────────────┘
```

---

## 📊核心业务流程

```
┌──────────────┐      ┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│   用户注册    │────▶│   JWT 登录   │────▶│  浏览/搜索商品 │────▶│  加入购物车   │
└──────────────┘      └──────────────┘      └──────────────┘      └──────┬───────┘
                                                                         │
┌──────────────┐      ┌──────────────┐      ┌──────────────┐             │
│   确认收货    │◀────│   订单发货    │◀────│   模拟支付    │  ◀─────────┘
│  (销量同步)   │      │  (物流录入)   │      │  (库存扣减)   │
└──────────────┘      └──────────────┘       └──────────────┘
```

---

## 🗄️ 数据库设计

### 核心数据表

| 表名 | 说明 |
|:---|:---|
| `sys_user` | 用户表（含 Spring Security 安全字段） |
| `sys_role` | 角色表 |
| `sys_user_role` | 用户角色关联表 |
| `pms_product` | 商品主表（SPU） |
| `pms_sku_stock` | SKU 库存表（规格、价格、库存） |
| `pms_category` | 商品分类表（支持多级树形结构） |
| `oms_order` | 订单主表 |
| `oms_order_item` | 订单明细表 |
| `oms_cart_item` | 购物车表 |
| `ums_member_receive_address` | 会员收货地址表 |
| `sms_home_banner` | 首页轮播图表 |
| `sys_file` | 文件上传记录表 |

### 关键设计特性

- **逻辑删除**：关键表均含 `is_deleted` 字段，保障数据可追溯
- **审计字段**：所有表均含 `create_time`、`update_time`、`create_by`、`update_by`
- **快照存储**：订单明细记录商品名称、价格、图片等快照，避免后续商品变更影响历史订单
- **状态机**：订单状态采用数值编码（0-待支付 → 1-待发货 → 2-已发货 → 3-待收货 → 4-已完成 / 5-已取消）

---

## 🔐 安全机制

### 认证流程

```
用户登录
    │
    ▼
BCrypt 密码比对
    │
    ▼
生成 JWT Token（含 userId, username, role, exp）
    │
    ▼
客户端存储 Token（LocalStorage）
    │
    ▼
后续请求携带 Token ──▶ Spring Security 拦截器校验 ──▶ 放行/拒绝
```

### 权限控制

| 角色 | 权限范围 |
|:---|:---|
| `ROLE_ROOT` | 超级管理员，拥有系统所有权限 |
| `ROLE_ADMIN` | 普通管理员，拥有系统的部分权限 |
| `ROLE_MERCHANT` | 商家管理员，拥有管理商品的权限 |
| `ROLE_USER` | 普通用户，仅允许浏览商城内容 |

### 防护措施

- ✅ SQL 注入防护：MyBatis-Plus 参数化查询
- ✅ XSS 攻击防护：前端模板自动转义 + 富文本 HTML 净化
- ✅ CSRF 防护：JWT 无状态认证 + SameSite Cookie 策略
- ✅ 越权访问防护：接口层 `@PreAuthorize` 注解 + 数据范围过滤
- ✅ 密码安全：BCrypt 自动加盐，工作因子 10

---

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/xxx`
3. 提交更改：`git commit -m 'Add some feature'`
4. 推送分支：`git push origin feature/xxx`
5. 创建 Pull Request

欢迎提交 Issue 或 Pull Request 共同完善项目！

---

## 📄 许可证

本项目基于 [MIT License](https://github.com/Carl-Reed/JoyFood-Mall-SpringBoot/blob/main/LICENSE) 开源。

<p align="center">
  <sub>Built with ❤️ by CarlReed | © 2026 JoyFood Mall</sub>
</p>
