package com.example.demo.services;

import com.example.demo.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class QueryServices {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SchemaService schemaService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent}")
    private String geminiApiUrl;

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    public AIQuery createAIQuery(QueryRequest queryRequest){
        AIQuery aiQuery = new AIQuery();
        aiQuery.setUserInput(queryRequest.getNaturalLanguageQuery());
        aiQuery.setTableNames(queryRequest.getTableNames());
        return aiQuery;
    }

    public AIQuery createHumanQuery(QueryRequest queryRequest){
        AIQuery aiQuery = new AIQuery();
        aiQuery.setUserInput(queryRequest.getHumanQuery());
        return aiQuery;
    }

    public Map<String,Object> createAIJsonPromptFormat(QueryRequest queryRequest){
        AIQuery aiQuery = createAIQuery(queryRequest);
        
        // Fetch real schema from database
        Map<String, Map<String, Map<String, Object>>> tableSchemas = 
            schemaService.getListOfTableSchemas(queryRequest.getTableNames());
        
        // Build schema description for AI
        StringBuilder schemaDescription = new StringBuilder();
        for (Map.Entry<String, Map<String, Map<String, Object>>> entry : tableSchemas.entrySet()) {
            String tableName = entry.getKey();
            Map<String, Object> columns = (Map<String, Object>) entry.getValue().get("columns");
            
            schemaDescription.append("Table: ").append(tableName).append("\n");
            schemaDescription.append("Columns: ");
            
            List<String> columnNames = new ArrayList<>(columns.keySet());
            schemaDescription.append(String.join(", ", columnNames));
            schemaDescription.append("\n\n");
        }
        
        Map<String, Object> aiJsonPrompt = new HashMap<>();
        aiJsonPrompt.put("requirements", aiQuery.getRequirements());
        aiJsonPrompt.put("security", aiQuery.getSecurityGuards());
        aiJsonPrompt.put("database_type", aiQuery.getDatabaseType());
        aiJsonPrompt.put("table_names", aiQuery.getTableNames());
        aiJsonPrompt.put("table_schemas", schemaDescription.toString());
        aiJsonPrompt.put("user_input", aiQuery.getUserInput());
        aiJsonPrompt.put("expected_return_type", aiQuery.getExpectedReturnType());
        return aiJsonPrompt;
    }

    public QueryResponse executeHumanQuery(String sqlQuery){
        QueryResponse response = new QueryResponse();
        long startTime = System.currentTimeMillis();
        try {
            var result = jdbcTemplate.queryForList(sqlQuery);
            long endTime = System.currentTimeMillis();
            response.setRc("200");
            response.setMessage("Query executed successfully.");
            response.setData(result);
            response.setExecutionTimeMs(endTime - startTime);
            response.setExecutedQuery(sqlQuery);
            response.setRowsAffected(result.size());
            response.setQueryType("HUMAN");

        } catch (Exception e) {
            response.setRc("500");
            response.setMessage("Error executing query: " + e.getMessage());
        }
        return response;
    }

    public QueryResponse executeAIQuery(QueryRequest queryRequest){
        QueryResponse response = new QueryResponse();
        long startTime = System.currentTimeMillis();

        if (queryRequest == null || queryRequest.getNaturalLanguageQuery() == null
            || queryRequest.getNaturalLanguageQuery().trim().isEmpty()) {
            response.setRc("400");
            response.setMessage("Natural language query is required for AI generation.");
            response.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            return response;
        }

        try {
            Map<String, Object> prompt = createAIJsonPromptFormat(queryRequest);
            String promptJson = objectMapper.writeValueAsString(prompt);
            
            String generatedSql = callGeminiAPI(promptJson);

            // Remove newlines and normalize whitespace
            String cleanedSql = generatedSql
                .replaceAll("\\n", " ")
                .replaceAll("\\s+", " ")
                .trim();
            
            if (cleanedSql.endsWith(";")) {
                cleanedSql = cleanedSql.substring(0, cleanedSql.length() - 1).trim();
            }

            // NOW EXECUTE THE GENERATED SQL
            var result = jdbcTemplate.queryForList(cleanedSql);
            
            response.setRc("200");
            response.setMessage("AI query generated and executed successfully.");
            response.setGeneratedSql(generatedSql);
            response.setData(result);
            response.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            response.setExecutedQuery(cleanedSql);
            response.setRowsAffected(result.size());
            response.setQueryType("AI");
            
        } catch (JsonProcessingException e) {
            response.setRc("500");
            response.setMessage("Failed to build AI prompt: " + e.getMessage());
        } catch (Exception e) {
            response.setRc("500");
            response.setMessage("Failed to execute AI query: " + e.getMessage());
        }

        response.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        return response;
    }

    private String callGeminiAPI(String jsonPrompt) throws Exception {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            throw new IllegalStateException("Missing Gemini API key");
        }

        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        List<Map<String, String>> parts = new ArrayList<>();
        Map<String, String> part = new HashMap<>();
        part.put("text", jsonPrompt);
        parts.add(part);
        content.put("parts", parts);
        contents.add(content);
        requestBody.put("contents", contents);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = geminiApiUrl + "?key=" + geminiApiKey;
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map<String, Object>> response = REST_TEMPLATE.exchange(
            url,
            HttpMethod.POST,
            entity,
            new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return extractSqlFromGeminiResponse(response.getBody());
        }

        throw new IllegalStateException("Gemini API refused the request");
    }

    @SuppressWarnings("unchecked")
    private String extractSqlFromGeminiResponse(Map<String, Object> body) throws Exception {
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalStateException("No candidates returned by Gemini");
        }

        Map<String, Object> firstCandidate = candidates.get(0);
        Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
        if (content == null) {
            throw new IllegalStateException("No content section in Gemini response");
        }

        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        if (parts == null || parts.isEmpty()) {
            throw new IllegalStateException("No parts returned by Gemini");
        }

        String text = (String) parts.get(0).get("text");
        if (text == null) {
            throw new IllegalStateException("Empty text returned from Gemini");
        }

        text = text.strip();
        
        // Remove markdown code fences
        if (text.startsWith("```sql")) {
            text = text.substring(6).strip();
        } else if (text.startsWith("```")) {
            text = text.substring(3).strip();
        }
        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3).strip();
        }
        
        // Remove trailing semicolon if present
        text = text.trim();
        if (text.endsWith(";")) {
            text = text.substring(0, text.length() - 1).trim();
        }

        return text;
    }

}