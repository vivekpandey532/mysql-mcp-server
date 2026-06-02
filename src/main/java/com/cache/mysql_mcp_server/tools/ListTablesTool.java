package com.cache.mysql_mcp_server.tools;

import com.cache.mysql_mcp_server.service.MySqlService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MCP tool for listing all tables in a specific MySQL database.
 */
@Component
public class ListTablesTool {

    private final MySqlService mySqlService;

    /**
     * Constructs a new ListTablesTool with the given MySqlService.
     *
     * @param mySqlService the service for MySQL operations
     */
    public ListTablesTool(MySqlService mySqlService) {
        this.mySqlService = mySqlService;
    }

    /**
     * Lists all tables in the specified MySQL database.
     *
     * @param database the name of the database to list tables from
     * @return list of table names
     */
    @Tool(description = "List all tables in a specific MySQL database")
    public List<String> listTables(@ToolParam(description = "The database name") String database) {
        return mySqlService.listTables(database);
    }
}
