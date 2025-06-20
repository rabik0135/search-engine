package searchengine.dto;

public record IndexDto(Integer id,
                       Integer pageId,
                       Integer lemmaId,
                       Float rank) {
}