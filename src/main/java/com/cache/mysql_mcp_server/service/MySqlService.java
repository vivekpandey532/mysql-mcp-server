package com.cache.mysql_mcp_server.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service layer for interacting with the MySQL database.
 * Provides methods for executing queries, DML statements, and retrieving metadata.
 */
@Service
public class MySqlService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructs a new MySqlService with the given JdbcTemplate.
     *
     * @param jdbcTemplate the Spring JdbcTemplate for database operations
     */
    public MySqlService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Executes a SELECT SQL query and returns the result as a list of row maps.
     *
     * @param sql the SELECT SQL query to execute
     * @return list of maps where each map represents a row with column names as keys
     */
    public List<Map<String, Object>> executeQuery(String sql) {
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * Executes a DML statement (INSERT, UPDATE, DELETE).
     *
     * @param sql the DML SQL statement to execute
     * @return the number of rows affected
     */
    public int executeUpdate(String sql) {
        return jdbcTemplate.update(sql);
    }

    /**
     * Lists all databases available on the MySQL server.
     *
     * @return list of database names
     */
    public List<String> listDatabases() {
        return jdbcTemplate.queryForList("SHOW DATABASES", String.class);
    }

    /**
     * Lists all tables in the specified database.
     *
     * @param database the name of the database
     * @return list of table names
     */
    public List<String> listTables(String database) {
        return jdbcTemplate.queryForList(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ?",
                String.class, database);
    }

    /**
     * Describes the schema of a table including column names, types, keys, and nullability.
     *
     * @param database the name of the database
     * @param table the name of the table
     * @return list of maps containing column metadata (COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_KEY, COLUMN_DEFAULT, EXTRA)
     */
    public List<Map<String, Object>> describeTable(String database, String table) {
        return jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_KEY, COLUMN_DEFAULT, EXTRA " +
                        "FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?",
                database, table);
    }
}
