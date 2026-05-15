USE test_process_tracker;

-- 1. 将默认演示账号密码统一更新为 BCrypt 加密值，明文均为 password。
UPDATE sys_user
SET password = '$2a$10$sXTL7gkCOj1s0WRbnvnvwewHC5TzZfA24uIc2/3PJRvql3rFpQULO'
WHERE username IN ('admin', 'pm', 'testlead', 'tester', 'dev', 'qa')
  AND (password = 'password' OR password NOT LIKE '$2%');

-- 2. 当前用户编辑、禁用、重置密码功能复用 sys_user.enabled、sys_user.password、sys_user_role 表，
--    不需要新增字段或新增数据表。

-- 3. 测试报告导出功能基于现有 test_plan、test_case、defect 表聚合生成，
--    不需要新增字段或新增数据表。
