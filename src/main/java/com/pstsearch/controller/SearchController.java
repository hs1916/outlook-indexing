package com.pstsearch.controller;

import com.pstsearch.dto.MailDetailDto;
import com.pstsearch.dto.SearchRequestDto;
import com.pstsearch.dto.SearchResultDto;
import com.pstsearch.service.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<Page<SearchResultDto>> search(
            SearchRequestDto request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        return ResponseEntity.ok(searchService.search(request, PageRequest.of(page, Math.min(size, 200))));
    }

    @GetMapping("/{mailId}")
    public ResponseEntity<MailDetailDto> detail(@PathVariable Long mailId) {
        return ResponseEntity.ok(searchService.getDetail(mailId));
    }
}
