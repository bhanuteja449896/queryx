package com.example.demo.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SchemaService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public List<String> listTables(){
        String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'";
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getString("table_name")
        );
    }

    public Map<String, List<Map<String, Object>>> getAllTableSchemas() {
        String sql = "SELECT table_name, column_name, data_type FROM information_schema.columns WHERE table_schema = 'public' ORDER BY table_name, ordinal_position";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String tableName = (String) row.get("table_name");
            
            Map<String, Object> columnDetails = new HashMap<>();
            columnDetails.put("column_name", row.get("column_name"));
            columnDetails.put("data_type", row.get("data_type"));

            result.computeIfAbsent(tableName, k -> new ArrayList<>()).add(columnDetails);
        }
        return result;
    }
}
