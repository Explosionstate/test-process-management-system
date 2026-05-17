package com.example.tracker.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    protected ObjectMapper objectMapper;

    // data.sql 中的用户ID映射（根据data.sql实际数据）
    protected static final Long ADMIN_ID = 1L;
    protected static final Long PM_ID = 2L;
    protected static final Long TESTLEAD_ID = 3L;
    protected static final Long TESTER_ID = 4L;
    protected static final Long DEV_ID = 5L;
    protected static final Long QA_ID = 6L;

    @BeforeEach
    void prepareBaseData() {
        // 只清理业务表（测试数据），保留 data.sql 初始化的用户/角色/权限
        jdbc.update("DELETE FROM test_task WHERE id >= 100");
        jdbc.update("DELETE FROM defect_history WHERE id >= 100");
        jdbc.update("DELETE FROM defect WHERE id >= 100");
        jdbc.update("DELETE FROM test_case WHERE id >= 100");
        jdbc.update("DELETE FROM test_plan WHERE id >= 100");
        // 清理测试创建的额外用户
        jdbc.update("DELETE FROM sys_user_role WHERE user_id >= 100");
        jdbc.update("DELETE FROM sys_user WHERE id >= 100");
    }

    protected MockHttpSession loginAs(String username) throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/api/auth/login")
                        .session(session)
                        .contentType("application/json")
                        .content(toJson(Map.of("username", username, "password", "password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        return session;
    }

    protected String toJson(Map<String, Object> map) throws JsonProcessingException {
        return objectMapper.writeValueAsString(map);
    }
}