package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A generic response DTO to provide a consistent response format from API endpoints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDTO {

    /**
     * The response code (e.g., "200" for success, "500" for error).
     */
    private String rc;

    /**
     * A descriptive message about the result of the operation.
     */
    private String message;
}
