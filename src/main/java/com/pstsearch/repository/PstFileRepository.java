package com.pstsearch.repository;

import com.pstsearch.entity.PstFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PstFileRepository extends JpaRepository<PstFile, Long> {
    Optional<PstFile> findByFilePath(String filePath);
    boolean existsByFilePath(String filePath);
}
