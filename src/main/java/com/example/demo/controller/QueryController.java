package com.example.demo.controller;

import com.example.demo.dto.QueryRequest;
import com.example.demo.dto.QueryResponse;
import com.example.demo.services.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/query")
@CrossOrigin(origins = "*")
public class QueryController {

  @Autowired private QueryService queryService;


  @PostMapping("/execute")
  public QueryResponse executeQuery(@RequestBody QueryRequest request) {
    if (request.getQueryType() == null || request.getQueryType().equals("human")) {
      return queryService.executeHumanQuery(request);
    } else if (request.getQueryType().equals("ai-enhanced")) {
      return queryService.executeAIQuery(request);
    } else {
      QueryResponse errorResponse = new QueryResponse();
      errorResponse.setRc("400");
      errorResponse.setMessage("Invalid query type. Must be 'human' or 'ai-enhanced'");
      return errorResponse;
    }
  }


  @PostMapping("/ai-generate")
  public QueryResponse generateAIQuery(@RequestBody QueryRequest request) {
    return queryService.generateAIQuery(request);
  }


  @PostMapping("/ai-execute")
  public QueryResponse executeAIQuery(@RequestBody QueryRequest request) {
    return queryService.executeAIQuery(request);
  }


  @PostMapping("/formula")
  public QueryResponse executeFormulaQuery(@RequestBody QueryRequest request) {
    return queryService.executeHumanQuery(request);
  }


  @GetMapping("/health")
  public QueryResponse healthCheck() {
    QueryResponse response = new QueryResponse();
    response.setRc("200");
    response.setMessage("Query service is running");
    return response;
  }

}
