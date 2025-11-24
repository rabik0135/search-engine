package searchengine.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.PageDto;
import searchengine.service.PageService;

import java.util.List;

@RestController
@RequestMapping("/page")
@RequiredArgsConstructor
public class PageController {

    private final PageService pageService;

    @GetMapping("/{id}")
    public PageDto getPageById(@PathVariable Long id) {
        return pageService.getById(id);
    }

    @GetMapping("/all")
    public List<PageDto> getAllPages() {
        return pageService.getAll();
    }

    @GetMapping("/siteId/{id}")
    public List<PageDto> getPagesBySiteId(@PathVariable Long id) {
        return pageService.findBySiteId(id);
    }

}
