package searchengine.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import searchengine.dto.LemmaDto;
import searchengine.model.Lemma;

@Mapper(componentModel = "spring")
public interface LemmaMapper {

    @Mapping(target = "siteId", source = "site.id")
    LemmaDto toDto(Lemma entity);

    @Mapping(target = "site", ignore = true)
    Lemma toEntity(LemmaDto dto);

}
