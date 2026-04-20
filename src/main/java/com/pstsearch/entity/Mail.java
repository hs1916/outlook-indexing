package com.pstsearch.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mails", indexes = {
        @Index(name = "idx_mails_pst_file_id", columnList = "pst_file_id"),
        @Index(name = "idx_mails_sent_date", columnList = "sentDate")
})
public class Mail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pst_file_id", nullable = false)
    private PstFile pstFile;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    private String senderEmail;
    private String senderName;

    @Column(columnDefinition = "TEXT")
    private String recipientEmails;   // 콤마 구분

    @Column(columnDefinition = "TEXT")
    private String recipientNames;    // 콤마 구분 (recipientEmails 와 순서 일치)

    private LocalDateTime sentDate;

    @Column(columnDefinition = "TEXT")
    private String attachmentNames;

    private long pstDescriptorId;   // PST 내부 메시지 식별자 (첨부파일 추출용)

    public Mail() {}

    // Getters
    public Long getId() { return id; }
    public PstFile getPstFile() { return pstFile; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public String getSenderEmail() { return senderEmail; }
    public String getSenderName() { return senderName; }
    public String getRecipientEmails() { return recipientEmails; }
    public String getRecipientNames() { return recipientNames; }
    public LocalDateTime getSentDate() { return sentDate; }
    public String getAttachmentNames() { return attachmentNames; }
    public long getPstDescriptorId() { return pstDescriptorId; }

    // Setters
    public void setPstFile(PstFile pstFile) { this.pstFile = pstFile; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setBody(String body) { this.body = body; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setRecipientEmails(String recipientEmails) { this.recipientEmails = recipientEmails; }
    public void setRecipientNames(String recipientNames) { this.recipientNames = recipientNames; }
    public void setSentDate(LocalDateTime sentDate) { this.sentDate = sentDate; }
    public void setAttachmentNames(String attachmentNames) { this.attachmentNames = attachmentNames; }
    public void setPstDescriptorId(long pstDescriptorId) { this.pstDescriptorId = pstDescriptorId; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Mail obj = new Mail();
        public Builder pstFile(PstFile v) { obj.pstFile = v; return this; }
        public Builder subject(String v) { obj.subject = v; return this; }
        public Builder body(String v) { obj.body = v; return this; }
        public Builder senderEmail(String v) { obj.senderEmail = v; return this; }
        public Builder senderName(String v) { obj.senderName = v; return this; }
        public Builder recipientEmails(String v) { obj.recipientEmails = v; return this; }
        public Builder recipientNames(String v) { obj.recipientNames = v; return this; }
        public Builder sentDate(LocalDateTime v) { obj.sentDate = v; return this; }
        public Builder attachmentNames(String v) { obj.attachmentNames = v; return this; }
        public Builder pstDescriptorId(long v) { obj.pstDescriptorId = v; return this; }
        public Mail build() { return obj; }
    }
}
