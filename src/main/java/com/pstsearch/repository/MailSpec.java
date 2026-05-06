package com.pstsearch.repository;

import com.pstsearch.dto.SearchRequestDto;
import com.pstsearch.entity.Mail;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class MailSpec {

    public static Specification<Mail> of(SearchRequestDto req) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            String subject = req.getSubjectOrNull();
            String body = req.getBodyOrNull();
            String senderEmail = req.getSenderEmailOrNull();
            String senderName = req.getSenderNameOrNull();
            String recipientEmail = req.getRecipientEmailOrNull();
            String recipientName = req.getRecipientNameOrNull();
            var dateFrom = req.getDateFromDateTime();
            var dateTo = req.getDateToDateTime();
            String attachment = req.getAttachmentOrNull();

            if (subject != null)
                predicates.add(cb.like(cb.lower(root.get("subject")), "%" + subject.toLowerCase() + "%"));
            if (body != null)
                predicates.add(cb.like(cb.lower(root.get("body")), "%" + body.toLowerCase() + "%"));
            if (senderEmail != null)
                predicates.add(cb.like(cb.lower(root.get("senderEmail")), "%" + senderEmail.toLowerCase() + "%"));
            if (senderName != null)
                predicates.add(cb.like(cb.lower(root.get("senderName")), "%" + senderName.toLowerCase() + "%"));
            if (recipientEmail != null)
                predicates.add(cb.like(cb.lower(root.get("recipientEmails")), "%" + recipientEmail.toLowerCase() + "%"));
            if (recipientName != null)
                predicates.add(cb.like(cb.lower(root.get("recipientNames")), "%" + recipientName.toLowerCase() + "%"));
            if (dateFrom != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("sentDate"), dateFrom));
            if (dateTo != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("sentDate"), dateTo));
            if (attachment != null)
                predicates.add(cb.like(cb.lower(root.get("attachmentNames")), "%" + attachment.toLowerCase() + "%"));

            // count 쿼리가 아닐 때만 ORDER BY 적용
            if (query != null && !Long.class.equals(query.getResultType())) {
                query.orderBy(cb.desc(root.get("sentDate")));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
