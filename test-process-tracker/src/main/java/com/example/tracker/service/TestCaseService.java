package com.example.tracker.service;

import com.example.tracker.common.BusinessException;
import com.example.tracker.domain.TestCaseRequest;
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

@Service
public class TestCaseService {
    private static final Logger log = LoggerFactory.getLogger(TestCaseService.class);
    private final JdbcTemplate jdbc;

    public TestCaseService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> list(Long planId, String result) {
        String sql = """
                select c.id,c.plan_id planId,p.name planName,c.module,c.title,c.precondition,c.steps,c.expected,c.actual,c.result,
                       u.real_name executorName,c.executed_at executedAt
                from test_case c join test_plan p on c.plan_id=p.id left join sys_user u on c.executor_id=u.id
                where (? is null or c.plan_id=?) and (? is null or c.result=?) order by c.id desc
                """;
        return jdbc.queryForList(sql, planId, planId, emptyToNull(result), emptyToNull(result));
    }

    public Map<String, Object> create(TestCaseRequest request) {
        if (request.planId() == null || request.title() == null || request.title().isBlank()) {
            throw new BusinessException("所属计划和用例标题不能为空");
        }
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("insert into test_case(plan_id,module,title,precondition,steps,expected,actual,result,executor_id) values(?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, request.planId());
            ps.setString(2, request.module());
            ps.setString(3, request.title());
            ps.setString(4, request.precondition());
            ps.setString(5, request.steps());
            ps.setString(6, request.expected());
            ps.setString(7, request.actual());
            ps.setString(8, value(request.result(), "未执行"));
            if (request.executorId() == null) ps.setNull(9, Types.BIGINT); else ps.setLong(9, request.executorId());
            return ps;
        }, keyHolder);
        Long id = keyHolder.getKey().longValue();
        log.info("test_case_created id={} planId={} title={} result={}", id, request.planId(), request.title(), value(request.result(), "未执行"));
        return jdbc.queryForMap("select * from test_case where id=?", id);
    }

    public void execute(Long id, TestCaseRequest request) {
        jdbc.update("update test_case set actual=?, result=?, executor_id=?, executed_at=now() where id=?",
                request.actual(), value(request.result(), "未执行"), request.executorId(), id);
        log.info("test_case_executed id={} result={} executorId={}", id, value(request.result(), "未执行"), request.executorId());
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static String value(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
