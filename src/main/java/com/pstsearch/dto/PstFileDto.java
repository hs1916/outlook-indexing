package com.pstsearch.dto;

import com.pstsearch.entity.IndexStatus;
import com.pstsearch.entity.PstFile;
import java.time.LocalDateTime;

public class PstFileDto {
    private final Long id;
    private final String filePath;
    private final String fileName;
    private final Long fileSizeBytes;
    private final IndexStatus status;
    private final Integer totalMailCount;
    private final Integer indexedMailCount;
    private final Integer errorCount;
    private final Integer elapsedSeconds;
    private final LocalDateTime indexedAt;
    private final LocalDateTime createdAt;

    private PstFileDto(PstFile e) {
        this.id = e.getId();
        this.filePath = e.getFilePath();
        this.fileName = e.getFileName();
        this.fileSizeBytes = e.getFileSizeBytes();
        this.status = e.getStatus();
        this.totalMailCount = e.getTotalMailCount();
        this.indexedMailCount = e.getIndexedMailCount();
        this.errorCount = e.getErrorCount();
        this.elapsedSeconds = e.getElapsedSeconds();
        this.indexedAt = e.getIndexedAt();
        this.createdAt = e.getCreatedAt();
    }

    public static PstFileDto from(PstFile entity) { return new PstFileDto(entity); }

    public Long getId() { return id; }
    public String getFilePath() { return filePath; }
    public String getFileName() { return fileName; }
    public Long getFileSizeBytes() { return fileSizeBytes; }
    public IndexStatus getStatus() { return status; }
    public Integer getTotalMailCount() { return totalMailCount; }
    public Integer getIndexedMailCount() { return indexedMailCount; }
    public Integer getErrorCount() { return errorCount; }
    public Integer getElapsedSeconds() { return elapsedSeconds; }
    public LocalDateTime getIndexedAt() { return indexedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
