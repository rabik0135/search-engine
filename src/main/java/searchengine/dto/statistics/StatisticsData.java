package searchengine.dto.statistics;

import lombok.Builder;

import java.util.List;

@Builder
public record StatisticsData(TotalStatistics total,
                             List<DetailedStatisticsItem> detailed) {
}