# 软件测试过程管理与缺陷跟踪系统项目日志与已完成清单

## 一、项目基本信息

| 项目项 | 内容 |
|---|---|
| 项目名称 | 软件测试过程管理与缺陷跟踪系统 |
| 项目类型 | Java Web 管理系统 |
| 后端技术 | Java 21、Spring Boot 3.x、Spring Security、Spring JDBC |
| 前端技术 | HTML、CSS、JavaScript |
| 数据库 | MySQL 8.x |
| 权限模型 | RBAC 角色权限控制 |
| 构建工具 | Maven |
| 默认端口 | 8080 |
| 默认访问地址 | `http://localhost:8080` |
| 默认账号 | `admin / password` |

## 二、项目已完成清单

### 1. 项目结构搭建

| 完成项 | 说明 | 状态 |
|---|---|---|
| Spring Boot 项目结构 | 已建立标准 `src/main/java`、`src/main/resources` 目录 | 已完成 |
| Maven 配置 | 已添加 `pom.xml`，引入 Spring Boot、Spring JDBC、Spring Security、MySQL 驱动 | 已完成 |
| 启动类 | 已添加 `com.example.tracker.TrackerApplication` | 已完成 |
| 静态资源目录 | 已使用 `src/main/resources/static` 存放前端页面 | 已完成 |
| Git 忽略配置 | 已添加 `.gitignore`，排除 `target/`、`out/`、本地配置和缓存文件 | 已完成 |

### 2. 数据库设计与初始化

| 完成项 | 说明 | 状态 |
|---|---|---|
| 数据库建表脚本 | `src/main/resources/db/schema.sql` | 已完成 |
| 初始化数据脚本 | `src/main/resources/db/data.sql` | 已完成 |
| 用户表 | `sys_user` | 已完成 |
| 角色表 | `sys_role` | 已完成 |
| 权限表 | `sys_permission` | 已完成 |
| 用户角色关联表 | `sys_user_role` | 已完成 |
| 角色权限关联表 | `sys_role_permission` | 已完成 |
| 测试计划表 | `test_plan` | 已完成 |
| 测试用例表 | `test_case` | 已完成 |
| 测试任务表 | `test_task` | 已完成 |
| 缺陷表 | `defect` | 已完成 |
| 缺陷流转记录表 | `defect_history` | 已完成 |

### 3. 登录与 RBAC 权限

| 完成项 | 说明 | 状态 |
|---|---|---|
| 用户登录 | 支持用户名和密码登录 | 已完成 |
| 密码加密统一化 | 默认账号和新增/重置用户密码统一使用 BCrypt 加密存储 | 已完成 |
| 用户退出 | 支持清除 Session | 已完成 |
| 当前用户信息 | 支持查询当前登录用户、角色和权限 | 已完成 |
| RBAC 角色权限 | 支持管理员、项目经理、测试负责人、测试人员、开发人员、质量管理人员 | 已完成 |
| 接口权限校验 | 后端接口根据权限点进行校验 | 已完成 |
| 前端权限控制 | 前端按钮根据权限点显示或隐藏 | 已完成 |

### 4. 业务模块

| 模块 | 已实现功能 | 状态 |
|---|---|---|
| 测试计划模块 | 查询、新增、修改、删除测试计划 | 已完成 |
| 测试用例模块 | 查询、新增、执行测试用例 | 已完成 |
| 测试任务模块 | 查询、分配任务、更新任务状态 | 已完成 |
| 缺陷管理模块 | 查询、筛选、提交缺陷 | 已完成 |
| 缺陷流转模块 | 支持新建、已分配、修复中、待验证、已关闭、重新打开 | 已完成 |
| 缺陷历史模块 | 查询缺陷状态流转历史 | 已完成 |
| 仪表盘模块 | 统计计划数、用例数、通过率、缺陷数、缺陷分布、任务进度 | 已完成 |
| 测试报告模块 | 根据测试计划生成报告数据，并支持 Word / PDF 导出 | 已完成 |
| 用户管理模块 | 查询用户、新增用户、编辑用户、启用/禁用用户、重置密码、查询角色 | 已完成 |

### 5. 前端界面

