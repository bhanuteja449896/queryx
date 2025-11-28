package com.example.demo.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for inserting data into a specified table.
 * This structure supports both single and batch inserts efficiently in a single request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsertData {

    /**
     * The name of the table where the data will be inserted.
     */
    private String tableName;

    /**
     * A list of rows to be inserted. Each row is represented by a list of its columns (ColumnData).
     */
    private List<List<ColumnData>> rows;

    /**
     * A static inner class to represent a single column's name and its corresponding value.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColumnData {
        /**
         * The name of the column (e.g., "username", "email").
         */
        private String name;

        /**
         * The value to insert into the column. Can be any object type (String, Integer, etc.).
         */
        private Object value;
    }
}
