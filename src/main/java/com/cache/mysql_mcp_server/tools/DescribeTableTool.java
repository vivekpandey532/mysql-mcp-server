package com.cache.mysql_mcp_server.tools;

import com.cache.mysql_mcp_server.service.MySqlService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DescribeTableTool {

    private final MySqlService mySqlService;

    public DescribeTableTool(MySqlService mySqlService) {
        this.mySqlService = mySqlService;
    }

    @McpTool(description = "Describe the schema of a table including columns, types, keys, and nullability")
    public String describeTable(
            @McpToolParam(description = "The database name") String database,
            @McpToolParam(description = "The table name") String table) {
        List<Map<String, Object>> schema = mySqlService.describeTable(database, table);
        if (schema.isEmpty()) return "Table not found.";

        StringBuilder sb = new StringBuilder();
        sb.append("Column | Type | Nullable | Key | Default | Extra\n");
        sb.append("----------------------------------------------------\n");
        for (Map<String, Object> col : schema) {
            sb.append(col.getOrDefault("COLUMN_NAME", "")).append(" | ")
              .append(col.getOrDefault("COLUMN_TYPE", "")).append(" | ")
              .append(col.getOrDefault("IS_NULLABLE", "")).append(" | ")
              .append(col.getOrDefault("COLUMN_KEY", "")).append(" | ")
              .append(col.getOrDefault("COLUMN_DEFAULT", "NULL")).append(" | ")
              .append(col.getOrDefault("EXTRA", "")).append("\n");
        }
        return sb.toString();
    }
}
