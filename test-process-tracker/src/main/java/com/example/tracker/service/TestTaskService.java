package com.example.tracker.service;

import com.example.tracker.common.BusinessException;
import com.example.tracker.domain.TestTaskRequest;
import com.example.tracker.repository.Sql;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Map;

@Service
public class TestTaskService {
    private final JdbcTemplate jdbc;

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
        if (request.planId() == null || request.assigneeId() == null || request.title() == null || request.title().isBlank()) {
            throw new BusinessException("计划、负责人和任务标题不能为空");
        }
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("insert into test_task(plan_id,case_id,title,assignee_id,status,due_date) values(?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, request.planId());
            if (request.caseId() == null) ps.setNull(2, Types.BIGINT); else ps.setLong(2, request.caseId());
            ps.setString(3, request.title());
            ps.setLong(4, request.assigneeId());
            ps.setString(5, value(request.status(), "待处理"));
            ps.setDate(6, Sql.date(request.dueDate()));
            return ps;
        }, keyHolder);
        return jdbc.queryForMap("select * from test_task where id=?", keyHolder.getKey().longValue());
    }

    public void updateStatus(Long id, TestTaskRequest request) {
        jdbc.update("update test_task set status=? where id=?", value(request.status(), "待处理"), id);
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static String value(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
