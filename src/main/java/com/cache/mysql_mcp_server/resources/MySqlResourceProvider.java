package com.cache.mysql_mcp_server.resources;

import com.cache.mysql_mcp_server.service.MySqlService;
import org.springframework.ai.mcp.annotation.McpArg;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class MySqlResourceProvider {

    private final MySqlService mySqlService;

    public MySqlResourceProvider(MySqlService mySqlService) {
        this.mySqlService = mySqlService;
    }

    @McpResource(
        uri = "mysql://databases",
        name = "list-databases",
        description = "Lists all databases available on the MySQL server",
        mimeType = "text/plain"
    )
    public String listDatabases() {
        List<String> databases = mySqlService.listDatabases();
        return "Databases:\n" + String.join("\n", databases);
    }

    @McpResource(
        uri = "mysql://tables/{database}",
        name = "list-tables",
        description = "Lists all tables in the given database",
        mimeType = "text/plain"
    )
    public String listTables(@McpArg(name = "database", description = "The database name", required = true) String database) {
        List<String> tables = mySqlService.listTables(database);
        return "Tables in " + database + ":\n" + String.join("\n", tables);
    }

    @McpResource(
        uri = "mysql://schema/{database}/{table}",
        name = "table-schema",
        description = "Returns the schema of a specific table including columns, types, keys, and nullability",
        mimeType = "text/plain"
    )
    public String tableSchema(
            @McpArg(name = "database", description = "The database name", required = true) String database,
            @McpArg(name = "table", description = "The table name", required = true) String table) {
        List<Map<String, Object>> schema = mySqlService.describeTable(database, table);
        if (schema.isEmpty()) return "Table not found.";

        StringBuilder sb = new StringBuilder("Schema for ").append(database).append(".").append(table).append(":\n");
        sb.append("Column | Type | Nullable | Key | Default | Extra\n");
        sb.append("------------------------------------------------------\n");
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
