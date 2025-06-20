package searchengine.dto;

import searchengine.model.Status;

import java.time.LocalDateTime;

public record SiteDto(Integer id,
                      Status status,
                      LocalDateTime statusTime,
                      String lastError,
                      String url,
                      String name) {
}