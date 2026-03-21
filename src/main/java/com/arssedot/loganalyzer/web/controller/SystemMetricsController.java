package com.arssedot.loganalyzer.web.controller;

import com.arssedot.loganalyzer.service.SystemMetricsService;
import com.arssedot.loganalyzer.web.dto.SystemMetricsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemMetricsController {

    private final SystemMetricsService systemMetricsService;

    @GetMapping("/metrics")
    public SystemMetricsDto metrics() {
        return systemMetricsService.getMetrics();
    }
}