| 页面 / 区域 | 说明 | 状态 |
|---|---|---|
| 登录页 | 支持用户登录 | 已完成 |
| 首页仪表盘 | 展示计划数量、用例数量、通过率、缺陷总数 | 已完成 |
| 缺陷状态轨道 | 展示缺陷生命周期 | 已完成 |
| 测试计划页 | 展示计划列表，支持新增计划 | 已完成 |
| 测试用例页 | 展示用例列表，支持新增和执行 | 已完成 |
| 测试任务页 | 展示任务列表，支持分配和状态更新 | 已完成 |
| 缺陷跟踪页 | 展示缺陷卡片，支持筛选和状态流转 | 已完成 |
| 测试报告页 | 支持生成测试报告数据，并导出 Word / PDF 文件 | 已完成 |
| 用户权限页 | 支持查看用户、新增用户、编辑用户、禁用用户和重置密码 | 已完成 |
| UI 风格 | 参考 galaxy、玻璃态卡片、曲线加载器、动态背景 | 已完成 |

## 三、项目阶段日志

| 阶段 | 工作内容 | 输出成果 |
|---|---|---|
| 第 1 阶段 | 明确项目方向为“软件测试过程管理与缺陷跟踪系统” | 项目方向、范围和核心功能确定 |
| 第 2 阶段 | 整理项目章程、范围说明书、需求规格说明书和项目管理计划 | 多份 Word 项目文档 |
| 第 3 阶段 | 初步实现 Java 原型服务和静态前端页面 | 可运行原型、接口雏形 |
| 第 4 阶段 | 优化前端视觉风格，参考动态曲线加载器、设计卡片和 galaxy 风格 | 深色星云风格前端页面 |
| 第 5 阶段 | 将原型重构为 Spring Boot + MySQL 项目 | 标准 Java Web 项目结构 |
| 第 6 阶段 | 增加登录认证、Session、RBAC 权限控制 | 登录接口、权限模型、默认角色权限 |
| 第 7 阶段 | 实现测试计划、测试用例、测试任务、缺陷和报告接口 | 完整业务接口 |
| 第 8 阶段 | 完善数据库脚本、README、GitHub 上传和部署说明 | 可交付代码仓库和部署说明 |
| 第 9 阶段 | 完成密码 BCrypt 统一化、用户编辑/禁用/重置密码、测试报告 Word/PDF 导出 | 安全性与交付能力增强 |

## 四、运行与部署说明

### 1. 环境要求

| 环境 | 要求 |
|---|---|
| JDK | JDK 21 |
| 数据库 | MySQL 8.x |
| IDE | IntelliJ IDEA 或 VS Code |
| 构建工具 | Maven，推荐使用 IDEA 内置 Maven |
| 浏览器 | Chrome / Edge |

### 2. 数据库初始化

进入 MySQL 后执行：

```sql
source src/main/resources/db/schema.sql;
source src/main/resources/db/data.sql;
```

或者直接复制两个 SQL 文件内容到 MySQL 客户端中执行。

### 3. 数据库连接配置

项目通过环境变量读取数据库账号和密码，避免提交真实密码。

```text
DB_USERNAME=root
DB_PASSWORD=你的MySQL密码
```

IDEA 运行配置中可填写：

```text
DB_USERNAME=root;DB_PASSWORD=admin
```

### 4. 启动方式

启动类：

```text
com.example.tracker.TrackerApplication
```

启动后访问：

```text
http://localhost:8080
```

默认登录账号：

```text
admin / password
```

## 五、默认角色与账号

| 用户名 | 密码 | 角色 | 说明 |
|---|---|---|---|
| admin | password | 管理员 | 拥有全部权限 |
| pm | password | 项目经理 | 管理计划、任务、缺陷分配和报表 |
| testlead | password | 测试负责人 | 管理测试计划、用例、任务和缺陷验证 |
| tester | password | 测试人员 | 执行用例、提交缺陷、验证缺陷 |
| dev | password | 开发人员 | 修复缺陷、提交待验证 |
| qa | password | 质量管理人员 | 查看质量统计和报告 |

## 六、权限点说明

| 权限编码 | 权限说明 |
|---|---|
| `user:manage` | 用户管理 |
| `plan:view` | 查看测试计划 |
| `plan:create` | 创建测试计划 |
| `plan:update` | 修改测试计划 |
| `case:view` | 查看测试用例 |
| `case:create` | 创建测试用例 |
| `case:execute` | 执行测试用例 |
| `task:view` | 查看测试任务 |
| `task:assign` | 分配测试任务 |
| `task:update` | 更新测试任务 |
| `defect:view` | 查看缺陷 |
| `defect:create` | 提交缺陷 |
| `defect:assign` | 分配缺陷 |
| `defect:fix` | 修复缺陷 |
| `defect:verify` | 验证缺陷 |
| `report:view` | 查看报表 |
| `report:create` | 生成测试报告 |

