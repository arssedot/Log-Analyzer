package com.arssedot.loganalyzer.service;

import com.arssedot.loganalyzer.domain.LogEntry;
import com.arssedot.loganalyzer.domain.LogLevel;
import com.arssedot.loganalyzer.repository.LogEntryRepository;
import com.arssedot.loganalyzer.web.dto.LogFilterDto;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LogQueryService {

    private final LogEntryRepository logEntryRepository;

    @Transactional(readOnly = true)
    public Page<LogEntry> search(LogFilterDto filter) {
        Specification<LogEntry> spec = buildSpec(filter);
        PageRequest pageable = PageRequest.of(
                filter.getPage(),
                filter.getSize(),
                Sort.by(Sort.Direction.DESC, "timestamp")
        );
        return logEntryRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctServices() {
        return logEntryRepository.findDistinctServiceNames();
    }

    @Transactional
    public long delete(LogFilterDto filter) {
        boolean hasFilter = hasText(filter.getLevel())
                || hasText(filter.getService())
                || hasText(filter.getFrom())
                || hasText(filter.getTo())
                || hasText(filter.getQ());

        if (!hasFilter) {
            long totalCount = logEntryRepository.count();
            logEntryRepository.deleteAllInBatch();
            return totalCount;
        }

        List<LogEntry> entriesToDelete = logEntryRepository.findAll(buildSpec(filter));
        if (!entriesToDelete.isEmpty()) {
            logEntryRepository.deleteAllInBatch(entriesToDelete);
        }
        return entriesToDelete.size();
    }

    private Specification<LogEntry> buildSpec(LogFilterDto filter) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hasText(filter.getLevel())) {
                try {
                    LogLevel logLevel = LogLevel.valueOf(filter.getLevel());
                    predicates.add(criteriaBuilder.equal(root.get("level"), logLevel));
                } catch (IllegalArgumentException ignored) {
                }
            }

            if (hasText(filter.getService())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("serviceName")),
                        "%" + filter.getService().toLowerCase() + "%"
                ));
            }

            Instant fromInstant = parseDateTime(filter.getFrom());
            if (fromInstant != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), fromInstant));
            }

            Instant toInstant = parseDateTime(filter.getTo());
            if (toInstant != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), toInstant));
            }

            if (hasText(filter.getQ())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("message")),
                        "%" + filter.getQ().toLowerCase() + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static Instant parseDateTime(String value) {
        if (!hasText(value)) return null;
        try {
            return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }
}
