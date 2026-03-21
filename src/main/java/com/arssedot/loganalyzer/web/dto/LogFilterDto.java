package com.arssedot.loganalyzer.web.dto;

import lombok.Data;

/**
 * Mutable filter DTO used for Thymeleaf form binding and REST query params.
 */
@Data
public class LogFilterDto {

    private String level;
    private String service;
    /** datetime-local format: yyyy-MM-ddTHH:mm */
    private String from;
    /** datetime-local format: yyyy-MM-ddTHH:mm */
    private String to;
    private String q;
    private int page = 0;
    private int size = 20;
}
