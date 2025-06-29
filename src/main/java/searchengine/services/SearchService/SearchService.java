package searchengine.services.SearchService;

import org.springframework.http.ResponseEntity;
import searchengine.dto.SearchResponse;

public interface SearchService {

    SearchResponse search(String query, String siteUrl);
}
