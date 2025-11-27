package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Services.SchemaService;

import java.util.List;
import java.util.Map;

@SpringBootApplication
@RestController
public class DemoApplication {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SchemaService schemaService;

    @GetMapping("/tables")
    public List<Map<String, Object>> listTables() {
      System.out.println("Tables api testing");
        return jdbcTemplate.queryForList("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'");
    }

    @GetMapping("/tables1")
    public List<String> GetAllTables(){
      return schemaService.listTables();
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
        System.out.println("Server running on port 8080");
    }
}
