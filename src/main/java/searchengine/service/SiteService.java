package searchengine.service;

import searchengine.config.SiteFromConfig;
import searchengine.dto.SiteDto;
import searchengine.model.Site;
import searchengine.model.Status;

import java.util.List;

public interface SiteService extends CRUDService<Site, Long, SiteDto> {

    void deleteSiteData(String url);

    Site getSiteByUrl(String url);

    Site createSiteFromConfig(SiteFromConfig siteFromConfig);

    void updateSiteStatus(Site site, Status status, String error);

    void updateSiteStatusTime(Site site);

    List<SiteDto> getAllSitesDto();

    List<Site> getAllSites();

    int getSitesCount();

}