## 七、接口格式说明

### 1. 通用响应格式

系统后端接口统一返回 JSON 格式。

成功响应：

```json
{
  "success": true,
  "data": {},
  "error": null
}
```

失败响应：

```json
{
  "success": false,
  "data": null,
  "error": "错误原因"
}
```

### 2. 登录认证接口

#### 用户登录

```http
POST /api/auth/login
Content-Type: application/json
```

请求示例：

```json
{
  "username": "admin",
  "password": "password"
}
```

响应示例：

```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    "roles": ["ADMIN"],
    "permissions": ["user:manage", "plan:view", "defect:view"]
  },
  "error": null
}
```

#### 查询当前用户

```http
GET /api/auth/me
```

#### 退出登录

```http
POST /api/auth/logout
```

### 3. 用户与角色接口

#### 查询用户列表

```http
GET /api/users
```

需要权限：`user:manage`

#### 新增用户

```http
POST /api/users
Content-Type: application/json
```

请求示例：

```json
{
  "username": "newtester",
  "password": "password",
  "realName": "新测试人员",
  "roleId": 4
}
```

#### 修改用户

```http
PUT /api/users/{id}
Content-Type: application/json
```

请求示例：

```json
{
  "realName": "测试负责人A",
  "roleId": 3,
  "enabled": true
}
```

#### 启用或禁用用户

```http
PATCH /api/users/{id}/enabled
Content-Type: application/json
```

请求示例：

```json
{
  "enabled": false
}
```

#### 重置用户密码

```http
PATCH /api/users/{id}/password
Content-Type: application/json
```

请求示例：

```json
{
  "password": "password"
}
```

说明：新密码会通过 BCrypt 加密后写入 `sys_user.password` 字段。

#### 查询角色列表

```http
GET /api/users/roles
```

### 4. 测试计划接口

#### 查询测试计划

```http
GET /api/plans
```

需要权限：`plan:view`

#### 新增测试计划

```http
POST /api/plans
Content-Type: application/json
```

请求示例：

```json
{
  "name": "回归测试计划",
  "objective": "验证缺陷修复后的核心流程",
  "scopeText": "缺陷模块、报表模块",
  "ownerId": 3,
  "status": "进行中",
  "startDate": "2026-05-01",
  "endDate": "2026-05-15"
}
```

#### 修改测试计划

```http
PUT /api/plans/{id}
```

#### 删除测试计划

```http
DELETE /api/plans/{id}
```

### 5. 测试用例接口

#### 查询测试用例

```http
GET /api/cases
```

可选参数：

| 参数 | 说明 |
|---|---|
| `planId` | 按测试计划筛选 |
| `result` | 按执行结果筛选 |

#### 新增测试用例

```http
POST /api/cases
Content-Type: application/json
```

请求示例：

```json
{
  "planId": 1,
  "module": "登录模块",
  "title": "错误密码登录提示",
  "precondition": "用户账号存在",
  "steps": "输入正确账号和错误密码后点击登录",
  "expected": "提示账号或密码错误",
  "actual": "页面无提示",
  "result": "失败",
  "executorId": 4
}
```

#### 执行测试用例

```http
PUT /api/cases/{id}/execute
Content-Type: application/json
```

请求示例：

```json
{
  "actual": "执行通过",
  "result": "通过",
  "executorId": 4
}
```

### 6. 测试任务接口

#### 查询测试任务

```http
GET /api/tasks
```

可选参数：

| 参数 | 说明 |
|---|---|
| `assigneeId` | 按负责人筛选 |
| `status` | 按任务状态筛选 |

#### 分配测试任务

```http
POST /api/tasks
Content-Type: application/json
```

请求示例：

```json
{
  "planId": 1,
  "caseId": 2,
  "title": "验证错误密码提示缺陷",
  "assigneeId": 4,
  "status": "待处理",
  "dueDate": "2026-05-20"
}
```

#### 更新任务状态

```http
PUT /api/tasks/{id}/status
Content-Type: application/json
```

请求示例：

```json
{
  "status": "已完成"
}
```

### 7. 缺陷管理接口

#### 查询缺陷

```http
GET /api/defects
```

可选参数：

| 参数 | 说明 |
|---|---|
| `status` | 按缺陷状态筛选 |
| `module` | 按所属模块筛选 |
| `ownerId` | 按负责人筛选 |

#### 提交缺陷

```http
POST /api/defects
Content-Type: application/json
```

