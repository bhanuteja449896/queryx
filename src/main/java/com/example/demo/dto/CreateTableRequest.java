package com.example.demo.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CreateTableRequest {
    
    @NotBlank
    private String tableName;

    @NotEmpty
    private List<ColumnDefinition> columns;

    @Data
    public static class ColumnDefinition {
        @NotBlank
        private String name;

        @NotBlank
        private String type;  // e.g., VARCHAR, INT, etc.

        private Integer length; // optional
        private boolean primaryKey;
        private boolean nullable = true;
    }

}
