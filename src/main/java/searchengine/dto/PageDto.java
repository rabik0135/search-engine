package searchengine.dto;

public record PageDto(Integer id,
                      Integer siteId,
                      String path,
                      Integer code,
                      String content) {
}