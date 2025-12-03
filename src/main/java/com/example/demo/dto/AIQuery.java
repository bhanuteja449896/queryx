package com.example.demo.dto;

import java.util.List;
import lombok.*;

/**
 * DTO that carries the AI prompt definition along with fixed requirements and security guards.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AIQuery {

    private static final List<String> DEFAULT_REQUIREMENTS = List.of(
        "Return a single clean PostgreSQL statement that satisfies the request.",
        "Avoid SELECT *; list explicit column names.",
        "Limit the result set (LIMIT clause) when no filtering is provided.",
        "Use PostgreSQL built-in functions when they make the query clearer (e.g., ILIKE for case-insensitive search).",
        "Favor indexed columns whenever they exist to maintain performance.");

    private static final List<String> DEFAULT_SECURITY = List.of(
        "Never inline user-provided values; treat them as placeholders/parameters.",
        "Block multiple statements separated by semicolons or SQL comments that could enable injection.",
        "Disallow access to pg_catalog, information_schema, or other system tables unless explicitly requested.",
        "Reject requests that ask for DROP, TRUNCATE, or DDL statements.",
        "Ensure string literals are wrapped in single quotes with proper escaping.");

    private String userInput;
    private List<String> tableNames;

    @Builder.Default
    private String databaseType = "postgresql";

    @Builder.Default
    private String expectedReturnType = "A single, executable PostgreSQL query that fulfills the request.";

    @Builder.Default
    private List<String> requirements = DEFAULT_REQUIREMENTS;

    @Builder.Default
    private List<String> securityGuards = DEFAULT_SECURITY;

    public static AIQuery withDefaults(String userInput, List<String> tableNames) {
        return AIQuery.builder()
            .userInput(userInput)
            .tableNames(tableNames)
            .build();
    }
}
