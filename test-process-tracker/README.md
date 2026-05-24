# 软件测试过程管理与缺陷跟踪系统

Spring Boot + 本地 MySQL 版本，保留 galaxy/玻璃态/曲线加载视觉前端，并实现登录、RBAC、测试计划、测试用例、测试任务、缺陷流转、统计报表。

## 环境

- JDK 21
- IntelliJ IDEA
- MySQL 8.x
- Maven，IDEA 可自动导入 `pom.xml`

## 初始化数据库

在 MySQL 中依次执行：

```sql
source src/main/resources/db/schema.sql;
source src/main/resources/db/data.sql;
```

或在 MySQL 客户端中打开这两个文件执行。

默认连接配置在 `src/main/resources/application.yml`，密码通过环境变量读取，避免提交真实密码：

```yaml
spring.datasource.url: ${DB_URL:jdbc:mysql://localhost:3306/test_process_tracker...}
spring.datasource.username: ${DB_USERNAME:root}
spring.datasource.password: ${DB_PASSWORD:}
```

在 IDEA 的运行配置 `Start Spring Boot` 中设置环境变量：

```text
DB_USERNAME=root;DB_PASSWORD=admin
```

也可以复制 `src/main/resources/application-local.example.yml` 为 `application-local.yml`，填写本地密码。`application-local.yml` 已被 `.gitignore` 忽略，不会上传。

## 默认账号

密码均为：`password`

| 用户名 | 角色 |
|---|---|
| admin | 管理员 |
| pm | 项目经理 |
| testlead | 测试负责人 |
| tester | 测试人员 |
| dev | 开发人员 |
| qa | 质量管理人员 |

## IDEA 启动

打开 `D:\软件项目管理\test-process-tracker`，等待 Maven 依赖导入完成。

如果你打开的是上一级目录 `D:\软件项目管理`，也可以运行根目录的 `pom.xml`，它已经把 `test-process-tracker` 作为 Maven 子模块。出现 `org.springframework...不存在` 时，说明 IDEA 还没有把项目作为 Maven 项目导入，请执行：

1. 右键根目录 `pom.xml` 或 `test-process-tracker/pom.xml`。
2. 选择 `Add as Maven Project`。
3. 打开右侧 `Maven` 面板，点击刷新按钮。
4. 等待依赖下载完成后再运行。

不要使用旧的普通 Java 模块 `软件项目管理` 直接 Make，否则会绕过 Maven 依赖。

启动方式：

- 运行配置选择 `Start Server`，点击绿色三角形。
- 或打开 `src/main/java/com/example/tracker/TrackerApplication.java`，点击 `main` 方法旁绿色三角形。

浏览器访问：

```text
http://localhost:8080
```

## 功能范围

- 登录认证
- RBAC 权限控制
- 用户管理
- 测试计划 CRUD
- 测试用例创建与执行
- 测试任务分配与状态更新
- 缺陷提交、筛选、状态流转、流转历史
- 仪表盘统计
- 测试报告数据生成与 Word / PDF 导出
- 用户编辑、启用 / 禁用、重置密码
- 请求访问日志、安全日志、业务操作日志和异常日志
- 前端系统日志查看与 `.log` 文件导出
- 缺陷、计划、用例、任务和用户列表分页显示
- 缺陷截图和附件上传、补充上传、查看、下载，文件统一保存到 `uploads/defects/`

## 详细文档

- `DEPLOYMENT.md`：部署文档
- `PROJECT_LOG.md`：项目日志、已完成清单、接口格式

## 日志

系统运行时会生成日志文件：

```text
logs/test-process-tracker.log
```

日志内容包括 HTTP 访问、登录成功/失败、权限拒绝、关键业务操作和系统异常。

管理员登录后可在“系统日志”页面查看最近日志，并下载完整 `.log` 文件。

## 企划书对应

- 质量管理：测试用例执行、缺陷验证、报告统计
- 进度管理：测试计划、任务截止日期、任务状态
- 沟通管理：缺陷负责人、流转备注、操作记录
- 项目监控：通过率、缺陷状态分布、任务进度、近期缺陷
