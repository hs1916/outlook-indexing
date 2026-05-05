package com.pstsearch.service;

import com.pstsearch.entity.Mail;
import com.pstsearch.entity.PstFile;
import com.pstsearch.repository.MailRepository;
import com.pstsearch.repository.PstFileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 배치 저장을 별도 트랜잭션으로 분리.
 * IndexingService 내부 self-invocation으로는 @Transactional이 적용되지 않으므로
 * 독립 빈으로 분리하여 각 배치가 즉시 커밋되도록 한다.
 *
 * pstFileId를 받아 트랜잭션 내부에서 getReferenceById()로 managed 프록시를 얻는다.
 * startIndexing()은 비트랜잭션 컨텍스트라 findById()가 반환한 pstFile이 detached 상태이므로,
 * saveBatch()에 직접 넘기면 persist 시 DetachedObjectException이 발생한다.
 */
@Service
public class BatchSaveService {

    private final MailRepository mailRepository;
    private final PstFileRepository pstFileRepository;

    public BatchSaveService(MailRepository mailRepository, PstFileRepository pstFileRepository) {
        this.mailRepository = mailRepository;
        this.pstFileRepository = pstFileRepository;
    }

    @Transactional
    public void saveBatch(List<Mail> batch, Long pstFileId) {
        if (batch.isEmpty()) return;
        PstFile managed = pstFileRepository.getReferenceById(java.util.Objects.requireNonNull(pstFileId));
        batch.forEach(m -> m.setPstFile(managed));
        mailRepository.saveAll(batch);
    }
}
