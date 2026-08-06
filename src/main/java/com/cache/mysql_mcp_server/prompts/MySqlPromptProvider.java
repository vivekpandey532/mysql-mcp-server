package com.cache.mysql_mcp_server.prompts;

import org.springframework.ai.mcp.annotation.McpArg;
import org.springframework.ai.mcp.annotation.McpPrompt;
import org.springframework.stereotype.Component;

@Component
public class MySqlPromptProvider {

    @McpPrompt(
        name = "write-query",
        description = "Generates a prompt to help write a SQL SELECT query for a given goal"
    )
    public String writeQuery(
            @McpArg(name = "database", description = "The database name", required = true) String database,
            @McpArg(name = "table", description = "The table name", required = true) String table,
            @McpArg(name = "goal", description = "What the query should achieve", required = true) String goal) {
        return String.format(
            "Write a SQL SELECT query for the table `%s.%s` that achieves the following goal: %s. " +
            "Return only the SQL query, no explanation.",
            database, table, goal);
    }

    @McpPrompt(
        name = "analyze-table",
        description = "Generates a prompt to analyze the structure and content of a MySQL table"
    )
    public String analyzeTable(
            @McpArg(name = "database", description = "The database name", required = true) String database,
            @McpArg(name = "table", description = "The table name", required = true) String table) {
        return String.format(
            "Analyze the MySQL table `%s.%s`. Describe its purpose based on column names and types, " +
            "identify primary/foreign keys, and suggest any potential data quality or design improvements.",
            database, table);
    }

    @McpPrompt(
        name = "explain-query-results",
        description = "Generates a prompt to explain the results of a SQL query in plain language"
    )
    public String explainQueryResults(
            @McpArg(name = "sql", description = "The SQL query that was executed", required = true) String sql,
            @McpArg(name = "results", description = "The query results as text", required = true) String results) {
        return String.format(
            "The following SQL query was executed:\n%s\n\nResults:\n%s\n\n" +
            "Explain these results in plain language, highlighting key insights.",
            sql, results);
    }
}
