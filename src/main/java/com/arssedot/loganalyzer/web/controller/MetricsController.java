package com.arssedot.loganalyzer.web.controller;

import com.arssedot.loganalyzer.service.MetricsService;
import com.arssedot.loganalyzer.web.dto.MetricsSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;

    @GetMapping("/summary")
    public MetricsSummaryDto summary() {
        return metricsService.getSummary();
    }
}
