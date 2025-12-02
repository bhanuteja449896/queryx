package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for AI query generation request to Google Gemini API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIQueryRequest {

    /**
     * Natural language description of what the user wants to query
     */
    private String userInput;

    /**
     * Database schema context (table structures) to help AI generate accurate queries
     */
    private String schemaContext;

    /**
     * Optional: Specific table name the query should target
     */
    private String targetTable;
}
