package searchengine.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.SiteFromConfig;
import searchengine.dto.SiteDto;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.util.mapper.SiteMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SiteService implements CRUDService<Site, Integer, SiteDto> {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final EntityManager entityManager;
    private final SiteMapper siteMapper;

    @Override
    public SiteDto create(SiteDto siteDto) {
        Site site = siteMapper.toEntity(siteDto);
        site.setStatusTime(LocalDateTime.now());
        Site saved = siteRepository.save(site);
        return siteMapper.toDto(saved);
    }

    @Override
    public SiteDto getById(Integer id) {
        return siteRepository.findById(id)
                .map(siteMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Site not found"));
    }

    @Override
    public List<SiteDto> getAll() {
        return siteRepository.findAll()
                .stream()
                .map(siteMapper::toDto)
                .toList();
    }

    @Override
    public SiteDto update(Integer id, SiteDto siteDto) {
        Site existing = siteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Site not found"));
        siteMapper.updateFromDto(siteDto, existing);
        existing.setStatusTime(LocalDateTime.now());

        Site updated = siteRepository.save(existing);
        return siteMapper.toDto(updated);
    }

    @Override
    public void delete(Integer id) {
        siteRepository.deleteById(id);
    }

    @Transactional
    public void deleteSiteData(String url) {
        siteRepository.findByUrl(url).ifPresent(
                siteRepository::delete
        );
    }

    public Site getSiteByUrl(String url) {
        return siteRepository.findByUrl(url).orElseThrow(
                () -> new RuntimeException("Site not found with URL: " + url)
        );
    }

    public Site createSiteFromConfig(SiteFromConfig siteFromConfig) {
        Site site = Site.builder()
                .status(Status.INDEXING)
                .statusTime(LocalDateTime.now())
                .lastError("No errors")
                .url(siteFromConfig.url())
                .name(siteFromConfig.name())
                .pages(new ArrayList<>())
                .build();
        return siteRepository.save(site);
    }

    public void updateSiteStatus(Site site, Status status, String error) {
        site.setStatus(status);
        site.setStatusTime(LocalDateTime.now());
        site.setLastError(error);
        siteRepository.save(site);
    }

    public void updateSiteStatusTime(Site site) {
        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);
    }

    public List<SiteDto> getAllSitesDto() {
        return siteRepository.findAll()
                .stream()
                .map(siteMapper::toDto)
                .toList();
    }

    public List<Site> getAllSites() {
        return siteRepository.findAll();
    }

    public int getSitesCount() {
        return getAllSitesDto().size();
    }
}