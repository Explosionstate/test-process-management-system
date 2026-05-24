package com.example.tracker.auth;

import com.example.tracker.common.BusinessException;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    public static final String SESSION_USER = "LOGIN_USER";
    private final JdbcTemplate jdbc;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public LoginUser login(LoginRequest request, HttpSession session) {
        log.info("login_attempt username={}", request.username());
        Map<String, Object> user = jdbc.queryForList("select * from sys_user where username=? and enabled=1", request.username())
                .stream().findFirst().orElseThrow(() -> {
                    log.warn("login_failed username={} reason=user_not_found_or_disabled", request.username());
                    return new BusinessException("用户名或密码错误");
                });
        String encoded = String.valueOf(user.get("password"));
        if (!passwordEncoder.matches(request.password(), encoded)) {
            log.warn("login_failed username={} reason=password_mismatch", request.username());
            throw new BusinessException("用户名或密码错误");
        }
        Long userId = ((Number) user.get("id")).longValue();
        LoginUser loginUser = loadUser(userId, String.valueOf(user.get("username")), String.valueOf(user.get("real_name")));
        session.setAttribute(SESSION_USER, loginUser);
        log.info("login_success username={} userId={} roles={}", loginUser.username(), loginUser.id(), loginUser.roles());
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
            log.warn("permission_denied username={} permission={}", user.username(), permission);
            throw new SecurityException("缺少权限：" + permission);
        }
    }

    private LoginUser loadUser(Long userId, String username, String realName) {
        List<String> roles = jdbc.queryForList("select r.code from sys_role r join sys_user_role ur on r.id=ur.role_id where ur.user_id=?", String.class, userId);
        List<String> permissions = jdbc.queryForList("select distinct p.code from sys_permission p join sys_role_permission rp on p.id=rp.permission_id join sys_user_role ur on rp.role_id=ur.role_id where ur.user_id=?", String.class, userId);
        return new LoginUser(userId, username, realName, roles, permissions);
    }

}
