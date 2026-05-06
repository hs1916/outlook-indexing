package com.pstsearch.service;

import com.pstsearch.dto.MailDetailDto;
import com.pstsearch.dto.SearchRequestDto;
import com.pstsearch.dto.SearchResultDto;
import com.pstsearch.entity.Mail;
import com.pstsearch.repository.MailRepository;
import com.pstsearch.repository.MailSpec;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class SearchService {

    private final MailRepository mailRepository;

    public SearchService(MailRepository mailRepository) {
        this.mailRepository = mailRepository;
    }

    @Transactional(readOnly = true)
    public Page<SearchResultDto> search(SearchRequestDto req, Pageable pageable) {
        Page<Mail> mails = mailRepository.findAll(MailSpec.of(req), pageable);
        return new PageImpl<>(
                mails.getContent().stream().map(SearchResultDto::from).toList(),
                pageable, mails.getTotalElements());
    }

    @Transactional(readOnly = true)
    public MailDetailDto getDetail(Long mailId) {
        Mail mail = mailRepository.findById(mailId)
                .orElseThrow(() -> new NoSuchElementException("메일을 찾을 수 없습니다: id=" + mailId));
        return MailDetailDto.from(mail);
    }
}
