package searchengine.util.mapper;

import org.mapstruct.*;
import searchengine.dto.PageDto;
import searchengine.model.Page;
import searchengine.model.Site;

@Mapper(componentModel = "spring", uses = {SiteMapper.class})
public interface PageMapper {
    @Mapping(target = "siteId", source = "site.id")
    PageDto toDto(Page entity);

    @Mapping(target = "site", ignore = true)
    Page toEntity(PageDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(PageDto dto, @MappingTarget Page entity);

    @Named("idToSite")
    default Site idToSite(Integer id) {
        if (id == null) {
            return null;
        }
        Site site = new Site();
        site.setId(id);
        return site;
    }

}
