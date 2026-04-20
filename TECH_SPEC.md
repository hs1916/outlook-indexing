# TECH_SPEC — Outlook PST 인덱서 & 메일 검색 시스템

**버전**: 2.0 (최종)
**작성일**: 2026-04-20
**작성자**: @architect
**기반 PRD**: docs/PRD.md v4.0

---

## 1. 기술 스택

| 구분 | 기술 | 버전 | 선정 근거 |
|------|------|------|----------|
| Language | Java | 17 LTS | 장기 지원, Record 등 현대 문법 |
| Backend Framework | Spring Boot | 3.3.x | 내장 Tomcat, 단일 JAR 배포, SSE 지원 |
| ORM | Spring Data JPA + Hibernate | 6.x | 선언적 쿼리, 타입 안전 |
| Database | SQLite | 3.x | 파일 기반, 외부 DB 서버 불필요 |
| SQLite Driver | xerial/sqlite-jdbc | 3.46.x | Java용 SQLite JDBC 드라이버 |
| SQLite Dialect | hibernate-community-dialects | 6.x | Hibernate SQLite 방언 |
| PST 파싱 | java-libpst | 1.0.x | 순수 Java, 외부 DLL 의존 없음 |
| Build Tool | Maven | 3.9.x | Spring Boot 공식 지원 |
| Frontend | React + TypeScript | 18 / 5.x | 컴포넌트 기반, 타입 안전 |
| Frontend Build | Vite | 5.x | 빠른 빌드, 경량 |
| Frontend 통합 | frontend-maven-plugin | 1.15.x | Maven 빌드 시 React 자동 빌드 후 JAR 포함 |
| HTTP Client (FE) | Axios | 1.x | API 호출 |
| Router (FE) | React Router | 6.x | SPA 라우팅 |
| Styling (FE) | Tailwind CSS | 3.x | 유틸리티 기반, 빠른 UI 구성 |

---

## 2. 단일 JAR 원칙

> **`java -jar pst-search.jar` 하나로 프론트엔드 + 백엔드 + DB 전부 동작한다.**

React 빌드 결과물이 JAR 내부 `BOOT-INF/classes/static/` 에 포함되어, Spring Boot 내장 Tomcat이 API와 정적 파일을 동일 포트(8080)에서 함께 서빙한다.

```
┌─────────────────────────────────────────────────────┐
│              pst-search.jar (단일 파일)               │
│                                                     │
│  Spring Boot (내장 Tomcat, :8080)                    │
│  ├── GET /              → index.html  ┐             │
│  ├── GET /assets/*      → JS/CSS      ├ React SPA   │
│  ├── GET /manage        → index.html  ┘             │
│  ├── GET /search        → index.html                │
│  │                                                   │
│  ├── GET  /api/pst-files               → PST 목록    │
│  ├── POST /api/pst-files               → PST 등록    │
│  ├── DELETE /api/pst-files/{id}        → PST 삭제    │
│  ├── POST /api/pst-files/{id}/index    → 인덱싱 시작 │
│  ├── GET  /api/pst-files/{id}/index/progress → SSE  │
│  ├── GET  /api/file-picker             → 파일 선택   │
│  ├── GET  /api/search                  → 메일 검색   │
│  ├── GET  /api/search/{id}             → 메일 상세   │
│  ├── GET  /api/mails/{id}/html-body    → HTML 본문   │
│  ├── GET  /api/mails/{id}/attachments/{i} → 첨부 다운│
│  ├── GET  /api/mails/{id}/attachments/zip → ZIP 다운 │
│  └── GET  /api/export/mails/text       → TXT 내보내기│
│                                                     │
│  pst-search.db (JAR 실행 경로에 자동 생성)            │
└─────────────────────────────────────────────────────┘
```

### Maven 빌드 흐름

```
mvn clean package
  1. frontend-maven-plugin: npm install → npm run build (Vite)
  2. maven-resources-plugin (prepare-package 단계):
       frontend/dist/* → target/classes/static/ 복사
  3. spring-boot-maven-plugin: → pst-search.jar 패키징
```

> **중요**: `mvn clean package` 로만 빌드. `mvn package` (clean 없이)는 구버전 정적 파일이 JAR에 포함될 수 있다.

### SPA 라우팅

`WebConfig.java` 에서 `/api` 로 시작하지 않는 단일 세그먼트 경로를 `index.html` 로 forward한다.

