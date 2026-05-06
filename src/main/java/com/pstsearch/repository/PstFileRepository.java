package com.pstsearch.repository;

import com.pstsearch.entity.IndexStatus;
import com.pstsearch.entity.PstFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PstFileRepository extends JpaRepository<PstFile, Long> {

    Optional<PstFile> findByFilePath(String filePath);
    boolean existsByFilePath(String filePath);

    /** 인덱싱 시작 시 상태 초기화 (self-invocation @Transactional 우회 — ID 기반 직접 UPDATE) */
    @Modifying
    @Transactional
    @Query("UPDATE PstFile p SET p.status = :status, p.indexedMailCount = 0, p.totalMailCount = 0, p.errorCount = 0, p.elapsedSeconds = 0, p.indexedAt = null WHERE p.id = :id")
    void resetStatus(@Param("id") Long id, @Param("status") IndexStatus status);

    /** 총 메일 수 업데이트 */
    @Modifying
    @Transactional
    @Query("UPDATE PstFile p SET p.totalMailCount = :total WHERE p.id = :id")
    void updateTotalCount(@Param("id") Long id, @Param("total") int total);

    /** 인덱싱 완료 업데이트 */
    @Modifying
    @Transactional
    @Query("UPDATE PstFile p SET p.status = :status, p.indexedMailCount = :indexed, p.errorCount = :errors, p.elapsedSeconds = :elapsed, p.indexedAt = :indexedAt WHERE p.id = :id")
    void updateDone(@Param("id") Long id,
                    @Param("status") IndexStatus status,
                    @Param("indexed") int indexed,
                    @Param("errors") int errors,
                    @Param("elapsed") int elapsed,
                    @Param("indexedAt") LocalDateTime indexedAt);
}
