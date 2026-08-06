# MySQL MCP Server

A Model Context Protocol (MCP) server that exposes MySQL database operations as tools, allowing AI agents (Claude, Amazon Q, etc.) to interact with MySQL databases through a standardized protocol. Communication happens via JSON-RPC messages over stdio.

> For architecture and design details, see [DESIGN.md](./DESIGN.md).

---

## Prerequisites

| Dependency | Version | Purpose |
|------------|---------|--------|
| Java (JDK) | 21+ | Run the MCP server |
| Maven | 3.8+ | Build the project |
| MySQL | 8.0+ | Database |
| Node.js | 18+ | Run MCP Inspector |

### Install Java (JDK 21+)

**Windows:**
```cmd
winget install Microsoft.OpenJDK.21
```
Set JAVA_HOME if not set automatically:
```cmd
setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-21"
setx PATH "%JAVA_HOME%\bin;%PATH%"
```

**macOS:**
```bash
brew install --cask temurin@21
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update && sudo apt install -y openjdk-21-jdk
echo 'export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc
```

**Linux (RHEL/CentOS/Fedora):**
```bash
sudo dnf install -y java-21-openjdk-devel
```

### Install Maven (3.8+)

**Windows:**
```cmd
winget install Apache.Maven
```

**macOS:**
```bash
brew install maven
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update && sudo apt install -y maven
```

**Linux (RHEL/CentOS/Fedora):**
```bash
sudo dnf install -y maven
```

### Install MySQL (8.0+)

**Windows:**
```cmd
winget install Oracle.MySQL
net start MySQL80
```

**macOS:**
```bash
brew install mysql
brew services start mysql
mysql_secure_installation
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update && sudo apt install -y mysql-server
sudo systemctl start mysql && sudo systemctl enable mysql
sudo mysql_secure_installation
```

**Linux (RHEL/CentOS/Fedora):**
```bash
sudo dnf install -y mysql-server
sudo systemctl start mysqld && sudo systemctl enable mysqld
sudo grep 'temporary password' /var/log/mysqld.log
sudo mysql_secure_installation
```

### Install Node.js (18+ — for MCP Inspector only)

**Windows:**
```cmd
winget install OpenJS.NodeJS.LTS
```

**macOS:**
```bash
brew install node
```

**Linux (Ubuntu/Debian):**
```bash
curl -fsSL https://deb.nodesource.com/setup_lts.x | sudo -E bash -
sudo apt install -y nodejs
```

**All platforms — using nvm:**
```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
source ~/.bashrc
nvm install --lts && nvm use --lts
```

### Verify All Dependencies

```bash
java --version
mvn --version
mysql --version
node --version
```

---

## Build

Clone and build:

```cmd
git clone <repo-url>
cd Mysql_mcp_server
mvn clean package -DskipTests
```

---

## How It Works

### Startup Flow

```
1. Spring Boot starts McpServerApplication
2. DataSource (HikariCP) connects to MySQL using env variables
3. Annotation scanner discovers @McpTool, @McpResource, and @McpPrompt annotated beans
4. MCP Server starts listening on stdio for JSON-RPC messages
```

### Communication Flow

```
AI Agent (Claude/Q)          MCP Server (this app)              MySQL
      │                              │                              │
      │── initialize ───────────────►│                              │
      │◄── server info + capabilities│                              │
      │                              │                              │
      │── tools/call (query) ────────►│                              │
      │                              │── JDBC executeQuery() ──────►│
      │                              │◄── ResultSet ────────────────│
      │◄── JSON result ──────────────│                              │
      │                              │                              │
      │── resources/read ────────────►│                              │
      │                              │── INFORMATION_SCHEMA ───────►│
      │                              │◄── metadata ─────────────────│
      │◄── resource content ─────────│                              │
      │                              │                              │
      │── prompts/get ───────────────►│                              │
      │◄── prompt template ──────────│                              │
```

### Key Classes

| Class | Role |
|-------|------|
| `McpServerApplication` | Spring Boot entry point |
| `MySqlService` | Executes SQL via `JdbcTemplate` |
| `QueryTool` | `@McpTool` — handles SELECT queries |
| `ExecuteTool` | `@McpTool` — handles DML statements |
| `ListDatabasesTool` | `@McpTool` — lists databases |
| `ListTablesTool` | `@McpTool` — lists tables |
| `DescribeTableTool` | `@McpTool` — describes table schema |
| `MySqlResourceProvider` | `@McpResource` — exposes DB metadata as resources |
| `MySqlPromptProvider` | `@McpPrompt` — exposes prompt templates |

