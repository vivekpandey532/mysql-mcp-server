# MySQL MCP Server - Client Setup Guide

## Prerequisites

- MySQL MCP Server built successfully (`target/mysql-mcp-server-1.0-SNAPSHOT.jar`)
- MySQL server running and accessible
- Java 21+ installed

---

## Option 1: Claude Desktop

### Step 1: Locate Config File

```bash
# macOS
~/Library/Application Support/Claude/claude_desktop_config.json

# Windows
%APPDATA%\Claude\claude_desktop_config.json

# Linux
~/.config/Claude/claude_desktop_config.json
```

If the file doesn't exist, create it.

### Step 2: Add MCP Server Config

```json
{
  "mcpServers": {
    "mysql": {
      "command": "java",
      "args": [
        "-jar",
        "/Users/vivekpandey/Documents/Project/twdc/Mysql_mcp_server/target/mysql-mcp-server-1.0-SNAPSHOT.jar"
      ],
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

### Step 3: Restart Claude Desktop

- Quit Claude Desktop completely (Cmd+Q on macOS)
- Reopen Claude Desktop
- You should see a 🔨 (hammer) icon in the chat input area indicating tools are available

### Step 4: Verify

Type in Claude: "List all my databases" — Claude will call the `listDatabases` tool and show results.

---

## Option 2: VS Code (GitHub Copilot / Amazon Q)

### Step 1: Create MCP Config in Workspace

Create `.vscode/mcp.json` in your project root:

```bash
mkdir -p .vscode
```

### Step 2: Add Server Configuration

`.vscode/mcp.json`:
```json
{
  "servers": {
    "mysql": {
      "command": "java",
      "args": [
        "-jar",
        "/Users/vivekpandey/Documents/Project/twdc/Mysql_mcp_server/target/mysql-mcp-server-1.0-SNAPSHOT.jar"
      ],
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

### Step 3: Enable MCP in VS Code

1. Open VS Code Settings (Cmd+,)
2. Search for "mcp"
3. Ensure `chat.mcp.enabled` is set to `true`

### Step 4: Verify

- Open the Command Palette (Cmd+Shift+P)
- Run: `MCP: List Servers`
- You should see "mysql" listed with status "running"
- In Copilot Chat, use agent mode and ask: "List tables in my database"

---

## Option 3: MCP Inspector (CLI Testing Tool)

The MCP Inspector is a web-based debugging tool that lets you interact with any MCP server directly — perfect for testing and development.

### Step 1: Install Node.js (if not installed)

```bash
# Check if Node.js is installed
node --version

# If not installed, use Homebrew on macOS
brew install node
```

### Step 2: Run MCP Inspector

```bash
# Set MySQL environment variables first
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_USER=root
export MYSQL_PASSWORD=yourpassword

# Launch inspector pointing to your MCP server
npx @modelcontextprotocol/inspector java -jar /Users/vivekpandey/Documents/Project/twdc/Mysql_mcp_server/target/mysql-mcp-server-1.0-SNAPSHOT.jar
```

### Step 3: Open Inspector UI

After running the command, you'll see output like:

```
MCP Inspector is up and running at http://localhost:5173
```

Open `http://localhost:5173` in your browser.

### Step 4: Explore the Inspector UI

The Inspector UI has several tabs:

#### Server Info Tab
- Shows server name: `mysql-mcp-server`
- Shows version: `1.0.0`
- Lists all capabilities (tools, resources, prompts)

#### Tools Tab
Lists all 5 registered tools:
- `query`
- `execute`
- `listDatabases`
- `listTables`
- `describeTable`

Click on any tool to see its input schema.

### Step 5: Test Each Tool

#### Test `listDatabases`:
1. Click on `listDatabases` tool
2. No parameters needed
3. Click "Run"
4. See output: `["information_schema", "mysql", "mydb", ...]`

#### Test `listTables`:
1. Click on `listTables` tool
2. Enter parameter: `database` = `mydb`
3. Click "Run"
4. See output: `["users", "orders", ...]`

#### Test `describeTable`:
1. Click on `describeTable` tool
2. Enter parameters:
   - `database` = `mydb`
   - `table` = `users`
3. Click "Run"
4. See column details with types, keys, nullability

#### Test `query`:
1. Click on `query` tool
2. Enter parameter: `sql` = `SELECT * FROM mydb.users LIMIT 5`
3. Click "Run"
4. See JSON array of row results

#### Test `execute`:
1. Click on `execute` tool
2. Enter parameter: `sql` = `INSERT INTO mydb.users (name) VALUES ('TestUser')`
3. Click "Run"
4. See output: `{"affectedRows": 1}`

### Step 6: View Raw JSON-RPC Messages

The Inspector also shows the raw JSON-RPC messages exchanged:

**Request:**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/call",
  "params": {
    "name": "listDatabases",
    "arguments": {}
  }
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "[\"information_schema\",\"mysql\",\"mydb\"]"
      }
    ]
  }
}
```

### Step 7: Debugging Tips

- If the server fails to start, check the Inspector's "stderr" panel for error logs
- If tools return errors, verify MySQL credentials in environment variables
- Use the "Notifications" tab to see server-sent notifications
- The "Ping" button verifies the server is responsive

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Server won't start | Check `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_USER`, `MYSQL_PASSWORD` env vars |
| Connection refused | Ensure MySQL is running: `mysql -u root -p -e "SELECT 1"` |
| Tools not showing | Restart the client (Claude/VS Code) after config changes |
| Inspector won't launch | Ensure Node.js 18+ is installed: `node --version` |
| Permission denied | Check MySQL user has required privileges |

---

## Quick Verification Script

Run this to verify everything is working end-to-end:

```bash
# 1. Check MySQL is accessible
mysql -h localhost -u root -p -e "SHOW DATABASES;"

# 2. Check Java version
java --version

# 3. Check JAR exists
ls -la target/mysql-mcp-server-1.0-SNAPSHOT.jar

# 4. Test with Inspector
export MYSQL_HOST=localhost MYSQL_PORT=3306 MYSQL_USER=root MYSQL_PASSWORD=yourpassword
npx @modelcontextprotocol/inspector java -jar target/mysql-mcp-server-1.0-SNAPSHOT.jar
```
