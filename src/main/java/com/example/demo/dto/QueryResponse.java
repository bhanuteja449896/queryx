package com.example.demo.dto;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for query execution results.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryResponse {

    /**
     * Response code: "200" for success, "400" for validation error, "500" for server error
     */
    private String rc;

    /**
     * Response message
     */
    private String message;

    /**
     * The generated or executed SQL query
     */
    private String executedQuery;

    /**
     * Query results (list of rows, where each row is a map of column name to value)
     */
    private List<Map<String, Object>> data;

    /**
     * Column names in the result set
     */
    private List<String> columns;

    /**
     * Number of rows affected (for INSERT, UPDATE, DELETE)
     */
    private Integer rowsAffected;

    /**
     * Query execution time in milliseconds
     */
    private Long executionTimeMs;

    /**
     * For AI-enhanced queries: the generated SQL before execution
     */
    private String generatedSql;

    /**
     * Query type executed
     */
    private String queryType;
}
