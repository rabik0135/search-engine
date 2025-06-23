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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LemmaServiceImpl implements LemmaService {
    private static final String WORD_TYPE_REGEX = "\\W\\w&&[^а-яА-Я\\s]";
    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ"};
    private final LuceneMorphology luceneMorphology;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    @SneakyThrows
    public LemmaServiceImpl(LemmaRepository lemmaRepository, IndexRepository indexRepository) {
        this.lemmaRepository = lemmaRepository;
        this.luceneMorphology = new RussianLuceneMorphology();
        this.indexRepository = indexRepository;
    }

    @Transactional
    public void saveLemmas(Site site, Page page) {
        String text = Jsoup.parse(page.getContent()).text();
        ConcurrentHashMap<String, Integer> lemmas = collectLemmas(text);

        if (site.getLemmas() == null) {
            site.setLemmas(new ArrayList<>());
        }

        for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
            String lemmaString = entry.getKey();
            int count = entry.getValue();

            Lemma lemma = lemmaRepository.findLemmaBySiteAndLemma(site, lemmaString);

            if (lemma != null) {
                lemma.setFrequency(lemma.getFrequency() + count);
                lemmaRepository.save(lemma);
            } else {
                lemma = Lemma.builder()
                        .site(site)
                        .lemma(lemmaString)
                        .frequency(count)
                        .build();
                lemmaRepository.save(lemma);
            }

            IndexData indexData = IndexData.builder()
                    .lemma(lemma)
                    .page(page)
                    .rank(Float.valueOf(lemma.getFrequency()))
                    .build();
            indexRepository.save(indexData);
        }
    }

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