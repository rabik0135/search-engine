package searchengine.service.impl;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.dto.SearchResponse;
import searchengine.dto.SearchResult;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.SiteRepository;
import searchengine.service.LemmaService;
import searchengine.service.SearchService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private static final double PERCENTAGE = 0.7;

    private final LemmaService lemmaService;
    private final LemmaRepository lemmaRepository;
    private final SiteRepository siteRepository;
    private final IndexRepository indexRepository;

    @Override
    public SearchResponse search(String query, String siteUrl) {
        if (query == null || query.trim().isEmpty()) {
            return SearchResponse.error("Задан пустой поисковой запрос");
        }

        Set<String> lemmas = lemmaService.getLemmaSet(query.toLowerCase());
        if (lemmas.isEmpty()) {
            return SearchResponse.error("Не удалось выделить леммы из запроса");
        }

        List<Site> sites = (siteUrl == null)
                ? siteRepository.findAll()
                : List.of(siteRepository.findByUrl(siteUrl).orElse(null));

        if (sites.isEmpty() || sites.get(0) == null) {
            return SearchResponse.error("Нет сайтов для поиска");
        }

        List<Lemma> filteredLemmas  = getFilteredLemmas(lemmas, sites);
        if (filteredLemmas.isEmpty()) {
            return SearchResponse.error("");
        }

        filteredLemmas.sort(Comparator.comparing(Lemma::getFrequency));

        Set<Page> filteredPages = findFilteredPages(filteredLemmas);
        if (filteredPages.isEmpty()) {
            return SearchResponse.success(List.of());
        }

        List<Long> lemmaIds = filteredLemmas.stream()
                .map(Lemma::getId)
                .collect(Collectors.toList());
        Map<Page, Double> relevanceMap = calculateRelevance(filteredPages, lemmaIds);

        double maxRelevance = relevanceMap.values().stream()
                .max(Double::compareTo)
                .orElse(1.0);

        List<SearchResult> results = relevanceMap.entrySet().stream()
                .sorted(Map.Entry.<Page, Double>comparingByValue().reversed())
                .map(entry -> {
                    Page page = entry.getKey();
                    double relevance = entry.getValue();
                    Site site = page.getSite();

                    String title = Jsoup.parse(page.getContent()).title();
                    String snippet = lemmaService.generateSnippet(page.getContent(), lemmas);

                    return SearchResult.builder()
                            .siteUrl(site.getUrl())
                            .siteName(site.getName())
                            .uri(site.getUrl() + page.getPath())
                            .title(title)
                            .snippet(snippet)
                            .relevance(relevance)
                            .build();
                }).collect(Collectors.toList());

        return SearchResponse.success(results);
    }

    private List<Lemma> getFilteredLemmas(Set<String> lemmas, List<Site> sites) {
        List<Lemma> allLemmas = lemmaRepository.findAllByLemmaInAndSiteIn(lemmas, sites);
        int totalPages = sites.stream()
                .mapToInt(site -> site.getPages().size())
                .sum();

        return allLemmas.stream()
                .filter(lemma -> lemma.getFrequency() < totalPages * PERCENTAGE)
                .collect(Collectors.toList());
    }

    private Map<Page, Double> calculateRelevance(Set<Page> pages, List<Long> lemmaIds) {
        Map<Page, Double> result = new HashMap<>();

        for (Page page : pages) {
            List<Index> indexes = indexRepository.findAllByPageAndLemmaIdIn(page, lemmaIds);
            double rankSum = indexes.stream()
                    .mapToDouble(Index::getRank)
                    .sum();
            result.put(page, rankSum);
        }
        return result;
    }

    private Set<Page> findFilteredPages(List<Lemma> filteredLemmas) {
        if (filteredLemmas.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Page> filteredPages = indexRepository.findPagesByLemma(filteredLemmas.get(0));

        for (int i = 1; i < filteredLemmas.size(); i++) {
            Lemma lemma = filteredLemmas.get(i);
            Set<Page> pagesWithCurrentLemma = indexRepository.findPagesByLemmaAndPageIn(lemma, filteredPages);
            filteredPages.retainAll(pagesWithCurrentLemma);

            if (filteredPages.isEmpty()) {
                break;                                                                                     //TODO: Вывод пустого массива
            }
        }
        return filteredPages;
    }
}