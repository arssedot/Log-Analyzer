package com.arssedot.loganalyzer.repository;

import com.arssedot.loganalyzer.domain.Widget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WidgetRepository extends JpaRepository<Widget, Long> {

    List<Widget> findByUserIdOrderByPosition(Long userId);

    int countByUserId(Long userId);

    void deleteByUserId(Long userId);
}
