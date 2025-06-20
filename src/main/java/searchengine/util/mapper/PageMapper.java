package searchengine.util.mapper;

import org.mapstruct.*;
import searchengine.dto.PageDto;
import searchengine.model.Page;

@Mapper(componentModel = "spring", uses = {SiteMapper.class})
public interface PageMapper {
    @Mapping(target = "siteId", source = "site.id")
    PageDto toDto(Page entity);

    @Mapping(target = "site", ignore = true)
    Page toEntity(PageDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(PageDto dto, @MappingTarget Page entity);
}