package com.example.tracker.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {
    private final JdbcTemplate jdbc;

    public DashboardService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Map<String, Object> dashboard() {
        Map<String, Object> data = new LinkedHashMap<>();
        Number planCount = jdbc.queryForObject("select count(*) from test_plan", Number.class);
        Number caseCount = jdbc.queryForObject("select count(*) from test_case", Number.class);
        Number passed = jdbc.queryForObject("select count(*) from test_case where result='通过'", Number.class);
        Number defectCount = jdbc.queryForObject("select count(*) from defect", Number.class);
        double passRate = caseCount.longValue() == 0 ? 0 : Math.round(passed.doubleValue() * 10000 / caseCount.doubleValue()) / 100.0;
        data.put("planCount", planCount);
        data.put("caseCount", caseCount);
        data.put("passedCaseCount", passed);
        data.put("passRate", passRate);
        data.put("defectCount", defectCount);
        data.put("defectsByStatus", jdbc.queryForList("select status,count(*) count from defect group by status"));
        data.put("defectsByModule", jdbc.queryForList("select module,count(*) count from defect group by module order by count desc"));
        data.put("taskProgress", jdbc.queryForList("select status,count(*) count from test_task group by status"));
        data.put("recentDefects", jdbc.queryForList("select id,title,module,status,updated_at updatedAt from defect order by updated_at desc limit 5"));
        return data;
    }

    public Map<String, Object> report(Long planId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("plan", jdbc.queryForMap("select * from test_plan where id=?", planId));
        data.put("cases", jdbc.queryForList("select result,count(*) count from test_case where plan_id=? group by result", planId));
        data.put("defects", jdbc.queryForList("select status,count(*) count from defect group by status"));
        data.put("conclusion", "系统根据测试用例执行结果、缺陷状态分布和测试任务进度生成测试报告，用于项目质量管理和验收决策。");
        return data;
    }
}
