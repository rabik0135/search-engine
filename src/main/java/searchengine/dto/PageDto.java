package searchengine.dto;

import searchengine.model.Site;

public record PageDto(Integer id,
                      Integer siteId,
                      String path,
                      Integer code,
                      String content) {
}
