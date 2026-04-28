package com.pstsearch.service;

import com.pstsearch.entity.Mail;
import com.pstsearch.repository.MailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 배치 저장을 별도 트랜잭션으로 분리.
 * IndexingService 내부 self-invocation으로는 @Transactional이 적용되지 않으므로
 * 독립 빈으로 분리하여 각 배치가 즉시 커밋되도록 한다.
 */
@Service
public class BatchSaveService {

    private final MailRepository mailRepository;

    public BatchSaveService(MailRepository mailRepository) {
        this.mailRepository = mailRepository;
    }

    @Transactional
    public void saveBatch(List<Mail> batch) {
        if (!batch.isEmpty()) {
            mailRepository.saveAll(batch);
        }
    }
}
