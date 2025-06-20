package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.services.PageService;

@RestController
@RequestMapping("/page")
@RequiredArgsConstructor
public class PageController {
    private final PageService pageService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getPageById(@PathVariable Integer id) {
        return ResponseEntity.ok(pageService.getById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllPages() {
        return ResponseEntity.ok(pageService.getAll());
    }

    @GetMapping("/siteId/{id}")
    public ResponseEntity<?> getPagesBySiteId(@PathVariable Integer id) {
        return ResponseEntity.ok(pageService.getAll());
    }
}