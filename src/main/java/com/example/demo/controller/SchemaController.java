package com.example.demo.controller;

import com.example.demo.dto.CreateTableRequest;
import com.example.demo.dto.ResponseDTO;
import com.example.demo.services.SchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public Map<String, Map<String, Map<String, Object>>> getTableSchema() {
        return schemaService.getAllTableSchemas();
    }
    
    @GetMapping("/{tableName}")
    public Map<String, Map<String, Object>> getTableSchema(@PathVariable String tableName) {
        return schemaService.getTableSchema(tableName);
    }

    @PostMapping("/update")
    public ResponseDTO updateTable(@RequestBody CreateTableRequest request){
        return schemaService.updateTable(request);
    }

    @PostMapping("/create")
    public ResponseDTO createTable(@RequestBody CreateTableRequest request){
        return schemaService.createTable(request);
    }

    @DeleteMapping("/delete/{tableName}")
    public ResponseDTO deleteTable(@PathVariable String tableName){
        return schemaService.dropTable(tableName);
    }
    
}
