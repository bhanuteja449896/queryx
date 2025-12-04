# QueryX API Documentation

## Overview
QueryX is an AI-powered SQL query generator and executor built with Spring Boot, integrated with Google Gemini for natural language to SQL conversion. The backend provides RESTful APIs for schema management, data operations, and query execution.

**Base URL:** `http://localhost:8080`

---

## 1. Query APIs (`/query`)

### 1.1 Execute Human SQL Query
**Endpoint:** `POST /query/execute`

**Description:** Execute a raw SQL query directly.

**Request Parameters:**
```
query (String, Query Parameter): The SQL query to execute
```

**Example Request:**
```bash
curl -X POST "http://localhost:8080/query/execute?query=SELECT%20*%20FROM%20users%20LIMIT%2010"
```

**Response (Success - 200):**
```json
{
  "rc": "200",
  "message": "Query executed successfully",
  "executedQuery": "SELECT * FROM users LIMIT 10",
  "data": [
    {
      "id": 1,
      "name": "John Doe",
      "email": "john@example.com"
    }
  ],
  "columns": ["id", "name", "email"],
  "rowsAffected": 10,
  "executionTimeMs": 45
}
```

**Response (Error - 400/500):**
```json
{
  "rc": "500",
  "message": "SQL error: Table not found",
  "executedQuery": null,
  "data": null,
  "columns": null,
  "rowsAffected": 0,
  "executionTimeMs": 10
}
```

---

### 1.2 Execute AI-Enhanced Query
**Endpoint:** `POST /query/ai-query`

**Description:** Send a natural language request and let Gemini AI generate and execute the SQL query.

**Request Body:**
```json
{
  "naturalLanguageQuery": "Show me all users with gmail email addresses",
  "tableNames": ["users", "orders"],
  "queryType": "ai-enhanced"
}
```

**Request Body Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `naturalLanguageQuery` | String | Yes | Natural language description of what data you need |
| `tableNames` | List<String> | Yes | Names of tables to consider for query generation |
| `queryType` | String | Optional | Query type identifier (e.g., "ai-enhanced") |
| `humanQuery` | String | Optional | Alternative field for direct SQL (not used with AI) |

**Example Request:**
```bash
curl -X POST "http://localhost:8080/query/ai-query" \
  -H "Content-Type: application/json" \
  -d '{
    "naturalLanguageQuery": "Find all users registered in 2024",
    "tableNames": ["users"],
    "queryType": "ai-enhanced"
  }'
```

**Response (Success - 200):**
```json
{
  "rc": "200",
  "message": "AI query executed successfully",
  "executedQuery": "SELECT * FROM users WHERE EXTRACT(YEAR FROM created_at) = 2024",
  "data": [
    {
      "id": 1,
      "name": "Alice Smith",
      "created_at": "2024-01-15"
    }
  ],
  "columns": ["id", "name", "created_at"],
  "rowsAffected": 25,
  "executionTimeMs": 120
}
```

**Response (Error - 400/500):**
```json
{
  "rc": "400",
  "message": "Invalid request: tableNames cannot be empty",
  "executedQuery": null,
  "data": null,
  "columns": null,
  "rowsAffected": 0,
  "executionTimeMs": 0
}
```

---

## 2. Schema APIs (`/schema`)

### 2.1 Get All Tables
**Endpoint:** `GET /schema/tables`

**Description:** Retrieve a list of all table names in the database.

**Request Parameters:** None

**Example Request:**
```bash
curl -X GET "http://localhost:8080/schema/tables"
```

**Response (Success - 200):**
```json
[
  "users",
  "orders",
  "products",
  "categories"
]
```

---

### 2.2 Get All Tables Schema
**Endpoint:** `GET /schema/tablesSchema`

**Description:** Retrieve detailed schema information for all tables in the database.

**Request Parameters:** None

**Example Request:**
```bash
curl -X GET "http://localhost:8080/schema/tablesSchema"
```

**Response (Success - 200):**
```json
{
  "users": {
    "id": {
      "type": "INTEGER",
      "nullable": false,
      "primaryKey": true
    },
    "name": {
      "type": "VARCHAR",
      "nullable": false,
      "length": 255
    },
    "email": {
      "type": "VARCHAR",
      "nullable": false,
      "length": 255
    }
  },
  "orders": {
    "order_id": {
      "type": "INTEGER",
      "nullable": false,
      "primaryKey": true
    },
    "user_id": {
      "type": "INTEGER",
      "nullable": false,
      "primaryKey": false
    },
    "amount": {
      "type": "NUMERIC",
      "nullable": false,
      "precision": 10,
      "scale": 2
    }
  }
}
```

