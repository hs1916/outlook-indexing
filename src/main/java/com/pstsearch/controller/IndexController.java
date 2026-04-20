package com.pstsearch.controller;

import com.pstsearch.service.IndexingService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/pst-files/{id}/index")
public class IndexController {

    private final IndexingService indexingService;

    public IndexController(IndexingService indexingService) {
        this.indexingService = indexingService;
    }

    @PostMapping
    public ResponseEntity<Void> startIndexing(@PathVariable Long id) {
        indexingService.startIndexing(id);
        return ResponseEntity.accepted().build();
    }

    @GetMapping(value = "/progress", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamProgress(@PathVariable Long id) {
        return indexingService.subscribe(id);
    }
}