```java
registry.addViewController("/")
        .setViewName("forward:/index.html");
registry.addViewController("/{path:^(?!api)[^\\.]*}")
        .setViewName("forward:/index.html");
```

### AWT Headless 비활성화

파일 탐색기 다이얼로그(`JFileChooser`) 사용을 위해 진입점에서 headless 모드를 해제한다.

```java
// PstSearchApplication.java
System.setProperty("java.awt.headless", "false");
SpringApplication.run(PstSearchApplication.class, args);
```

---

## 3. 시스템 아키텍처

```
[ 브라우저 ]
    │  HTTP / SSE  (localhost:8080)
    ▼
[ Spring Boot ]
    ├── PstFileController   → PST 파일 등록/삭제/목록
    ├── IndexController     → 인덱싱 시작 / SSE 진행률
    ├── FilePickerController→ JFileChooser 파일 선택
    ├── SearchController    → 메일 검색 / 상세 조회
    ├── AttachmentController→ 첨부파일 개별·ZIP 다운로드 / HTML 본문
    └── ExportController    → 검색결과 TXT 내보내기
    │
    ├── PstFileService
    ├── IndexingService     → java-libpst 파싱, 비동기(@Async)
    ├── SearchService
    └── AttachmentService   → PST 파일 경로·DescriptorId 조회 (@Transactional)
    │
    └── SQLite DB (pst-search.db)
            ├── pst_files
            └── mails
```

---

## 4. 프로젝트 파일 구조

```
pst-search/
├── pom.xml
├── src/main/java/com/pstsearch/
│   ├── PstSearchApplication.java
│   ├── config/
│   │   ├── WebConfig.java           # SPA fallback 라우팅
│   │   └── AsyncConfig.java         # 인덱싱 비동기 ThreadPool
│   ├── controller/
│   │   ├── PstFileController.java
│   │   ├── IndexController.java
│   │   ├── FilePickerController.java # GET /api/file-picker
│   │   ├── SearchController.java
│   │   ├── AttachmentController.java # 첨부 다운로드 / HTML 본문
│   │   └── ExportController.java    # TXT 내보내기
│   ├── service/
│   │   ├── PstFileService.java
│   │   ├── IndexingService.java
│   │   ├── SearchService.java
│   │   └── AttachmentService.java
│   ├── repository/
│   │   ├── PstFileRepository.java
│   │   └── MailRepository.java
│   ├── entity/
│   │   ├── PstFile.java
│   │   ├── Mail.java
│   │   └── IndexStatus.java         # enum: PENDING/INDEXING/DONE/ERROR
│   └── dto/
│       ├── PstFileDto.java
│       ├── IndexProgressDto.java
│       ├── SearchRequestDto.java
│       ├── SearchResultDto.java
│       └── MailDetailDto.java
├── src/main/resources/
│   └── application.yml
└── frontend/src/
    ├── App.tsx                      # h-screen 레이아웃, 검색/관리 라우팅
    ├── types/index.ts
    ├── api/
    │   ├── pstApi.ts
    │   └── searchApi.ts
    ├── pages/
    │   ├── ManagePage.tsx
    │   └── SearchPage.tsx           # 리본 + 드래그 패널 레이아웃
    └── components/
        ├── manage/
        │   ├── PstFileTable.tsx
        │   ├── PstFileAddModal.tsx   # 파일 탐색기 연동
        │   └── IndexingProgressBar.tsx
        └── search/
            ├── SearchRibbon.tsx     # 리본형 검색 조건 (토글)
            ├── SearchResultList.tsx # 좌측 compact 목록 패널
            └── MailContentPanel.tsx # 우측 인라인 메일 뷰어
```

---

## 5. 데이터베이스 스키마

### pst_files 테이블
```sql
CREATE TABLE pst_files (
    id                 INTEGER  PRIMARY KEY AUTOINCREMENT,
    file_path          TEXT     NOT NULL UNIQUE,
    file_name          TEXT     NOT NULL,
    file_size_bytes    BIGINT,
    status             TEXT     NOT NULL DEFAULT 'PENDING',
    total_mail_count   INTEGER  DEFAULT 0,
    indexed_mail_count INTEGER  DEFAULT 0,
    error_count        INTEGER  DEFAULT 0,
    elapsed_seconds    INTEGER,
    indexed_at         DATETIME,
    created_at         DATETIME NOT NULL
);
```

