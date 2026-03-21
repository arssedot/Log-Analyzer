package com.arssedot.loganalyzer.repository;

import com.arssedot.loganalyzer.domain.LogEntry;
import com.arssedot.loganalyzer.domain.LogLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface LogEntryRepository extends JpaRepository<LogEntry, Long>, JpaSpecificationExecutor<LogEntry> {

    List<LogEntry> findByTimestampAfterOrderByTimestampAsc(Instant since);

    long countByTimestampAfter(Instant since);

    long countByLevelAndTimestampAfter(LogLevel level, Instant since);

    @Query("SELECT l.level, COUNT(l) FROM LogEntry l GROUP BY l.level")
    List<Object[]> countGroupedByLevel();

    @Query("SELECT l.serviceName, COUNT(l) FROM LogEntry l GROUP BY l.serviceName ORDER BY COUNT(l) DESC")
    List<Object[]> findTopServices(Pageable pageable);

    @Query("SELECT DISTINCT l.serviceName FROM LogEntry l ORDER BY l.serviceName")
    List<String> findDistinctServiceNames();

    Page<LogEntry> findAllByOrderByTimestampDesc(Pageable pageable);
}
