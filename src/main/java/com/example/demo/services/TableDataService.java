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
      if (data == null
          || data.getTableName() == null
          || data.getTableName().isEmpty()
          || data.getRows() == null
          || data.getRows().isEmpty()) {
        throw new IllegalArgumentException("Table name and rows cannot be null or empty.");
      }

      List<InsertData.ColumnData> firstRow = data.getRows().get(0);
      if (firstRow == null || firstRow.isEmpty()) {
        throw new IllegalArgumentException("Columns list cannot be null or empty for a row.");
      }

      // Use backticks for quoting identifiers to ensure compatibility with MySQL and other databases.
      String tableName = "`" + data.getTableName() + "`";
      String columnsPart =
          firstRow.stream()
              .map(col -> "`" + col.getName() + "`")
              .collect(Collectors.joining(", "));
      String placeholders = firstRow.stream().map(c -> "?").collect(Collectors.joining(", "));

      String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columnsPart, placeholders);

      List<Object[]> batchArgs = new ArrayList<>();
      for (List<InsertData.ColumnData> row : data.getRows()) {
        Object[] values = row.stream().map(InsertData.ColumnData::getValue).toArray();
        batchArgs.add(values);
      }

      jdbcTemplate.batchUpdate(sql, batchArgs);

      responseDTO.setRc("200");
      responseDTO.setMessage(
          String.format(
              "%d row(s) inserted successfully into table '%s'.",
              data.getRows().size(),
              data.getTableName()));

    } catch (Exception e) {
      responseDTO.setRc("500");
      responseDTO.setMessage("Error during batch insert: " + e.getMessage());
    }

    return responseDTO;
  }


}
