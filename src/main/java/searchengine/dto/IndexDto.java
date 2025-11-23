package searchengine.dto;

public record IndexDto(
        Long id,
        Long pageId,
        Long lemmaId,
        Float rank
) {
}
