package searchengine.services.SiteIndexing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.model.Site;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.LemmaService.LemmaService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
public class SiteCrawlerFactory {
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaService lemmaService;

    public SiteCrawlerTask create(Site site, AtomicBoolean indexing) {
        String baseUrl = site.getUrl();
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        return new SiteCrawlerTask(baseUrl, site, pageRepository, siteRepository, lemmaService, ConcurrentHashMap.newKeySet(), indexing);
    }
}