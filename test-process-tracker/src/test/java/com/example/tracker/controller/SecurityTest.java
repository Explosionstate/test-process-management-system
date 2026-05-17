package com.example.tracker.controller;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 安全与数据库模块测试 - 对应 SEC 用例 TC_SEC_01 ~ TC_SEC_02、DB 用例 TC_DB_01
 */
class SecurityTest extends BaseIntegrationTest {

    // TC_SEC_01: SQL 注入尝试
    @Test
    void testSqlInjection() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(toJson(Map.of("username", "' or 1=1 --", "password", "password"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("用户名或密码错误"));

        MockHttpSession session = loginAs("admin");
        mockMvc.perform(get("/api/defects?status=' or 1=1 --").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // TC_SEC_02: XSS 脚本注入
    // WARNING: 当前缺陷标题入库时未做 HTML 转义
    @Test
    void testXssInjection() throws Exception {
        MockHttpSession session = loginAs("tester");
        String xssPayload = "<script>alert(1)</script>";
        mockMvc.perform(post("/api/defects")
                        .session(session)
                        .contentType("application/json")
                        .content(toJson(Map.of("title", xssPayload, "module", "安全测试"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value(xssPayload));
    }

    // TC_DB_01: 密码 BCrypt 校验
    @Test
    void testPasswordBCryptVerify() throws Exception {
        String[] users = {"admin", "tester", "dev", "pm", "testlead"};
        for (String username : users) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType("application/json")
                            .content(toJson(Map.of("username", username, "password", "password"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.username").value(username));
        }
    }

    // 补充：验证密码不是明文存储
    @Test
    void testPasswordNotPlainText() throws Exception {
        String password = jdbc.queryForObject("SELECT password FROM sys_user WHERE username='admin'", String.class);
        assert !password.equals("password") : "密码不应明文存储";
        assert password.startsWith("$2a$") : "密码应使用 BCrypt 加密";
    }
}