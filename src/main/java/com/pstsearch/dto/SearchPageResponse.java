package com.pstsearch.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public class SearchPageResponse {
    private final List<SearchResultDto> content;
    private final long totalElements;
    private final int totalPages;
    private final int number;
    private final int size;

    private SearchPageResponse(Page<SearchResultDto> page) {
        this.content = page.getContent();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.number = page.getNumber();
        this.size = page.getSize();
    }

    public static SearchPageResponse of(Page<SearchResultDto> page) {
        return new SearchPageResponse(page);
    }

    public List<SearchResultDto> getContent() { return content; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public int getNumber() { return number; }
    public int getSize() { return size; }
}
