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

  /**
   * A single, flexible endpoint for inserting data into a table.
   * This endpoint uses a "list of lists" structure to handle both single and multiple row inserts in a single request.
   *
   * @param data The InsertData object containing the table name and a list of rows to insert.
   * @return A ResponseDTO indicating the result of the operation.
   */
  @PostMapping("/insert")
  public ResponseDTO insert(@RequestBody InsertData data) {
    return tableDataService.insertData(data);
  }


}
