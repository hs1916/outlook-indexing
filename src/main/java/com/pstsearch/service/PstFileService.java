package com.pstsearch.service;

import com.pstsearch.dto.PstFileDto;
import com.pstsearch.entity.IndexStatus;
import com.pstsearch.entity.PstFile;
import com.pstsearch.repository.MailRepository;
import com.pstsearch.repository.PstFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PstFileService {

    private static final Logger log = LoggerFactory.getLogger(PstFileService.class);

    private final PstFileRepository pstFileRepository;
    private final MailRepository mailRepository;

    public PstFileService(PstFileRepository pstFileRepository, MailRepository mailRepository) {
        this.pstFileRepository = pstFileRepository;
        this.mailRepository = mailRepository;
    }

    @Transactional(readOnly = true)
    public List<PstFileDto> findAll() {
        return pstFileRepository.findAll().stream().map(PstFileDto::from).toList();
    }

    @Transactional
    public PstFileDto register(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("파일을 찾을 수 없습니다: " + filePath);
        }
        if (!filePath.toLowerCase().endsWith(".pst")) {
            throw new IllegalArgumentException("PST 파일만 등록할 수 있습니다: " + filePath);
        }
        if (pstFileRepository.existsByFilePath(filePath)) {
            return PstFileDto.from(pstFileRepository.findByFilePath(filePath).get());
        }

        PstFile pstFile = PstFile.builder()
                .filePath(filePath)
                .fileName(file.getName())
                .fileSizeBytes(file.length())
                .status(IndexStatus.PENDING)
                .build();

        return PstFileDto.from(pstFileRepository.save(pstFile));
    }

    @Transactional
    public void delete(Long id) {
        PstFile pstFile = findEntityById(id);
        mailRepository.deleteByPstFileId(id);
        pstFileRepository.delete(pstFile);
        log.info("PST 파일 삭제 완료: {}", pstFile.getFileName());
    }

    @Transactional(readOnly = true)
    public PstFile findEntityById(Long id) {
        return pstFileRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("PST 파일을 찾을 수 없습니다: id=" + id));
    }
}
