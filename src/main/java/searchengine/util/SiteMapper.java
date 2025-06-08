package searchengine.util;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;
import searchengine.dto.SiteDto;
import searchengine.model.Site;

@Mapper(componentModel = "spring")
public interface SiteMapper {

    Site toEntity(SiteDto dto);
    SiteDto toDto(Site entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(SiteDto dto, @MappingTarget Site entity);

}
