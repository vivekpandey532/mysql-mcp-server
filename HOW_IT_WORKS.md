# MySQL MCP Server - How It Works

## What is MCP?

Model Context Protocol (MCP) is an open standard that allows AI agents (Claude, Amazon Q, etc.) to interact with external tools and data sources through a unified protocol. The communication happens via JSON-RPC messages over stdio (standard input/output).

---

## How This Server Works

### 1. Startup Flow

```
┌──────────────────────────────────────────────────────────────────┐
│                        Application Startup                         │
├──────────────────────────────────────────────────────────────────┤
│                                                                    │
│  1. Spring Boot starts McpServerApplication                       │
│  2. DataSource (HikariCP) connects to MySQL using env variables   │
│  3. McpServerConfig registers all tool beans with MCP framework   │
│  4. MCP Server starts listening on stdio for JSON-RPC messages    │
│                                                                    │
└──────────────────────────────────────────────────────────────────┘
```

### 2. Communication Flow

```
AI Agent (Claude/Q)                MCP Server (this app)                MySQL
      │                                    │                              │
      │── initialize ─────────────────────►│                              │
      │◄── server info + tool list ────────│                              │
      │                                    │                              │
      │── tools/call (query) ─────────────►│                              │
      │                                    │── JDBC executeQuery() ──────►│
      │                                    │◄── ResultSet ────────────────│
      │◄── JSON result ───────────────────│                              │
      │                                    │                              │
      │── tools/call (list_tables) ───────►│                              │
      │                                    │── SHOW TABLES ──────────────►│
      │                                    │◄── table names ──────────────│
      │◄── JSON array ────────────────────│                              │
```

### 3. MCP Protocol Messages

**Agent sends a tool call:**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/call",
  "params": {
    "name": "query",
    "arguments": {
      "sql": "SELECT * FROM users LIMIT 5"
    }
  }
}
```

**Server responds:**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "[{\"id\":1,\"name\":\"Alice\"},{\"id\":2,\"name\":\"Bob\"}]"
      }
    ]
  }
}
```

---

## Available Tools

### `query`
Executes a SELECT statement and returns results as JSON array.

```
Input:  { "sql": "SELECT id, name FROM users WHERE active = 1" }
Output: [{"id": 1, "name": "Alice"}, {"id": 2, "name": "Bob"}]
```

### `execute`
Executes DML statements (INSERT, UPDATE, DELETE, CREATE, DROP).

```
Input:  { "sql": "INSERT INTO users (name) VALUES ('Charlie')" }
Output: {"affectedRows": 1}
```

### `listDatabases`
Lists all databases on the MySQL server.

```
Input:  (none)
Output: ["information_schema", "mydb", "testdb"]
```

### `listTables`
Lists all tables in a specific database.

```
Input:  { "database": "mydb" }
Output: ["users", "orders", "products"]
```

### `describeTable`
Returns column details for a table.

```
Input:  { "database": "mydb", "table": "users" }
Output: [
  {"COLUMN_NAME": "id", "COLUMN_TYPE": "int", "IS_NULLABLE": "NO", "COLUMN_KEY": "PRI"},
  {"COLUMN_NAME": "name", "COLUMN_TYPE": "varchar(255)", "IS_NULLABLE": "YES", "COLUMN_KEY": ""}
]
```

---

## How Spring AI MCP Wires Everything

```
@Tool annotation on methods
        │
        ▼
MethodToolCallbackProvider scans annotated methods
        │
        ▼
Registers them as MCP tools with name + description + input schema
        │
        ▼
MCP Server (stdio transport) exposes tools to connecting agents
        │
        ▼
When agent calls a tool → framework deserializes args → invokes method → returns result
```

### Key Classes

