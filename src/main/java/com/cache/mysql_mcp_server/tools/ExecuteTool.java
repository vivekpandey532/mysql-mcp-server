package com.cache.mysql_mcp_server.tools;

import com.cache.mysql_mcp_server.service.MySqlService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * MCP tool for executing DML statements (INSERT, UPDATE, DELETE) against the MySQL database.
 */
@Component
public class ExecuteTool {

    private final MySqlService mySqlService;

    /**
     * Constructs a new ExecuteTool with the given MySqlService.
     *
     * @param mySqlService the service for MySQL operations
     */
    public ExecuteTool(MySqlService mySqlService) {
        this.mySqlService = mySqlService;
    }

    /**
     * Executes a DML statement and returns the number of affected rows.
     *
     * @param sql the DML SQL statement to execute (INSERT, UPDATE, or DELETE)
     * @return string indicating the number of affected rows, or error message on failure
     */
    @Tool(description = "Execute a DML statement (INSERT, UPDATE, DELETE) against the MySQL database")
    public String execute(@ToolParam(description = "The SQL DML statement to execute") String sql) {
        try {
            int affectedRows = mySqlService.executeUpdate(sql);
            return "Affected rows: " + affectedRows;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
