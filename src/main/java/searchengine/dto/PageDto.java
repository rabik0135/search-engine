package searchengine.dto;

public record PageDto(
        Long id,

        Long siteId,

        String path,

        Integer code,

        String content
) {
}
