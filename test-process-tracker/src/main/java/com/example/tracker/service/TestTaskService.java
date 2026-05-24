package com.example.tracker.service;

import com.example.tracker.common.BusinessException;
import com.example.tracker.domain.TestTaskRequest;
import com.example.tracker.repository.Sql;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class TestTaskService {
    private static final Logger log = LoggerFactory.getLogger(TestTaskService.class);
    private final JdbcTemplate jdbc;

    // 合法状态枚举
    private static final Set<String> VALID_STATUSES = new LinkedHashSet<>();
    static {
        VALID_STATUSES.add("待处理");
        VALID_STATUSES.add("进行中");
        VALID_STATUSES.add("已完成");
        VALID_STATUSES.add("已取消");
    }

    public TestTaskService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> list(Long assigneeId, String status) {
        return jdbc.queryForList("""
                select t.id,t.plan_id planId,p.name planName,t.case_id caseId,t.title,t.status,t.due_date dueDate,u.real_name assigneeName
                from test_task t join test_plan p on t.plan_id=p.id join sys_user u on t.assignee_id=u.id
                where (? is null or t.assignee_id=?) and (? is null or t.status=?) order by t.id desc
                """, assigneeId, assigneeId, emptyToNull(status), emptyToNull(status));
    }

    public Map<String, Object> create(TestTaskRequest request) {
        // 校验计划存在
        if (request.planId() == null) {
            throw new BusinessException("计划不能为空");
        }
        Integer planCount = jdbc.queryForObject("SELECT COUNT(*) FROM test_plan WHERE id = ?", Integer.class, request.planId());
        if (planCount == null || planCount == 0) {
            throw new BusinessException("计划不存在");
        }

        // 校验用户存在且启用
        if (request.assigneeId() == null) {
            throw new BusinessException("负责人不能为空");
        }
        Integer userCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE id = ? AND enabled = 1",
                Integer.class, request.assigneeId());
        if (userCount == null || userCount == 0) {
            throw new BusinessException("用户不存在或已禁用");
        }

        // 校验任务标题
        if (request.title() == null || request.title().isBlank()) {
            throw new BusinessException("任务标题不能为空");
        }

        // 校验状态枚举
        String status = value(request.status(), "待处理");
        if (!VALID_STATUSES.contains(status)) {
            throw new BusinessException("状态值非法，可选：" + VALID_STATUSES);
        }

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("insert into test_task(plan_id,case_id,title,assignee_id,status,due_date) values(?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, request.planId());
            if (request.caseId() == null) ps.setNull(2, Types.BIGINT); else ps.setLong(2, request.caseId());
            ps.setString(3, request.title());
            ps.setLong(4, request.assigneeId());
            ps.setString(5, status);
            ps.setDate(6, Sql.date(request.dueDate()));
            return ps;
        }, keyHolder);
        Long id = keyHolder.getKey().longValue();
        log.info("test_task_created id={} planId={} assigneeId={} status={}", id, request.planId(), request.assigneeId(), status);
        return jdbc.queryForMap("select * from test_task where id=?", id);
    }

    public void updateStatus(Long id, TestTaskRequest request) {
        // 校验状态枚举
        String status = value(request.status(), "待处理");
        if (!VALID_STATUSES.contains(status)) {
            throw new BusinessException("状态值非法，可选：" + VALID_STATUSES);
        }
        jdbc.update("update test_task set status=? where id=?", status, id);
        log.info("test_task_status_updated id={} status={}", id, status);
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static String value(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
