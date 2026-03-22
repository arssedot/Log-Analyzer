package com.arssedot.loganalyzer.service;

import com.arssedot.loganalyzer.domain.LogEntry;
import com.arssedot.loganalyzer.domain.LogLevel;
import com.arssedot.loganalyzer.repository.LogEntryRepository;
import com.arssedot.loganalyzer.web.dto.MetricsSummaryDto;
import com.arssedot.loganalyzer.web.dto.MetricsSummaryDto.ServiceStat;
import com.arssedot.loganalyzer.web.dto.MetricsSummaryDto.TimeSeriesPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private static final DateTimeFormatter HOUR_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneOffset.UTC);

    private final LogEntryRepository repository;

    @Transactional(readOnly = true)
    public MetricsSummaryDto getSummary() {
        return getSummary(null);
    }

    @Transactional(readOnly = true)
    public MetricsSummaryDto getSummary(String serviceName) {
        boolean scoped = serviceName != null && !serviceName.isBlank();

        long total = scoped ? repository.countByServiceName(serviceName) : repository.count();

        Map<String, Long> countByLevel = scoped ? buildCountByLevelForService(serviceName) : buildCountByLevel();

        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        long logsLastHour = scoped
                ? repository.countByServiceNameAndTimestampAfter(serviceName, oneHourAgo)
                : repository.countByTimestampAfter(oneHourAgo);

        double errorRate = computeErrorRate(logsLastHour, oneHourAgo, serviceName);

        long servicesCount = scoped ? 1L : repository.countDistinctServiceNames();

        List<ServiceStat> topServices = scoped
                ? List.of(new ServiceStat(serviceName, total))
                : repository.findTopServices(PageRequest.of(0, 10)).stream()
                        .map(row -> new ServiceStat((String) row[0], (Long) row[1]))
                        .toList();

        List<TimeSeriesPoint> timeSeries = buildHourlyTimeSeries(24, scoped ? serviceName : null);

        return new MetricsSummaryDto(total, countByLevel, errorRate, topServices, timeSeries, logsLastHour, servicesCount);
    }

    private Map<String, Long> buildCountByLevel() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (LogLevel level : LogLevel.values()) {
            result.put(level.name(), 0L);
        }
        repository.countGroupedByLevel().forEach(row ->
                result.put(((LogLevel) row[0]).name(), (Long) row[1])
        );
        return result;
    }

    private Map<String, Long> buildCountByLevelForService(String serviceName) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (LogLevel level : LogLevel.values()) {
            result.put(level.name(), 0L);
        }
        repository.countGroupedByLevelForService(serviceName).forEach(row ->
                result.put(((LogLevel) row[0]).name(), (Long) row[1])
        );
        return result;
    }

    private double computeErrorRate(long totalRecent, Instant oneHourAgo, String serviceName) {
        if (totalRecent == 0) return 0.0;
        boolean scoped = serviceName != null && !serviceName.isBlank();
        long errors = scoped
                ? repository.countByServiceNameAndLevelAndTimestampAfter(serviceName, LogLevel.ERROR, oneHourAgo)
                : repository.countByLevelAndTimestampAfter(LogLevel.ERROR, oneHourAgo);
        return Math.round((errors * 100.0 / totalRecent) * 10.0) / 10.0;
    }

    private List<TimeSeriesPoint> buildHourlyTimeSeries(int hours, String serviceName) {
        Instant since = Instant.now().minus(hours, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS);
        List<LogEntry> entries = (serviceName != null)
                ? repository.findByServiceNameAndTimestampAfterOrderByTimestampAsc(serviceName, since)
                : repository.findByTimestampAfterOrderByTimestampAsc(since);

        Map<String, Long> buckets = new LinkedHashMap<>();
        Instant cursor = since;
        Instant now = Instant.now().truncatedTo(ChronoUnit.HOURS);
        while (!cursor.isAfter(now)) {
            buckets.put(HOUR_FORMATTER.format(cursor), 0L);
            cursor = cursor.plus(1, ChronoUnit.HOURS);
        }

        entries.forEach(e -> {
            String bucket = HOUR_FORMATTER.format(e.getTimestamp().truncatedTo(ChronoUnit.HOURS));
            buckets.merge(bucket, 1L, Long::sum);
        });

        return buckets.entrySet().stream()
                .map(entry -> new TimeSeriesPoint(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
