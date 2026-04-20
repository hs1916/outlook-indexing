package com.pstsearch.controller;

import com.pstsearch.dto.SearchRequestDto;
import com.pstsearch.entity.Mail;
import com.pstsearch.repository.MailRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String SEPARATOR = "=".repeat(80);

    private final MailRepository mailRepository;

    public ExportController(MailRepository mailRepository) {
        this.mailRepository = mailRepository;
    }

    /** 검색조건에 맞는 모든 메일 본문을 TXT 파일로 내보내기 */
    @GetMapping("/mails/text")
    @Transactional(readOnly = true)
    public ResponseEntity<StreamingResponseBody> exportText(SearchRequestDto req) {

        List<Mail> mails = mailRepository.searchAll(
                req.getSubjectOrNull(), req.getBodyOrNull(),
                req.getSenderEmailOrNull(), req.getSenderNameOrNull(),
                req.getRecipientEmailOrNull(), req.getRecipientNameOrNull(),
                req.getDateFromDateTime(), req.getDateToDateTime(),
                req.getAttachmentOrNull());

        StreamingResponseBody body = out -> {
            PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8);
            writer.printf("총 %d건%n%n", mails.size());

            for (int idx = 0; idx < mails.size(); idx++) {
                Mail m = mails.get(idx);
                writer.println(SEPARATOR);
                writer.printf("[%d/%d]%n", idx + 1, mails.size());
                writer.printf("제목: %s%n", nvl(m.getSubject()));
                writer.printf("날짜: %s%n", m.getSentDate() != null ? m.getSentDate().format(FMT) : "-");
                writer.printf("보낸사람: %s <%s>%n", nvl(m.getSenderName()), nvl(m.getSenderEmail()));
                if (m.getRecipientNames() != null || m.getRecipientEmails() != null) {
                    writer.printf("받는사람: %s%n", formatRecipients(m.getRecipientNames(), m.getRecipientEmails()));
                }
                if (m.getAttachmentNames() != null && !m.getAttachmentNames().isBlank()) {
                    writer.printf("첨부파일: %s%n", m.getAttachmentNames());
                }
                writer.println();
                writer.println(m.getBody() != null ? m.getBody() : "(본문 없음)");
                writer.println();
            }
            writer.flush();
        };

        String filename = URLEncoder.encode("mail_export.txt", StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType("text/plain; charset=UTF-8"))
                .body(body);
    }

    private static String nvl(String s) {
        return s != null ? s : "";
    }

    private static String formatRecipients(String names, String emails) {
        String[] nameArr = names != null ? names.split(",") : new String[0];
        String[] emailArr = emails != null ? emails.split(",") : new String[0];
        int len = Math.max(nameArr.length, emailArr.length);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            if (i > 0) sb.append("; ");
            String n = i < nameArr.length ? nameArr[i].trim() : "";
            String e = i < emailArr.length ? emailArr[i].trim() : "";
            if (!n.isEmpty()) sb.append(n);
            if (!e.isEmpty()) sb.append(" <").append(e).append(">");
        }
        return sb.toString();
    }
}
