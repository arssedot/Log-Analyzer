package com.arssedot.loganalyzer.service;

import com.arssedot.loganalyzer.domain.LogEntry;
import com.arssedot.loganalyzer.domain.LogLevel;
import com.arssedot.loganalyzer.repository.LogEntryRepository;
import com.arssedot.loganalyzer.web.dto.LogIngestRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogIngestionService {

    private final LogEntryRepository repository;

    @Transactional
    public LogEntry ingest(LogIngestRequest request) {
        LogLevel level;
        try {
            level = LogLevel.valueOf(request.level());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown log level '{}', defaulting to INFO", request.level());
            level = LogLevel.INFO;
        }

        LogEntry entry = LogEntry.builder()
                .timestamp(request.timestamp() != null ? request.timestamp() : Instant.now())
                .level(level)
                .serviceName(request.serviceName())
                .message(request.message())
                .traceId(request.traceId())
                .host(request.host())
                .build();

        return repository.save(entry);
    }
}
