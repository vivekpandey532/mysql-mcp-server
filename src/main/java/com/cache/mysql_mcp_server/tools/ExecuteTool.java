package com.cache.mysql_mcp_server.tools;

import com.cache.mysql_mcp_server.service.MySqlService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class ExecuteTool {

    private final MySqlService mySqlService;

    public ExecuteTool(MySqlService mySqlService) {
        this.mySqlService = mySqlService;
    }

    @McpTool(description = "Execute a DML statement (INSERT, UPDATE, DELETE) against the MySQL database")
    public String execute(@McpToolParam(description = "The SQL DML statement to execute") String sql) {
        try {
            int affectedRows = mySqlService.executeUpdate(sql);
            return "Affected rows: " + affectedRows;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
