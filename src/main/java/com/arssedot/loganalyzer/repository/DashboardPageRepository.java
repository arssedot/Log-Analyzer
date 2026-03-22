package com.arssedot.loganalyzer.repository;

import com.arssedot.loganalyzer.domain.DashboardPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DashboardPageRepository extends JpaRepository<DashboardPage, Long> {

    List<DashboardPage> findByUserIdOrderByPosition(Long userId);

    int countByUserId(Long userId);

    Optional<DashboardPage> findFirstByUserIdOrderByPosition(Long userId);
}
