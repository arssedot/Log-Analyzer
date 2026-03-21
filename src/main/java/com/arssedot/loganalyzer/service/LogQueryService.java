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

    private final LogEntryRepository repository;

    @Transactional(readOnly = true)
    public Page<LogEntry> search(LogFilterDto filter) {
        Specification<LogEntry> spec = buildSpec(filter);
        PageRequest pageable = PageRequest.of(
                filter.getPage(),
                filter.getSize(),
                Sort.by(Sort.Direction.DESC, "timestamp")
        );
        return repository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctServices() {
        return repository.findDistinctServiceNames();
    }

    private Specification<LogEntry> buildSpec(LogFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hasText(filter.getLevel())) {
                try {
                    LogLevel level = LogLevel.valueOf(filter.getLevel());
                    predicates.add(cb.equal(root.get("level"), level));
                } catch (IllegalArgumentException ignored) {
                }
            }

            if (hasText(filter.getService())) {
                predicates.add(cb.like(
                        cb.lower(root.get("serviceName")),
                        "%" + filter.getService().toLowerCase() + "%"
                ));
            }

            Instant from = parseDateTime(filter.getFrom());
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), from));
            }

            Instant to = parseDateTime(filter.getTo());
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), to));
            }

            if (hasText(filter.getQ())) {
                predicates.add(cb.like(
                        cb.lower(root.get("message")),
                        "%" + filter.getQ().toLowerCase() + "%"
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static Instant parseDateTime(String value) {
        if (!hasText(value)) return null;
        try {
            return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
