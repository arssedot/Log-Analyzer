package com.arssedot.loganalyzer.service;

import com.arssedot.loganalyzer.domain.DashboardPage;
import com.arssedot.loganalyzer.domain.User;
import com.arssedot.loganalyzer.domain.Widget;
import com.arssedot.loganalyzer.repository.DashboardPageRepository;
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
    private final DashboardPageRepository pageRepository;

    @Transactional(readOnly = true)
    public List<Widget> getUserWidgets(User user, Long pageId) {
        DashboardPage page = resolvePageForUser(user, pageId);
        List<Widget> widgets = widgetRepository.findByPageIdOrderByPosition(page.getId());
        if (widgets.isEmpty()) {
            return createDefaultWidgets(user, page);
        }
        return widgets;
    }

    @Transactional
    public Widget addWidget(User user, Long pageId, String type) {
        DashboardPage page = resolvePageForUser(user, pageId);
        int nextPos = widgetRepository.countByPageId(page.getId());
        Widget widget = Widget.builder()
                .user(user)
                .page(page)
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
    public List<Widget> updatePositions(User user, Long pageId, List<Long> orderedIds) {
        DashboardPage page = resolvePageForUser(user, pageId);
        List<Widget> widgets = widgetRepository.findByPageIdOrderByPosition(page.getId());
        for (Widget w : widgets) {
            int idx = orderedIds.indexOf(w.getId());
            if (idx >= 0) w.setPosition(idx);
        }
        return widgetRepository.saveAll(widgets);
    }

    private DashboardPage resolvePageForUser(User user, Long pageId) {
        if (pageId != null) {
            DashboardPage page = pageRepository.findById(pageId)
                    .orElseThrow(() -> new NoSuchElementException("Page not found: " + pageId));
            if (!page.getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("Page does not belong to current user");
            }
            return page;
        }
        return pageRepository.findFirstByUserIdOrderByPosition(user.getId())
                .orElseGet(() -> {
                    DashboardPage def = DashboardPage.builder()
                            .user(user).name("Default").position(0).build();
                    return pageRepository.save(def);
                });
    }

    private List<Widget> createDefaultWidgets(User user, DashboardPage page) {
        record Def(String type, String size, int pos) {}
        List<Def> defaults = List.of(
                new Def("STAT_TOTAL",      "SMALL",  0),
                new Def("STAT_ERRORS",     "SMALL",  1),
                new Def("STAT_WARNS",      "SMALL",  2),
                new Def("STAT_ERROR_RATE", "SMALL",  3),
                new Def("STAT_LAST_HOUR",  "SMALL",  4),
                new Def("STAT_SERVICES",   "SMALL",  5),
                new Def("CHART_TIMELINE",  "LARGE",  6),
                new Def("CHART_LEVEL",     "MEDIUM", 7)
        );
        List<Widget> list = defaults.stream()
                .map(d -> Widget.builder()
                        .user(user)
                        .page(page)
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
            case "STAT_TOTAL"       -> "Total Logs";
            case "STAT_ERRORS"      -> "Errors";
            case "STAT_WARNS"       -> "Warnings";
            case "STAT_ERROR_RATE"  -> "Error Rate (1h)";
            case "STAT_INFOS"       -> "Info Logs";
            case "STAT_LAST_HOUR"   -> "Last Hour";
            case "STAT_SERVICES"    -> "Services";
            case "CHART_LEVEL"      -> "By Log Level";
            case "CHART_TIMELINE"   -> "Logs Over Time";
            case "CHART_SERVICES"   -> "Top Services";
            case "CHART_LEVEL_BAR"  -> "Level Distribution";
            case "TABLE_RECENT"     -> "Recent Logs";
            case "TABLE_ERRORS"     -> "Recent Errors";
            case "GAUGE_CPU"        -> "CPU Load";
            case "GAUGE_RAM"        -> "Memory";
            case "GAUGE_DISK"       -> "Disk Usage";
            default                 -> type;
        };
    }

    private static String defaultSize(String type) {
        return switch (type) {
            case "STAT_TOTAL", "STAT_ERRORS", "STAT_WARNS",
                 "STAT_ERROR_RATE", "STAT_INFOS", "STAT_LAST_HOUR",
                 "STAT_SERVICES"                              -> "SMALL";
            case "CHART_TIMELINE", "TABLE_RECENT",
                 "TABLE_ERRORS"                              -> "LARGE";
            case "CHART_LEVEL", "CHART_SERVICES",
                 "CHART_LEVEL_BAR",
                 "GAUGE_CPU", "GAUGE_RAM", "GAUGE_DISK"      -> "MEDIUM";
            default                                          -> "MEDIUM";
        };
    }
}
