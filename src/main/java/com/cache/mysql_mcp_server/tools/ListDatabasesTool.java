package com.cache.mysql_mcp_server.tools;

import com.cache.mysql_mcp_server.service.MySqlService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MCP tool for listing all databases available on the MySQL server.
 */
@Component
public class ListDatabasesTool {

    private final MySqlService mySqlService;

    /**
     * Constructs a new ListDatabasesTool with the given MySqlService.
     *
     * @param mySqlService the service for MySQL operations
     */
    public ListDatabasesTool(MySqlService mySqlService) {
        this.mySqlService = mySqlService;
    }

    /**
     * Lists all databases available on the MySQL server.
     *
     * @return list of database names
     */
    @Tool(description = "List all databases available on the MySQL server")
    public List<String> listDatabases() {
        return mySqlService.listDatabases();
    }
}
