package com.pstsearch.dto;

import com.pstsearch.entity.IndexStatus;

public class IndexProgressDto {
    private final int indexedCount;
    private final int totalCount;
    private final int percent;
    private final IndexStatus status;
    private final Integer errorCount;
    private final Integer elapsedSeconds;

    private IndexProgressDto(Builder b) {
        this.indexedCount = b.indexedCount;
        this.totalCount = b.totalCount;
        this.percent = b.percent;
        this.status = b.status;
        this.errorCount = b.errorCount;
        this.elapsedSeconds = b.elapsedSeconds;
    }

    public int getIndexedCount() { return indexedCount; }
    public int getTotalCount() { return totalCount; }
    public int getPercent() { return percent; }
    public IndexStatus getStatus() { return status; }
    public Integer getErrorCount() { return errorCount; }
    public Integer getElapsedSeconds() { return elapsedSeconds; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private int indexedCount;
        private int totalCount;
        private int percent;
        private IndexStatus status;
        private Integer errorCount;
        private Integer elapsedSeconds;

        public Builder indexedCount(int v) { this.indexedCount = v; return this; }
        public Builder totalCount(int v) { this.totalCount = v; return this; }
        public Builder percent(int v) { this.percent = v; return this; }
        public Builder status(IndexStatus v) { this.status = v; return this; }
        public Builder errorCount(Integer v) { this.errorCount = v; return this; }
        public Builder elapsedSeconds(Integer v) { this.elapsedSeconds = v; return this; }
        public IndexProgressDto build() { return new IndexProgressDto(this); }
    }
}
