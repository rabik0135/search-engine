package searchengine.services.SiteIndexing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.lemmaService.LemmaServiceImpl;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveAction;


public class SiteCrawlerTask extends RecursiveAction {
    private final static int DELAY_MS = 700;

    private final String url;
    private final Site site;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaServiceImpl lemmaService;
    private final Set<String> visited;


    public SiteCrawlerTask(String url, Site site, PageRepository pageRepository, SiteRepository siteRepository, LemmaServiceImpl lemmaService, Set<String> visited) {
        this.url = url;
        this.site = site;
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.lemmaService = lemmaService;
        this.visited = visited;
    }

    @Override
    protected void compute() {
        try {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Task interrupted before start");
            }

            Thread.sleep(DELAY_MS);

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Task interrupted during delay");
            }

            Document document = Jsoup.connect(url)
                    .userAgent("HeliontSearchBot/1.0 (+http://heliont.ru/bot.html)")
                    .referrer("http://www.google.com")
                    .get();

            int statusCode = Jsoup.connect(url).execute().statusCode();
            String path = normalizePath(url);

            if (pageRepository.existsBySiteAndPath(site, path)) {
                return;
            }

            Page page = Page.builder()
                    .site(site)
                    .code(statusCode)
                    .content(document.outerHtml())
                    .path(path)
                    .build();

            pageRepository.save(page);
            lemmaService.saveLemmas(site, page);

            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);

            Elements links = document.select("a[href]");
            List<SiteCrawlerTask> subtasks = new ArrayList<>();

            for (Element link : links) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Task interrupted during link parsing");
                }

                String absUrl = link.absUrl("href");
                if (!absUrl.startsWith(site.getUrl())) {
                    continue;
                }

                String normalizedSubPath = normalizePath(absUrl);
                String fullUrl = site.getUrl() + normalizedSubPath;

                synchronized (visited) {
                    if (visited.contains(fullUrl)) {
                        continue;
                    }
                    visited.add(fullUrl);
                }

                subtasks.add(new SiteCrawlerTask(fullUrl, site, pageRepository, siteRepository, lemmaService, visited));
            }
            invokeAll(subtasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            site.setStatus(Status.FAILED);
            site.setLastError(e.getMessage());
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);
        } catch (Exception e) {
            site.setStatusTime(LocalDateTime.now());
            site.setStatus(Status.FAILED);
            site.setLastError(e.getMessage());
            siteRepository.save(site);
        }
    }

    private String normalizePath(String rawUrl) {
        try {
            URI uri = new URI(rawUrl);
            String path = uri.getPath();

            if (path == null || path.isEmpty()) {
                return "/";
            }

            path = path.replaceAll("/{2,}", "/");

            if (path.endsWith("/") && path.length() > 1) {
                path = path.substring(0, path.length() - 1);
            }

            return path.toLowerCase();
        } catch (URISyntaxException e) {
            return "/";
        }
    }
}