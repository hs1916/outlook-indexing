package com.pstsearch.service;

import com.pstsearch.repository.MailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class AttachmentService {

    private final MailRepository mailRepository;

    public AttachmentService(MailRepository mailRepository) {
        this.mailRepository = mailRepository;
    }

    public record MailAttachmentInfo(String pstFilePath, long pstDescriptorId) {}

    @Transactional(readOnly = true)
    public MailAttachmentInfo getInfo(Long mailId) {
        return mailRepository.findByIdWithPstFile(mailId)
                .map(m -> new MailAttachmentInfo(m.getPstFile().getFilePath(), m.getPstDescriptorId()))
                .orElseThrow(() -> new NoSuchElementException("메일 없음: " + mailId));
    }
}
