package searchengine.services.SiteCrawler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.SiteFromConfig;
import searchengine.config.SitesList;
import searchengine.dto.IndexingResponse;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repository.SiteRepository;
import searchengine.services.SiteService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class SiteCrawlerServiceImpl implements SiteCrawlerService {
    private final SitesList sites;
    private final SiteService siteService;
    private final SiteRepository siteRepository;
    private final SiteCrawlerFactory crawlerFactory;

    private final AtomicBoolean indexing = new AtomicBoolean(false);
    private ForkJoinPool forkJoinPool;
    private final List<Site> activeSites = new CopyOnWriteArrayList<>();

    @Override
    public IndexingResponse startIndexing() {
        if (indexing.get()) {
            return new IndexingResponse(false, "Индексация уже запущена");
        }

        indexing.set(true);
        activeSites.clear();

        for (SiteFromConfig siteFromConfig : sites.getSites()) {
            new Thread(() -> {
                try {
                    indexSite(siteFromConfig);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        return new IndexingResponse(true, null);
    }

    @Override
    public IndexingResponse stopIndexing() {
        if (!indexing.get()) {
            return new IndexingResponse(false, "Индексация не запущена");
        }

        indexing.set(false);

        if (forkJoinPool != null && !forkJoinPool.isShutdown()) {
            forkJoinPool.shutdownNow();
        }

        activeSites.forEach(site -> siteService.updateSiteStatus(site, Status.FAILED, "Индексация остановлена пользователем"));
        return new IndexingResponse(true, null);
    }

    @Override
    public IndexingResponse indexOnePage(String siteUrl) {
      return sites.getSites().stream()
                .filter(siteFromConfig -> siteUrl.equals(siteFromConfig.url()))
                .findFirst()
                .map(this::indexSiteAndReturnSuccess)
                .orElseGet(() -> new IndexingResponse(false, "Данная страница находится за пределами сайтов, указанных в конфигурационном файле"));
    }

    private IndexingResponse indexSiteAndReturnSuccess(SiteFromConfig siteFromConfig) {
        indexSite(siteFromConfig);
        return new IndexingResponse(true, null);
    }

    @Override
    public boolean isIndexing() {
        return indexing.get();
    }

    private void indexSite(SiteFromConfig siteFromConfig) {
        siteService.deleteSiteData(siteFromConfig.url());

        Site site = siteService.createSiteFromConfig(siteFromConfig);
        activeSites.add(site);

        try {
            forkJoinPool = new ForkJoinPool();
            SiteCrawlerTask task = crawlerFactory.create(site);
            forkJoinPool.invoke(task);

            site.setStatus(Status.INDEXED);
        } catch (Exception e) {
            site.setStatus(Status.FAILED);
            site.setLastError(e.getMessage());
        }

        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);
        activeSites.remove(site);
    }
}