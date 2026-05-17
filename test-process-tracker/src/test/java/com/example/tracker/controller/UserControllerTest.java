package com.example.tracker.controller;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户管理模块测试 - 对应 USER 用例 TC_USER_01 ~ TC_USER_05
 */
class UserControllerTest extends BaseIntegrationTest {

    // TC_USER_01: Admin 新增用户
    @Test
    void testAdminCreateUser() throws Exception {
        MockHttpSession session = loginAs("admin");
        mockMvc.perform(post("/api/users")
                        .session(session)
                        .contentType("application/json")
                        .content(toJson(Map.of("username", "newuser", "password", "password", "realName", "新用户", "roleId", 4))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("newuser"));
    }

    // TC_USER_02: 新增重复用户名
    @Test
    void testCreateDuplicateUsername() throws Exception {
        MockHttpSession session = loginAs("admin");
        mockMvc.perform(post("/api/users")
                        .session(session)
                        .contentType("application/json")
                        .content(toJson(Map.of("username", "dupuser", "password", "password", "realName", "重复"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/users")
                        .session(session)
                        .contentType("application/json")
                        .content(toJson(Map.of("username", "dupuser", "password", "password", "realName", "重复2"))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    // TC_USER_03: Tester 越权访问用户管理
    @Test
    void testTesterAccessUserManage() throws Exception {
        MockHttpSession session = loginAs("tester");
        mockMvc.perform(get("/api/users").session(session))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("缺少权限：user:manage"));
    }

    // TC_USER_04: Dev 越权创建计划
    @Test
    void testDevUnauthorizedCreatePlan() throws Exception {
        MockHttpSession session = loginAs("dev");
        mockMvc.perform(post("/api/plans")
                        .session(session)
                        .contentType("application/json")
                        .content(toJson(Map.of("name", "非法计划", "objective", "测试"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("缺少权限：plan:create"));
    }

    // TC_USER_05: 禁用后登录验证
    @Test
    void testDisableUserThenLogin() throws Exception {
        MockHttpSession adminSession = loginAs("admin");
        mockMvc.perform(post("/api/users")
                        .session(adminSession)
                        .contentType("application/json")
                        .content(toJson(Map.of("username", "tobedisabled", "password", "password", "realName", "待禁用"))))
                .andExpect(status().isOk());

        Long userId = jdbc.queryForObject("SELECT id FROM sys_user WHERE username='tobedisabled'", Long.class);

        MockHttpSession userSession = new MockHttpSession();
        mockMvc.perform(post("/api/auth/login")
                        .session(userSession)
                        .contentType("application/json")
                        .content(toJson(Map.of("username", "tobedisabled", "password", "password"))))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/users/" + userId + "/enabled")
                        .session(adminSession)
                        .contentType("application/json")
                        .content(toJson(Map.of("enabled", false))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(toJson(Map.of("username", "tobedisabled", "password", "password"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("用户名或密码错误"));
    }

    // 补充：查询角色列表
    @Test
    void testGetRoles() throws Exception {
        MockHttpSession session = loginAs("admin");
        mockMvc.perform(get("/api/users/roles").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}