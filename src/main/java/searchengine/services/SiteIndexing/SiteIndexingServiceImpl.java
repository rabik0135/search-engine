package searchengine.services.SiteIndexing;

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
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class SiteIndexingServiceImpl implements SiteIndexingService {
    private final SitesList sites;
    private final SiteService siteService;
    private final SiteRepository siteRepository;
    private final SiteCrawlerFactory crawlerFactory;

    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final AtomicBoolean indexing = new AtomicBoolean(false);
    private final List<Site> activeSites = new CopyOnWriteArrayList<>();
    private final Set<Future<?>> runningTasks = ConcurrentHashMap.newKeySet();

    @Override
    public IndexingResponse startIndexing() {
        if (indexing.get()) {
            return new IndexingResponse(false, "Индексация уже запущена");
        }

        indexing.set(true);
        activeSites.clear();
        runningTasks.clear();

        for (SiteFromConfig siteFromConfig : sites.getSites()) {
            Future<?> future = executorService.submit(() -> {
                try {
                    indexSite(siteFromConfig);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            runningTasks.add(future);
        }

        return new IndexingResponse(true, null);
    }

    @Override
    public IndexingResponse stopIndexing() {
        if (!indexing.get()) {
            return new IndexingResponse(false, "Индексация не запущена");
        }

        indexing.set(false);

        for (Future<?> task : runningTasks) {
            task.cancel(true);
        }
        runningTasks.clear();

        activeSites.forEach(site -> siteService.updateSiteStatus(site, Status.FAILED, "Индексация остановлена пользователем"));
        return new IndexingResponse(true, null);
    }

    @Override
    public IndexingResponse indexOnePage(String siteUrl) {
      indexing.set(true);
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
            SiteCrawlerTask task = crawlerFactory.create(site, indexing);
            task.invoke();
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