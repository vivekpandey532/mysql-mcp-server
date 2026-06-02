package com.cache.mysql_mcp_server.tools;

import com.cache.mysql_mcp_server.service.MySqlService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool for executing SELECT SQL queries against the MySQL database.
 */
@Component
public class QueryTool {

    private final MySqlService mySqlService;

    /**
     * Constructs a new QueryTool with the given MySqlService.
     *
     * @param mySqlService the service for MySQL operations
     */
    public QueryTool(MySqlService mySqlService) {
        this.mySqlService = mySqlService;
    }

    /**
     * Executes a SELECT SQL query and returns results in a formatted table string.
     *
     * @param sql the SELECT SQL query to execute
     * @return formatted string with column headers and row data, or error message on failure
     */
    @Tool(description = "Execute a SELECT SQL query against the MySQL database and return results")
    public String query(@ToolParam(description = "The SELECT SQL query to execute") String sql) {
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
