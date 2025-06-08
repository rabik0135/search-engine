package searchengine.services.SiteCrawler;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.services.SiteService;
import searchengine.util.LinkValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

import static java.lang.Thread.sleep;

@RequiredArgsConstructor
public class SiteCrawlerTask extends RecursiveAction {

    private final Site site;
    private final String baseUrl;
    private final String currentUrl;
    private final SiteCrawlerServiceImpl siteCrawlerService;
    private final SiteService siteService;
    private final Set<String> visitedUrls;
    private final LinkValidator linkValidator;

    @Override
    protected void compute() {
        if (!linkValidator.isValid(currentUrl, baseUrl)) {
            return;
        }

        synchronized (visitedUrls) {
            if (visitedUrls.contains(currentUrl) || pageExistsInDb(currentUrl)) {
                return;
            }
            visitedUrls.add(currentUrl);
        }

        try {
            Page page = siteCrawlerService.processPage(currentUrl, site);
            siteService.updateSiteStatusTime(site);

            List<String> newLinks = extractLinks(page.getContent());
            List<SiteCrawlerTask> subtasks = new ArrayList<>();

            for (String link : newLinks) {
                synchronized (visitedUrls) {
                    if (!visitedUrls.contains(link) && !pageExistsInDb(link)) {
                        subtasks.add(new SiteCrawlerTask(site, baseUrl, link, siteCrawlerService, siteService, visitedUrls, linkValidator));
                    }
                }
            }

            ForkJoinTask.invokeAll(subtasks);
            Thread.sleep(siteCrawlerService.getPageProcessingTimeoutMs());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private boolean pageExistsInDb(String url) {
        String path = siteCrawlerService.getPathFromUrl(url);
        return siteCrawlerService.getPageRepository().existsBySiteAndPath(site, path);
    }

    private List<String> extractLinks(String content) {
        try {
            sleep(500);
            Document document = Jsoup.parse(content, currentUrl);
            return document.select("a[href]").stream()
                    .map(link -> link.attr("abs:href"))
                    .filter(link -> !link.isEmpty())
                    .filter(link -> linkValidator.isValid(link, baseUrl))
                    .distinct()
                    .toList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
