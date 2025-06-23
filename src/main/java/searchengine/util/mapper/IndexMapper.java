package searchengine.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import searchengine.dto.IndexDto;
import searchengine.model.IndexData;

@Mapper(componentModel = "spring")
public interface IndexMapper {
    @Mapping(target = "pageId", source = "page.id")
    @Mapping(target = "lemmaId", source = "lemma.id")
    IndexDto toDto(IndexData entity);

    @Mapping(target = "page", ignore = true)
    @Mapping(target = "lemma", ignore = true)
    IndexData toEntity(IndexDto dto);
}