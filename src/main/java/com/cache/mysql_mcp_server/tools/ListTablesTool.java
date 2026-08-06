package com.cache.mysql_mcp_server.tools;

import com.cache.mysql_mcp_server.service.MySqlService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListTablesTool {

    private final MySqlService mySqlService;

    public ListTablesTool(MySqlService mySqlService) {
        this.mySqlService = mySqlService;
    }

    @McpTool(description = "List all tables in a specific MySQL database")
    public List<String> listTables(@McpToolParam(description = "The database name") String database) {
        return mySqlService.listTables(database);
    }
}
