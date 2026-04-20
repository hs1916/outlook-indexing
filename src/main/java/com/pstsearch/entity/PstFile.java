package com.pstsearch.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pst_files")
public class PstFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String filePath;

    @Column(nullable = false)
    private String fileName;

    private Long fileSizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IndexStatus status = IndexStatus.PENDING;

    private Integer totalMailCount = 0;
    private Integer indexedMailCount = 0;
    private Integer errorCount = 0;
    private Integer elapsedSeconds;
    private LocalDateTime indexedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public PstFile() {}

    // Getters
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

    // Setters
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
    public void setStatus(IndexStatus status) { this.status = status; }
    public void setTotalMailCount(Integer totalMailCount) { this.totalMailCount = totalMailCount; }
    public void setIndexedMailCount(Integer indexedMailCount) { this.indexedMailCount = indexedMailCount; }
    public void setErrorCount(Integer errorCount) { this.errorCount = errorCount; }
    public void setElapsedSeconds(Integer elapsedSeconds) { this.elapsedSeconds = elapsedSeconds; }
    public void setIndexedAt(LocalDateTime indexedAt) { this.indexedAt = indexedAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final PstFile obj = new PstFile();
        public Builder filePath(String v) { obj.filePath = v; return this; }
        public Builder fileName(String v) { obj.fileName = v; return this; }
        public Builder fileSizeBytes(Long v) { obj.fileSizeBytes = v; return this; }
        public Builder status(IndexStatus v) { obj.status = v; return this; }
        public Builder totalMailCount(Integer v) { obj.totalMailCount = v; return this; }
        public Builder indexedMailCount(Integer v) { obj.indexedMailCount = v; return this; }
        public Builder errorCount(Integer v) { obj.errorCount = v; return this; }
        public PstFile build() { return obj; }
    }
}
