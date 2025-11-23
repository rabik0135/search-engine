package searchengine.service;

import searchengine.dto.IndexingResponse;

public interface SiteIndexingService {
    IndexingResponse startIndexing();
    IndexingResponse stopIndexing();
    IndexingResponse  indexOnePage(String siteUrl);
    boolean isIndexing();
}