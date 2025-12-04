# Network Error Fix - Debugging Guide

## Problem
The system was showing a generic "Network Error" message when executing AI queries, making it difficult to diagnose the actual root cause.

## Root Causes

1. **Insufficient Error Handling** - The `callGeminiAPI()` method didn't catch or log specific network errors from RestTemplate
2. **No Logging** - Missing SLF4J logger made it impossible to debug API calls and responses
3. **Generic Error Messages** - All exceptions were caught broadly without distinguishing between API errors, network errors, and SQL errors
4. **Missing API Key Validation** - The Gemini API key validation was too minimal

## Solutions Implemented

### 1. Enhanced Error Handling in `callGeminiAPI()`
```java
try {
    ResponseEntity<Map<String, Object>> response = REST_TEMPLATE.exchange(...);
    // Handle response
} catch (RestClientException e) {
    // Specific network error handling
    logger.error("Network error communicating with Gemini API: " + e.getMessage(), e);
    throw new RuntimeException(errorMsg, e);
} catch (Exception e) {
    // General error handling
    logger.error("Error calling Gemini API: " + e.getMessage(), e);
}
```

### 2. Added SLF4J Logging
```java
private static final Logger logger = LoggerFactory.getLogger(QueryServices.class);
```

Added logging at different levels:
- **DEBUG**: For detailed tracing (prompt creation, API URL, response received)
- **INFO**: For important events (query execution, generated SQL, successful executions)
- **ERROR/WARN**: For problems (network errors, missing API keys, validation failures)

### 3. Better Error Messages
- API Key validation: "Missing Gemini API key. Check application.properties for gemini.api.key"
- Network errors: "Network error communicating with Gemini API: [specific error]"
- API errors: "Gemini API returned status: [status code]"
- Execution errors: Distinct messages for prompt building, network issues, and query execution

### 4. Enhanced Logging Configuration
Added to `application.properties`:
```properties
logging.level.root=INFO
logging.level.com.example.demo=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
```

## How to Debug Network Errors Now

1. **Check Application Logs** - All errors are now logged with detailed messages and stack traces
   - Look for lines starting with `[ERROR]` or `[WARN]`

2. **Verify Gemini API Key**
   - Check `/workspaces/queryx/src/main/resources/application.properties`
   - Ensure `gemini.api.key` is set and not empty
   - Verify the API key is valid with Google Cloud

3. **Check Network Connectivity**
   - Ensure the server can reach `https://generativelanguage.googleapis.com`
   - Look for connection timeout errors in logs

4. **Verify API URL**
   - Confirm `gemini.api.url` is correct in application.properties
   - Current URL: `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent`

## Response Codes

| Code | Meaning | Action |
|------|---------|--------|
| 200 | Success | Query executed successfully |
| 400 | Bad Request | Invalid input (empty query, missing table names) |
| 500 | Internal Error | Check logs for details (prompt building, SQL execution) |
| 503 | Service Unavailable | Network error with Gemini API - check logs |

## Example Error Scenarios

### Scenario 1: Missing API Key
**Error Message**: "Network error: Missing Gemini API key. Check application.properties for gemini.api.key"
**Log Entry**: `ERROR com.example.demo.services.QueryServices - Missing Gemini API key...`
**Solution**: Add API key to `application.properties`

### Scenario 2: Network Connection Issue
**Error Message**: "Network error: Connection timeout"
**Log Entry**: `ERROR com.example.demo.services.QueryServices - Network error communicating with Gemini API: Connection timeout`
**Solution**: Check internet connection and firewall rules

### Scenario 3: Invalid SQL Generated
**Error Message**: "Failed to execute AI query: SQL syntax error..."
**Log Entry**: Shows both the generated SQL and the SQL error
**Solution**: Try rephrasing the natural language query

## Testing the Fix

1. Make a request with invalid natural language query:
```bash
curl -X POST "http://localhost:8080/query/ai-query" \
  -H "Content-Type: application/json" \
  -d '{"naturalLanguageQuery": "", "tableNames": []}'
```
Expected: `"rc": "400"` with message about empty query

2. Monitor logs while making requests:
```bash
# From another terminal, if using Docker or watching logs
tail -f /path/to/application/logs/
```

3. Test with valid query:
```bash
curl -X POST "http://localhost:8080/query/ai-query" \
  -H "Content-Type: application/json" \
  -d '{"naturalLanguageQuery": "Show all users", "tableNames": ["users"]}'
```

## Files Modified

1. **QueryServices.java**
   - Added logger initialization
   - Enhanced error handling in `callGeminiAPI()`
   - Added detailed logging throughout
   - Better error messages in catch blocks

2. **application.properties**
   - Added logging configuration
   - Configured DEBUG level for application packages

