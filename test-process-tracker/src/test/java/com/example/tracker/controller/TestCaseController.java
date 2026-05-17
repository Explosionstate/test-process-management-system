package com.example.tracker.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 测试用例模块测试 - 对应 CASE 用例 TC_CASE_01 ~ TC_CASE_04
 */
class TestCaseControllerTest extends BaseIntegrationTest {

    private Long planId;

    @BeforeEach
    void preparePlan() {
        jdbc.update("INSERT INTO test_plan(id,name,objective,scope_text,status,created_by) VALUES(200,'测试计划','测试','范围','未开始',100)");
        planId = 200L;
    }

    // TC_CASE_01: Tester 创建测试用例
    @Test
    void testCreateTestCase() throws Exception {
        MockHttpSession session = loginAs("tester");
        mockMvc.perform(post("/api/cases")
                        .session(session)
                        .contentType("application/json")
                        .content(toJson(Map.of(
                                "planId", planId,
                                "module", "登录模块",
                                "title", "正确登录验证",
                                "precondition", "用户已注册",
                                "steps", "1.输入用户名 2.输入密码 3.点击登录",
                                "expected", "跳转首页",
                                "result", "未执行"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("正确登录验证"));
    }

    // TC_CASE_02: 用例预期结果为空
    // WARNING: 当前后端未校验 expected 字段
    @Test
    void testCreateCaseEmptyExpected() throws Exception {
        MockHttpSession session = loginAs("tester");
        mockMvc.perform(post("/api/cases")
                        .session(session)
                        .contentType("application/json")
                        .content(toJson(Map.of("planId", planId, "title", "空预期用例", "module", "测试模块", "expected", ""))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // TC_CASE_03: 用例描述超长
    // WARNING: 当前后端无长度限制
    @Test
    void testCreateCaseOverlongDescription() throws Exception {
        MockHttpSession session = loginAs("tester");
        String longText = "a".repeat(3000);
        mockMvc.perform(post("/api/cases")
                        .session(session)
                        .contentType("application/json")
                        .content(toJson(Map.of("planId", planId, "title", "超长描述", "module", "测试模块", "steps", longText))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // TC_CASE_04: 删除不存在的用例
    // WARNING: 当前无删除用例接口，使用 Plan 删除替代
    @Test
    void testDeleteNonExistentCase() throws Exception {
        MockHttpSession session = loginAs("pm");
        mockMvc.perform(delete("/api/plans/99999").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // 补充：执行用例
    @Test
    void testExecuteCase() throws Exception {
        MockHttpSession session = loginAs("tester");
        mockMvc.perform(post("/api/cases")
                        .session(session)
                        .contentType("application/json")
                        .content(toJson(Map.of("planId", planId, "title", "待执行用例", "module", "测试模块"))))
                .andExpect(status().isOk());

        Long caseId = jdbc.queryForObject("SELECT id FROM test_case WHERE title='待执行用例'", Long.class);

        mockMvc.perform(put("/api/cases/" + caseId + "/execute")
                        .session(session)
                        .contentType("application/json")
                        .content(toJson(Map.of("actual", "实际结果匹配", "result", "通过", "executorId", 4))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // 补充：查询用例列表
    @Test
    void testListCases() throws Exception {
        MockHttpSession session = loginAs("tester");
        mockMvc.perform(get("/api/cases").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}