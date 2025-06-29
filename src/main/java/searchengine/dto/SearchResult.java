package searchengine.dto;

import lombok.Builder;

@Builder
public record SearchResult(String siteUrl,
                           String siteName,
                           String uri,
                           String title,
                           String snippet,
                           double relevance) {
}