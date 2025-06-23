package searchengine.services.SiteIndexing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.model.Site;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.lemmaService.LemmaServiceImpl;

import java.util.HashSet;

@Component
@RequiredArgsConstructor
public class SiteCrawlerFactory {

    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaServiceImpl lemmaService;

    public SiteCrawlerTask create(Site site) {
        return new SiteCrawlerTask(site.getUrl(), site, pageRepository, siteRepository, lemmaService ,new HashSet<>());
    }
}