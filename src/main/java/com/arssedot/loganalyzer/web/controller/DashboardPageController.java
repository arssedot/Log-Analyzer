package com.arssedot.loganalyzer.web.controller;

import com.arssedot.loganalyzer.domain.User;
import com.arssedot.loganalyzer.service.DashboardPageService;
import com.arssedot.loganalyzer.service.DashboardPageService.PageDto;
import com.arssedot.loganalyzer.service.DashboardPageService.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pages")
@RequiredArgsConstructor
public class DashboardPageController {

    private final DashboardPageService pageService;

    @GetMapping
    public List<PageDto> getPages(@AuthenticationPrincipal User user) {
        return pageService.getPages(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PageDto createPage(@AuthenticationPrincipal User user,
                              @RequestBody PageRequest request) {
        return pageService.createPage(user, request);
    }

    @PutMapping("/{id}")
    public PageDto updatePage(@AuthenticationPrincipal User user,
                              @PathVariable Long id,
                              @RequestBody PageRequest request) {
        return pageService.updatePage(user, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePage(@AuthenticationPrincipal User user, @PathVariable Long id) {
        pageService.deletePage(user, id);
    }
}
