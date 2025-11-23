package searchengine.service;

import searchengine.dto.SearchResponse;

public interface SearchService {
    SearchResponse search(String query, String siteUrl);
}