### mails 테이블
```sql
CREATE TABLE mails (
    id                 INTEGER  PRIMARY KEY AUTOINCREMENT,
    pst_file_id        INTEGER  NOT NULL,
    subject            TEXT,
    body               TEXT,                 -- 검색용 plain text (최대 10,000자)
    sender_email       TEXT,
    sender_name        TEXT,
    recipient_emails   TEXT,                 -- 콤마 구분, 순서 일치
    recipient_names    TEXT,                 -- 콤마 구분, 순서 일치
    sent_date          DATETIME,
    attachment_names   TEXT,                 -- 콤마 구분
    pst_descriptor_id  BIGINT,               -- PST 내부 식별자 (첨부 추출용)
    FOREIGN KEY (pst_file_id) REFERENCES pst_files(id) ON DELETE CASCADE
);

CREATE INDEX idx_mails_pst_file_id ON mails(pst_file_id);
CREATE INDEX idx_mails_sent_date   ON mails(sent_date);
```

> `pst_descriptor_id`: java-libpst의 `message.getDescriptorNodeId()` 값. 첨부파일 다운로드 및 HTML 본문 조회 시 `PSTObject.detectAndLoadPSTObject(pstFile, descriptorId)` 로 메시지를 직접 로드하는 데 사용한다.

---

## 6. API 명세

### 6-1. PST 파일 관리

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/pst-files` | 등록된 PST 파일 목록 |
| POST | `/api/pst-files` | PST 파일 경로 등록 |
| DELETE | `/api/pst-files/{id}` | PST 파일 및 인덱스 삭제 |

**POST /api/pst-files — Request Body**
```json
{ "filePath": "C:\\data\\archive.pst" }
```

**GET /api/pst-files — Response**
```json
[{
  "id": 1, "filePath": "C:\\data\\archive.pst", "fileName": "archive.pst",
  "fileSizeBytes": 5368709120, "status": "DONE",
  "totalMailCount": 12500, "indexedMailCount": 12487,
  "errorCount": 13, "elapsedSeconds": 480, "indexedAt": "2026-04-20T10:30:00"
}]
```

---

### 6-2. 인덱싱

| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/pst-files/{id}/index` | 인덱싱 시작 (비동기) |
| GET | `/api/pst-files/{id}/index/progress` | SSE 진행률 스트림 (`text/event-stream`) |

**SSE 이벤트 형식**
```
data: {"indexedCount":500,"totalCount":12500,"percent":4,"status":"INDEXING"}
data: {"indexedCount":12500,"totalCount":12500,"percent":100,"status":"DONE","errorCount":13,"elapsedSeconds":480}
```

---

### 6-3. 파일 탐색기

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/file-picker` | JFileChooser 다이얼로그 호출, 선택된 경로 반환 |

**Response** `200 { "filePath": "C:\\Users\\..." }` / `204 No Content` (취소 시)

---

### 6-4. 메일 검색

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/search` | 메일 검색 (페이지네이션) |
| GET | `/api/search/{mailId}` | 메일 상세 조회 |

**GET /api/search — Query Parameters**

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| subject | String | 제목 부분 일치 |
| body | String | 본문 부분 일치 |
| senderName | String | 보낸사람 이름 부분 일치 |
| senderEmail | String | 보낸사람 이메일 부분 일치 |
| recipientName | String | 받는사람 이름 부분 일치 |
| recipientEmail | String | 받는사람 이메일 부분 일치 |
| dateFrom | String | 날짜 시작 (yyyy-MM-dd) |
| dateTo | String | 날짜 종료 (yyyy-MM-dd) |
| attachment | String | 첨부파일명 부분 일치 |
| page | Integer | 페이지 번호 (0-based, 기본 0) |
| size | Integer | 페이지 크기 (기본 50, 최대 200) |

**GET /api/search — Response** (Spring `Page<SearchResultDto>`)
```json
{
  "content": [{
    "id": 1021, "subject": "Q1 보고서 검토", "senderName": "홍길동",
    "senderEmail": "hong@company.com", "sentDate": "2025-03-15T09:22:00",
    "hasAttachment": true, "pstFileName": "archive.pst"
  }],
  "totalElements": 234, "totalPages": 5, "number": 0, "size": 50
}
```

**GET /api/search/{mailId} — Response**
```json
{
  "id": 1021, "subject": "Q1 보고서 검토",
  "senderName": "홍길동", "senderEmail": "hong@company.com",
  "recipientNames": "김철수, 이영희",
  "recipientEmails": "kim@company.com, lee@company.com",
  "sentDate": "2025-03-15T09:22:00",
  "body": "안녕하세요...",
  "attachmentNames": ["Q1_report.xlsx", "reference.pdf"],
  "pstFileName": "archive.pst"
}
```

