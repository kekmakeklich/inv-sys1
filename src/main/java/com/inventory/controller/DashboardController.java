package com.inventory.controller;

import com.inventory.service.InventoryReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {
    private final InventoryReportService reportService;

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("report", reportService.generateInventoryReport());
        return "dashboard";
    }
}