---

### 2.3 Get Single Table Schema
**Endpoint:** `GET /schema/{tableName}`

**Description:** Retrieve schema information for a specific table.

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `tableName` | String | Name of the table |

**Example Request:**
```bash
curl -X GET "http://localhost:8080/schema/users"
```

**Response (Success - 200):**
```json
{
  "id": {
    "type": "INTEGER",
    "nullable": false,
    "primaryKey": true
  },
  "name": {
    "type": "VARCHAR",
    "nullable": false,
    "length": 255
  },
  "email": {
    "type": "VARCHAR",
    "nullable": false,
    "length": 255
  },
  "created_at": {
    "type": "TIMESTAMP",
    "nullable": true
  }
}
```

---

### 2.4 Create Table
**Endpoint:** `POST /schema/create`

**Description:** Create a new table in the database.

**Request Body:**
```json
{
  "tableName": "products",
  "columns": [
    {
      "name": "id",
      "type": "INTEGER",
      "primaryKey": true,
      "nullable": false
    },
    {
      "name": "name",
      "type": "VARCHAR",
      "length": 255,
      "nullable": false
    },
    {
      "name": "price",
      "type": "NUMERIC",
      "length": 10,
      "nullable": false
    },
    {
      "name": "description",
      "type": "TEXT",
      "nullable": true
    }
  ]
}
```

**Request Body Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `tableName` | String | Yes | Name of the table to create |
| `columns` | List<ColumnDefinition> | Yes | List of column definitions |

**ColumnDefinition Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | String | Yes | Column name |
| `type` | String | Yes | Data type (VARCHAR, INTEGER, NUMERIC, TEXT, TIMESTAMP, etc.) |
| `length` | Integer | No | Length constraint (for VARCHAR and NUMERIC types) |
| `primaryKey` | Boolean | No | Mark as primary key (default: false) |
| `nullable` | Boolean | No | Allow NULL values (default: true) |

**Example Request:**
```bash
curl -X POST "http://localhost:8080/schema/create" \
  -H "Content-Type: application/json" \
  -d '{
    "tableName": "products",
    "columns": [
      {"name": "id", "type": "INTEGER", "primaryKey": true, "nullable": false},
      {"name": "name", "type": "VARCHAR", "length": 255, "nullable": false},
      {"name": "price", "type": "NUMERIC", "length": 10, "nullable": false}
    ]
  }'
```

**Response (Success - 200):**
```json
{
  "rc": "200",
  "message": "Table created successfully"
}
```

**Response (Error - 400/500):**
```json
{
  "rc": "500",
  "message": "Table already exists"
}
```

---

### 2.5 Update Table
**Endpoint:** `POST /schema/update`

**Description:** Update an existing table schema (add/modify columns).

**Request Body:**
```json
{
  "tableName": "products",
  "columns": [
    {
      "name": "id",
      "type": "INTEGER",
      "primaryKey": true,
      "nullable": false
    },
    {
      "name": "name",
      "type": "VARCHAR",
      "length": 255,
      "nullable": false
    },
    {
      "name": "new_column",
      "type": "VARCHAR",
      "length": 100,
      "nullable": true
    }
  ]
}
```

**Example Request:**
```bash
curl -X POST "http://localhost:8080/schema/update" \
  -H "Content-Type: application/json" \
  -d '{
    "tableName": "products",
    "columns": [...]
  }'
```

**Response (Success - 200):**
```json
{
  "rc": "200",
  "message": "Table updated successfully"
}
```

---

### 2.6 Delete Table
**Endpoint:** `DELETE /schema/delete/{tableName}`

**Description:** Drop a table from the database.

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `tableName` | String | Name of the table to delete |

**Example Request:**
```bash
curl -X DELETE "http://localhost:8080/schema/delete/products"
```

**Response (Success - 200):**
```json
{
  "rc": "200",
  "message": "Table deleted successfully"
}
```

**Response (Error - 404/500):**
```json
{
  "rc": "500",
  "message": "Table does not exist"
}
```

---

## 3. Table Data APIs (`/tabledata`)

### 3.1 Insert Data
**Endpoint:** `POST /tabledata/insert`

**Description:** Insert one or multiple rows into a specified table.

