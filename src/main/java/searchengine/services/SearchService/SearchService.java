package searchengine.services.SearchService;

import searchengine.dto.SearchResponse;

public interface SearchService {
    SearchResponse search(String query, String siteUrl);
}
