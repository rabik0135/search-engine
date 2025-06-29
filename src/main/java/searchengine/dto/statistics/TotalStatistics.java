package searchengine.dto.statistics;

import lombok.Builder;

@Builder
public record TotalStatistics(int sites,
                              int pages,
                              int lemmas,
                              boolean indexing) {
}