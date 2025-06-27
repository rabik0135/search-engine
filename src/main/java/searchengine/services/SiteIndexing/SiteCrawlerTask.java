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
import searchengine.services.LemmaService.LemmaService;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicBoolean;


public class SiteCrawlerTask extends RecursiveAction {
    private final static int DELAY_MS = 700;

    private final String url;
    private final Site site;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaService lemmaService;
    private final Set<String> visited;
    private final AtomicBoolean indexing;


    public SiteCrawlerTask(String url, Site site, PageRepository pageRepository, SiteRepository siteRepository, LemmaService lemmaService, Set<String> visited, AtomicBoolean indexing) {
        this.url = url;
        this.site = site;
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.lemmaService = lemmaService;
        this.visited = visited;
        this.indexing = indexing;
    }

    @Override
    protected void compute() {
        if (!indexing.get()) {
            return;
        }

        try {
            Thread.sleep(DELAY_MS);

            if (!indexing.get()) {
                return;
            }

            var connection = Jsoup.connect(url)
                    .userAgent("HeliontSearchBot/1.0 (+http://heliont.ru/bot.html)")
                    .referrer("http://www.google.com")
                    .ignoreHttpErrors(true)
                    .timeout(10_000);

            var response = connection.execute();
            String contentType = response.contentType();

            if (contentType == null ||
                    !(contentType.startsWith("text/") ||
                            contentType.contains("xml") ||
                            contentType.startsWith("html"))){
                return;
            }

            Document document;
            try {
                document = response.parse();
            } catch (Exception e) {
                return;
            }
            int statusCode = response.statusCode();
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
            lemmaService.processPage(page);
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);

            Elements links = document.select("a[href]");
            List<SiteCrawlerTask> subtasks = new ArrayList<>();

            for (Element link : links) {
                if (!indexing.get()) {
                    return;
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

                subtasks.add(new SiteCrawlerTask(fullUrl, site, pageRepository, siteRepository, lemmaService, visited, indexing));
            }
            invokeAll(subtasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            failSite("Индексация прервана", e);
        } catch (Exception e) {
            failSite("Ошибка при индексации", e);
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

            return path.endsWith("/") && path.length() > 1 ? path.substring(0, path.length() - 1) : path.toLowerCase();
        } catch (URISyntaxException e) {
            return "/";
        }
    }


    private void failSite(String message, Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Unhandled content type")) {
                return;
            }

        site.setStatus(Status.FAILED);
        site.setLastError(message + ": " + e.getMessage());
        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);
    }
}