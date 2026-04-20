package com.pstsearch.dto;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SearchRequestDto {
    private String subject;
    private String body;
    private String senderEmail;
    private String senderName;
    private String recipientEmail;
    private String recipientName;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateTo;

    private String attachment;

    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public String getSenderEmail() { return senderEmail; }
    public String getSenderName() { return senderName; }
    public String getRecipientEmail() { return recipientEmail; }
    public String getRecipientName() { return recipientName; }
    public LocalDate getDateFrom() { return dateFrom; }
    public LocalDate getDateTo() { return dateTo; }
    public String getAttachment() { return attachment; }

    public void setSubject(String subject) { this.subject = subject; }
    public void setBody(String body) { this.body = body; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public void setDateFrom(LocalDate dateFrom) { this.dateFrom = dateFrom; }
    public void setDateTo(LocalDate dateTo) { this.dateTo = dateTo; }
    public void setAttachment(String attachment) { this.attachment = attachment; }

    private String nullIfBlank(String v) {
        return (v == null || v.isBlank()) ? null : v;
    }

    public String getSubjectOrNull()        { return nullIfBlank(subject); }
    public String getBodyOrNull()           { return nullIfBlank(body); }
    public String getSenderEmailOrNull()    { return nullIfBlank(senderEmail); }
    public String getSenderNameOrNull()     { return nullIfBlank(senderName); }
    public String getRecipientEmailOrNull() { return nullIfBlank(recipientEmail); }
    public String getRecipientNameOrNull()  { return nullIfBlank(recipientName); }
    public String getAttachmentOrNull()     { return nullIfBlank(attachment); }

    public LocalDateTime getDateFromDateTime() {
        return dateFrom != null ? dateFrom.atStartOfDay() : null;
    }

    public LocalDateTime getDateToDateTime() {
        return dateTo != null ? dateTo.atTime(23, 59, 59) : null;
    }
}
