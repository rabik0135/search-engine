package searchengine.dto.statistics;

import lombok.Builder;

@Builder
public record StatisticsResponse(
        boolean result,

        StatisticsData statistics
) {
}
