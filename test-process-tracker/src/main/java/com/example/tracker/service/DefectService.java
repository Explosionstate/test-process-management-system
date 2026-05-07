package com.example.tracker.service;

import com.example.tracker.auth.LoginUser;
import com.example.tracker.common.BusinessException;
import com.example.tracker.domain.DefectRequest;
import com.example.tracker.domain.TransitionRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Map;

@Service
public class DefectService {
    private final JdbcTemplate jdbc;

    public DefectService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> list(String status, String module, Long ownerId) {
        return jdbc.queryForList("""
                select d.id,d.title,d.module,d.severity,d.priority,d.steps,d.expected,d.actual,d.status,
                       reporter.real_name reporterName,owner.real_name ownerName,d.created_at createdAt,d.updated_at updatedAt
                from defect d join sys_user reporter on d.reporter_id=reporter.id left join sys_user owner on d.owner_id=owner.id
                where (? is null or d.status=?) and (? is null or d.module like concat('%',?,'%')) and (? is null or d.owner_id=?)
                order by d.id desc
                """, emptyToNull(status), emptyToNull(status), emptyToNull(module), emptyToNull(module), ownerId, ownerId);
    }

    @Transactional
    public Map<String, Object> create(DefectRequest request, LoginUser user) {
        if (request.title() == null || request.title().isBlank() || request.module() == null || request.module().isBlank()) {
            throw new BusinessException("缺陷标题和所属模块不能为空");
        }
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("insert into defect(title,module,severity,priority,steps,expected,actual,status,reporter_id,owner_id) values(?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, request.title());
            ps.setString(2, request.module());
            ps.setString(3, value(request.severity(), "中"));
            ps.setString(4, value(request.priority(), "中"));
            ps.setString(5, request.steps());
            ps.setString(6, request.expected());
            ps.setString(7, request.actual());
            ps.setString(8, "NEW");
            ps.setLong(9, user.id());
            if (request.ownerId() == null) ps.setNull(10, Types.BIGINT); else ps.setLong(10, request.ownerId());
            return ps;
        }, keyHolder);
        Long id = keyHolder.getKey().longValue();
        jdbc.update("insert into defect_history(defect_id,from_status,to_status,operator_id,note) values(?,?,?,?,?)", id, null, "NEW", user.id(), "缺陷创建");
        return jdbc.queryForMap("select * from defect where id=?", id);
    }

    @Transactional
    public void transition(Long id, TransitionRequest request, LoginUser user) {
        Map<String, Object> defect = jdbc.queryForList("select * from defect where id=?", id).stream()
                .findFirst().orElseThrow(() -> new BusinessException("缺陷不存在"));
        String current = String.valueOf(defect.get("status"));
        String next = request.status();
        if (!canTransition(current, next)) {
            throw new BusinessException("非法状态流转：" + current + " -> " + next);
        }
        requireTransitionPermission(user, next);
        jdbc.update("update defect set status=?, owner_id=coalesce(?, owner_id), updated_at=now() where id=?", next, request.ownerId(), id);
        jdbc.update("insert into defect_history(defect_id,from_status,to_status,operator_id,note) values(?,?,?,?,?)",
                id, current, next, user.id(), request.note());
    }

    public List<Map<String, Object>> history(Long id) {
        return jdbc.queryForList("""
                select h.id,h.defect_id defectId,h.from_status fromStatus,h.to_status toStatus,u.real_name operatorName,h.note,h.operated_at operatedAt
                from defect_history h join sys_user u on h.operator_id=u.id where h.defect_id=? order by h.id
                """, id);
    }

    private static boolean canTransition(String current, String next) {
        return switch (current) {
            case "NEW" -> "ASSIGNED".equals(next);
            case "ASSIGNED" -> "FIXING".equals(next);
            case "FIXING" -> "PENDING_VERIFY".equals(next);
            case "PENDING_VERIFY" -> "CLOSED".equals(next) || "REOPENED".equals(next);
            case "REOPENED" -> "ASSIGNED".equals(next) || "FIXING".equals(next);
            default -> false;
        };
    }

    private static void requireTransitionPermission(LoginUser user, String next) {
        String permission = switch (next) {
            case "ASSIGNED" -> "defect:assign";
            case "FIXING", "PENDING_VERIFY" -> "defect:fix";
            case "CLOSED", "REOPENED" -> "defect:verify";
            default -> "defect:view";
        };
        if (!user.has(permission)) {
            throw new SecurityException("缺少权限：" + permission);
        }
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static String value(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
