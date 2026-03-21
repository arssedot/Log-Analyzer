package com.arssedot.loganalyzer.web.dto;

import java.util.List;
import java.util.Map;

public record MetricsSummaryDto(
        long totalLogs,
        Map<String, Long> countByLevel,
        double errorRate,
        List<ServiceStat> topServices,
        List<TimeSeriesPoint> timeSeriesData,
        long logsLastHour,
        long servicesCount
) {
    public record ServiceStat(String serviceName, long count) {}

    public record TimeSeriesPoint(String label, long count) {}
}
