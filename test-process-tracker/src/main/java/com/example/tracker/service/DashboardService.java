package com.example.tracker.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {
    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);
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

    public byte[] exportWord(Long planId) {
        Map<String, Object> data = report(planId);
        String html = """
                <html><head><meta charset='UTF-8'><style>
                body{font-family:'Microsoft YaHei',Arial,sans-serif;line-height:1.7;color:#1f2937;}
                h1{font-size:24px;text-align:center;} h2{font-size:18px;margin-top:24px;}
                table{border-collapse:collapse;width:100%;margin:12px 0;} th,td{border:1px solid #999;padding:8px;text-align:left;} th{background:#d9eaf7;}
                </style></head><body>
                """ + reportHtml(data) + "</body></html>";
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        log.info("report_exported planId={} format=word bytes={}", planId, bytes.length);
        return bytes;
    }

    public byte[] exportPdf(Long planId) {
        Map<String, Object> data = report(planId);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("Test Report"));
            document.add(new Paragraph(stripHtml(reportHtml(data))));
            document.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("PDF 报告生成失败", e);
        }
        byte[] bytes = out.toByteArray();
        log.info("report_exported planId={} format=pdf bytes={}", planId, bytes.length);
        return bytes;
    }

    @SuppressWarnings("unchecked")
    private String reportHtml(Map<String, Object> data) {
        Map<String, Object> plan = (Map<String, Object>) data.get("plan");
        List<Map<String, Object>> cases = (List<Map<String, Object>>) data.get("cases");
        List<Map<String, Object>> defects = (List<Map<String, Object>>) data.get("defects");
        StringBuilder html = new StringBuilder();
        html.append("<h1>软件测试报告</h1>");
        html.append("<h2>一、测试计划</h2>");
        html.append("<p>计划名称：").append(plan.get("name")).append("</p>");
        html.append("<p>测试目标：").append(plan.get("objective")).append("</p>");
        html.append("<p>测试范围：").append(plan.get("scope_text")).append("</p>");
        html.append("<h2>二、测试用例执行情况</h2><table><tr><th>执行结果</th><th>数量</th></tr>");
        for (Map<String, Object> row : cases) {
            html.append("<tr><td>").append(row.get("result")).append("</td><td>").append(row.get("count")).append("</td></tr>");
        }
        html.append("</table><h2>三、缺陷状态分布</h2><table><tr><th>缺陷状态</th><th>数量</th></tr>");
        for (Map<String, Object> row : defects) {
            html.append("<tr><td>").append(row.get("status")).append("</td><td>").append(row.get("count")).append("</td></tr>");
        }
        html.append("</table><h2>四、测试结论</h2><p>").append(data.get("conclusion")).append("</p>");
        return html.toString();
    }

    private String stripHtml(String html) {
        return html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }
}
