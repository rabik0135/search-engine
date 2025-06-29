package searchengine.config;

import lombok.SneakyThrows;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LemmaConfiguration {
    @Bean
    @SneakyThrows
    public LuceneMorphology luceneMorphology() {
        return new RussianLuceneMorphology();
    }
}