package com.arssedot.loganalyzer.web.controller;

import com.arssedot.loganalyzer.service.LogQueryService;
import com.arssedot.loganalyzer.service.MetricsService;
import com.arssedot.loganalyzer.web.dto.LogFilterDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final LogQueryService logQueryService;
    private final MetricsService metricsService;

    @GetMapping("/")
    public String dashboard(@ModelAttribute LogFilterDto filter, Model model) {
        model.addAttribute("metrics", metricsService.getSummary());
        model.addAttribute("logs", logQueryService.search(filter));
        model.addAttribute("filter", filter);
        model.addAttribute("services", logQueryService.getDistinctServices());
        return "dashboard";
    }
}
