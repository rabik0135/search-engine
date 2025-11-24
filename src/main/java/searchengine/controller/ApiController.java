package searchengine.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.IndexingResponse;
import searchengine.dto.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.service.SearchService;
import searchengine.service.SiteIndexingService;
import searchengine.service.StatisticsService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final SiteIndexingService siteIndexingService;
    private final SearchService searchService;

    @GetMapping("/statistics")
    public StatisticsResponse statistics() {
        return statisticsService.getStatistics();
    }

    @GetMapping("/startIndexing")
    public IndexingResponse startIndexing() {
        return siteIndexingService.startIndexing();
    }

    @GetMapping("/stopIndexing")
    public IndexingResponse stopIndexing() {
        return siteIndexingService.stopIndexing();
    }

    @PostMapping("/indexPage")
    public IndexingResponse addPageForIndexing(@RequestParam String url) {
        return siteIndexingService.indexOnePage(url);
    }

    @GetMapping("/search")
    public SearchResponse search(
            @RequestParam String query,
            @RequestParam(required = false) String site) {

        return searchService.search(query, site);
    }

}
