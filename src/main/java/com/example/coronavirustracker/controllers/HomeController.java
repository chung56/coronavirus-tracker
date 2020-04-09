package com.example.coronavirustracker.controllers;

import com.example.coronavirustracker.models.LocationStats;
import com.example.coronavirustracker.service.CoronaVirusDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    CoronaVirusDataService coronaVirusDataService;
//    have to create getter for service in CoronaVirusDataService

    @GetMapping("/")
    public String home(Model model){
        List<LocationStats> allStats = coronaVirusDataService.getAllStats();
        int totalCases = allStats.stream().mapToInt(stat->stat.getLatestTotalCases()).sum();
        int totalNewCases = allStats.stream().mapToInt(stat->stat.getDiffFromPreviousDay()).sum();

        model.addAttribute("locationStats",allStats);
        model.addAttribute("totalReportedCases",totalCases);
        model.addAttribute("totalNewCases",totalNewCases);
        return "home";
    }
}
