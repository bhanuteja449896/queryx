package com.example.demo.services;

import com.example.demo.dto.CreateTableRequest;
import com.example.demo.dto.ResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SchemaService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<String> listTables() {
        return jdbcTemplate.queryForList("SELECT tablename FROM pg_catalog.pg_tables WHERE schemaname != 'pg_catalog' AND schemaname != 'information_schema'", String.class);
    }

    public Map<String, Map<String, Map<String, Object>>> getAllTableSchemas() {
        List<String> tables = listTables();
        return tables.stream().collect(Collectors.toMap(table -> table, this::getTableSchema));
    }

    public Map<String, Map<String, Object>> getTableSchema(String tableName) {
        String sql = "SELECT column_name, data_type, character_maximum_length, is_nullable FROM information_schema.columns WHERE table_name = ?";
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql, tableName);

        return Map.of("columns", columns.stream().collect(Collectors.toMap(col -> (String) col.get("column_name"), col -> col)));
    }

    public Map<String,Map<String,Map<String,Object>>> getListOfTableSchemas(List<String> tableNames) {
        return tableNames.stream().collect(Collectors.toMap(table -> table, this::getTableSchema));
    }

    public boolean isTableExists(String tableName) {
        String sql = "SELECT count(*) FROM information_schema.tables WHERE table_name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName);
        return count != null && count > 0;
    }

    public ResponseDTO createTable(CreateTableRequest request) {
        ResponseDTO responseDTO = new ResponseDTO();
        try {
            if (isTableExists(request.getTableName())) {
                responseDTO.setRc("400");
                responseDTO.setMessage("Table already exists");
                return responseDTO;
            }

            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE ").append(request.getTableName()).append(" (");

            StringBuilder pkColumns = new StringBuilder();

            for (int i = 0; i < request.getColumns().size(); i++) {
                var col = request.getColumns().get(i);

                sql.append(col.getName()).append(" ").append(col.getType());
                if (col.getLength() != null && col.getLength() > 0) {
                    sql.append("(").append(col.getLength()).append(")");
                }
                if (col.isPrimaryKey()) {
                    pkColumns.append(col.getName()).append(",");
                }
                if (!col.isNullable()) {
                    sql.append(" NOT NULL");
                }
                if (i < request.getColumns().size() - 1) {
                    sql.append(", ");
                }
            }

            if (pkColumns.length() > 0) {
                pkColumns.deleteCharAt(pkColumns.length() - 1);
                sql.append(", PRIMARY KEY (").append(pkColumns).append(")");
            }

            sql.append(")");
            jdbcTemplate.execute(sql.toString());

            responseDTO.setRc("200");
            responseDTO.setMessage("Table created successfully");

        } catch (Exception e) {
            responseDTO.setRc("500");
            responseDTO.setMessage("Error creating table: " + e.getMessage());
        }
        return responseDTO;
    }

    public ResponseDTO dropTable(String tableName) {
        ResponseDTO responseDTO = new ResponseDTO();
        try {
            String sql = "DROP TABLE IF EXISTS " + tableName;
            jdbcTemplate.execute(sql);

            responseDTO.setRc("200");
            responseDTO.setMessage("Table dropped successfully");

            return responseDTO;
        } catch (Exception e) {
            responseDTO.setRc("500");
            responseDTO.setMessage("Error dropping table: " + e.getMessage());
        }
        return responseDTO;
    }

    public ResponseDTO updateTable(CreateTableRequest request) {
    ResponseDTO responseDTO = new ResponseDTO();
    String tableName = request.getTableName();

    if (!isTableExists(tableName)) {
        responseDTO.setRc("404");
        responseDTO.setMessage("Table '" + tableName + "' does not exist.");
        return responseDTO;
    }

    try {
        // --- Get Current Schema ---
        Map<String, Map<String, Object>> currentSchema = getTableSchema(tableName);
        Map<String, Object> currentColumns = (Map<String, Object>) currentSchema.get("columns");
        List<String> currentColumnNames = new ArrayList<>(currentColumns.keySet());

        // --- Get Desired Schema ---
        List<CreateTableRequest.ColumnDefinition> desiredColumns = request.getColumns();
        List<String> desiredColumnNames = desiredColumns.stream().map(CreateTableRequest.ColumnDefinition::getName).collect(Collectors.toList());

        // --- SQL commands to execute ---
        List<String> alterCommands = new ArrayList<>();

        // --- Handle Column Deletion ---
        for (String colName : currentColumnNames) {
            if (!desiredColumnNames.contains(colName)) {
                alterCommands.add(String.format("ALTER TABLE %s DROP COLUMN %s", tableName, colName));
            }
        }

        // --- Handle Column Addition and Modification ---
        for (CreateTableRequest.ColumnDefinition desiredCol : desiredColumns) {
            String colName = desiredCol.getName();
            if (!currentColumnNames.contains(colName)) {
                // Add new column
                String colDefinition = getColumnSql(desiredCol);
                alterCommands.add(String.format("ALTER TABLE %s ADD COLUMN %s", tableName, colDefinition));
            } else {
                // Modify existing column - This can be complex, for simplicity, we'll focus on type and nullability
                Map<String, Object> currentCol = (Map<String, Object>) currentColumns.get(colName);
                String currentType = (String) currentCol.get("data_type");
                String desiredType = desiredCol.getType();
                
                if (!currentType.equalsIgnoreCase(desiredType)) {
                     alterCommands.add(String.format("ALTER TABLE %s ALTER COLUMN %s TYPE %s", tableName, colName, desiredType));
                }
            }
        }

        // Execute all alter commands
        for (String command : alterCommands) {
            jdbcTemplate.execute(command);
        }

        responseDTO.setRc("200");
        responseDTO.setMessage("Table '" + tableName + "' updated successfully.");

    } catch (DataAccessException e) {
        responseDTO.setRc("500");
        responseDTO.setMessage("Error updating table: " + e.getMostSpecificCause().getMessage());
    }
    return responseDTO;
}

    private String getColumnSql(CreateTableRequest.ColumnDefinition col) {
        StringBuilder sql = new StringBuilder();
        sql.append(col.getName()).append(" ").append(col.getType());
        if (col.getLength() != null && col.getLength() > 0) {
            sql.append("(").append(col.getLength()).append(")");
        }
        if (!col.isNullable()) {
            sql.append(" NOT NULL");
        }
        return sql.toString();
    }

}
