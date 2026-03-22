package com.arssedot.loganalyzer.web.controller;

import com.arssedot.loganalyzer.service.MetricsService;
import com.arssedot.loganalyzer.web.dto.MetricsSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;

    @GetMapping("/summary")
    public MetricsSummaryDto summary(@RequestParam(required = false) String service) {
        return metricsService.getSummary(service);
    }
}
