package com.example.demo.controller;

import com.example.demo.dto.InsertData;
import com.example.demo.dto.ResponseDTO;
import com.example.demo.services.TableDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tabledata")
public class TableDataController {

  @Autowired private TableDataService tableDataService;


  @PostMapping("/insert")
  public ResponseDTO insert(@RequestBody InsertData data) {
    return tableDataService.insertData(data);
  }


}
