package com.example.demo.services;

import com.example.demo.dto.AIQueryRequest;
import com.example.demo.dto.QueryRequest;
import com.example.demo.dto.QueryResponse;
import java.sql.ResultSetMetaData;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class QueryService {

  @Autowired private JdbcTemplate jdbcTemplate;

  @Value("${gemini.api.key:}")
  private String geminiApiKey;

  @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent}")
  private String geminiApiUrl;

  private static final List<String> ALLOWED_QUERY_TYPES =
      Arrays.asList("SELECT", "INSERT", "UPDATE", "DELETE", "CREATE", "ALTER", "DROP");

  /**
   * Execute a human-written SQL query with validation and safety checks.
   *
   * @param request The query request
   * @return Query execution results
   */
  public QueryResponse executeHumanQuery(QueryRequest request) {
    QueryResponse response = new QueryResponse();
    long startTime = System.currentTimeMillis();

    try {
      String query = request.getQuery();

      // Validate query
      if (query == null || query.trim().isEmpty()) {
        response.setRc("400");
        response.setMessage("Query cannot be empty");
        return response;
      }

      // Security validation
      if (!isQuerySafe(query)) {
        response.setRc("400");
        response.setMessage(
            "Query contains potentially dangerous operations. Please review your query.");
        return response;
      }

      // Determine query type
      String queryType = getQueryType(query);
      response.setQueryType(queryType);
      response.setExecutedQuery(query);

      // Execute based on query type
      if (queryType.equals("SELECT")) {
        executeSelectQuery(query, response);
      } else {
        executeModifyingQuery(query, response);
      }

      response.setRc("200");
      response.setExecutionTimeMs(System.currentTimeMillis() - startTime);

    } catch (DataAccessException e) {
      response.setRc("500");
      response.setMessage("Database error: " + e.getMessage());
    } catch (Exception e) {
      response.setRc("500");
      response.setMessage("Error executing query: " + e.getMessage());
    }

    return response;
  }

  /**
   * Generate SQL query using AI (Google Gemini) from natural language input.
   *
   * @param request The AI query request
   * @return Response with generated SQL
   */
  public QueryResponse generateAIQuery(QueryRequest request) {
    QueryResponse response = new QueryResponse();

    try {
      // Validate input
      if (request.getNaturalLanguageQuery() == null
          || request.getNaturalLanguageQuery().trim().isEmpty()) {
        response.setRc("400");
        response.setMessage("Natural language query cannot be empty");
        return response;
      }

      // Get schema context for the specified table or all tables
      String schemaContext = getSchemaContext(request.getTableName());

      // Build AI request
      AIQueryRequest aiRequest = new AIQueryRequest();
      aiRequest.setUserInput(request.getNaturalLanguageQuery());
      aiRequest.setSchemaContext(schemaContext);
      aiRequest.setTargetTable(request.getTableName());

      // Call Gemini API to generate SQL
      String generatedSql = callGeminiAPI(aiRequest);

      response.setRc("200");
      response.setGeneratedSql(generatedSql);
      response.setMessage("SQL query generated successfully. Please review before executing.");
      response.setQueryType("ai-enhanced");

    } catch (Exception e) {
      response.setRc("500");
      response.setMessage("Error generating AI query: " + e.getMessage());
    }

    return response;
  }

  /**
   * Execute AI-generated query after user verification.
   *
   * @param request The query request with AI-generated SQL
   * @return Query execution results
   */
  public QueryResponse executeAIQuery(QueryRequest request) {
    QueryResponse response = new QueryResponse();
    long startTime = System.currentTimeMillis();

    try {
      String query = request.getQuery(); // This should be the verified AI-generated SQL

      // Validate query
      if (query == null || query.trim().isEmpty()) {
        response.setRc("400");
        response.setMessage("Query cannot be empty");
        return response;
      }

      // Security validation
      if (!isQuerySafe(query)) {
        response.setRc("400");
        response.setMessage("Generated query contains unsafe operations");
        return response;
      }

      // Determine query type
      String queryType = getQueryType(query);
      response.setQueryType("ai-enhanced");
      response.setExecutedQuery(query);

      // Execute based on query type
      if (queryType.equals("SELECT")) {
        executeSelectQuery(query, response);
      } else {
        executeModifyingQuery(query, response);
      }

      response.setRc("200");
      response.setExecutionTimeMs(System.currentTimeMillis() - startTime);

    } catch (Exception e) {
      response.setRc("500");
      response.setMessage("Error executing AI-generated query: " + e.getMessage());
    }

    return response;
  }

  /**
   * Execute a SELECT query and return results.
   */
  private void executeSelectQuery(String query, QueryResponse response) {
    List<Map<String, Object>> results =
        jdbcTemplate.query(
            query,
            (rs, rowNum) -> {
              Map<String, Object> row = new LinkedHashMap<>();
              ResultSetMetaData metaData = rs.getMetaData();
              int columnCount = metaData.getColumnCount();

              for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                Object value = rs.getObject(i);
                row.put(columnName, value);
              }
              return row;
            });

    response.setData(results);

    // Extract column names
    if (results != null && !results.isEmpty()) {
      List<String> columns = new ArrayList<>(results.get(0).keySet());
      response.setColumns(columns);
      response.setMessage(
          String.format("Query executed successfully. Retrieved %d row(s).", results.size()));
    } else {
      response.setColumns(new ArrayList<>());
      response.setMessage("Query executed successfully. No rows returned.");
    }
  }

  /**
   * Execute INSERT, UPDATE, DELETE, or DDL queries.
   */
  private void executeModifyingQuery(String query, QueryResponse response) {
    int rowsAffected = jdbcTemplate.update(query);
    response.setRowsAffected(rowsAffected);
    response.setMessage(
        String.format("Query executed successfully. %d row(s) affected.", rowsAffected));
  }

  /**
   * Get schema context for AI query generation.
   */
  private String getSchemaContext(String tableName) {
    StringBuilder context = new StringBuilder();

    try {
      if (tableName != null && !tableName.isEmpty()) {
        // Get specific table schema
        String tableSchema = getTableSchema(tableName);
        context.append(tableSchema);
      } else {
        // Get all tables schema
        List<String> tables = getAllTableNames();
        for (String table : tables) {
          context.append(getTableSchema(table)).append("\n\n");
        }
      }
    } catch (Exception e) {
      context.append("Schema information not available: ").append(e.getMessage());
    }

    return context.toString();
  }

  /**
   * Get schema for a specific table.
   */
  private String getTableSchema(String tableName) {
    StringBuilder schema = new StringBuilder();
    schema.append("Table: ").append(tableName).append("\n");
    schema.append("Columns:\n");

    String columnQuery =
        "SELECT column_name, data_type, character_maximum_length, is_nullable, column_default "
            + "FROM information_schema.columns "
            + "WHERE table_name = ? AND table_schema = 'public' "
            + "ORDER BY ordinal_position";

    List<Map<String, Object>> columns =
        jdbcTemplate.queryForList(columnQuery, tableName.toLowerCase());

    for (Map<String, Object> col : columns) {
      schema
          .append("  - ")
          .append(col.get("column_name"))
          .append(" (")
          .append(col.get("data_type"));

      if (col.get("character_maximum_length") != null) {
        schema.append("(").append(col.get("character_maximum_length")).append(")");
      }

      schema
          .append(", nullable: ")
          .append(col.get("is_nullable"))
          .append(", default: ")
          .append(col.get("column_default") != null ? col.get("column_default") : "none")
          .append(")\n");
    }

    return schema.toString();
  }

  /**
   * Get all table names in the database.
   */
  private List<String> getAllTableNames() {
    String query =
        "SELECT table_name FROM information_schema.tables "
            + "WHERE table_schema = 'public' AND table_type = 'BASE TABLE'";

    return jdbcTemplate.queryForList(query, String.class);
  }

  /**
   * Call Google Gemini API to generate SQL from natural language.
   */
  private String callGeminiAPI(AIQueryRequest aiRequest) throws Exception {
    if (geminiApiKey == null || geminiApiKey.isEmpty()) {
      throw new IllegalStateException(
          "Gemini API key not configured. Please set gemini.api.key in application.properties");
    }

    RestTemplate restTemplate = new RestTemplate();

    // Build prompt for Gemini
    String prompt = buildGeminiPrompt(aiRequest);

    // Prepare request body for Gemini API
    Map<String, Object> requestBody = new HashMap<>();
    List<Map<String, Object>> contents = new ArrayList<>();
    Map<String, Object> content = new HashMap<>();
    List<Map<String, String>> parts = new ArrayList<>();
    Map<String, String> part = new HashMap<>();
    part.put("text", prompt);
    parts.add(part);
    content.put("parts", parts);
    contents.add(content);
    requestBody.put("contents", contents);

    // Set headers
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    // Make API call
    String url = geminiApiUrl + "?key=" + geminiApiKey;
    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

    ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(url, HttpMethod.POST, entity, 
            new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});

    // Parse response
    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
      return extractSqlFromGeminiResponse(response.getBody());
    } else {
      throw new Exception("Failed to get response from Gemini API");
    }
  }

  /**
   * Build prompt for Gemini API.
   */
  private String buildGeminiPrompt(AIQueryRequest aiRequest) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("You are a SQL query generator for PostgreSQL database.\n\n");
    prompt.append("Database Schema:\n");
    prompt.append(aiRequest.getSchemaContext());
    prompt.append("\n\n");
    prompt.append("User Request: ").append(aiRequest.getUserInput()).append("\n\n");

    if (aiRequest.getTargetTable() != null && !aiRequest.getTargetTable().isEmpty()) {
      prompt.append("Target Table: ").append(aiRequest.getTargetTable()).append("\n\n");
    }

    prompt.append(
        "Instructions:\n"
            + "1. Generate ONLY the SQL query, nothing else.\n"
            + "2. Do not include any explanations, markdown formatting, or code blocks.\n"
            + "3. Use proper PostgreSQL syntax.\n"
            + "4. Use double quotes for identifiers if needed.\n"
            + "5. Return only the raw SQL query that can be executed directly.\n"
            + "6. If the request involves formulas or expressions (like count * price), include them in the SELECT clause.\n\n"
            + "Generate the SQL query:");

    return prompt.toString();
  }

  /**
   * Extract SQL query from Gemini API response.
   */
  @SuppressWarnings("unchecked")
  private String extractSqlFromGeminiResponse(Map<String, Object> responseBody) throws Exception {
    try {
      List<Map<String, Object>> candidates =
          (List<Map<String, Object>>) responseBody.get("candidates");
      if (candidates == null || candidates.isEmpty()) {
        throw new Exception("No candidates in Gemini response");
      }

      Map<String, Object> firstCandidate = candidates.get(0);
      Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
      List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

      if (parts == null || parts.isEmpty()) {
        throw new Exception("No parts in Gemini response");
      }

      String text = (String) parts.get(0).get("text");

      // Clean up the response - remove markdown code blocks if present
      text = text.trim();
      if (text.startsWith("```sql")) {
        text = text.substring(6);
      } else if (text.startsWith("```")) {
        text = text.substring(3);
      }

      if (text.endsWith("```")) {
        text = text.substring(0, text.length() - 3);
      }

      return text.trim();

    } catch (Exception e) {
      throw new Exception("Failed to parse Gemini response: " + e.getMessage());
    }
  }

  /**
   * Validate if query is safe to execute.
   */
  private boolean isQuerySafe(String query) {
    String upperQuery = query.toUpperCase().trim();

    // Block multiple statements
    if (upperQuery.split(";").length > 2) { // Allow one semicolon at the end
      return false;
    }

    // Block dangerous commands
    List<String> dangerousPatterns =
        Arrays.asList(
            "DROP DATABASE",
            "DROP SCHEMA",
            "TRUNCATE DATABASE",
            "GRANT",
            "REVOKE",
            "CREATE USER",
            "DROP USER",
            "ALTER USER");

    for (String pattern : dangerousPatterns) {
      if (upperQuery.contains(pattern)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Determine the type of SQL query.
   */
  private String getQueryType(String query) {
    String upperQuery = query.toUpperCase().trim();

    for (String type : ALLOWED_QUERY_TYPES) {
      if (upperQuery.startsWith(type)) {
        return type;
      }
    }

    return "UNKNOWN";
  }
}
