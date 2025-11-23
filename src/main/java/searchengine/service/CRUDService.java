package searchengine.service;

import java.util.List;

public interface CRUDService <T, ID, DTO>{
    DTO create(DTO dto);
    DTO getById(ID id);
    List<DTO> getAll();
    DTO update(ID id, DTO dto);
    void delete(ID id);
}