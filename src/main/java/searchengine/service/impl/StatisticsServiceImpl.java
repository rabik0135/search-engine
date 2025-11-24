package searchengine.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.Site;
import searchengine.service.LemmaService;
import searchengine.service.PageService;
import searchengine.service.SiteIndexingService;
import searchengine.service.SiteService;
import searchengine.service.StatisticsService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SiteService siteService;
    private final PageService pageService;
    private final LemmaService lemmaService;
    private final SiteIndexingService siteIndexingService;

    @Override
    public StatisticsResponse getStatistics() {
        return StatisticsResponse.builder()
                .result(true)
                .statistics(getStatisticsData())
                .build();
    }

    private TotalStatistics getTotalStatistics() {
        return TotalStatistics.builder()
                .sites(siteService.getSitesCount())
                .pages(pageService.getPagesCount())
                .lemmas(lemmaService.getLemmasCount())
                .indexing(siteIndexingService.isIndexing())
                .build();
    }

    private DetailedStatisticsItem getDetailedStatisticsItem(Site site) {
        long timestampInSeconds = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
        return DetailedStatisticsItem.builder()
                .url(site.getUrl())
                .name(site.getName())
                .status(String.valueOf(site.getStatus()))
                .statusTime(timestampInSeconds)
                .error(site.getLastError())
                .pages(site.getPages().size())
                .lemmas(site.getLemmas().size())
                .build();
    }

    private List<DetailedStatisticsItem> getDetailedStatisticsItemList() {
        List<DetailedStatisticsItem> detailedStatisticsItems = new ArrayList<>();
        List<Site> sites = siteService.getAllSites();
        for (Site site : sites) {
            detailedStatisticsItems.add(getDetailedStatisticsItem(site));
        }
        return detailedStatisticsItems;
    }

    private StatisticsData getStatisticsData() {
        return StatisticsData.builder()
                .total(getTotalStatistics())
                .detailed(getDetailedStatisticsItemList())
                .build();
    }

}