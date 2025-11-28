package com.example.demo.controller;

import com.example.demo.services.SchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/schema")
public class SchemaController {

    @Autowired
    private SchemaService schemaService;

    @GetMapping("/tables")
    public List<String> GetAllTables(){
        return schemaService.listTables();
    }

    @GetMapping("/tablesSchema")
    public Map<String, List<Map<String, Object>>> getTableSchema() {
        return schemaService.getAllTableSchemas();
    }
    
}
