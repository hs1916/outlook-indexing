package com.pstsearch.repository;

import com.pstsearch.entity.Mail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface MailRepository extends JpaRepository<Mail, Long> {

    @Query("""
            SELECT m FROM Mail m
            WHERE (:subject IS NULL OR LOWER(m.subject) LIKE LOWER(CONCAT('%', :subject, '%')))
              AND (:body IS NULL OR LOWER(m.body) LIKE LOWER(CONCAT('%', :body, '%')))
              AND (:senderEmail IS NULL OR LOWER(m.senderEmail) LIKE LOWER(CONCAT('%', :senderEmail, '%')))
              AND (:senderName IS NULL OR LOWER(m.senderName) LIKE LOWER(CONCAT('%', :senderName, '%')))
              AND (:recipientEmail IS NULL OR LOWER(m.recipientEmails) LIKE LOWER(CONCAT('%', :recipientEmail, '%')))
              AND (:recipientName IS NULL OR LOWER(m.recipientNames) LIKE LOWER(CONCAT('%', :recipientName, '%')))
              AND (:dateFrom IS NULL OR m.sentDate >= :dateFrom)
              AND (:dateTo IS NULL OR m.sentDate <= :dateTo)
              AND (:attachment IS NULL OR LOWER(m.attachmentNames) LIKE LOWER(CONCAT('%', :attachment, '%')))
            ORDER BY m.sentDate DESC
            """)
    Page<Mail> search(
            @Param("subject") String subject,
            @Param("body") String body,
            @Param("senderEmail") String senderEmail,
            @Param("senderName") String senderName,
            @Param("recipientEmail") String recipientEmail,
            @Param("recipientName") String recipientName,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            @Param("attachment") String attachment,
            Pageable pageable
    );

    @Query("""
            SELECT m FROM Mail m
            WHERE (:subject IS NULL OR LOWER(m.subject) LIKE LOWER(CONCAT('%', :subject, '%')))
              AND (:body IS NULL OR LOWER(m.body) LIKE LOWER(CONCAT('%', :body, '%')))
              AND (:senderEmail IS NULL OR LOWER(m.senderEmail) LIKE LOWER(CONCAT('%', :senderEmail, '%')))
              AND (:senderName IS NULL OR LOWER(m.senderName) LIKE LOWER(CONCAT('%', :senderName, '%')))
              AND (:recipientEmail IS NULL OR LOWER(m.recipientEmails) LIKE LOWER(CONCAT('%', :recipientEmail, '%')))
              AND (:recipientName IS NULL OR LOWER(m.recipientNames) LIKE LOWER(CONCAT('%', :recipientName, '%')))
              AND (:dateFrom IS NULL OR m.sentDate >= :dateFrom)
              AND (:dateTo IS NULL OR m.sentDate <= :dateTo)
              AND (:attachment IS NULL OR LOWER(m.attachmentNames) LIKE LOWER(CONCAT('%', :attachment, '%')))
            ORDER BY m.sentDate DESC
            """)
    java.util.List<Mail> searchAll(
            @Param("subject") String subject,
            @Param("body") String body,
            @Param("senderEmail") String senderEmail,
            @Param("senderName") String senderName,
            @Param("recipientEmail") String recipientEmail,
            @Param("recipientName") String recipientName,
            @Param("dateFrom") java.time.LocalDateTime dateFrom,
            @Param("dateTo") java.time.LocalDateTime dateTo,
            @Param("attachment") String attachment
    );

    @Query("SELECT m FROM Mail m JOIN FETCH m.pstFile WHERE m.id = :id")
    java.util.Optional<Mail> findByIdWithPstFile(@Param("id") Long id);

    @Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("DELETE FROM Mail m WHERE m.pstFile.id = :pstFileId")
    void deleteByPstFileId(@Param("pstFileId") Long pstFileId);
}
