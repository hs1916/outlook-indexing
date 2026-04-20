package com.pstsearch.dto;

import com.pstsearch.entity.Mail;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MailDetailDto {
    private final Long id;
    private final String subject;
    private final String senderEmail;
    private final String senderName;
    private final String recipientEmails;
    private final String recipientNames;
    private final LocalDateTime sentDate;
    private final String body;
    private final List<String> attachmentNames;
    private final String pstFileName;

    private MailDetailDto(Mail mail) {
        this.id = mail.getId();
        this.subject = mail.getSubject();
        this.senderEmail = mail.getSenderEmail();
        this.senderName = mail.getSenderName();
        this.recipientEmails = mail.getRecipientEmails();
        this.recipientNames = mail.getRecipientNames();
        this.sentDate = mail.getSentDate();
        this.body = mail.getBody();
        this.pstFileName = mail.getPstFile().getFileName();

        if (mail.getAttachmentNames() != null && !mail.getAttachmentNames().isBlank()) {
            this.attachmentNames = Arrays.stream(mail.getAttachmentNames().split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).toList();
        } else {
            this.attachmentNames = Collections.emptyList();
        }
    }

    public static MailDetailDto from(Mail mail) { return new MailDetailDto(mail); }

    public Long getId() { return id; }
    public String getSubject() { return subject; }
    public String getSenderEmail() { return senderEmail; }
    public String getSenderName() { return senderName; }
    public String getRecipientEmails() { return recipientEmails; }
    public String getRecipientNames() { return recipientNames; }
    public LocalDateTime getSentDate() { return sentDate; }
    public String getBody() { return body; }
    public List<String> getAttachmentNames() { return attachmentNames; }
    public String getPstFileName() { return pstFileName; }
}
