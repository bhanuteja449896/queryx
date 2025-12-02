package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for handling query requests from the frontend.
 * Supports both human-written SQL queries and natural language inputs for AI enhancement.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryRequest {

    /**
     * The SQL query to execute (for human queries mode).
     */
    private String query;

    /**
     * Natural language input for AI-enhanced query generation.
     * Example: "Show me all users with email containing gmail"
     */
    private String naturalLanguageQuery;

    /**
     * Table name for context (optional, helps AI generate better queries).
     */
    private String tableName;

    /**
     * Query type: "human" or "ai-enhanced"
     */
    private String queryType;

    /**
     * Flag to indicate if this is just generating the AI query (not executing it).
     * If true, returns the generated SQL for user verification.
     */
    private boolean generateOnly;
}