---

### 6-5. 첨부파일 & HTML 본문

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/mails/{mailId}/attachments/{index}` | 첨부파일 개별 다운로드 (0-based) |
| GET | `/api/mails/{mailId}/attachments/zip` | 전체 첨부파일 ZIP 다운로드 |
| GET | `/api/mails/{mailId}/html-body` | HTML 본문 반환 (인라인 이미지 포함) |

- 첨부 다운로드는 PST 파일을 직접 열어 `StreamingResponseBody`로 스트리밍한다
- HTML 본문 조회 시 `cid:` 참조 이미지를 base64 data URI로 교체하여 반환한다
- HTML 본문이 없는 경우 plain text를 `<pre>` 태그로 래핑하여 반환한다

**인라인 이미지 처리 흐름**
```
1. getBodyHTML() 호출
2. 모든 첨부파일 순회 → att.getContentId() 존재 시 이미지 후보
3. att.getFileInputStream() → byte[] → Base64 인코딩
4. HTML 내 "cid:xxx" → "data:image/png;base64,..." 로 치환 (정규식, 대소문자 무시)
5. text/html; charset=UTF-8 으로 응답
```

---

### 6-6. TXT 내보내기

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/export/mails/text` | 검색조건에 해당하는 전체 메일 본문을 TXT로 스트리밍 |

- Query Parameters: `/api/search` 와 동일한 검색 파라미터 (page/size 제외)
- 페이지네이션 없이 전체 조회 후 메일 구분선(`=` × 80) + 메타정보 + 본문 순으로 출력
- `Content-Disposition: attachment; filename*=UTF-8''mail_export.txt`

---

## 7. 핵심 클래스 명세

### Entity

```java
// Mail.java — 실제 구현 기준
@Entity @Table(name = "mails")
public class Mail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pst_file_id", nullable = false)
    private PstFile pstFile;

    private String subject;
    @Column(columnDefinition = "TEXT") private String body;

    private String senderEmail;
    private String senderName;
    @Column(columnDefinition = "TEXT") private String recipientEmails; // 콤마 구분
    @Column(columnDefinition = "TEXT") private String recipientNames;  // 순서 일치

    private LocalDateTime sentDate;
    @Column(columnDefinition = "TEXT") private String attachmentNames; // 콤마 구분
    private long pstDescriptorId; // PSTMessage.getDescriptorNodeId()
}
```

### Repository

```java
// MailRepository.java
public interface MailRepository extends JpaRepository<Mail, Long> {

    // 페이지네이션 검색
    @Query("SELECT m FROM Mail m WHERE ...")
    Page<Mail> search(
        @Param("subject") String subject,
        @Param("body") String body,
        @Param("senderEmail") String senderEmail,
        @Param("senderName") String senderName,
        @Param("recipientEmail") String recipientEmail,
        @Param("recipientName") String recipientName,
        @Param("dateFrom") LocalDateTime dateFrom,
        @Param("dateTo") LocalDateTime dateTo,
        @Param("attachment") String attachment,
        Pageable pageable
    );

    // 전체 조회 (TXT 내보내기용, 페이지 없음)
    @Query("SELECT m FROM Mail m WHERE ...")
    List<Mail> searchAll( /* 동일 파라미터, Pageable 없음 */ );

    // 첨부파일 추출용 (PST 경로 + DescriptorId)
    @Query("SELECT m FROM Mail m JOIN FETCH m.pstFile WHERE m.id = :id")
    Optional<Mail> findByIdWithPstFile(@Param("id") Long id);

    @Modifying
    @Query("DELETE FROM Mail m WHERE m.pstFile.id = :pstFileId")
    void deleteByPstFileId(@Param("pstFileId") Long pstFileId);
}
```

### Service

```java
// AttachmentService.java — @Transactional DB 조회를 Controller 밖에서 처리
@Service
public class AttachmentService {
    public record MailAttachmentInfo(String pstFilePath, long pstDescriptorId) {}

    @Transactional(readOnly = true)
    public MailAttachmentInfo getInfo(Long mailId);
}

// IndexingService.java
@Service
public class IndexingService {
    @Async("indexingExecutor")
    public void startIndexing(Long pstFileId);      // 비동기 인덱싱

    public SseEmitter subscribeProgress(Long pstFileId); // SSE 구독
}
```

### Controller

