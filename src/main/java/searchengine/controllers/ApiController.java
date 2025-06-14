package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.IndexingResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.PageService;
import searchengine.services.SiteCrawler.SiteCrawlerServiceImpl;
import searchengine.services.StatisticsService.StatisticsService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final SiteCrawlerServiceImpl siteCrawlerService;
    private final PageService pageService;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<?> startIndexing() {
        IndexingResponse response = siteCrawlerService.startIndexing();
        return response.result()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<?> stopIndexing() {
        IndexingResponse response = siteCrawlerService.stopIndexing();
        return response.result()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }
}
