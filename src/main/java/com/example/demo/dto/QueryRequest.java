package com.example.demo.dto;

import java.util.List;

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
    private String humanQuery;

    /**
     * Natural language input for AI-enhanced query generation.
     * Example: "Show me all users with email containing gmail"
     */
    private String naturalLanguageQuery;

    /**
     * Table name for context (optional, helps AI generate better queries).
     */
    private List<String> tableNames;

    /**
     * Query type: "human" or "ai-enhanced"
     */
    private String queryType;

}