```java
// AttachmentController.java
@RestController @RequestMapping("/api/mails")
public class AttachmentController {
    // GET /{mailId}/attachments/{index}  → StreamingResponseBody
    // GET /{mailId}/attachments/zip      → ZipOutputStream 스트리밍
    // GET /{mailId}/html-body            → byte[] (text/html, CID 교체 완료)
}

// ExportController.java
@RestController @RequestMapping("/api/export")
public class ExportController {
    // GET /mails/text → PrintWriter로 UTF-8 TXT 스트리밍
}

// FilePickerController.java
@RestController
public class FilePickerController {
    // GET /api/file-picker → SwingUtilities.invokeAndWait + JFileChooser
}
```

### Config

```java
// AsyncConfig.java — 인덱싱 전용 스레드풀
@Bean("indexingExecutor")
public Executor indexingExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(4);
    executor.setQueueCapacity(10);
    executor.setThreadNamePrefix("indexing-");
    return executor;
}
```

---

## 8. Frontend 컴포넌트 명세

### 타입 정의 (`types/index.ts`)

```typescript
export type IndexStatus = 'PENDING' | 'INDEXING' | 'DONE' | 'ERROR'

export interface SearchRequest {
  subject?: string; body?: string
  senderName?: string; senderEmail?: string
  recipientName?: string; recipientEmail?: string
  dateFrom?: string; dateTo?: string
  attachment?: string
  page?: number; size?: number
}

export interface SearchResult {
  id: number; subject: string
  senderName: string | null; senderEmail: string | null
  sentDate: string | null; hasAttachment: boolean; pstFileName: string
}

export interface MailDetail {
  id: number; subject: string
  senderName: string | null; senderEmail: string | null
  recipientNames: string | null; recipientEmails: string | null
  sentDate: string | null; body: string
  attachmentNames: string[]; pstFileName: string
}

export interface SearchPage {
  content: SearchResult[]
  totalElements: number; totalPages: number; number: number; size: number
}
```

### 화면 컴포넌트

| 컴포넌트 | 주요 Props | 역할 |
|---------|-----------|------|
| `App` | — | `h-screen` 루트 레이아웃. 검색 페이지는 full-width/height, 관리 페이지는 max-w 컨테이너 |
| `SearchPage` | — | 리본 + 드래그 패널 상태 관리. `leftPercent` 상태로 좌측 패널 너비 제어 |
| `SearchRibbon` | `open`, `onToggle`, `onSearch`, `loading` | 상단 리본형 검색 조건. 토글로 접힘/펼침. 8개 조건 필드 |
| `SearchResultList` | `page`, `selectedId`, `onSelect`, `onPageChange`, `lastReq`, ... | 좌측 compact 카드 목록. 건수 표시, TXT 내보내기 링크, 페이지네이션 |
| `MailContentPanel` | `mailId` | 우측 인라인 메일 뷰어. 메타정보 헤더(고정) + HTML iframe(flex-1) |
| `ManagePage` | — | PST 관리 화면 |
| `PstFileTable` | `files`, `onIndex`, `onDelete` | PST 파일 목록 테이블 |
| `PstFileAddModal` | `onAdd`, `onClose` | PST 등록 모달. "파일 선택" → `/api/file-picker` 호출 |
| `IndexingProgressBar` | `pstFileId` | SSE 연결, 진행률 표시 |

### 드래그 패널 구현 (`SearchPage.tsx`)

```typescript
const startDrag = useCallback((e: React.MouseEvent) => {
  e.preventDefault()
  isDragging.current = true

  const onMove = (ev: MouseEvent) => {
    const rect = containerRef.current!.getBoundingClientRect()
    const pct = ((ev.clientX - rect.left) / rect.width) * 100
    setLeftPercent(Math.min(Math.max(pct, 15), 60)) // 15%~60% 클램프
  }
  const onUp = () => {
    isDragging.current = false
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onUp)
    document.body.style.cursor = ''
    document.body.style.userSelect = ''
  }

  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onUp)
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
}, [])
```

---

## 9. 설정 파일

### `application.yml`

```yaml
server:
  port: ${SERVER_PORT:8080}   # 포트 변경: java -jar pst-search.jar --server.port=9090

spring:
  datasource:
    url: jdbc:sqlite:${DB_PATH:./pst-search.db}
    driver-class-name: org.sqlite.JDBC
    hikari:
      maximum-pool-size: 1   # SQLite는 단일 연결
      connection-init-sql: >
        PRAGMA journal_mode=WAL;
        PRAGMA synchronous=NORMAL;
        PRAGMA foreign_keys=ON;
  jpa:
    database-platform: org.hibernate.community.dialect.SQLiteDialect
    hibernate:
      ddl-auto: update
    show-sql: false
  mvc:
    async:
      request-timeout: -1   # SSE 타임아웃 없음

logging:
  level:
    com.pstsearch: INFO
```

