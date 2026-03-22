package com.arssedot.loganalyzer.web.controller;

import com.arssedot.loganalyzer.domain.User;
import com.arssedot.loganalyzer.domain.Widget;
import com.arssedot.loganalyzer.service.WidgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/widgets")
@RequiredArgsConstructor
public class WidgetController {

    private final WidgetService widgetService;

    @GetMapping
    public List<WidgetDto> getWidgets(@AuthenticationPrincipal User user,
                                      @RequestParam(required = false) Long pageId) {
        return widgetService.getUserWidgets(user, pageId).stream()
                .map(WidgetDto::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WidgetDto addWidget(@AuthenticationPrincipal User user,
                               @RequestParam(required = false) Long pageId,
                               @RequestBody AddWidgetRequest request) {
        Widget widget = widgetService.addWidget(user, pageId, request.type());
        return WidgetDto.from(widget);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeWidget(@AuthenticationPrincipal User user, @PathVariable Long id) {
        widgetService.removeWidget(user, id);
    }

    @PutMapping("/order")
    public void reorder(@AuthenticationPrincipal User user,
                        @RequestParam(required = false) Long pageId,
                        @RequestBody List<Long> orderedIds) {
        widgetService.updatePositions(user, pageId, orderedIds);
    }

    public record WidgetDto(Long id, String type, String title, int position, String size) {
        static WidgetDto from(Widget w) {
            return new WidgetDto(w.getId(), w.getType(), w.getTitle(), w.getPosition(), w.getSize());
        }
    }

    public record AddWidgetRequest(String type) {}
}
