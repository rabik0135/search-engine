package searchengine.dto;

import java.util.List;

public record SearchResponse(boolean result,
                             Integer count,
                             List<SearchResult> data,
                             String error) {

    public static SearchResponse success(List<SearchResult> results) {
        return new SearchResponse(true, results.size(), results, null);
    }

    public static SearchResponse error(String errorMessage) {
        return new SearchResponse(false, null, null, errorMessage);
    }
}