---

## Available Tools

| Tool | Description |
|------|-------------|
| `query` | Execute SELECT queries and return results |
| `execute` | Execute INSERT/UPDATE/DELETE statements |
| `listDatabases` | List all databases on the server |
| `listTables` | List tables in a specific database |
| `describeTable` | Get table schema (columns, types, keys) |

---

## Available Resources

| Resource | Description |
|----------|-------------|
| `mysql://databases` | Lists all databases on the server |
| `mysql://tables/{database}` | Lists all tables in the given database |
| `mysql://schema/{database}/{table}` | Returns full schema of a specific table |

---

## Available Prompts

| Prompt | Arguments | Description |
|--------|-----------|-------------|
| `write-query` | `database`, `table`, `goal` | Generates a SQL SELECT query for a given goal |
| `analyze-table` | `database`, `table` | Analyzes structure and suggests improvements |
| `explain-query-results` | `sql`, `results` | Explains query results in plain language |

---

## Client Setup

### Option 1: Claude Desktop

Locate or create the config file:

- **Windows:** `%APPDATA%\Claude\claude_desktop_config.json`
- **macOS:** `~/Library/Application Support/Claude/claude_desktop_config.json`
- **Linux:** `~/.config/Claude/claude_desktop_config.json`

**Windows:**
```json
{
  "mcpServers": {
    "mysql": {
      "command": "java",
      "args": ["-jar", "C:\\path\\to\\Mysql_mcp_server\\target\\mysql-mcp-server-1.0-SNAPSHOT.jar"],
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

**macOS/Linux:**
```json
{
  "mcpServers": {
    "mysql": {
      "command": "java",
      "args": ["-jar", "/path/to/Mysql_mcp_server/target/mysql-mcp-server-1.0-SNAPSHOT.jar"],
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

Restart Claude Desktop. You should see a 🔨 icon in the chat input area indicating tools are available.

---

### Option 2: Amazon Q Developer (VS Code)

Create `.aws/amazonq/mcp.json`:

**Windows:**
```json
{
  "mcpServers": {
    "mysql": {
      "command": "java",
      "args": ["-jar", "C:\\path\\to\\Mysql_mcp_server\\target\\mysql-mcp-server-1.0-SNAPSHOT.jar"],
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

**macOS/Linux:**
```json
{
  "mcpServers": {
    "mysql": {
      "command": "java",
      "args": ["-jar", "/path/to/Mysql_mcp_server/target/mysql-mcp-server-1.0-SNAPSHOT.jar"],
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

Restart Amazon Q. Then ask naturally: *"List all my databases"*

---

### Option 3: MCP Inspector

**Windows (Command Prompt):**
```cmd
set MYSQL_HOST=localhost
set MYSQL_PORT=3306
set MYSQL_USER=root
set MYSQL_PASSWORD=yourpassword

npx @modelcontextprotocol/inspector@0.14.3 java -jar target\mysql-mcp-server-1.0-SNAPSHOT.jar
```

**Windows (PowerShell):**
```powershell
$env:MYSQL_HOST="localhost"
$env:MYSQL_PORT="3306"
$env:MYSQL_USER="root"
$env:MYSQL_PASSWORD="yourpassword"

npx @modelcontextprotocol/inspector@0.14.3 java -jar target\mysql-mcp-server-1.0-SNAPSHOT.jar
```

**macOS/Linux:**
```bash
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_USER=root
export MYSQL_PASSWORD=yourpassword

npx @modelcontextprotocol/inspector@0.14.3 java -jar target/mysql-mcp-server-1.0-SNAPSHOT.jar
```

Open the URL printed in the terminal (includes auth token) in your browser.

---

## Example Agent Interaction

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

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Server won't start | Check `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_USER`, `MYSQL_PASSWORD` env vars |
| Connection refused | Ensure MySQL is running: `net start MySQL80` (Windows) / `brew services start mysql` (macOS) / `sudo systemctl start mysql` (Linux) |
| Tools not showing | Restart the client (Claude/VS Code/Amazon Q) after config changes |
| Inspector won't launch | Ensure Node.js 18+ is installed: `node --version` |
| Permission denied | Check MySQL user has required privileges |
| Windows path errors | Use double backslashes `\\` or forward slashes `/` in JSON config |
| `java` not found | Ensure JAVA_HOME is set and `bin` is in PATH |
| `mvn` not found | Ensure MAVEN_HOME is set and `bin` is in PATH |
| `node` not found | Restart terminal after installation or re-source shell profile |
| Build fails | Ensure Java 21+ is active: `java --version` |
