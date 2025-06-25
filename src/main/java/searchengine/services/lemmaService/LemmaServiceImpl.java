package searchengine.services.lemmaService;

import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.model.IndexData;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class LemmaServiceImpl implements LemmaService {
    private static final String WORD_TYPE_REGEX = "\\W\\w&&[^а-яА-Я\\s]";
    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ"};
    private final LuceneMorphology luceneMorphology;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final static int MAX_RETRIES = 3;

    @SneakyThrows
    public LemmaServiceImpl(LemmaRepository lemmaRepository, IndexRepository indexRepository) {
        this.lemmaRepository = lemmaRepository;
        this.luceneMorphology = new RussianLuceneMorphology();
        this.indexRepository = indexRepository;
    }

    public void saveLemmas(Site site, Page page) {
        String text = Jsoup.parse(page.getContent()).text();
        ConcurrentHashMap<String, Integer> lemmas = collectLemmas(text);

        retry(() -> saveLemmasPhase1(site, lemmas), "saveLemmasPhase1");
        retry(() -> saveLemmasPhase2(site, page, lemmas), "saveLemmasPhase2");
    }

    private void retry(Runnable operation, String operationName) {
        int attempts = 0;
        while (true) {
            try {
                operation.run();
                break;
            } catch (Exception e) {
                attempts++;
                if (isDeadlockException(e) && attempts < MAX_RETRIES) {
                    System.out.println("Deadlock detected in " + operationName + ", retrying " + attempts + "/" + MAX_RETRIES);
                    try {
                        Thread.sleep(100L * attempts);
                    } catch (InterruptedException ignored) {}
                } else {
                    throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
                }
            }
        }
    }

    private boolean isDeadlockException(Throwable e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof SQLException) {
                SQLException sqlException = (SQLException) cause;
                if ("40001".equals(sqlException.getSQLState()) || sqlException.getErrorCode() == 1213) {
                    return true;
                }
            }
            cause = cause.getCause();
        }
        return false;
    }

    @Transactional
    public void saveLemmasPhase1(Site site, ConcurrentHashMap<String, Integer> lemmas) {
        List<String> sortedLemmas = lemmas.keySet().stream()
                .sorted()
                .toList();

        List<Lemma> existingLemmas = lemmaRepository.findAllBySiteAndLemmaIn(site, sortedLemmas);
        Map<String, Lemma> lemmaMap = existingLemmas.stream()
                .collect(Collectors.toMap(
                        Lemma::getLemma,
                        l -> l,
                        (lemma1, lemma2) -> {
                            lemma1.setFrequency(lemma1.getFrequency() + lemma2.getFrequency());
                            return lemma1;
                        }
                        ));

        List<Lemma> lemmasToSave = new ArrayList<>();

        for (String lemmaText : sortedLemmas) {
            int count = lemmas.get(lemmaText);
            Lemma lemma = lemmaMap.get(lemmaText);
            if (lemma != null) {
                lemma.setFrequency(lemma.getFrequency() + count);
            } else {
                lemma = Lemma.builder()
                        .site(site)
                        .lemma(lemmaText)
                        .frequency(count)
                        .build();
            }
            lemmasToSave.add(lemma);
        }
        lemmaRepository.saveAll(lemmasToSave);
    }

    @Transactional
    public void saveLemmasPhase2(Site site, Page page, ConcurrentHashMap<String, Integer> lemmas) {
        List<String> sortedLemmas = lemmas.keySet().stream()
                .sorted()
                .toList();
        List<Lemma> savedLemmas = lemmaRepository.findAllBySiteAndLemmaIn(site, sortedLemmas);
        Map<String, Lemma> lemmaMap = savedLemmas.stream()
                .collect(Collectors.toMap(
                        Lemma::getLemma,
                        l -> l,
                        (lemma1, lemma2) -> {
                            lemma1.setFrequency(lemma1.getFrequency() + lemma2.getFrequency());
                            return lemma1;
                        }
                ));

        List<IndexData> indexDataToSave = new ArrayList<>();
        for (String lemmaText : sortedLemmas) {
            Lemma lemma = lemmaMap.get(lemmaText);
            if (lemma != null) {
                indexDataToSave.add(IndexData.builder()
                        .lemma(lemma)
                        .page(page)
                        .rank(Float.valueOf(lemmas.get(lemma.getLemma())))
                        .build());
            }
        }
        indexRepository.saveAll(indexDataToSave);
    }

    /*@Transactional
    public void saveLemmas(Site site, Page page) {


        List<Lemma> existingLemmas = lemmaRepository.findAllBySiteAndLemmaIn(site, lemmas.keySet());
        Map<String, Lemma> lemmaMap = existingLemmas.stream()
                .collect(Collectors.toMap(Lemma::getLemma, l -> l));

        List<Lemma> lemmasToSave = new ArrayList<>();
        List<IndexData> indexDataToSave = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
            String lemmaText = entry.getKey();
            int count = entry.getValue();

            Lemma lemma = lemmaMap.get(lemmaText);
            if (lemma != null) {
                lemma.setFrequency(lemma.getFrequency() + count);
            } else {
                lemma = Lemma.builder()
                        .site(site)
                        .lemma(lemmaText)
                        .frequency(count)
                        .build();
            }
            lemmasToSave.add(lemma);
        }

        for (int i = 0; i < lemmasToSave.size(); i += 50) {
            List<Lemma> batch = lemmasToSave.subList(i, Math.min(i + 50, lemmasToSave.size()));
            lemmaRepository.saveAll(batch);
        }

        for (Lemma lemma : lemmasToSave) {
            indexDataToSave.add(IndexData.builder()
                    .lemma(lemma)
                    .page(page)
                    .rank(Float.valueOf(lemmas.get(lemma.getLemma())))
                    .build());
        }
        indexRepository.saveAll(indexDataToSave);
    }*/

    @Override
    public ConcurrentHashMap<String, Integer> collectLemmas(String text) {
        String[] words = arrayContainsRussianWords(text);
        ConcurrentHashMap<String, Integer> lemmas = new ConcurrentHashMap<>();

        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }

            List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
            if (anyWordBaseBelongToParticle(wordBaseForms)) {
                continue;
            }

            List<String> normalForms = luceneMorphology.getNormalForms(word);
            if (normalForms.isEmpty()) {
                continue;
            }

            String normalWord = normalForms.get(0);

            if (lemmas.containsKey(normalWord)) {
                lemmas.put(normalWord, lemmas.get(normalWord) + 1);
            } else {
                lemmas.put(normalWord, 1);
            }
        }
        return lemmas;
    }


    private String[] arrayContainsRussianWords(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("([^а-я\\s])", " ")
                .trim()
                .split("\\s+");
    }

    private boolean anyWordBaseBelongToParticle(List<String> wordBaseForms) {
        return wordBaseForms
                .stream()
                .anyMatch(this::hasParticleProperty);
    }

    private boolean hasParticleProperty(String wordBase) {
        for (String property : particlesNames) {
            if (wordBase.toUpperCase().contains(property)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCorrectWordForm(String word) {
        List<String> wordInfo = luceneMorphology.getMorphInfo(word);
        for (String morphInfo : wordInfo) {
            if (morphInfo.matches(WORD_TYPE_REGEX)) {
                return false;
            }
        }
        return true;
    }
}