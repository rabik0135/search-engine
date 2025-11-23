package searchengine.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.PageDto;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.service.CRUDService;
import searchengine.mapper.PageMapper;
import searchengine.service.PageService;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PageServiceImpl implements PageService {

    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final PageMapper pageMapper;

    @Override
    public PageDto create(PageDto pageDto) {
        Page page = pageMapper.toEntity(pageDto);

        Site site = siteRepository.findById(pageDto.siteId())
                .orElseThrow(() -> new EntityNotFoundException("Site not found"));
        page.setSite(site);

        Page saved = pageRepository.save(page);
        return pageMapper.toDto(saved);
    }

    @Override
    public PageDto getById(Long id) {
        return pageRepository.findById(id)
                .map(pageMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Page not found"));
    }

    @Override
    public List<PageDto> getAll() {
        return pageRepository.findAll()
                .stream()
                .map(pageMapper::toDto)
                .toList();
    }

    @Override
    public PageDto update(Long id, PageDto pageDto) {
        Page existing = pageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Page not found"));

        pageMapper.updateFromDto(pageDto, existing);

        if (!existing.getSite().getId().equals(pageDto.siteId())) {
            Site newSite = siteRepository.findById(pageDto.siteId())
                    .orElseThrow(() -> new EntityNotFoundException("Site not found"));
            existing.setSite(newSite);
        }

        Page updated = pageRepository.save(existing);
        return pageMapper.toDto(updated);
    }

    @Override
    public void delete(Long id) {
        pageRepository.deleteById(id);
    }

    public List<PageDto> findBySiteId(Long siteId) {
        return pageRepository.findBySiteId(siteId)
                .stream()
                .map(pageMapper::toDto)
                .toList();
    }

    public List<PageDto> getAllPages() {
        return pageRepository.findAll()
                .stream()
                .map(pageMapper::toDto)
                .toList();
    }

    public int getPagesCount() {
        return getAllPages().size();
    }

}
