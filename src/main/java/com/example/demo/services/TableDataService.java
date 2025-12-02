package com.example.demo.services;

import com.example.demo.dto.InsertData;
import com.example.demo.dto.ResponseDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TableDataService {

  @Autowired private JdbcTemplate jdbcTemplate;

  @Transactional
  public ResponseDTO insertData(InsertData data) {
    ResponseDTO responseDTO = new ResponseDTO();

    try {
      // Validate input data
      if (data == null
          || data.getTableName() == null
          || data.getTableName().isEmpty()
          || data.getRows() == null
          || data.getRows().isEmpty()) {
        throw new IllegalArgumentException("Table name and rows cannot be null or empty.");
      }

      // Validate table name to prevent SQL injection
      if (!isValidIdentifier(data.getTableName())) {
        throw new IllegalArgumentException("Invalid table name. Only alphanumeric characters and underscores are allowed.");
      }

      List<InsertData.ColumnData> firstRow = data.getRows().get(0);
      if (firstRow == null || firstRow.isEmpty()) {
        throw new IllegalArgumentException("Columns list cannot be null or empty for a row.");
      }

      // Extract column names from the first row and validate them
      List<String> columnNames = new ArrayList<>();
      for (InsertData.ColumnData col : firstRow) {
        if (col.getName() == null || col.getName().isEmpty()) {
          throw new IllegalArgumentException("Column name cannot be null or empty.");
        }
        if (!isValidIdentifier(col.getName())) {
          throw new IllegalArgumentException("Invalid column name: " + col.getName());
        }
        columnNames.add(col.getName());
      }

      // Validate that all rows have the same columns in the same order
      for (int i = 0; i < data.getRows().size(); i++) {
        List<InsertData.ColumnData> currentRow = data.getRows().get(i);
        if (currentRow == null || currentRow.size() != columnNames.size()) {
          throw new IllegalArgumentException(
              String.format("Row %d has a different number of columns than the first row.", i));
        }

        // Check if column names match
        for (int j = 0; j < currentRow.size(); j++) {
          String currentColName = currentRow.get(j).getName();
          if (!columnNames.get(j).equals(currentColName)) {
            throw new IllegalArgumentException(
                String.format(
                    "Row %d has different column names or order. Expected '%s' but got '%s' at position %d.",
                    i, columnNames.get(j), currentColName, j));
          }
        }
      }

      // Build the SQL statement with proper quoting
      String tableName = quoteIdentifier(data.getTableName());
      String columnsPart =
          columnNames.stream()
              .map(this::quoteIdentifier)
              .collect(Collectors.joining(", "));
      String placeholders = columnNames.stream().map(c -> "?").collect(Collectors.joining(", "));

      String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columnsPart, placeholders);

      // Prepare batch arguments
      List<Object[]> batchArgs = new ArrayList<>();
      for (List<InsertData.ColumnData> row : data.getRows()) {
        Object[] values = row.stream().map(InsertData.ColumnData::getValue).toArray();
        batchArgs.add(values);
      }

      // Execute batch insert
      int[] updateCounts = jdbcTemplate.batchUpdate(sql, batchArgs);
      int successCount = 0;
      for (int count : updateCounts) {
        if (count > 0) {
          successCount++;
        }
      }

      responseDTO.setRc("200");
      responseDTO.setMessage(
          String.format(
              "%d row(s) inserted successfully into table '%s'.",
              successCount,
              data.getTableName()));

    } catch (IllegalArgumentException e) {
      responseDTO.setRc("400");
      responseDTO.setMessage("Validation error: " + e.getMessage());
    } catch (Exception e) {
      responseDTO.setRc("500");
      responseDTO.setMessage("Error during batch insert: " + e.getMessage());
    }

    return responseDTO;
  }

  /**
   * Validates that an identifier (table name or column name) contains only safe characters.
   * Allows alphanumeric characters, underscores, and spaces (which will be quoted).
   *
   * @param identifier the identifier to validate
   * @return true if valid, false otherwise
   */
  private boolean isValidIdentifier(String identifier) {
    if (identifier == null || identifier.isEmpty()) {
      return false;
    }
    // Allow alphanumeric, underscores, and spaces
    return identifier.matches("^[a-zA-Z0-9_\\s]+$");
  }

  /**
   * Quotes an identifier using double quotes (standard SQL).
   * Also escapes any embedded double quotes by doubling them.
   *
   * @param identifier the identifier to quote
   * @return the quoted identifier
   */
  private String quoteIdentifier(String identifier) {
    // Escape any double quotes in the identifier by doubling them
    String escaped = identifier.replace("\"", "\"\"");
    return "\"" + escaped + "\"";
  }


  
}
