package searchengine.services.SiteIndexing;

import searchengine.dto.IndexingResponse;

public interface SiteIndexingService {
    IndexingResponse startIndexing();
    IndexingResponse stopIndexing();
    IndexingResponse  indexOnePage(String siteUrl);
    boolean isIndexing();
}