package com.pstsearch.dto;

import com.pstsearch.entity.Mail;
import java.time.LocalDateTime;

public class SearchResultDto {
    private final Long id;
    private final String subject;
    private final String senderEmail;
    private final String senderName;
    private final LocalDateTime sentDate;
    private final boolean hasAttachment;
    private final String pstFileName;

    private SearchResultDto(Mail mail) {
        this.id = mail.getId();
        this.subject = mail.getSubject();
        this.senderEmail = mail.getSenderEmail();
        this.senderName = mail.getSenderName();
        this.sentDate = mail.getSentDate();
        this.hasAttachment = mail.getAttachmentNames() != null && !mail.getAttachmentNames().isBlank();
        this.pstFileName = mail.getPstFile().getFileName();
    }

    public static SearchResultDto from(Mail mail) { return new SearchResultDto(mail); }

    public Long getId() { return id; }
    public String getSubject() { return subject; }
    public String getSenderEmail() { return senderEmail; }
    public String getSenderName() { return senderName; }
    public LocalDateTime getSentDate() { return sentDate; }
    public boolean isHasAttachment() { return hasAttachment; }
    public String getPstFileName() { return pstFileName; }
}
