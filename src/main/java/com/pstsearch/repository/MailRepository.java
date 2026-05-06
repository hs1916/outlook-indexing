package com.pstsearch.repository;

import com.pstsearch.entity.Mail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface MailRepository extends JpaRepository<Mail, Long>, JpaSpecificationExecutor<Mail> {

    @Query("SELECT m FROM Mail m JOIN FETCH m.pstFile WHERE m.id = :id")
    java.util.Optional<Mail> findByIdWithPstFile(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("DELETE FROM Mail m WHERE m.pstFile.id = :pstFileId")
    void deleteByPstFileId(@Param("pstFileId") Long pstFileId);
}
