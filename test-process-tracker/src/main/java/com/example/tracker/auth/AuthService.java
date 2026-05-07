package com.example.tracker.auth;

import com.example.tracker.common.BusinessException;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AuthService {
    public static final String SESSION_USER = "LOGIN_USER";
    private final JdbcTemplate jdbc;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public LoginUser login(LoginRequest request, HttpSession session) {
        Map<String, Object> user = jdbc.queryForList("select * from sys_user where username=? and enabled=1", request.username())
                .stream().findFirst().orElseThrow(() -> new BusinessException("用户名或密码错误"));
        String encoded = String.valueOf(user.get("password"));
        if (!passwordMatches(request.password(), encoded)) {
            throw new BusinessException("用户名或密码错误");
        }
        Long userId = ((Number) user.get("id")).longValue();
        LoginUser loginUser = loadUser(userId, String.valueOf(user.get("username")), String.valueOf(user.get("real_name")));
        session.setAttribute(SESSION_USER, loginUser);
        return loginUser;
    }

    public LoginUser current(HttpSession session) {
        Object user = session.getAttribute(SESSION_USER);
        if (user instanceof LoginUser loginUser) {
            return loginUser;
        }
        throw new SecurityException("请先登录");
    }

    public void require(HttpSession session, String permission) {
        LoginUser user = current(session);
        if (!user.has(permission)) {
            throw new SecurityException("缺少权限：" + permission);
        }
    }

    private LoginUser loadUser(Long userId, String username, String realName) {
        List<String> roles = jdbc.queryForList("select r.code from sys_role r join sys_user_role ur on r.id=ur.role_id where ur.user_id=?", String.class, userId);
        List<String> permissions = jdbc.queryForList("select distinct p.code from sys_permission p join sys_role_permission rp on p.id=rp.permission_id join sys_user_role ur on rp.role_id=ur.role_id where ur.user_id=?", String.class, userId);
        return new LoginUser(userId, username, realName, roles, permissions);
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (storedPassword == null) {
            return false;
        }
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return storedPassword.equals(rawPassword);
    }
}