请求示例：

```json
{
  "title": "错误密码登录时缺少错误提示",
  "module": "登录模块",
  "severity": "中",
  "priority": "高",
  "steps": "输入正确账号和错误密码后点击登录",
  "expected": "提示账号或密码错误",
  "actual": "页面无提示",
  "ownerId": 5
}
```

#### 缺陷状态流转

```http
POST /api/defects/{id}/transition
Content-Type: application/json
```

请求示例：

```json
{
  "status": "ASSIGNED",
  "ownerId": 5,
  "note": "分配给开发人员修复"
}
```

缺陷状态枚举：

| 状态编码 | 中文含义 |
|---|---|
| `NEW` | 新建 |
| `ASSIGNED` | 已分配 |
| `FIXING` | 修复中 |
| `PENDING_VERIFY` | 待验证 |
| `CLOSED` | 已关闭 |
| `REOPENED` | 重新打开 |

#### 查询缺陷流转历史

```http
GET /api/defects/{id}/history
```

### 8. 仪表盘与报告接口

#### 查询仪表盘统计

```http
GET /api/dashboard
```

返回内容包括：

| 字段 | 说明 |
|---|---|
| `planCount` | 测试计划数量 |
| `caseCount` | 测试用例数量 |
| `passedCaseCount` | 已通过测试用例数量 |
| `passRate` | 测试通过率 |
| `defectCount` | 缺陷总数 |
| `defectsByStatus` | 按状态统计缺陷 |
| `defectsByModule` | 按模块统计缺陷 |
| `taskProgress` | 任务进度统计 |
| `recentDefects` | 最近更新缺陷 |

#### 生成测试报告数据

```http
GET /api/report?planId=1
```

#### 导出 Word 测试报告

```http
GET /api/report/export?planId=1&format=word
```

返回文件：`test-report.doc`

#### 导出 PDF 测试报告

```http
GET /api/report/export?planId=1&format=pdf
```

返回文件：`test-report.pdf`

说明：Word 导出使用 Word 可直接打开的 HTML 文档格式，PDF 导出使用 OpenPDF 生成。

## 八、缺陷状态流转规则

| 当前状态 | 下一状态 | 操作权限 |
|---|---|---|
| `NEW` | `ASSIGNED` | `defect:assign` |
| `ASSIGNED` | `FIXING` | `defect:fix` |
| `FIXING` | `PENDING_VERIFY` | `defect:fix` |
| `PENDING_VERIFY` | `CLOSED` | `defect:verify` |
| `PENDING_VERIFY` | `REOPENED` | `defect:verify` |
| `REOPENED` | `ASSIGNED` | `defect:assign` |
| `REOPENED` | `FIXING` | `defect:fix` |

## 九、已完成优化项

| 优化项 | 完成说明 |
|---|---|
| 密码加密统一化 | 已取消明文密码兼容，登录统一使用 BCrypt 校验；默认用户、重置密码和新增用户均按 BCrypt 存储 |
| 用户编辑与禁用 | 已新增用户编辑、启用/禁用、重置密码接口，并补充前端操作入口 |
| 测试报告导出 | 已新增 `/api/report/export` 接口，支持 Word 和 PDF 两种导出格式 |

## 十、数据库增量 SQL

本次优化不需要新增表或新增字段，用户编辑与禁用复用已有 `sys_user.enabled`、`sys_user.password`、`sys_user_role` 表结构；测试报告导出基于已有 `test_plan`、`test_case`、`defect` 表聚合生成。

已提供增量 SQL 文件：

```text
src/main/resources/db/upgrade-2026-05-user-report.sql
```

执行方式：

```sql
source src/main/resources/db/upgrade-2026-05-user-report.sql;
```

核心 SQL：

```sql
UPDATE sys_user
SET password = '$2a$10$sXTL7gkCOj1s0WRbnvnvwewHC5TzZfA24uIc2/3PJRvql3rFpQULO'
WHERE username IN ('admin', 'pm', 'testlead', 'tester', 'dev', 'qa')
  AND (password = 'password' OR password NOT LIKE '$2%');
```

## 十一、当前仍可优化内容

| 优化项 | 说明 |
|---|---|
| 缺陷附件 | 当前未实现截图或附件上传 |
| 分页查询 | 当前列表未做分页，数据量大时可优化 |
| 自动化测试 | 后续可增加单元测试和接口测试 |
| Maven 命令行环境 | 当前主要依赖 IDEA 内置 Maven，后续可安装系统 Maven |
