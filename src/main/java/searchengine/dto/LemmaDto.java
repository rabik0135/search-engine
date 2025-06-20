package searchengine.dto;

public record LemmaDto(Integer id,
                       Integer siteId,
                       String lemma,
                       Integer frequency) {
}