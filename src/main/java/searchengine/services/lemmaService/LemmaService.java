package searchengine.services.lemmaService;

import java.util.Map;

public interface LemmaService {
    Map<String, Integer> collectLemmas(String text);

}