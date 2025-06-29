package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.IndexingResponse;
import searchengine.dto.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.PageService;
import searchengine.services.SearchService.SearchService;
import searchengine.services.SiteIndexing.SiteIndexingService;
import searchengine.services.SiteService;
import searchengine.services.StatisticsService.StatisticsService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ApiController {
    private final StatisticsService statisticsService;
    private final SiteIndexingService siteIndexingService;
    private final SearchService searchService;
    private final SiteService siteService;


    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        StatisticsResponse response = statisticsService.getStatistics();
        return response.isResult()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<?> startIndexing() {
        IndexingResponse response = siteIndexingService.startIndexing();
        return response.result()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<?> stopIndexing() {
        IndexingResponse response = siteIndexingService.stopIndexing();
        return response.result()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/indexPage")
    public ResponseEntity<?> addPageForIndexing(@RequestParam String url) {
        IndexingResponse response = siteIndexingService.indexOnePage(url);
        return response.result()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String query,
                                    @RequestParam(required = false) String siteUrl
    ) {
        SearchResponse response = searchService.search(query, siteUrl);
        return response.result()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }
}