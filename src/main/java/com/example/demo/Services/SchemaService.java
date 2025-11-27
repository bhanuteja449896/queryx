package com.example.demo.Services;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SchemaService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public List<String> listTables(){
        String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'";
        return jdbcTemplate.query( sql , (rs, rowNum) -> rs.getString("table_name") );
    }

}
