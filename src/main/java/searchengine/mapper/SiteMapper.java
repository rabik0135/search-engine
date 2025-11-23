package searchengine.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import searchengine.dto.SiteDto;
import searchengine.model.Site;

@Mapper(componentModel = "spring")
public interface SiteMapper {

    @Mapping(target = "pages", ignore = true)
    @Mapping(target = "lemmas", ignore = true)
    Site toEntity(SiteDto dto);

    SiteDto toDto(Site entity);

    @Mapping(target = "pages", ignore = true)
    @Mapping(target = "lemmas", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(SiteDto dto, @MappingTarget Site entity);

    default Long mapSiteToId(Site site) {
        return site != null ? site.getId() : null;
    }

    default Site mapIdToSite(Long siteId) {
        if (siteId == null) {
            return null;
        }
        Site site = new Site();
        site.setId(siteId);
        return site;
    }

}
