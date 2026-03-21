package com.arssedot.loganalyzer.service;

import com.arssedot.loganalyzer.domain.User;
import com.arssedot.loganalyzer.domain.Widget;
import com.arssedot.loganalyzer.repository.WidgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class WidgetService {

    private final WidgetRepository widgetRepository;

    @Transactional(readOnly = true)
    public List<Widget> getUserWidgets(User user) {
        List<Widget> widgets = widgetRepository.findByUserIdOrderByPosition(user.getId());
        if (widgets.isEmpty()) {
            return createDefaultWidgets(user);
        }
        return widgets;
    }

    @Transactional
    public Widget addWidget(User user, String type) {
        int nextPos = widgetRepository.countByUserId(user.getId());
        Widget widget = Widget.builder()
                .user(user)
                .type(type)
                .title(defaultTitle(type))
                .size(defaultSize(type))
                .position(nextPos)
                .build();
        return widgetRepository.save(widget);
    }

    @Transactional
    public void removeWidget(User user, Long widgetId) {
        Widget widget = widgetRepository.findById(widgetId)
                .orElseThrow(() -> new NoSuchElementException("Widget not found: " + widgetId));
        if (!widget.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Widget does not belong to current user");
        }
        widgetRepository.delete(widget);
    }

    @Transactional
    public List<Widget> updatePositions(User user, List<Long> orderedIds) {
        List<Widget> widgets = widgetRepository.findByUserIdOrderByPosition(user.getId());
        for (Widget w : widgets) {
            int idx = orderedIds.indexOf(w.getId());
            if (idx >= 0) w.setPosition(idx);
        }
        return widgetRepository.saveAll(widgets);
    }

    private List<Widget> createDefaultWidgets(User user) {
        record Def(String type, String size, int pos) {}
        List<Def> defaults = List.of(
                new Def("STAT_TOTAL",      "SMALL",  0),
                new Def("STAT_ERRORS",     "SMALL",  1),
                new Def("STAT_WARNS",      "SMALL",  2),
                new Def("STAT_ERROR_RATE", "SMALL",  3),
                new Def("CHART_TIMELINE",  "LARGE",  4),
                new Def("CHART_LEVEL",     "MEDIUM", 5)
        );
        List<Widget> list = defaults.stream()
                .map(d -> Widget.builder()
                        .user(user)
                        .type(d.type())
                        .title(defaultTitle(d.type()))
                        .size(d.size())
                        .position(d.pos())
                        .build())
                .toList();
        return widgetRepository.saveAll(list);
    }

    private static String defaultTitle(String type) {
        return switch (type) {
            case "STAT_TOTAL"      -> "Total Logs";
            case "STAT_ERRORS"     -> "Errors";
            case "STAT_WARNS"      -> "Warnings";
            case "STAT_ERROR_RATE" -> "Error Rate (1h)";
            case "CHART_LEVEL"     -> "By Log Level";
            case "CHART_TIMELINE"  -> "Logs Over Time";
            case "CHART_SERVICES"  -> "Top Services";
            case "TABLE_RECENT"    -> "Recent Logs";
            default                -> type;
        };
    }

    private static String defaultSize(String type) {
        return switch (type) {
            case "STAT_TOTAL", "STAT_ERRORS", "STAT_WARNS", "STAT_ERROR_RATE" -> "SMALL";
            case "CHART_TIMELINE", "TABLE_RECENT"                              -> "LARGE";
            case "CHART_LEVEL", "CHART_SERVICES"                               -> "MEDIUM";
            default                                                             -> "MEDIUM";
        };
    }
}
