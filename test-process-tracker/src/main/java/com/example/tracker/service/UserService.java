package com.example.tracker.service;

import com.example.tracker.common.BusinessException;
import com.example.tracker.domain.UserRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Service
public class UserService {
    private final JdbcTemplate jdbc;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(JdbcTemplate jdbc, BCryptPasswordEncoder passwordEncoder) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Map<String, Object>> users() {
        return jdbc.queryForList("""
                select u.id,u.username,u.real_name realName,u.enabled,coalesce(group_concat(r.name), '') roles,
                       min(r.id) roleId
                from sys_user u left join sys_user_role ur on u.id=ur.user_id left join sys_role r on ur.role_id=r.id
                group by u.id order by u.id
                """);
    }

    public List<Map<String, Object>> roles() {
        return jdbc.queryForList("select id,code,name from sys_role order by id");
    }

    @Transactional
    public Map<String, Object> create(UserRequest request) {
        if (request.username() == null || request.username().isBlank() || request.password() == null || request.password().isBlank()) {
            throw new BusinessException("用户名和密码不能为空");
        }
        var keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("insert into sys_user(username,password,real_name) values(?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, request.username());
            ps.setString(2, passwordEncoder.encode(request.password()));
            ps.setString(3, request.realName() == null || request.realName().isBlank() ? request.username() : request.realName());
            return ps;
        }, keyHolder);
        Long userId = keyHolder.getKey().longValue();
        if (request.roleId() != null) {
            jdbc.update("insert into sys_user_role(user_id, role_id) values(?,?)", userId, request.roleId());
        }
        return jdbc.queryForMap("select id,username,real_name realName,enabled from sys_user where id=?", userId);
    }

    @Transactional
    public void update(Long id, UserRequest request) {
        if (request.realName() == null || request.realName().isBlank()) {
            throw new BusinessException("姓名不能为空");
        }
        jdbc.update("update sys_user set real_name=?, enabled=? where id=?",
                request.realName(), request.enabled() == null || request.enabled() ? 1 : 0, id);
        if (request.roleId() != null) {
            jdbc.update("delete from sys_user_role where user_id=?", id);
            jdbc.update("insert into sys_user_role(user_id, role_id) values(?,?)", id, request.roleId());
        }
    }

    public void setEnabled(Long id, boolean enabled) {
        jdbc.update("update sys_user set enabled=? where id=?", enabled ? 1 : 0, id);
    }

    public void resetPassword(Long id, UserRequest request) {
        if (request.password() == null || request.password().isBlank()) {
            throw new BusinessException("新密码不能为空");
        }
        jdbc.update("update sys_user set password=? where id=?", passwordEncoder.encode(request.password()), id);
    }
}
