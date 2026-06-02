package com.cache.mysql_mcp_server.tools;

import com.cache.mysql_mcp_server.service.MySqlService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool for describing the schema of a MySQL table.
 */
@Component
public class DescribeTableTool {

    private final MySqlService mySqlService;

    /**
     * Constructs a new DescribeTableTool with the given MySqlService.
     *
     * @param mySqlService the service for MySQL operations
     */
    public DescribeTableTool(MySqlService mySqlService) {
        this.mySqlService = mySqlService;
    }

    /**
     * Describes the schema of a table including columns, types, keys, and nullability.
     *
     * @param database the name of the database
     * @param table the name of the table to describe
     * @return formatted string containing column metadata
     */
    @Tool(description = "Describe the schema of a table including columns, types, keys, and nullability")
    public String describeTable(
            @ToolParam(description = "The database name") String database,
            @ToolParam(description = "The table name") String table) {
        List<Map<String, Object>> schema = mySqlService.describeTable(database, table);
        if (schema.isEmpty()) return "Table not found.";

        StringBuilder sb = new StringBuilder();
        sb.append("Column | Type | Nullable | Key | Default | Extra");
        sb.append("----------------------------------------------------");
        for (Map<String, Object> col : schema) {
            sb.append(col.getOrDefault("COLUMN_NAME", ""))
                    .append(" | ")
                    .append(col.getOrDefault("COLUMN_TYPE", ""))
                    .append(" | ")
                    .append(col.getOrDefault("IS_NULLABLE", ""))
                    .append(" | ")
                    .append(col.getOrDefault("COLUMN_KEY", ""))
                    .append(" | ")
                    .append(col.getOrDefault("COLUMN_DEFAULT", "NULL"))
                    .append(" | ")
                    .append(col.getOrDefault("EXTRA", ""));
        }
        return sb.toString();
    }
}
