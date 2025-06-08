package searchengine.services.SiteCrawler;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import searchengine.config.SiteFromConfig;
import searchengine.config.SitesList;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.PageService;
import searchengine.services.SiteService;
import searchengine.util.LinkValidator;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

@RequiredArgsConstructor
@Service
public class SiteCrawlerServiceImpl implements SiteCrawlerService {

    private final SitesList sites;
    private final SiteService siteService;
    private final PageRepository pageRepository;
    private final LinkValidator linkValidator;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final SiteRepository siteRepository;

    @Getter
    @Value("${indexing-settings.page-processing-timeout-ms}")
    private int pageProcessingTimeoutMs;

    @Value("${indexing.user-agent}")
    private String userAgent;

    @Value("${indexing.referrer}")
    private String referrer;

    @Value("${indexing.delay-ms}")
    private int delayMs;

    @Value("${indexing.timeout-ms}")
    private int timeoutMs;



    @Override
    public void startIndexing() {
        /*sites.getSites().forEach(
                siteFromConfig -> executorService.submit(() -> indexSite(siteFromConfig))
        );*/
        sites.getSites().forEach(this::indexSite);
    }


    public void indexSite(SiteFromConfig siteFromConfig) {
        siteService.deleteSiteData(siteFromConfig.url());

        Site site = siteService.createSiteFromConfig(siteFromConfig);

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        Set<String> visitedUrls = Collections.synchronizedSet(new HashSet<>());
        try {
            forkJoinPool.invoke(new SiteCrawlerTask(site, siteFromConfig.url(), siteFromConfig.url(), this, siteService, visitedUrls, linkValidator));

            siteService.updateSiteStatus(site, Status.INDEXED, null);
        } catch (Exception e) {
            siteService.updateSiteStatus(site, Status.FAILED, "Indexing failed: " + e.getMessage());
        }
    }



    public Page processPage(String url, Site site) {
        try {
            Connection.Response response = Jsoup.connect(url)
                    .userAgent(userAgent)
                    .referrer(referrer)
                    .timeout(timeoutMs)
                    .execute();

            Page page = Page.builder()
                    .site(site)
                    .path(getPathFromUrl(url))
                    .code(response.statusCode())
                    .content(response.parse().html())
                    .build();

            site.getPages().add(page);
            siteRepository.save(site);
            return pageRepository.save(page);
        } catch (IOException e) {
            Page errorPage = Page.builder()
                    .site(site)
                    .path(getPathFromUrl(url))
                    .code(500)
                    .content("Error: " + e.getMessage())
                    .build();

            site.getPages().add(errorPage);
            siteRepository.save(site);
            return pageRepository.save(errorPage);
        }
    }

    public String getPathFromUrl(String url) {
        try {
            URI uri = new URI(url).normalize();
            return uri.getPath() + (uri.getQuery() != null ? "?" + uri.getQuery() : "");
        } catch (URISyntaxException e) {
            return url;
        }
    }

    public PageRepository getPageRepository() {
        return pageRepository;
    }
}
