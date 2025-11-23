package searchengine.dto;

public record LemmaDto(
        Long id,
        Long siteId,
        String lemma,
        Integer frequency
) {
}