| Class | Role |
|-------|------|
| `McpServerApplication` | Spring Boot entry point |
| `McpServerConfig` | Registers tool beans with `MethodToolCallbackProvider` |
| `MySqlService` | Executes SQL via `JdbcTemplate` |
| `QueryTool` | `@Tool` annotated - handles SELECT |
| `ExecuteTool` | `@Tool` annotated - handles DML/DDL |
| `ListDatabasesTool` | `@Tool` annotated - SHOW DATABASES |
| `ListTablesTool` | `@Tool` annotated - lists tables |
| `DescribeTableTool` | `@Tool` annotated - INFORMATION_SCHEMA query |

---

## Setup & Run

### Prerequisites
- Java 21+
- Maven 3.8+
- MySQL server running

### Build
```bash
mvn clean package -DskipTests
```

### Run
```bash
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_USER=root
export MYSQL_PASSWORD=yourpassword

java -jar target/mysql-mcp-server-1.0-SNAPSHOT.jar
```

### Configure AI Agent

Add to your MCP client config (e.g. `claude_desktop_config.json` or VS Code MCP settings):

```json
{
  "mcpServers": {
    "mysql": {
      "command": "java",
      "args": ["-jar", "/absolute/path/to/mysql-mcp-server-1.0-SNAPSHOT.jar"],
      "env": {
        "MYSQL_HOST": "localhost",
        "MYSQL_PORT": "3306",
        "MYSQL_USER": "root",
        "MYSQL_PASSWORD": "yourpassword"
      }
    }
  }
}
```

---

## MCP Prompts

Prompts are pre-built templates that guide the AI agent to perform common DB operations. When a user selects a prompt in the client UI, it generates a structured message that the agent uses to call the right tools.

### Available Prompts

| Prompt | Description | Required Arguments |
|--------|-------------|-------------------|
| `query_data` | SELECT data from a table | database, table |
| `insert_data` | INSERT rows into a table | database, table, data (JSON) |
| `update_data` | UPDATE existing rows | database, table, set_values, condition |
| `delete_data` | DELETE rows from a table | database, table, condition |
| `create_table` | CREATE a new table | database, table, schema_description |
| `analyze_table` | Analyze table structure + sample data | database, table |

### How Prompts Work

```
User selects prompt "query_data" in Claude/VS Code
        │
        ▼
Client shows form: database=?, table=?, condition=?, columns=?, limit=?
        │
        ▼
User fills: database="mydb", table="users", condition="age > 25"
        │
        ▼
MCP Server generates message:
  "Execute this SQL query using the `query` tool:
   SELECT * FROM mydb.users WHERE age > 25 LIMIT 50
   Return the results in a readable table format."
        │
        ▼
AI Agent receives the message → calls `query` tool → returns results
```

### Prompt Usage Examples

#### In Claude Desktop:
1. Click the 📎 (attachment) icon → select "Prompts"
2. Choose `query_data`
3. Fill in: database = `mydb`, table = `orders`, condition = `status = 'pending'`
4. Claude generates and executes the query automatically

#### In VS Code:
1. Open Copilot Chat in agent mode
2. Type `/` to see available prompts
3. Select `insert_data`
4. Fill in: database = `mydb`, table = `users`, data = `{"name":"Alice","email":"alice@example.com"}`
5. Agent validates schema and executes the insert

#### In MCP Inspector:
1. Go to the **Prompts** tab
2. Click on any prompt (e.g. `analyze_table`)
3. Fill arguments: database = `mydb`, table = `users`
4. Click "Get Prompt" to see the generated message
5. Copy the message to test with the tools

---

## Example Agent Interaction

Once connected, an AI agent can naturally use the database:

> **User:** "Show me all tables in the orders database"
>
> **Agent** calls `listTables(database: "orders")` → gets `["customers", "orders", "products"]`
>
> **User:** "What columns does the customers table have?"
>
> **Agent** calls `describeTable(database: "orders", table: "customers")` → gets schema details
>
> **User:** "Find all customers from New York"
>
> **Agent** calls `query(sql: "SELECT * FROM orders.customers WHERE city = 'New York'")` → returns results
