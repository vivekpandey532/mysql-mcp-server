package com.cache.mysql_mcp_server.tools;

import com.cache.mysql_mcp_server.service.MySqlService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListDatabasesTool {

    private final MySqlService mySqlService;

    public ListDatabasesTool(MySqlService mySqlService) {
        this.mySqlService = mySqlService;
    }

    @McpTool(description = "List all databases available on the MySQL server")
    public List<String> listDatabases() {
        return mySqlService.listDatabases();
    }
}
