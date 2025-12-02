package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for structuring the input request to Google Gemini API for SQL query generation.
 * This class encapsulates all necessary information for the AI to generate safe,
 * valid PostgreSQL queries based on natural language input.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeminiQueryRequest {

    /**
     * Natural language user input describing what query to generate.
     * Example: "Get all users with gmail email addresses"
     */
    private String userInput;

    /**
     * Database type - specifies which SQL dialect to use.
     * For this application: "postgresql"
     */
    private String databaseType;

    /**
     * Complete table schemas with column information.
     * Map structure: tableName -> columns -> columnDetails
     * Example: {"users": {"columns": {"id": {data_type: "integer"}, "email": {data_type: "varchar"}}}}
     */
    private Map<String, Map<String, Map<String, Object>>> tableSchemas;

    /**
     * Security and generation rules for the AI model.
     * These rules ensure the generated query is safe and follows best practices.
     */
    private QueryGenerationRules rules;

    /**
     * Inner class defining strict rules for SQL query generation.
     * These rules are sent to Gemini to ensure safe, valid SQL generation.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QueryGenerationRules {

        /**
         * Maximum number of rows to return (for SELECT queries).
         * Default: 1000
         */
        private Integer maxRows;

        /**
         * Whether to allow DELETE operations.
         * Default: false for safety
         */
        private boolean allowDelete;

        /**
         * Whether to allow UPDATE operations.
         * Default: false for safety
         */
        private boolean allowUpdate;

        /**
         * Whether to allow DROP/TRUNCATE operations.
         * Default: false for safety
         */
        private boolean allowDropOrTruncate;

        /**
         * Whether to allow CREATE operations.
         * Default: false for safety
         */
        private boolean allowCreate;

        /**
         * List of security rules and constraints for query generation.
         * These are sent as instructions to the AI model.
         */
        private SecurityConstraints securityConstraints;
    }

    /**
     * Security constraints to prevent SQL injection and ensure safe query generation.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SecurityConstraints {

        /**
         * Strict SQL injection prevention rules.
         */
        private String sqlInjectionProtection;

        /**
         * Query formatting guidelines.
         */
        private String formattingRules;

        /**
         * Output format requirements.
         */
        private String outputRequirements;

        /**
         * Additional constraints for query generation.
         */
        private String additionalConstraints;

        /**
         * Get default security constraints for PostgreSQL query generation.
         */
        public static SecurityConstraints getDefault() {
            SecurityConstraints constraints = new SecurityConstraints();

            constraints.setSqlInjectionProtection(
                "CRITICAL SECURITY RULES:\n" +
                "1. NEVER include user input directly in queries without proper escaping\n" +
                "2. Use parameterized queries format where user values are replaced with placeholders\n" +
                "3. Reject any input containing: semicolons (;), multiple statements, or SQL comments (-- or /* */)\n" +
                "4. Do not allow UNION, EXEC, EXECUTE, or system procedure calls\n" +
                "5. Validate all table and column names against the provided schema\n" +
                "6. Do not allow dynamic SQL generation or nested queries from user input\n" +
                "7. Block any attempt to access system tables or information_schema beyond what's provided\n" +
                "8. Ensure all string literals are properly quoted using single quotes\n" +
                "9. Do not allow hexadecimal or encoded values that could hide malicious code\n"
            );

            constraints.setFormattingRules(
                "QUERY FORMATTING RULES:\n" +
                "1. Use proper PostgreSQL syntax and conventions\n" +
                "2. Use double quotes for table/column identifiers only when necessary (e.g., mixed case or reserved words)\n" +
                "3. Use single quotes for string literals\n" +
                "4. Write clean, readable SQL with proper indentation\n" +
                "5. Use explicit column names instead of SELECT *\n" +
                "6. Include ORDER BY clause when appropriate\n" +
                "7. Use proper JOIN syntax (INNER JOIN, LEFT JOIN, etc.)\n" +
                "8. Add LIMIT clause to prevent excessive data retrieval\n" +
                "9. Use standard PostgreSQL data types and functions\n" +
                "10. Optimize for performance (use indexes when available)"
            );

            constraints.setOutputRequirements(
                "OUTPUT REQUIREMENTS:\n" +
                "1. Return ONLY the raw SQL query - no explanations, markdown, or code blocks\n" +
                "2. Do not include triple backticks (```) or language identifiers (sql)\n" +
                "3. Do not add comments or annotations in the output\n" +
                "4. Do not include multiple statements separated by semicolons\n" +
                "5. Return a single, executable PostgreSQL query\n" +
                "6. Ensure the query ends without a trailing semicolon\n" +
                "7. Do not include any text before or after the query\n" +
                "8. The query must be immediately executable as-is"
            );

            constraints.setAdditionalConstraints(
                "ADDITIONAL CONSTRAINTS:\n" +
                "1. Only use tables and columns present in the provided schema\n" +
                "2. Respect data types and constraints from the schema\n" +
                "3. Handle NULL values appropriately\n" +
                "4. Use PostgreSQL-specific functions when appropriate (e.g., ILIKE for case-insensitive search)\n" +
                "5. Consider using appropriate indexes for WHERE clauses\n" +
                "6. Validate date/time formats for PostgreSQL\n" +
                "7. Use proper casting when comparing different data types\n" +
                "8. Handle special characters in string literals correctly\n" +
                "9. Consider query performance implications\n" +
                "10. Return meaningful error if the request cannot be fulfilled safely"
            );

            return constraints;
        }
    }

    /**
     * Build a formatted prompt string for the Gemini API.
     * This method creates the complete prompt including schema, rules, and user input.
     */
    public String buildPrompt() {
        StringBuilder prompt = new StringBuilder();

        // Header
        prompt.append("=== SQL QUERY GENERATOR FOR POSTGRESQL ===\n\n");

        // Database Type
        prompt.append("DATABASE TYPE: ").append(databaseType != null ? databaseType.toUpperCase() : "POSTGRESQL").append("\n\n");

        // Schema Information
        prompt.append("=== DATABASE SCHEMA ===\n");
        if (tableSchemas != null && !tableSchemas.isEmpty()) {
            for (Map.Entry<String, Map<String, Map<String, Object>>> tableEntry : tableSchemas.entrySet()) {
                String tableName = tableEntry.getKey();
                Map<String, Map<String, Object>> tableInfo = tableEntry.getValue();
                
                prompt.append("\nTable: ").append(tableName).append("\n");
                prompt.append("Columns:\n");

                if (tableInfo.containsKey("columns")) {
                    Map<String, Object> columns = (Map<String, Object>) tableInfo.get("columns");
                    for (Map.Entry<String, Object> colEntry : columns.entrySet()) {
                        String colName = colEntry.getKey();
                        Map<String, Object> colDetails = (Map<String, Object>) colEntry.getValue();
                        
                        prompt.append("  - ").append(colName);
                        if (colDetails.containsKey("data_type")) {
                            prompt.append(" (").append(colDetails.get("data_type"));
                            if (colDetails.containsKey("character_maximum_length") && 
                                colDetails.get("character_maximum_length") != null) {
                                prompt.append("(").append(colDetails.get("character_maximum_length")).append(")");
                            }
                            prompt.append(")");
                        }
                        if (colDetails.containsKey("is_nullable")) {
                            String nullable = colDetails.get("is_nullable").toString();
                            if ("NO".equalsIgnoreCase(nullable)) {
                                prompt.append(" NOT NULL");
                            }
                        }
                        prompt.append("\n");
                    }
                }
            }
        }
        prompt.append("\n");

        // Security Constraints
        if (rules != null && rules.getSecurityConstraints() != null) {
            SecurityConstraints constraints = rules.getSecurityConstraints();
            
            prompt.append("=== SECURITY AND INJECTION PROTECTION ===\n");
            prompt.append(constraints.getSqlInjectionProtection()).append("\n\n");

            prompt.append("=== FORMATTING RULES ===\n");
            prompt.append(constraints.getFormattingRules()).append("\n\n");

            prompt.append("=== OUTPUT REQUIREMENTS ===\n");
            prompt.append(constraints.getOutputRequirements()).append("\n\n");

            prompt.append("=== ADDITIONAL CONSTRAINTS ===\n");
            prompt.append(constraints.getAdditionalConstraints()).append("\n\n");
        }

        // Query Type Restrictions
        if (rules != null) {
            prompt.append("=== ALLOWED OPERATIONS ===\n");
            prompt.append("SELECT queries: ALWAYS ALLOWED\n");
            prompt.append("INSERT queries: ALLOWED\n");
            prompt.append("UPDATE queries: ").append(rules.isAllowUpdate() ? "ALLOWED" : "NOT ALLOWED").append("\n");
            prompt.append("DELETE queries: ").append(rules.isAllowDelete() ? "ALLOWED" : "NOT ALLOWED").append("\n");
            prompt.append("CREATE/ALTER queries: ").append(rules.isAllowCreate() ? "ALLOWED" : "NOT ALLOWED").append("\n");
            prompt.append("DROP/TRUNCATE queries: ").append(rules.isAllowDropOrTruncate() ? "ALLOWED" : "NOT ALLOWED").append("\n");
            
            if (rules.getMaxRows() != null && rules.getMaxRows() > 0) {
                prompt.append("Maximum rows for SELECT: ").append(rules.getMaxRows()).append("\n");
            }
            prompt.append("\n");
        }

        // User Request
        prompt.append("=== USER REQUEST ===\n");
        prompt.append(userInput).append("\n\n");

        // Final instruction
        prompt.append("=== INSTRUCTION ===\n");
        prompt.append("Generate a single, safe, executable PostgreSQL query that fulfills the user request.\n");
        prompt.append("Follow ALL security rules and constraints above.\n");
        prompt.append("Return ONLY the SQL query with no additional text, explanations, or formatting.\n");

        return prompt.toString();
    }

    /**
     * Get a default GeminiQueryRequest with standard security settings.
     */
    public static GeminiQueryRequest createDefault(String userInput, 
                                                     Map<String, Map<String, Map<String, Object>>> tableSchemas) {
        QueryGenerationRules rules = QueryGenerationRules.builder()
            .maxRows(1000)
            .allowDelete(false)
            .allowUpdate(false)
            .allowDropOrTruncate(false)
            .allowCreate(false)
            .securityConstraints(SecurityConstraints.getDefault())
            .build();

        return GeminiQueryRequest.builder()
            .userInput(userInput)
            .databaseType("postgresql")
            .tableSchemas(tableSchemas)
            .rules(rules)
            .build();
    }

}
