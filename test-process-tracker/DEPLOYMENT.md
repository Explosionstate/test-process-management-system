# 软件测试过程管理与缺陷跟踪系统部署文档

## 1. 部署目标

本文档用于指导其他成员或验收人员在本地部署“软件测试过程管理与缺陷跟踪系统”。系统采用 Spring Boot + MySQL 架构，前端静态资源由 Spring Boot 统一提供，部署后通过浏览器访问。

## 2. 环境要求

| 环境项 | 要求 |
|---|---|
| 操作系统 | Windows 10 / Windows 11 / Linux 均可 |
| JDK | JDK 21 |
| 数据库 | MySQL 8.x |
| 构建工具 | Maven，或 IntelliJ IDEA 内置 Maven |
| IDE | IntelliJ IDEA 推荐 |
| 浏览器 | Chrome / Edge |
| 默认端口 | 8080 |

## 3. 获取代码

```powershell
git clone https://github.com/Explosionstate/test-process-management-system.git
cd test-process-management-system\test-process-tracker
```

如果使用 IDEA，建议直接打开：

```text
test-process-management-system/test-process-tracker
```

## 4. 初始化数据库

登录 MySQL 后执行项目中的初始化脚本。

```sql
source src/main/resources/db/schema.sql;
source src/main/resources/db/data.sql;
```

如果已经部署过旧版本，并且只需要应用用户维护与报告导出优化，可额外执行：

```sql
source src/main/resources/db/upgrade-2026-05-user-report.sql;
```

说明：

| SQL 文件 | 作用 |
|---|---|
| `schema.sql` | 创建数据库和数据表 |
| `data.sql` | 写入默认角色、权限、账号和示例数据 |
| `upgrade-2026-05-user-report.sql` | 将默认账号密码更新为 BCrypt，并说明用户维护和报告导出无需新增表 |

## 5. 数据库连接配置

项目通过环境变量读取数据库账号密码，避免将真实密码提交到仓库。

### IDEA 配置方式

打开运行配置 `Start Spring Boot`，在 `Environment variables` 中填写：

```text
DB_USERNAME=root;DB_PASSWORD=你的MySQL密码
```

例如本机：

```text
DB_USERNAME=root;DB_PASSWORD=admin
```

如数据库不是本机默认地址，可同时配置：

```text
DB_URL=jdbc:mysql://localhost:3306/test_process_tracker?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
```

### 命令行配置方式

PowerShell：

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的MySQL密码"
```

## 6. 启动项目

### 方式一：IDEA 启动

1. 用 IDEA 打开 `test-process-tracker`。
2. 等待 Maven 依赖加载完成。
3. 选择运行配置 `Start Spring Boot`。
4. 点击绿色三角形启动。

也可以打开启动类运行：

```text
src/main/java/com/example/tracker/TrackerApplication.java
```

启动类：

```text
com.example.tracker.TrackerApplication
```

### 方式二：命令行启动

如果系统已安装 Maven：

```powershell
cd test-process-tracker
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的MySQL密码"
mvn spring-boot:run
```

如果没有安装 Maven，但安装了 IntelliJ IDEA，可使用 IDEA 内置 Maven，例如：

```powershell
cd D:\软件项目管理\test-process-tracker
$env:DB_USERNAME="root"
$env:DB_PASSWORD="admin"
& "D:\java\IntelliJ IDEA 2025.1.3\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run
```

## 7. 访问系统

浏览器访问：

```text
http://localhost:8080
```

默认账号：

| 用户名 | 密码 | 角色 |
|---|---|---|
| admin | password | 管理员 |
| pm | password | 项目经理 |
| testlead | password | 测试负责人 |
| tester | password | 测试人员 |
| dev | password | 开发人员 |
| qa | password | 质量管理人员 |

## 8. 功能验证清单

部署完成后，建议按以下顺序验证系统功能。

| 验证项 | 操作 | 预期结果 |
|---|---|---|
| 登录 | 使用 `admin / password` 登录 | 成功进入系统首页 |
| 仪表盘 | 查看首页统计卡片 | 显示计划数、用例数、通过率、缺陷数 |
| 测试计划 | 新增测试计划 | 列表中出现新计划 |
| 测试用例 | 新增测试用例并执行 | 用例状态更新为通过或失败 |
| 测试任务 | 分配任务并更新状态 | 任务状态可变为进行中或已完成 |
| 缺陷提交 | 提交缺陷 | 缺陷列表出现新缺陷，状态为新建 |
| 缺陷流转 | 按按钮流转缺陷 | 缺陷状态按规则变化 |
| 用户管理 | 新增、编辑、禁用、重置密码 | 用户数据更新成功 |
| 测试报告 | 生成报告并导出 | 可下载 Word 或 PDF 报告 |

## 9. 常见问题

### 9.1 Spring 相关包不存在

现象：

```text
程序包 org.springframework... 不存在
```

原因：IDEA 没有把项目识别为 Maven 项目。

解决：右键 `pom.xml`，选择 `Add as Maven Project`，然后刷新 Maven 面板。

### 9.2 登录返回 400

原因可能包括：

| 原因 | 解决方式 |
|---|---|
| 数据库未初始化 | 执行 `schema.sql` 和 `data.sql` |
| 默认密码未升级为 BCrypt | 执行 `upgrade-2026-05-user-report.sql` |
| 输入账号密码错误 | 使用 `admin / password` |

### 9.3 数据库连接失败

检查：

| 检查项 | 说明 |
|---|---|
| MySQL 是否启动 | 确认本地 MySQL 服务运行中 |
| 数据库账号密码 | 确认 `DB_USERNAME`、`DB_PASSWORD` 正确 |
| 数据库地址 | 确认 `DB_URL` 指向正确数据库 |

### 9.4 端口 8080 被占用

解决方式：

```powershell
netstat -ano | findstr :8080
```

找到 PID 后结束进程，或修改启动端口：

```powershell
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8090
```

## 10. 交付文件说明

| 文件 | 说明 |
|---|---|
| `README.md` | 项目简介和快速启动说明 |
| `DEPLOYMENT.md` | 部署文档 |
| `PROJECT_LOG.md` | 项目日志、已完成清单和接口格式 |
| `schema.sql` | 数据库建表脚本 |
| `data.sql` | 初始化数据脚本 |
| `upgrade-2026-05-user-report.sql` | 增量升级 SQL |
| `pom.xml` | Maven 依赖配置 |

## 11. 部署结论

完成上述步骤后，系统即可在本地运行。系统支持登录认证、RBAC 权限控制、测试计划、测试用例、测试任务、缺陷流转、统计分析、测试报告 Word/PDF 导出和用户管理，满足课程项目演示与验收要求。
