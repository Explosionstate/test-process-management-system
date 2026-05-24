package com.example.tracker.service;

import com.example.tracker.auth.LoginUser;
import com.example.tracker.common.BusinessException;
import com.example.tracker.domain.TestPlanRequest;
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

@Service
public class TestPlanService {
    private static final Logger log = LoggerFactory.getLogger(TestPlanService.class);
    private final JdbcTemplate jdbc;

    public TestPlanService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> list() {
        return jdbc.queryForList("""
                select p.id,p.name,p.objective,p.scope_text scopeText,p.status,p.start_date startDate,p.end_date endDate,
                       u.real_name ownerName,p.created_at createdAt
                from test_plan p left join sys_user u on p.owner_id=u.id order by p.id desc
                """);
    }

    public Map<String, Object> create(TestPlanRequest request, LoginUser user) {
        if (request.name() == null || request.name().isBlank()) {
            throw new BusinessException("测试计划名称不能为空");
        }
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("insert into test_plan(name,objective,scope_text,owner_id,status,start_date,end_date,created_by) values(?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, request.name());
            ps.setString(2, request.objective());
            ps.setString(3, request.scopeText());
            if (request.ownerId() == null) ps.setNull(4, Types.BIGINT); else ps.setLong(4, request.ownerId());
            ps.setString(5, value(request.status(), "未开始"));
            ps.setDate(6, Sql.date(request.startDate()));
            ps.setDate(7, Sql.date(request.endDate()));
            ps.setLong(8, user.id());
            return ps;
        }, keyHolder);
        Long id = keyHolder.getKey().longValue();
        log.info("test_plan_created id={} name={} creator={}", id, request.name(), user.username());
        return jdbc.queryForMap("select * from test_plan where id=?", id);
    }

    public void update(Long id, TestPlanRequest request) {
        jdbc.update("update test_plan set name=?, objective=?, scope_text=?, owner_id=?, status=?, start_date=?, end_date=? where id=?",
                request.name(), request.objective(), request.scopeText(), request.ownerId(), value(request.status(), "未开始"),
                Sql.date(request.startDate()), Sql.date(request.endDate()), id);
        log.info("test_plan_updated id={} name={} status={}", id, request.name(), value(request.status(), "未开始"));
    }

    public void delete(Long id) {
        jdbc.update("delete from test_plan where id=?", id);
        log.info("test_plan_deleted id={}", id);
    }

    private static String value(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
