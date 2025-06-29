package searchengine.services.LemmaService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.dto.LemmaDto;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LemmaServiceImpl implements LemmaService {
    private static final String WORD_TYPE_REGEX = "\\W\\w&&[^а-яА-Я\\s]";
    private static final String[] PARTICLES_NAMES = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ", "ЧАСТ"};

    private final LuceneMorphology luceneMorphology;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;


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

    /*@Override
    @Transactional
    public void processPage(Page page) {
        Site site = page.getSite();
        String text = Jsoup.parse(page.getContent()).text();
        Map<String, Integer> lemmaMap = collectLemmas(text);

        for (Map.Entry<String, Integer> entry : lemmaMap.entrySet()) {
            String lemmaText = entry.getKey();
            int countInPage = entry.getValue();

            Lemma lemma = lemmaRepository.findBySiteAndLemma(site, lemmaText)
                    .map(existingLemma -> {
                        existingLemma.setFrequency(existingLemma.getFrequency() + countInPage);
                        return lemmaRepository.save(existingLemma);
                    })
                    .orElseGet(() -> {
                        return lemmaRepository.save(Lemma.builder()
                                .site(site)
                                .lemma(lemmaText)
                                .frequency(countInPage)
                                .build());
                    });

            Index index = Index.builder()
                    .page(page)
                    .lemma(lemma)
                    .rank((float) countInPage)
                    .build();
            indexRepository.save(index);
        }
    }*/

    /*@Override
    @Transactional
    public void processPage(Page page) {
        Site site = page.getSite();
        String text = Jsoup.parse(page.getContent()).text();

        Map<String, Integer> lemmaCounts = collectLemmas(text);
        if (lemmaCounts.isEmpty()) {
            return;
        }
        Set<String> lemmaTexts = lemmaCounts.keySet()
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        List<Lemma> existingLemmas = lemmaRepository.findAllBySiteAndLemmaIn(site, lemmaTexts);
        Map<String, Lemma> lemmaMap = existingLemmas.stream()
                .collect(Collectors.toMap(
                        Lemma::getLemma,
                        lemma -> lemma,
                        (existing, duplicate) -> existing));

        List<Lemma> lemmasToSaveOrUpdate = new ArrayList<>();
        List<Index> indexesToSaveOrUpdate = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : lemmaCounts.entrySet()) {
            String lemmaText = entry.getKey();
            int count = entry.getValue();

            Lemma lemma = lemmaMap.get(lemmaText);
            if (lemma != null) {
                lemma.setFrequency(lemma.getFrequency() + count);
            } else {
                lemma = Lemma.builder()
                        .lemma(lemmaText.toLowerCase())
                        .frequency(count)
                        .site(site)
                        .build();
                lemmaMap.put(lemmaText, lemma);
            }

            lemmasToSaveOrUpdate.add(lemma);

            Index index = Index.builder()
                    .page(page)
                    .lemma(lemma)
                    .rank((float) count)
                    .build();
            indexesToSaveOrUpdate.add(index);
        }

        lemmaRepository.saveAll(lemmasToSaveOrUpdate);
        indexRepository.saveAll(indexesToSaveOrUpdate);
    }*/

    @Override
    @Transactional
    public void processPage(Page page) {
        Site site = page.getSite();
        String text = Jsoup.parse(page.getContent()).text();

        Map<String, Integer> lemmaCounts = collectLemmas(text);
        if (lemmaCounts.isEmpty()) return;

        List<Lemma> lemmasToSave = new ArrayList<>();
        List<Index> indexesToSave = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : lemmaCounts.entrySet()) {
            String lemmaText = entry.getKey().toLowerCase();
            int count = entry.getValue();

            Lemma lemma = lemmaRepository.findBySiteAndLemma(site, lemmaText)
                    .map(existing -> {
                        existing.setFrequency(existing.getFrequency() + 1);
                        return existing;
                    })
                    .orElseGet(() -> Lemma.builder()
                            .site(site)
                            .lemma(lemmaText)
                            .frequency(1)
                            .build());

            lemmasToSave.add(lemma);

            Index index = Index.builder()
                    .page(page)
                    .lemma(lemma)
                    .rank((float) count)
                    .build();
            indexesToSave.add(index);
        }

        Map<String, Lemma> uniqueLemmas = lemmasToSave.stream()
                .collect(Collectors.toMap(
                        l -> l.getSite().getId() + "-" + l.getLemma(),
                        l -> l,
                        (l1, l2) -> l1));

        lemmaRepository.saveAll(uniqueLemmas.values());
        indexRepository.saveAll(indexesToSave);
    }

    @Override
    public int getLemmasCount() {
        return lemmaRepository.findAll().size();
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
        for (String property: PARTICLES_NAMES) {
            if (wordBase.toUpperCase().contains(property)) {
                return true;
            }
        }
        return false;
    }


}