**Request Body:**
```json
{
  "tableName": "users",
  "rows": [
    [
      {"name": "id", "value": 1},
      {"name": "name", "value": "John Doe"},
      {"name": "email", "value": "john@example.com"}
    ],
    [
      {"name": "id", "value": 2},
      {"name": "name", "value": "Jane Smith"},
      {"name": "email", "value": "jane@example.com"}
    ]
  ]
}
```

**Request Body Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `tableName` | String | Yes | Name of the table |
| `rows` | List<List<ColumnData>> | Yes | List of rows to insert |

**ColumnData Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | String | Yes | Column name |
| `value` | Object | Yes | Column value (String, Integer, Double, Boolean, etc.) |

**Example Request (Single Row):**
```bash
curl -X POST "http://localhost:8080/tabledata/insert" \
  -H "Content-Type: application/json" \
  -d '{
    "tableName": "users",
    "rows": [
      [
        {"name": "id", "value": 1},
        {"name": "name", "value": "Alice Johnson"},
        {"name": "email", "value": "alice@example.com"}
      ]
    ]
  }'
```

**Example Request (Multiple Rows):**
```bash
curl -X POST "http://localhost:8080/tabledata/insert" \
  -H "Content-Type: application/json" \
  -d '{
    "tableName": "users",
    "rows": [
      [
        {"name": "id", "value": 1},
        {"name": "name", "value": "John"},
        {"name": "email", "value": "john@example.com"}
      ],
      [
        {"name": "id", "value": 2},
        {"name": "name", "value": "Jane"},
        {"name": "email", "value": "jane@example.com"}
      ]
    ]
  }'
```

**Response (Success - 200):**
```json
{
  "rc": "200",
  "message": "2 rows inserted successfully"
}
```

**Response (Error - 400/500):**
```json
{
  "rc": "500",
  "message": "Table not found or insert failed"
}
```

---

## Configuration

**Database Configuration (application.properties):**
```properties
# Server Configuration
server.port=8080

# PostgreSQL Database Configuration
spring.datasource.url=jdbc:postgresql://HOST:PORT/DATABASE?sslmode=require
spring.datasource.username=USERNAME
spring.datasource.password=PASSWORD
spring.datasource.driver-class-name=org.postgresql.Driver

# Gemini AI Configuration
gemini.api.key=YOUR_GEMINI_API_KEY
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent
```

---

## Response Codes

| Code | Meaning | Description |
|------|---------|-------------|
| 200 | OK | Request successful |
| 400 | Bad Request | Invalid input or validation error |
| 500 | Internal Server Error | Database error or server error |

---

## Common Use Cases

### 1. Generate and Execute an AI Query
```bash
# User asks for data in natural language
curl -X POST "http://localhost:8080/query/ai-query" \
  -H "Content-Type: application/json" \
  -d '{
    "naturalLanguageQuery": "Show all active orders from the last 30 days",
    "tableNames": ["orders", "users"],
    "queryType": "ai-enhanced"
  }'
```

### 2. Create a New Table
```bash
curl -X POST "http://localhost:8080/schema/create" \
  -H "Content-Type: application/json" \
  -d '{
    "tableName": "customers",
    "columns": [
      {"name": "id", "type": "INTEGER", "primaryKey": true},
      {"name": "email", "type": "VARCHAR", "length": 255},
      {"name": "phone", "type": "VARCHAR", "length": 20}
    ]
  }'
```

### 3. Insert Data into Table
```bash
curl -X POST "http://localhost:8080/tabledata/insert" \
  -H "Content-Type: application/json" \
  -d '{
    "tableName": "customers",
    "rows": [
      [
        {"name": "id", "value": 1},
        {"name": "email", "value": "customer1@example.com"},
        {"name": "phone", "value": "+1234567890"}
      ]
    ]
  }'
```

### 4. View Database Schema
```bash
# Get all tables
curl -X GET "http://localhost:8080/schema/tables"

# Get schema for specific table
curl -X GET "http://localhost:8080/schema/users"

# Get schema for all tables
curl -X GET "http://localhost:8080/schema/tablesSchema"
```

---

## Frontend Integration Notes

1. **CORS Enabled:** All APIs have CORS support for cross-origin requests from frontend applications.
2. **Content-Type:** Use `Content-Type: application/json` for POST/PUT requests.
3. **Error Handling:** Always check the `rc` field in responses to handle errors appropriately.
4. **AI Query Generation:** Provide meaningful table names and natural language descriptions for better SQL generation.
5. **Data Types:** Ensure values in insert operations match the column data types defined in the schema.

