package searchengine.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.apache.lucene.morphology.LuceneMorphology;
import org.jsoup.Jsoup;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.service.LemmaService;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LemmaServiceImpl implements LemmaService {
    private static final String WORD_TYPE_REGEX = "\\W\\w&&[^а-яА-Я\\s]";
    private static final String[] PARTICLES_NAMES = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ", "ЧАСТ"};

    private final LuceneMorphology luceneMorphology;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final Map<Long, ReentrantLock> siteLocks = new ConcurrentHashMap<>();


    @Override
    public Map<String, Integer> collectLemmas(String text) {
        String[] words = arrayContainsRussianWords(text);
        Map<String, Integer> lemmas = new HashMap<>();

        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }

            List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
            if (anyWordBelongToParticle(wordBaseForms)) {
                continue;
            }

            List<String> normalForms = luceneMorphology.getNormalForms(word);
            if (normalForms.isEmpty()) {
                continue;
            }

            String normalWord = normalForms.get(0).toLowerCase();

            if (lemmas.containsKey(normalWord)) {
                lemmas.put(normalWord, lemmas.get(normalWord) + 1);
            } else {
                lemmas.put(normalWord, 1);
            }
        }
        return lemmas;
    }

    @Override
    public Set<String> getLemmaSet(String text) {
        String[] words = arrayContainsRussianWords(text);
        Set<String> lemmaSet = new HashSet<>();
        for (String word : words) {
            if (!word.isEmpty() && isCorrectedWordForm(word)) {
                List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
                if (anyWordBelongToParticle(wordBaseForms)) {
                    continue;
                }
                lemmaSet.addAll(luceneMorphology.getNormalForms(word));
            }
        }
        return lemmaSet;
    }

    @Override
    public void processPage(Page page) {
        Long siteId = page.getSite().getId();
        ReentrantLock lock = siteLocks.computeIfAbsent(siteId, id -> new ReentrantLock());

        int maxAttempts = 3;
        int attempt = 0;

        while (attempt < maxAttempts) {
            lock.lock();
            try {
                doProcessPage(page);
                return;
            } catch (DataAccessException e) {
                if (isDeadlockException(e)) {
                    attempt++;
                    if (attempt >= maxAttempts) {
                        throw e;
                    }
                    try {
                        Thread.sleep(100 + new Random().nextInt(200));
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during retry delay", exception);
                    }
                } else {
                    throw e;
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private boolean isDeadlockException(DataAccessException e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof SQLException sqlException) {
                return "40001".equals(sqlException.getSQLState()) || sqlException.getMessage().toLowerCase().contains("deadlock");
            }
            cause = cause.getCause();
        }
        return false;
    }

    @Transactional
    public void doProcessPage(Page page) {
        Map<String, Integer> lemmaMap = collectLemmas(Jsoup.parse(page.getContent()).text());
        Site site = page.getSite();
        List<Lemma> existingLemmas = lemmaRepository.findAllBySiteAndLemmaIn(site, lemmaMap.keySet());

        Map<String, Lemma> existingLemmaMap = existingLemmas.stream()
                .collect(Collectors.toMap(Lemma::getLemma, Function.identity()));

        List<Lemma> newLemmas = new ArrayList<>();
        List<Index> indexes = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : lemmaMap.entrySet()) {
            String lemmaString = entry.getKey();
            Integer count = entry.getValue();

            Lemma lemma = existingLemmaMap.get(lemmaString);
            if (lemma == null) {
                lemma = new Lemma();
                lemma.setLemma(lemmaString);
                lemma.setSite(site);
                lemma.setFrequency(1);
                newLemmas.add(lemma);
            } else {
                lemma.setFrequency(lemma.getFrequency() + 1);
            }

            Index index = new Index();
            index.setPage(page);
            index.setLemma(lemma);
            index.setRank((float) count);
            indexes.add(index);
        }

        lemmaRepository.saveAll(newLemmas);
        indexRepository.saveAll(indexes);
    }

    @Override
    public int getLemmasCount() {
        return lemmaRepository.findAll().size();
    }

    @Override
    public String generateSnippet(String content, Set<String> lemmas) {
        String text = Jsoup.parse(content).text();
        String[] words = arrayContainsRussianWords(text);

        int snippetLength = 30;
        int contextRadius = 20;

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            Set<String> wordLemmas = getLemmaSet(word);
            if (wordLemmas.stream().anyMatch(lemmas::contains)) {
                int start = Math.max(0, i - contextRadius);
                int end = Math.min(words.length, i + contextRadius);

                StringBuilder snippet = new StringBuilder();

                for (int j = start; j < end; j++) {
                    String w = words[j];
                    Set<String> wLemmas = getLemmaSet(w);

                    if (wLemmas.stream().anyMatch(lemmas::contains)) {
                        snippet.append("<b>").append(w).append("</b>");
                    } else {
                        snippet.append(w);
                    }

                    snippet.append(" ");
                }
                return (start > 0 ? "..." : "") + snippet.toString().trim() + (end < words.length ? "..." : "");
            }
        }
        return ("");
    }


    private boolean isCorrectedWordForm(String word) {
        List<String> wordInfo = luceneMorphology.getMorphInfo(word);
        for (String morphInfo : wordInfo) {
            if (morphInfo.matches(WORD_TYPE_REGEX)) {
                return false;
            }
        }
        return true;
    }

    private String[] arrayContainsRussianWords(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("([^а-я\\s])", " ")
                .trim()
                .split("\\s+");
    }

    private boolean anyWordBelongToParticle(List<String> wordBaseForms) {
        return wordBaseForms.stream().anyMatch(this::hasParticleProperty);
    }

    private boolean hasParticleProperty(String wordBase) {
        for (String property : PARTICLES_NAMES) {
            if (wordBase.toUpperCase().contains(property)) {
                return true;
            }
        }
        return false;
    }

}
