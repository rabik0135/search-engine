package searchengine.mapper;

import org.mapstruct.*;
import searchengine.dto.PageDto;
import searchengine.model.Page;

@Mapper(componentModel = "spring", uses = {SiteMapper.class})
public interface PageMapper {

    @Mapping(target = "siteId", source = "site")
    PageDto toDto(Page entity);

    @Mapping(target = "site", ignore = true)
    Page toEntity(PageDto dto);

    @Mapping(target = "site", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(PageDto dto, @MappingTarget Page entity);

}
