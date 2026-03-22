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

    @Query("SELECT COUNT(DISTINCT l.serviceName) FROM LogEntry l")
    long countDistinctServiceNames();

    @Query("SELECT l FROM LogEntry l WHERE l.level = :level ORDER BY l.timestamp DESC")
    List<LogEntry> findTopByLevelOrderByTimestampDesc(@Param("level") LogLevel level, Pageable pageable);

    Page<LogEntry> findAllByOrderByTimestampDesc(Pageable pageable);

    // Service-scoped variants for per-page metrics
    long countByServiceName(String serviceName);

    long countByServiceNameAndTimestampAfter(String serviceName, Instant since);

    long countByServiceNameAndLevelAndTimestampAfter(String serviceName, LogLevel level, Instant since);

    @Query("SELECT l.level, COUNT(l) FROM LogEntry l WHERE l.serviceName = :svc GROUP BY l.level")
    List<Object[]> countGroupedByLevelForService(@Param("svc") String serviceName);

    @Query("SELECT l FROM LogEntry l WHERE l.serviceName = :svc AND l.timestamp > :since ORDER BY l.timestamp ASC")
    List<LogEntry> findByServiceNameAndTimestampAfterOrderByTimestampAsc(@Param("svc") String serviceName, @Param("since") Instant since);
}
