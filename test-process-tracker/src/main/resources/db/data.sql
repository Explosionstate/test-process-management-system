USE test_process_tracker;

INSERT INTO sys_user(username, password, real_name) VALUES
('admin', 'password', '系统管理员'),
('pm', 'password', '项目经理'),
('testlead', 'password', '测试负责人'),
('tester', 'password', '测试人员'),
('dev', 'password', '开发人员'),
('qa', 'password', '质量管理人员');

INSERT INTO sys_role(code, name) VALUES
('ADMIN', '管理员'), ('PROJECT_MANAGER', '项目经理'), ('TEST_LEAD', '测试负责人'),
('TESTER', '测试人员'), ('DEVELOPER', '开发人员'), ('QA', '质量管理人员');

INSERT INTO sys_permission(code, name) VALUES
('user:manage', '用户管理'), ('plan:view', '查看测试计划'), ('plan:create', '创建测试计划'), ('plan:update', '修改测试计划'),
('case:view', '查看测试用例'), ('case:create', '创建测试用例'), ('case:execute', '执行测试用例'),
('task:view', '查看测试任务'), ('task:assign', '分配测试任务'), ('task:update', '更新测试任务'),
('defect:view', '查看缺陷'), ('defect:create', '提交缺陷'), ('defect:assign', '分配缺陷'), ('defect:fix', '修复缺陷'), ('defect:verify', '验证缺陷'),
('report:view', '查看报表'), ('report:create', '生成测试报告');

INSERT INTO sys_user_role(user_id, role_id)
SELECT u.id, r.id FROM sys_user u JOIN sys_role r ON
(u.username='admin' AND r.code='ADMIN') OR (u.username='pm' AND r.code='PROJECT_MANAGER') OR
(u.username='testlead' AND r.code='TEST_LEAD') OR (u.username='tester' AND r.code='TESTER') OR
(u.username='dev' AND r.code='DEVELOPER') OR (u.username='qa' AND r.code='QA');

INSERT INTO sys_role_permission(role_id, permission_id)
SELECT r.id, p.id FROM sys_role r CROSS JOIN sys_permission p WHERE r.code='ADMIN';

INSERT INTO sys_role_permission(role_id, permission_id)
SELECT r.id, p.id FROM sys_role r JOIN sys_permission p ON p.code IN ('plan:view','plan:create','plan:update','task:view','task:assign','defect:view','defect:assign','report:view','report:create') WHERE r.code='PROJECT_MANAGER';

INSERT INTO sys_role_permission(role_id, permission_id)
SELECT r.id, p.id FROM sys_role r JOIN sys_permission p ON p.code IN ('plan:view','plan:create','plan:update','case:view','case:create','case:execute','task:view','task:assign','task:update','defect:view','defect:create','defect:assign','defect:verify','report:view','report:create') WHERE r.code='TEST_LEAD';

INSERT INTO sys_role_permission(role_id, permission_id)
SELECT r.id, p.id FROM sys_role r JOIN sys_permission p ON p.code IN ('plan:view','case:view','case:create','case:execute','task:view','task:update','defect:view','defect:create','defect:verify','report:view') WHERE r.code='TESTER';

INSERT INTO sys_role_permission(role_id, permission_id)
SELECT r.id, p.id FROM sys_role r JOIN sys_permission p ON p.code IN ('task:view','task:update','defect:view','defect:fix','report:view') WHERE r.code='DEVELOPER';

INSERT INTO sys_role_permission(role_id, permission_id)
SELECT r.id, p.id FROM sys_role r JOIN sys_permission p ON p.code IN ('plan:view','case:view','task:view','defect:view','report:view','report:create') WHERE r.code='QA';

INSERT INTO test_plan(name, objective, scope_text, owner_id, status, start_date, end_date, created_by)
VALUES ('登录与缺陷闭环测试计划', '验证登录、测试用例执行、缺陷提交和报告统计闭环', '登录模块、缺陷模块、报表模块', 3, '进行中', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 14 DAY), 2);

INSERT INTO test_case(plan_id, module, title, precondition, steps, expected, actual, result, executor_id)
VALUES
(1, '登录模块', '正确账号密码登录', '用户账号存在', '输入正确账号和密码后点击登录', '进入系统首页', '进入系统首页', '通过', 4),
(1, '登录模块', '错误密码登录提示', '用户账号存在', '输入正确账号和错误密码后点击登录', '提示账号或密码错误', '页面无提示', '失败', 4);

INSERT INTO test_task(plan_id, case_id, title, assignee_id, status, due_date)
VALUES (1, 2, '验证错误密码提示缺陷', 4, '进行中', DATE_ADD(CURDATE(), INTERVAL 3 DAY));

INSERT INTO defect(title, module, severity, priority, steps, expected, actual, status, reporter_id, owner_id)
VALUES ('错误密码登录时缺少错误提示', '登录模块', '中', '高', '输入正确账号和错误密码后点击登录', '提示账号或密码错误', '页面无提示', 'NEW', 4, NULL);

INSERT INTO defect_history(defect_id, from_status, to_status, operator_id, note)
VALUES (1, NULL, 'NEW', 4, '缺陷创建');
