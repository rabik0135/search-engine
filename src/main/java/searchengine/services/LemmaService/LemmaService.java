package searchengine.services.LemmaService;


import searchengine.model.Page;

import java.util.Map;
import java.util.Set;

public interface LemmaService {
    Map<String, Integer> collectLemmas(String text);
    Set<String> getLemmaSet(String text);
    void processPage(Page page);
    int getLemmasCount();
}
