package com.example.tracker.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 测试任务模块测试 - 对应 TASK 用例 TC_TASK_01 ~ TC_TASK_02
 */
class TaskControllerTest extends BaseIntegrationTest {

    private Long planId;

    @BeforeEach
    void preparePlan() {
        jdbc.update("INSERT INTO test_plan(id,name,objective,scope_text,status,created_by) VALUES(201,'任务计划','测试','范围','未开始',100)");
        planId = 201L;
    }

    // TC_TASK_01: 分配任务给不存在用户
    @Test
    void testCreateTaskWithInvalidUser() throws Exception {
        MockHttpSession session = loginAs("testlead");
        mockMvc.perform(post("/api/tasks")
                        .session(session)
                        .contentType("application/json")
                        .content(toJson(Map.of(
                                "planId", planId,
                                "title", "非法用户任务",
                                "assigneeId", 99999,
                                "status", "待处理"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("用户不存在或已禁用"));
    }

    // TC_TASK_02: 填写无效任务状态
    @Test
    void testCreateTaskWithInvalidStatus() throws Exception {
        MockHttpSession session = loginAs("testlead");
        mockMvc.perform(post("/api/tasks")
                        .session(session)
                        .contentType("application/json")
                        .content(toJson(Map.of(
                                "planId", planId,
                                "title", "非法状态任务",
                                "assigneeId", 4,
                                "status", "不存在的状态"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("状态值非法，可选：[待处理, 进行中, 已完成, 已取消]"));
    }

    // 补充：正常创建和更新任务
    @Test
    void testCreateAndUpdateTask() throws Exception {
        MockHttpSession session = loginAs("testlead");
        mockMvc.perform(post("/api/tasks")
                        .session(session)
                        .contentType("application/json")
                        .content(toJson(Map.of(
                                "planId", planId,
                                "title", "正常任务",
                                "assigneeId", 4,
                                "status", "待处理",
                                "dueDate", "2026-05-20"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("待处理"));

        Long taskId = jdbc.queryForObject("SELECT id FROM test_task WHERE title='正常任务'", Long.class);

        mockMvc.perform(put("/api/tasks/" + taskId + "/status")
                        .session(session)
                        .contentType("application/json")
                        .content(toJson(Map.of("status", "进行中"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // 补充：查询任务列表
    @Test
    void testListTasks() throws Exception {
        MockHttpSession session = loginAs("testlead");
        mockMvc.perform(get("/api/tasks").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}