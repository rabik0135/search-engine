package searchengine.service;

import searchengine.dto.PageDto;
import searchengine.model.Page;

import java.util.List;

public interface PageService extends CRUDService<Page, Long, PageDto>{

    List<PageDto> findBySiteId(Long siteId);

    List<PageDto> getAllPages();

    int getPagesCount();

}
