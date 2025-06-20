package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.services.SiteService;

@RestController
@RequestMapping("/site")
@RequiredArgsConstructor
public class SiteController {
    private final SiteService siteService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getSiteById(@PathVariable Integer id) {
        return ResponseEntity.ok(siteService.getById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllSites() {
        return ResponseEntity.ok(siteService.getAll());
    }
}