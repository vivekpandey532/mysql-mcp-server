package com.cache.mysql_mcp_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the MySQL MCP Server application.
 * This Spring Boot application exposes MySQL database operations as MCP tools
 * that can be consumed by AI clients (Amazon Q, Claude, etc.) via the Model Context Protocol.
 */
@SpringBootApplication
public class McpServerApplication {

    /**
     * Starts the Spring Boot application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }
}
