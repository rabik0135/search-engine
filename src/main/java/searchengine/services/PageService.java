package searchengine.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.PageDto;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.util.PageMapper;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PageService implements CRUDService<Page, Integer, PageDto>{

    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final PageMapper pageMapper;


    @Override
    public PageDto create(PageDto pageDto) {
        Page page = pageMapper.toEntity(pageDto);

        Site site = siteRepository.findById(pageDto.site().getId())
                .orElseThrow(() -> new EntityNotFoundException("Site not found"));
        page.setSite(site);

        Page saved = pageRepository.save(page);
        return pageMapper.toDto(saved);
    }

    @Override
    public PageDto getById(Integer id) {
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
    public PageDto update(Integer id, PageDto pageDto) {
        Page existing = pageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Page not found"));

        pageMapper.updateFromDto(pageDto, existing);

        if (!existing.getSite().getId().equals(pageDto.site().getId())) {
            Site newSite = siteRepository.findById(pageDto.site().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Site not found"));
            existing.setSite(newSite);
        }

        Page updated = pageRepository.save(existing);
        return pageMapper.toDto(updated);
    }

    @Override
    public void delete(Integer id) {
        pageRepository.deleteById(id);
    }

    public List<PageDto> findBySiteId(Integer siteId) {
        return pageRepository.findBySiteId(siteId)
                .stream()
                .map(pageMapper::toDto)
                .toList();
    }
}
