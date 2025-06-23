package searchengine.services.lemmaService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;

@Component
@RequiredArgsConstructor
public class LemmaServiceFactory {

    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    public LemmaServiceImpl create() {
        return new LemmaServiceImpl(lemmaRepository, indexRepository);
    }
}