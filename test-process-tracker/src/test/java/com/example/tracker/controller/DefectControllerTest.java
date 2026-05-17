package com.example.tracker.controller;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 缺陷管理模块测试 - 对应 DEFECT 用例 TC_DEFECT_01 ~ TC_DEFECT_06
 */
class DefectControllerTest extends BaseIntegrationTest {

    // TC_DEFECT_01: Tester 提交缺陷
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

    // TC_DEFECT_02: 缺陷标题为空
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

    // TC_DEFECT_03: 严重程度未选择
    // WARNING: 当前后端对空值使用默认值"中"
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

    // TC_DEFECT_04: 缺陷状态流转(完整) NEW->ASSIGNED->FIXING->PENDING_VERIFY->CLOSED
    @Test
    void testDefectFullTransition() throws Exception {
        MockHttpSession testerSession = loginAs("tester");
        mockMvc.perform(post("/api/defects")
                        .session(testerSession)
                        .contentType("application/json")
                        .content(toJson(Map.of("title", "状态流转测试", "module", "测试模块"))))
                .andExpect(status().isOk());

        Long defectId = jdbc.queryForObject("SELECT id FROM defect WHERE title='状态流转测试'", Long.class);

        MockHttpSession devSession = loginAs("dev");
        mockMvc.perform(post("/api/defects/" + defectId + "/transition")
                        .session(devSession)
                        .contentType("application/json")
                        .content(toJson(Map.of("status", "ASSIGNED", "ownerId", 5, "note", "分配给开发"))))
                .andExpect(status().isOk());

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

    // TC_DEFECT_05: 非负责人修改状态（越权）
    @Test
    void testUnauthorizedTransition() throws Exception {
        MockHttpSession testerSession = loginAs("tester");
        mockMvc.perform(post("/api/defects")
                        .session(testerSession)
                        .contentType("application/json")
                        .content(toJson(Map.of("title", "越权测试", "module", "测试模块"))))
                .andExpect(status().isOk());

        Long defectId = jdbc.queryForObject("SELECT id FROM defect WHERE title='越权测试'", Long.class);

        mockMvc.perform(post("/api/defects/" + defectId + "/transition")
                        .session(testerSession)
                        .contentType("application/json")
                        .content(toJson(Map.of("status", "FIXING", "note", "非法操作"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("缺少权限：defect:fix"));
    }

    // TC_DEFECT_06: 重新打开缺陷 REOPENED
    @Test
    void testReopenDefect() throws Exception {
        MockHttpSession testerSession = loginAs("tester");
        MockHttpSession devSession = loginAs("dev");

        mockMvc.perform(post("/api/defects")
                        .session(testerSession)
                        .contentType("application/json")
                        .content(toJson(Map.of("title", "重开测试", "module", "测试模块"))))
                .andExpect(status().isOk());
        Long defectId = jdbc.queryForObject("SELECT id FROM defect WHERE title='重开测试'", Long.class);

        mockMvc.perform(post("/api/defects/" + defectId + "/transition")
                .session(devSession).contentType("application/json")
                .content(toJson(Map.of("status", "ASSIGNED", "ownerId", 5)))).andExpect(status().isOk());
        mockMvc.perform(post("/api/defects/" + defectId + "/transition")
                .session(devSession).contentType("application/json")
                .content(toJson(Map.of("status", "FIXING")))).andExpect(status().isOk());
        mockMvc.perform(post("/api/defects/" + defectId + "/transition")
                .session(devSession).contentType("application/json")
                .content(toJson(Map.of("status", "PENDING_VERIFY")))).andExpect(status().isOk());
        mockMvc.perform(post("/api/defects/" + defectId + "/transition")
                .session(testerSession).contentType("application/json")
                .content(toJson(Map.of("status", "CLOSED")))).andExpect(status().isOk());

        mockMvc.perform(post("/api/defects/" + defectId + "/transition")
                        .session(testerSession)
                        .contentType("application/json")
                        .content(toJson(Map.of("status", "REOPENED", "note", "问题复现"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/defects").session(testerSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("REOPENED"));
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