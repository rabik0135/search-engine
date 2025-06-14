package searchengine.services.SiteCrawler;

import searchengine.dto.IndexingResponse;

public interface SiteCrawlerService {
    IndexingResponse startIndexing();
    IndexingResponse stopIndexing();
    boolean isIndexing();
}
