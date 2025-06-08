package searchengine.dto;

import searchengine.model.Site;

public record PageDto(Integer id,
                      Site site,
                      String path,
                      Integer code,
                      String content) {
}
