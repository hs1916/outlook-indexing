package com.pstsearch.service;

import com.pff.PSTAttachment;
import com.pff.PSTFile;
import com.pff.PSTFolder;
import com.pff.PSTMessage;
import com.pff.PSTObject;
import com.pff.PSTRecipient;
import com.pstsearch.dto.IndexProgressDto;
import com.pstsearch.entity.IndexStatus;
import com.pstsearch.entity.Mail;
import com.pstsearch.entity.PstFile;
import com.pstsearch.repository.MailRepository;
import com.pstsearch.repository.PstFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class IndexingService {

    private static final Logger log = LoggerFactory.getLogger(IndexingService.class);
    private static final int BATCH_SIZE = 500;
    private static final long SSE_TIMEOUT = 0L;

    private final PstFileRepository pstFileRepository;
    private final MailRepository mailRepository;

    private final Map<Long, List<SseEmitter>> emitterMap = new ConcurrentHashMap<>();
    private final Map<Long, IndexProgressDto> progressMap = new ConcurrentHashMap<>();

    public IndexingService(PstFileRepository pstFileRepository, MailRepository mailRepository) {
        this.pstFileRepository = pstFileRepository;
        this.mailRepository = mailRepository;
    }

    public SseEmitter subscribe(Long pstFileId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitterMap.computeIfAbsent(pstFileId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(pstFileId, emitter));
        emitter.onTimeout(() -> removeEmitter(pstFileId, emitter));
        emitter.onError(e -> removeEmitter(pstFileId, emitter));

        IndexProgressDto current = progressMap.get(pstFileId);
        if (current != null) sendEvent(emitter, current);

        return emitter;
    }

    @Async("indexingExecutor")
    @Transactional
    public void startIndexing(Long pstFileId) {
        PstFile pstFile = pstFileRepository.findById(pstFileId)
                .orElseThrow(() -> new NoSuchElementException("PST 파일 없음: " + pstFileId));

        mailRepository.deleteByPstFileId(pstFileId);

        pstFile.setStatus(IndexStatus.INDEXING);
        pstFile.setIndexedMailCount(0);
        pstFile.setTotalMailCount(0);
        pstFile.setErrorCount(0);
        pstFileRepository.save(pstFile);

        long startTime = System.currentTimeMillis();
        AtomicInteger totalCount = new AtomicInteger(0);
        AtomicInteger indexedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        try {
            PSTFile pst = new PSTFile(pstFile.getFilePath());
            log.info("[{}] 메일 수 계산 중...", pstFile.getFileName());
            totalCount.set(countMessages(pst.getRootFolder()));
            log.info("[{}] 총 메일 수: {}", pstFile.getFileName(), totalCount.get());

            pstFile.setTotalMailCount(totalCount.get());
            pstFileRepository.save(pstFile);

            PSTFile pst2 = new PSTFile(pstFile.getFilePath());
            List<Mail> batch = new ArrayList<>(BATCH_SIZE);
            processFolder(pst2.getRootFolder(), pstFile, batch, indexedCount, errorCount, totalCount, pstFileId, startTime);

            if (!batch.isEmpty()) {
                mailRepository.saveAll(batch);
                batch.clear();
            }

            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            pstFile.setStatus(IndexStatus.DONE);
            pstFile.setIndexedMailCount(indexedCount.get());
            pstFile.setErrorCount(errorCount.get());
            pstFile.setElapsedSeconds((int) elapsed);
            pstFile.setIndexedAt(LocalDateTime.now());
            pstFileRepository.save(pstFile);

            IndexProgressDto done = IndexProgressDto.builder()
                    .indexedCount(indexedCount.get()).totalCount(totalCount.get())
                    .percent(100).status(IndexStatus.DONE)
                    .errorCount(errorCount.get()).elapsedSeconds((int) elapsed).build();

            progressMap.put(pstFileId, done);
            broadcastProgress(pstFileId, done);
            completeEmitters(pstFileId);

            log.info("[{}] 인덱싱 완료: {}건 / 오류: {}건 / {}초",
                    pstFile.getFileName(), indexedCount.get(), errorCount.get(), elapsed);

        } catch (Exception e) {
            log.error("[{}] 인덱싱 실패: {}", pstFile.getFileName(), e.getMessage(), e);
            pstFile.setStatus(IndexStatus.ERROR);
            pstFileRepository.save(pstFile);

            IndexProgressDto error = IndexProgressDto.builder()
                    .indexedCount(indexedCount.get()).totalCount(totalCount.get())
                    .percent(calcPercent(indexedCount.get(), totalCount.get()))
                    .status(IndexStatus.ERROR).errorCount(errorCount.get()).build();

            progressMap.put(pstFileId, error);
            broadcastProgress(pstFileId, error);
            completeEmitters(pstFileId);
        }
    }

    private void processFolder(PSTFolder folder, PstFile pstFile,
                                List<Mail> batch, AtomicInteger indexed,
                                AtomicInteger errors, AtomicInteger total,
                                Long pstFileId, long startTime) throws Exception {
        if (folder.hasSubfolders()) {
            for (PSTFolder sub : folder.getSubFolders()) {
                processFolder(sub, pstFile, batch, indexed, errors, total, pstFileId, startTime);
            }
        }
        if (folder.getContentCount() > 0) {
            PSTObject obj = folder.getNextChild();
            while (obj != null) {
                if (obj instanceof PSTMessage message) {
                    try {
                        batch.add(toMail(message, pstFile));
                        indexed.incrementAndGet();
                        if (batch.size() >= BATCH_SIZE) {
                            mailRepository.saveAll(batch);
                            batch.clear();
                            int percent = calcPercent(indexed.get(), total.get());
                            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                            IndexProgressDto progress = IndexProgressDto.builder()
                                    .indexedCount(indexed.get()).totalCount(total.get())
                                    .percent(percent).status(IndexStatus.INDEXING)
                                    .errorCount(errors.get()).elapsedSeconds((int) elapsed).build();
                            progressMap.put(pstFileId, progress);
                            broadcastProgress(pstFileId, progress);
                        }
                    } catch (Exception e) {
                        errors.incrementAndGet();
                        log.debug("메일 파싱 오류 (스킵): {}", e.getMessage());
                    }
                }
                obj = folder.getNextChild();
            }
        }
    }

    private Mail toMail(PSTMessage message, PstFile pstFile) throws Exception {
        String senderEmail = message.getSenderEmailAddress();
        String senderName  = message.getSenderName();
        if (senderEmail != null && senderEmail.isBlank()) senderEmail = null;
        if (senderName  != null && senderName.isBlank())  senderName  = null;

        StringBuilder emailsSb = new StringBuilder();
        StringBuilder namesSb  = new StringBuilder();
        for (int i = 0; i < message.getNumberOfRecipients(); i++) {
            try {
                PSTRecipient r = message.getRecipient(i);
                String email = r.getEmailAddress();
                String name  = r.getDisplayName();
                if (email != null && email.isBlank()) email = null;
                if (name  != null && name.isBlank())  name  = null;

                if (emailsSb.length() > 0) { emailsSb.append(", "); namesSb.append(", "); }
                emailsSb.append(email != null ? email : "");
                namesSb.append(name   != null ? name  : "");
            } catch (Exception ignored) {}
        }

        StringBuilder attachSb = new StringBuilder();
        for (int i = 0; i < message.getNumberOfAttachments(); i++) {
            try {
                PSTAttachment att = message.getAttachment(i);
                String name = att.getLongFilename();
                if (name == null || name.isBlank()) name = att.getFilename();
                if (name != null && !name.isBlank()) {
                    if (attachSb.length() > 0) attachSb.append(", ");
                    attachSb.append(name);
                }
            } catch (Exception ignored) {}
        }

        LocalDateTime sentDate = null;
        Date date = message.getMessageDeliveryTime();
        if (date != null) sentDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        String body = message.getBody();
        if (body != null && body.length() > 10000) body = body.substring(0, 10000);

        return Mail.builder()
                .pstFile(pstFile).subject(message.getSubject()).body(body)
                .senderEmail(senderEmail).senderName(senderName)
                .recipientEmails(emailsSb.toString()).recipientNames(namesSb.toString())
                .sentDate(sentDate).attachmentNames(attachSb.toString())
                .pstDescriptorId(message.getDescriptorNodeId()).build();
    }

    private int countMessages(PSTFolder folder) throws Exception {
        int count = folder.getContentCount();
        if (folder.hasSubfolders()) {
            for (PSTFolder sub : folder.getSubFolders()) count += countMessages(sub);
        }
        return count;
    }

    private int calcPercent(int indexed, int total) {
        if (total <= 0) return 0;
        return Math.min(99, (int) ((indexed * 100.0) / total));
    }

    private void broadcastProgress(Long pstFileId, IndexProgressDto progress) {
        List<SseEmitter> emitters = emitterMap.getOrDefault(pstFileId, Collections.emptyList());
        emitters.forEach(e -> sendEvent(e, progress));
    }

    private void sendEvent(SseEmitter emitter, IndexProgressDto progress) {
        try { emitter.send(SseEmitter.event().data(progress)); }
        catch (Exception e) { log.debug("SSE 전송 실패"); }
    }

    private void completeEmitters(Long pstFileId) {
        List<SseEmitter> emitters = emitterMap.remove(pstFileId);
        if (emitters != null) emitters.forEach(e -> { try { e.complete(); } catch (Exception ignored) {} });
        progressMap.remove(pstFileId);
    }

    private void removeEmitter(Long pstFileId, SseEmitter emitter) {
        List<SseEmitter> emitters = emitterMap.get(pstFileId);
        if (emitters != null) emitters.remove(emitter);
    }
}
