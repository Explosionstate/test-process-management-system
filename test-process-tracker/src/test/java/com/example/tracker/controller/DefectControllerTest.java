package com.example.tracker.controller;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DefectControllerTest extends BaseIntegrationTest {

    @Test
    void testCreateDefect() throws Exception {
        MockHttpSession session = loginAs("tester");
        mockMvc.perform(post("/api/defects")
                        .session(session)
                        .contentType("application/json")
                        .content(toJson(Map.of(
                                "title", "登录按钮无响应",
                                "module", "登录模块",
                                "severity", "高",
                                "priority", "高",
                                "steps", "点击登录按钮",
                                "expected", "跳转首页",
                                "actual", "无反应"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("NEW"));
    }

    @Test
    void testCreateDefectEmptyTitle() throws Exception {
        MockHttpSession session = loginAs("tester");
        mockMvc.perform(post("/api/defects")
                        .session(session)
                        .contentType("application/json")
                        .content(toJson(Map.of("title", "", "module", "登录模块"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("缺陷标题和所属模块不能为空"));
    }

    @Test
    void testCreateDefectEmptySeverity() throws Exception {
        MockHttpSession session = loginAs("tester");
        mockMvc.perform(post("/api/defects")
                        .session(session)
                        .contentType("application/json")
                        .content(toJson(Map.of("title", "严重程度为空", "module", "登录模块", "severity", ""))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testDefectFullTransition() throws Exception {
        MockHttpSession testerSession = loginAs("tester");
        mockMvc.perform(post("/api/defects")
                        .session(testerSession)
                        .contentType("application/json")
                        .content(toJson(Map.of("title", "状态流转测试", "module", "测试模块"))))
                .andExpect(status().isOk());

        Long defectId = jdbc.queryForObject("SELECT id FROM defect WHERE title='状态流转测试'", Long.class);

        MockHttpSession testSession = loginAs("testlead");
        mockMvc.perform(post("/api/defects/" + defectId + "/transition")
                        .session(testSession)
                        .contentType("application/json")
                        .content(toJson(Map.of("status", "ASSIGNED", "ownerId", 5, "note", "分配给开发"))))
                .andExpect(status().isOk());
        MockHttpSession devSession = loginAs("dev");
        mockMvc.perform(post("/api/defects/" + defectId + "/transition")
                        .session(devSession)
                        .contentType("application/json")
                        .content(toJson(Map.of("status", "FIXING", "note", "开始修复"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/defects/" + defectId + "/transition")
                        .session(devSession)
                        .contentType("application/json")
                        .content(toJson(Map.of("status", "PENDING_VERIFY", "note", "修复完成"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/defects/" + defectId + "/transition")
                        .session(testerSession)
                        .contentType("application/json")
                        .content(toJson(Map.of("status", "CLOSED", "note", "验证通过"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/defects/" + defectId + "/history").session(testerSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(5));
    }


    @Test
    void testUnauthorizedTransition() throws Exception {
        MockHttpSession testerSession = loginAs("tester");
        mockMvc.perform(post("/api/defects")
                        .session(testerSession)
                        .contentType("application/json")
                        .content(toJson(Map.of("title", "越权测试", "module", "测试模块"))))
                .andExpect(status().isOk());

        Long defectId = jdbc.queryForObject("SELECT id FROM defect WHERE title='越权测试'", Long.class);

        MockHttpSession leadSession = loginAs("testlead");
        mockMvc.perform(post("/api/defects/" + defectId + "/transition")
                        .session(leadSession)
                        .contentType("application/json")
                        .content(toJson(Map.of("status", "ASSIGNED", "ownerId", 5, "note", "分配给开发"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/defects/" + defectId + "/transition")
                        .session(testerSession)
                        .contentType("application/json")
                        .content(toJson(Map.of("status", "FIXING", "note", "非法操作"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("缺少权限：defect:fix"));
    }


    // 补充：非法状态流转
    @Test
    void testInvalidTransition() throws Exception {
        MockHttpSession testerSession = loginAs("tester");
        mockMvc.perform(post("/api/defects")
                        .session(testerSession)
                        .contentType("application/json")
                        .content(toJson(Map.of("title", "非法流转", "module", "测试模块"))))
                .andExpect(status().isOk());
        Long defectId = jdbc.queryForObject("SELECT id FROM defect WHERE title='非法流转'", Long.class);

        mockMvc.perform(post("/api/defects/" + defectId + "/transition")
                        .session(testerSession)
                        .contentType("application/json")
                        .content(toJson(Map.of("status", "CLOSED"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("非法状态流转：NEW -> CLOSED"));
    }
}