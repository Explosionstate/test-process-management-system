package com.example.tracker.controller;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class AuthControllerTest extends BaseIntegrationTest {

    // TC_LOGIN_01: 正确账号登录
    @Test
    void testLoginSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(toJson(Map.of("username", "admin", "password", "password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    // TC_LOGIN_02: 错误密码登录
    @Test
    void testLoginWrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(toJson(Map.of("username", "admin", "password", "123456"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("用户名或密码错误"));
    }

    // TC_LOGIN_03: 用户名为空
    @Test
    void testLoginEmptyUsername() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(toJson(Map.of("username", "", "password", "password"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // TC_LOGIN_04: 密码为空
    @Test
    void testLoginEmptyPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(toJson(Map.of("username", "admin", "password", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // TC_LOGIN_05: 账号不存在
    @Test
    void testLoginUserNotExist() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(toJson(Map.of("username", "notexist", "password", "password"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("用户名或密码错误"));
    }

    // TC_LOGIN_06: 禁用账号登录
    @Test
    void testLoginDisabledUser() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(toJson(Map.of("username", "disabled_user", "password", "password"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("用户名或密码错误"));
    }

    // TC_LOGIN_07: 未登录访问受保护页
    @Test
    void testAccessProtectedWithoutLogin() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("请先登录"));
    }

    // 补充：登出后访问
    @Test
    void testLogoutAndAccess() throws Exception {
        MockHttpSession session = loginAs("admin");
        mockMvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/dashboard").session(session))
                .andExpect(status().isUnauthorized());
    }
}