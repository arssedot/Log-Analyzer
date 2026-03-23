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

    private final LogEntryRepository logEntryRepository;

    @Transactional(readOnly = true)
    public MetricsSummaryDto getSummary() {
        return getSummary(null);
    }

    @Transactional(readOnly = true)
    public MetricsSummaryDto getSummary(String serviceName) {
        boolean isScopedToService = serviceName != null && !serviceName.isBlank();

        long totalLogs = isScopedToService
                ? logEntryRepository.countByServiceName(serviceName)
                : logEntryRepository.count();

        Map<String, Long> countByLevel = isScopedToService
                ? buildCountByLevelForService(serviceName)
                : buildCountByLevel();

        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        long logsLastHour = isScopedToService
                ? logEntryRepository.countByServiceNameAndTimestampAfter(serviceName, oneHourAgo)
                : logEntryRepository.countByTimestampAfter(oneHourAgo);

        double errorRate = computeErrorRate(logsLastHour, oneHourAgo, serviceName);

        long servicesCount = isScopedToService ? 1L : logEntryRepository.countDistinctServiceNames();

        List<ServiceStat> topServices = isScopedToService
                ? List.of(new ServiceStat(serviceName, totalLogs))
                : logEntryRepository.findTopServices(PageRequest.of(0, 10)).stream()
                        .map(serviceRow -> new ServiceStat((String) serviceRow[0], (Long) serviceRow[1]))
                        .toList();

        List<TimeSeriesPoint> timeSeries = buildHourlyTimeSeries(24, isScopedToService ? serviceName : null);

        return new MetricsSummaryDto(totalLogs, countByLevel, errorRate, topServices, timeSeries, logsLastHour, servicesCount);
    }

    private Map<String, Long> buildCountByLevel() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (LogLevel level : LogLevel.values()) {
            result.put(level.name(), 0L);
        }
        logEntryRepository.countGroupedByLevel().forEach(levelRow ->
                result.put(((LogLevel) levelRow[0]).name(), (Long) levelRow[1])
        );
        return result;
    }

    private Map<String, Long> buildCountByLevelForService(String serviceName) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (LogLevel level : LogLevel.values()) {
            result.put(level.name(), 0L);
        }
        logEntryRepository.countGroupedByLevelForService(serviceName).forEach(levelRow ->
                result.put(((LogLevel) levelRow[0]).name(), (Long) levelRow[1])
        );
        return result;
    }

    private double computeErrorRate(long totalLogsInLastHour, Instant oneHourAgo, String serviceName) {
        if (totalLogsInLastHour == 0) return 0.0;
        boolean isScopedToService = serviceName != null && !serviceName.isBlank();
        long errorCount = isScopedToService
                ? logEntryRepository.countByServiceNameAndLevelAndTimestampAfter(serviceName, LogLevel.ERROR, oneHourAgo)
                : logEntryRepository.countByLevelAndTimestampAfter(LogLevel.ERROR, oneHourAgo);
        return Math.round((errorCount * 100.0 / totalLogsInLastHour) * 10.0) / 10.0;
    }

    private List<TimeSeriesPoint> buildHourlyTimeSeries(int hoursBack, String serviceName) {
        Instant since = Instant.now().minus(hoursBack, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS);
        List<LogEntry> entries = (serviceName != null)
                ? logEntryRepository.findByServiceNameAndTimestampAfterOrderByTimestampAsc(serviceName, since)
                : logEntryRepository.findByTimestampAfterOrderByTimestampAsc(since);

        Map<String, Long> buckets = new LinkedHashMap<>();
        Instant cursor = since;
        Instant nowTruncated = Instant.now().truncatedTo(ChronoUnit.HOURS);
        while (!cursor.isAfter(nowTruncated)) {
            buckets.put(HOUR_FORMATTER.format(cursor), 0L);
            cursor = cursor.plus(1, ChronoUnit.HOURS);
        }

        entries.forEach(logEntry -> {
            String hourBucket = HOUR_FORMATTER.format(logEntry.getTimestamp().truncatedTo(ChronoUnit.HOURS));
            buckets.merge(hourBucket, 1L, Long::sum);
        });

        return buckets.entrySet().stream()
                .map(bucketEntry -> new TimeSeriesPoint(bucketEntry.getKey(), bucketEntry.getValue()))
                .collect(Collectors.toList());
    }
}
