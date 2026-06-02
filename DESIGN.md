# MySQL MCP Server - Design Document

## Overview

A Model Context Protocol (MCP) server that exposes MySQL database operations as tools, allowing AI agents (like Claude, Amazon Q) to interact with MySQL databases through a standardized protocol.

---

## High-Level Design (HLD)

### Architecture

```
┌─────────────────┐       MCP (stdio/SSE)       ┌─────────────────────┐        JDBC         ┌───────────┐
│   AI Agent      │ ◄──────────────────────────► │  MySQL MCP Server   │ ◄──────────────────► │  MySQL DB │
│ (Claude/Q/etc)  │    JSON-RPC over stdio       │  (Java 21 + Spring) │    Connection Pool   │           │
└─────────────────┘                              └─────────────────────┘                      └───────────┘
```

### Components

1. **MCP Transport Layer** – Handles JSON-RPC communication over stdio (standard input/output)
2. **Tool Registry** – Registers and exposes MySQL operations as MCP tools
3. **Query Executor** – Executes SQL queries safely with parameterization
4. **Connection Manager** – Manages MySQL connection pool (HikariCP)
5. **Schema Inspector** – Provides database/table metadata introspection

### Exposed MCP Tools

| Tool Name | Description |
|-----------|-------------|
| `query` | Execute SELECT queries and return results |
| `execute` | Execute INSERT/UPDATE/DELETE statements |
| `list_databases` | List all accessible databases |
| `list_tables` | List tables in a database |
| `describe_table` | Get table schema (columns, types, keys) |
| `create_table` | Create a new table |
| `drop_table` | Drop a table |

### Technology Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.x + Spring AI MCP Server
- **Database Driver**: MySQL Connector/J
- **Connection Pool**: HikariCP
- **Build Tool**: Maven
- **Transport**: stdio (for local agent integration)

---

## Low-Level Design (LLD)

### Package Structure

```
com.cache.mysql_mcp_server/
├── McpServerApplication.java          # Entry point
├── config/
│   └── McpServerConfig.java           # MCP tool registration
├── prompts/
│   └── McpPromptConfig.java           # MCP prompt templates for DB operations
├── tools/
│   ├── QueryTool.java                 # SELECT query execution
│   ├── ExecuteTool.java               # DML execution (INSERT/UPDATE/DELETE)
│   ├── ListDatabasesTool.java         # List databases
│   ├── ListTablesTool.java            # List tables
│   └── DescribeTableTool.java         # Describe table schema
└── service/
    └── MySqlService.java              # Core DB interaction logic
```

### Class Design

#### 1. McpServerApplication.java
- Spring Boot main class
- Bootstraps the MCP server with stdio transport

#### 2. MySqlService.java
```java
// Core service handling all DB interactions
public class MySqlService {
    ResultSet executeQuery(String sql, List<Object> params);
    int executeUpdate(String sql, List<Object> params);
    List<String> listDatabases();
    List<String> listTables(String database);
    List<ColumnInfo> describeTable(String database, String table);
}
```

#### 3. Tool Registration (McpServerConfig.java)
Each tool is registered as a `@Bean` of type `ToolCallback` using Spring AI MCP SDK:

```java
@Bean
ToolCallbackProvider mysqlTools(MySqlService service) {
    return MethodToolCallbackProvider.builder()
        .toolObjects(new QueryTool(service), new ExecuteTool(service), ...)
        .build();
}
```

#### 4. Tool Input/Output Contracts

**query**
- Input: `{ "sql": "SELECT * FROM users WHERE id = ?", "params": [1] }`
- Output: JSON array of row objects

**execute**
- Input: `{ "sql": "INSERT INTO users (name) VALUES (?)", "params": ["Alice"] }`
- Output: `{ "affectedRows": 1 }`

**list_databases**
- Input: `{}`
- Output: `["db1", "db2"]`

**list_tables**
- Input: `{ "database": "mydb" }`
- Output: `["users", "orders"]`

**describe_table**
- Input: `{ "database": "mydb", "table": "users" }`
- Output: `[{ "column": "id", "type": "INT", "key": "PRI", "nullable": false }]`

### Configuration (application.yml)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306
    username: ${MYSQL_USER:root}
    password: ${MYSQL_PASSWORD:}
    hikari:
      maximum-pool-size: 5
  ai:
    mcp:
      server:
        name: mysql-mcp-server
        version: 1.0.0
        transport: stdio
```

### Security Considerations

- Credentials loaded from environment variables, never hardcoded
- Parameterized queries only – prevents SQL injection
- Read-only mode configurable via `MYSQL_READONLY=true` env var
- Connection pool size limited to prevent resource exhaustion

### Sequence Diagram – Query Execution

```
Agent              MCP Server            MySqlService           MySQL
  │                    │                      │                   │
  │─── tools/call ────►│                      │                   │
  │   (tool: query)    │                      │                   │
  │                    │── executeQuery() ───►│                   │
  │                    │                      │── JDBC query ────►│
  │                    │                      │◄── ResultSet ─────│
  │                    │◄── List<Map> ────────│                   │
  │◄── result JSON ────│                      │                   │
```

### Build & Run

```bash
# Build
mvn clean package -DskipTests

# Run (agent connects via stdio)
java -jar target/mysql-mcp-server.jar

# Environment variables
export MYSQL_USER=root
export MYSQL_PASSWORD=secret
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
```

### MCP Client Configuration (claude_desktop_config.json)

```json
{
  "mcpServers": {
    "mysql": {
      "command": "java",
      "args": ["-jar", "/path/to/mysql-mcp-server.jar"],
      "env": {
        "MYSQL_USER": "root",
        "MYSQL_PASSWORD": "secret",
        "MYSQL_HOST": "localhost",
        "MYSQL_PORT": "3306"
      }
    }
  }
}
```

---

## Implementation Workflow

1. **Phase 1** – Setup Spring Boot + MCP SDK dependencies in pom.xml
2. **Phase 2** – Implement DataSource config + MySqlService
3. **Phase 3** – Implement MCP tools (query, execute, list, describe)
4. **Phase 4** – Register tools and wire stdio transport
5. **Phase 5** – Test with MCP inspector / AI agent