### `pom.xml` 핵심 설정

```xml
<!-- 최종 JAR 파일명 -->
<finalName>pst-search</finalName>

<!-- frontend-maven-plugin: React 빌드 -->
<plugin>
  <groupId>com.github.eirslett</groupId>
  <artifactId>frontend-maven-plugin</artifactId>
  <version>1.15.0</version>
  <configuration>
    <workingDirectory>frontend</workingDirectory>
    <nodeVersion>v20.11.0</nodeVersion>
  </configuration>
</plugin>

<!-- maven-resources-plugin: 빌드 결과물 복사
     phase = prepare-package (jar 패키징 직전) → 항상 최신 빌드 포함 보장 -->
<execution>
  <id>copy-react-build</id>
  <phase>prepare-package</phase>
  <goals><goal>copy-resources</goal></goals>
  <configuration>
    <outputDirectory>${project.build.outputDirectory}/static</outputDirectory>
    <resources>
      <resource><directory>${project.basedir}/frontend/dist</directory></resource>
    </resources>
  </configuration>
</execution>
```

---

## 10. PRD 수용 기준 검증 매트릭스

### 기능 1: PST 파일 & 인덱스 관리

| 수용 기준 | 구현 위치 |
|----------|----------|
| 파일 탐색기로 PST 등록 | `PstFileAddModal` → `GET /api/file-picker` → `JFileChooser` |
| PST 목록 테이블 | `PstFileTable` → `GET /api/pst-files` |
| 인덱싱 진행률 실시간 표시 | `IndexingProgressBar` → SSE `/index/progress` |
| 완료 후 총 메일 수 / 소요 시간 | `pst_files.total_mail_count`, `elapsed_seconds` |
| 상태 4종 | `IndexStatus` enum, `PstFileTable` 상태 배지 |
| 파일 삭제 (인덱스 포함) | `DELETE /api/pst-files/{id}` + `ON DELETE CASCADE` |
| 재인덱싱 덮어쓰기 | `IndexingService`: 기존 데이터 삭제 후 재삽입 |
| 파싱 오류 스킵 | `IndexingService.toMail()`: per-mail try-catch |

### 기능 2: 메일 검색

| 수용 기준 | 구현 위치 |
|----------|----------|
| 리본형 검색 조건 (토글) | `SearchRibbon` — `open` state, chevron 버튼 |
| 8개 조건 검색 | `SearchRibbon` → `GET /api/search` → `MailRepository.search()` |
| 좌측 목록 + 우측 내용 2패널 | `SearchPage` flex 레이아웃 |
| 드래그 패널 크기 조절 | `SearchPage.startDrag()` — mousemove + leftPercent |
| 반응형 | `h-screen flex flex-col`, 하위 모두 `flex-1 min-h-0` |
| 인라인 메일 상세 | `MailContentPanel` — 메타 헤더 + iframe |
| HTML 본문 렌더링 | `GET /api/mails/{id}/html-body` → `text/html` |
| 인라인 이미지 (CID) | `AttachmentController.resolveCidImages()` — base64 data URI 교체 |
| 첨부파일 개별 다운로드 | `GET /api/mails/{id}/attachments/{index}` → StreamingResponseBody |
| 첨부파일 ZIP 다운로드 | `GET /api/mails/{id}/attachments/zip` → ZipOutputStream |
| 검색결과 TXT 내보내기 | `GET /api/export/mails/text` → UTF-8 TXT 스트리밍 |
| 날짜 내림차순 정렬 | `MailRepository.search()` `ORDER BY m.sentDate DESC` |
| 페이지네이션 (50건) | `SearchResultList` + `Pageable(page, 50)` |

---

## 11. 실행 방법

```bash
# 빌드
mvn clean package -DskipTests

# 실행
java -jar target/pst-search.jar

# 접속
http://localhost:8080

# 포트 변경
java -jar target/pst-search.jar --server.port=9090

# DB 경로 지정
java -jar target/pst-search.jar --spring.datasource.url=jdbc:sqlite:/data/my.db
```

DB 파일(`pst-search.db`)은 JAR 실행 경로에 자동 생성된다.
