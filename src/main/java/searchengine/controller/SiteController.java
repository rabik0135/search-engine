package searchengine.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.SiteDto;
import searchengine.service.SiteService;

import java.util.List;

@RestController
@RequestMapping("/site")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;

    @GetMapping("/{id}")
    public SiteDto getSiteById(@PathVariable Long id) {
        return siteService.getById(id);
    }

    @GetMapping("/all")
    public List<SiteDto> getAllSites() {
        return siteService.getAll();
    }

}
