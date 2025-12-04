package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.*;
import com.example.demo.services.QueryServices;

@RestController
@RequestMapping("/query")
@CrossOrigin(origins = "*")
public class QueryController {
    
    @Autowired
    private QueryServices queryServices;

    @PostMapping("/execute")
    public QueryResponse executeHumanQuery(@RequestParam("query") String sqlQuery) {
        return queryServices.executeHumanQuery(sqlQuery);
    }

    @PostMapping("/ai-query")
    public QueryResponse executeAIQuery(@RequestBody QueryRequest queryRequest) {
        return queryServices.executeAIQuery(queryRequest);
    }

}