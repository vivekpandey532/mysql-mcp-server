package com.cache.mysql_mcp_server.config;

import com.cache.mysql_mcp_server.tools.*;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class that registers all MySQL MCP tools with the Spring AI framework.
 * These tools are exposed to MCP clients for database interaction.
 */
@Configuration
public class McpServerConfig {

    /**
     * Creates a ToolCallbackProvider that registers all MySQL tool beans
     * for discovery and invocation by MCP clients.
     *
     * @param queryTool tool for executing SELECT queries
     * @param executeTool tool for executing DML statements
     * @param listDatabasesTool tool for listing all databases
     * @param listTablesTool tool for listing tables in a database
     * @param describeTableTool tool for describing table schema
     * @return the configured ToolCallbackProvider
     */
    @Bean
    public ToolCallbackProvider mysqlToolCallbackProvider(
            QueryTool queryTool,
            ExecuteTool executeTool,
            ListDatabasesTool listDatabasesTool,
            ListTablesTool listTablesTool,
            DescribeTableTool describeTableTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(queryTool, executeTool, listDatabasesTool, listTablesTool, describeTableTool)
                .build();
    }
}
