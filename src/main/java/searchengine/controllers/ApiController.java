package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.SiteCrawler.SiteCrawlerService;
import searchengine.services.StatisticsService.StatisticsService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final SiteCrawlerService siteCrawlerService;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity startIndexing() {
        siteCrawlerService.startIndexing();
        return ResponseEntity.ok(1);
    }
}
