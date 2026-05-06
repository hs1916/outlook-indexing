-- PST 검색 시스템 테스트 데이터 시드
-- 사용법: sqlite3 pst-search.db < tools/seed_test_data.sql
-- 주의: 앱이 실행 중이면 먼저 종료 후 실행할 것

-- 테스트용 PST 파일 레코드 (실제 파일 없이 가상으로 등록)
INSERT INTO pst_files (
    file_path, file_name, file_size_bytes, status,
    total_mail_count, indexed_mail_count, error_count,
    elapsed_seconds, indexed_at, created_at
) VALUES (
    'C:\테스트\sample_test.pst',
    'sample_test.pst',
    1048576,
    'DONE',
    2, 2, 0,
    3,
    datetime('now', 'localtime'),
    datetime('now', 'localtime')
);

-- 방금 삽입된 pst_files 의 id 를 변수처럼 재사용
-- (sqlite3 에서는 last_insert_rowid() 사용)

INSERT INTO mails (
    pst_file_id,
    subject,
    body,
    sender_email,
    sender_name,
    recipient_emails,
    recipient_names,
    sent_date,
    attachment_names,
    pst_descriptor_id
) VALUES (
    last_insert_rowid(),
    '2분기 프로젝트 일정 공유 드립니다',
    '안녕하세요.

2분기 주요 프로젝트 일정을 공유드립니다.

1. 요구사항 분석: 4월 1일 ~ 4월 15일
2. 설계: 4월 16일 ~ 4월 30일
3. 개발: 5월 1일 ~ 6월 15일
4. 테스트: 6월 16일 ~ 6월 30일

첨부된 엑셀 파일을 확인해 주시기 바랍니다.
문의사항은 언제든지 연락 주세요.

감사합니다.',
    'kim.manager@company.co.kr',
    '김관리',
    'lee.dev@company.co.kr, park.qa@company.co.kr, choi.design@company.co.kr',
    '이개발, 박품질, 최디자인',
    datetime('2026-04-10 09:30:00'),
    '2분기_프로젝트_일정.xlsx',
    10001
);

INSERT INTO mails (
    pst_file_id,
    subject,
    body,
    sender_email,
    sender_name,
    recipient_emails,
    recipient_names,
    sent_date,
    attachment_names,
    pst_descriptor_id
) VALUES (
    (SELECT id FROM pst_files WHERE file_name = 'sample_test.pst'),
    '[긴급] 서버 장애 보고 및 조치 결과',
    '안녕하세요, 인프라팀 정엔지니어입니다.

오늘 오전 10시 15분에 발생한 서버 장애 관련 조치 결과를 보고드립니다.

[장애 내용]
- 발생 시각: 2026-04-22 10:15
- 복구 시각: 2026-04-22 10:48
- 영향 범위: 결제 서비스 API 응답 지연 (평균 8초)

[원인]
DB 커넥션 풀 고갈로 인한 타임아웃 발생.
배치 작업과 일반 트래픽이 동일 풀을 공유한 것이 원인.

[조치 내용]
1. 배치 전용 커넥션 풀 분리 적용
2. 커넥션 풀 사이즈 20 → 50 상향 조정
3. 모니터링 알림 임계값 조정

이상입니다. 추가 질의사항은 회신 부탁드립니다.',
    'jung.infra@company.co.kr',
    '정엔지니어',
    'kim.manager@company.co.kr, cto@company.co.kr',
    '김관리, 최기술',
    datetime('2026-04-22 11:05:00'),
    '장애보고서_20260422.pdf, 모니터링_스크린샷.png',
    10002
);

SELECT '테스트 데이터 삽입 완료: ' || count(*) || '건' FROM mails
WHERE pst_file_id = (SELECT id FROM pst_files WHERE file_name = 'sample_test.pst');
