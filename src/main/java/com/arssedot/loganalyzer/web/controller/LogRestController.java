package com.arssedot.loganalyzer.web.controller;

import com.arssedot.loganalyzer.domain.LogEntry;
import com.arssedot.loganalyzer.service.LogIngestionService;
import com.arssedot.loganalyzer.service.LogQueryService;
import com.arssedot.loganalyzer.web.dto.LogFilterDto;
import com.arssedot.loganalyzer.web.dto.LogIngestRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogRestController {

    private final LogIngestionService ingestionService;
    private final LogQueryService queryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LogEntry ingest(@Valid @RequestBody LogIngestRequest request) {
        return ingestionService.ingest(request);
    }

    @GetMapping
    public Page<LogEntry> search(LogFilterDto filter) {
        return queryService.search(filter);
    }
}
