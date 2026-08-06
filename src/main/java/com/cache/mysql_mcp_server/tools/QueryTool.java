package com.cache.mysql_mcp_server.tools;

import com.cache.mysql_mcp_server.service.MySqlService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class QueryTool {

    private final MySqlService mySqlService;

    public QueryTool(MySqlService mySqlService) {
        this.mySqlService = mySqlService;
    }

    @McpTool(description = "Execute a SELECT SQL query against the MySQL database and return results")
    public String query(@McpToolParam(description = "The SELECT SQL query to execute") String sql) {
        try {
            List<Map<String, Object>> results = mySqlService.executeQuery(sql);
            if (results.isEmpty()) return "No results found.";

            StringBuilder sb = new StringBuilder();
            List<String> columns = results.get(0).keySet().stream().toList();
            sb.append(String.join(" | ", columns)).append("\n");
            sb.append("-".repeat(columns.stream().mapToInt(c -> c.length() + 3).sum())).append("\n");
            for (Map<String, Object> row : results) {
                sb.append(columns.stream()
                        .map(col -> row.get(col) != null ? row.get(col).toString() : "NULL")
                        .reduce((a, b) -> a + " | " + b)
                        .orElse(""))
                        .append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
