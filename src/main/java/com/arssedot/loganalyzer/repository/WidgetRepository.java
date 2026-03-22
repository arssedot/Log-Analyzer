package com.arssedot.loganalyzer.repository;

import com.arssedot.loganalyzer.domain.Widget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WidgetRepository extends JpaRepository<Widget, Long> {

    List<Widget> findByUserIdOrderByPosition(Long userId);

    List<Widget> findByPageIdOrderByPosition(Long pageId);

    int countByUserId(Long userId);

    int countByPageId(Long pageId);

    void deleteByUserId(Long userId);
}
