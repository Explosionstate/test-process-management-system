package com.example.tracker.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

/**
 * 仪表盘/报表模块测试 - 对应 DASH 用例 TC_DASH_01 ~ TC_DASH_04
 */
class DashControllerTest extends BaseIntegrationTest {

    @BeforeEach
    void prepareData() {
        // 清理所有业务数据（包括 data.sql 初始化的）
        jdbc.update("DELETE FROM test_task");
        jdbc.update("DELETE FROM defect_history");
        jdbc.update("DELETE FROM defect");
        jdbc.update("DELETE FROM test_case");
        jdbc.update("DELETE FROM test_plan");

        // 插入测试数据
        jdbc.update("INSERT INTO test_plan(id,name,objective,scope_text,status,created_by) VALUES(300,'报表计划','测试','范围','未开始',1)");
        jdbc.update("INSERT INTO test_case(id,plan_id,module,title,result) VALUES(300,300,'模块A','用例1','通过'),(301,300,'模块B','用例2','失败'),(302,300,'模块C','用例3','未执行')");
        jdbc.update("INSERT INTO defect(id,title,module,severity,priority,status,reporter_id) VALUES(300,'缺陷1','模块A','高','高','NEW',1),(301,'缺陷2','模块B','中','中','ASSIGNED',1)");
        jdbc.update("INSERT INTO test_task(id,plan_id,title,assignee_id,status) VALUES(300,300,'任务1',4,'待处理'),(301,300,'任务2',4,'进行中')");
    }

    // TC_DASH_01: 仪表盘数据统计
    @Test
    void testDashboardStats() throws Exception {
        MockHttpSession session = loginAs("testlead");
        mockMvc.perform(get("/api/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.planCount").value(1))
                .andExpect(jsonPath("$.data.caseCount").value(3))
                .andExpect(jsonPath("$.data.passedCaseCount").value(1))
                .andExpect(jsonPath("$.data.defectCount").value(2));
    }

    // TC_DASH_02: 通过率计算验证
    @Test
    void testPassRateCalculation() throws Exception {
        MockHttpSession session = loginAs("testlead");
        mockMvc.perform(get("/api/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.passRate").value(33.33));
    }

    // TC_DASH_03: 查询不存在的计划ID
    // WARNING: 当前返回 500，建议 404
    @Test
    void testReportWithInvalidPlanId() throws Exception {
        MockHttpSession session = loginAs("testlead");
        mockMvc.perform(get("/api/report?planId=99999").session(session))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    // TC_DASH_04: 测试报告生成
    @Test
    void testReportExport() throws Exception {
        MockHttpSession session = loginAs("testlead");
        mockMvc.perform(get("/api/report/export?planId=300&format=word").session(session))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/msword"))
                .andExpect(header().stringValues("Content-Disposition", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("test-report.doc"))));

        mockMvc.perform(get("/api/report/export?planId=300&format=pdf").session(session))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().stringValues("Content-Disposition", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("test-report.pdf"))));
    }

    // 补充：报表数据查询
    @Test
    void testReportData() throws Exception {
        MockHttpSession session = loginAs("testlead");
        mockMvc.perform(get("/api/report?planId=300").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.plan.name").value("报表计划"))
                .andExpect(jsonPath("$.data.cases").isArray());
    }
}