package com.pstsearch.controller;

import com.pff.PSTAttachment;
import com.pff.PSTFile;
import com.pff.PSTMessage;
import com.pff.PSTObject;
import com.pstsearch.service.AttachmentService;
import com.pstsearch.service.AttachmentService.MailAttachmentInfo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/mails")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    /** index: 첨부파일 표시 순서 기준 0-based */
    @GetMapping("/{mailId}/attachments/{index}")
    public ResponseEntity<StreamingResponseBody> download(
            @PathVariable Long mailId,
            @PathVariable int index) throws Exception {

        MailAttachmentInfo info = attachmentService.getInfo(mailId);

        PSTFile pstFile = new PSTFile(info.pstFilePath());
        try {
            PSTMessage message = (PSTMessage) PSTObject.detectAndLoadPSTObject(pstFile, info.pstDescriptorId());

            int named = 0;
            for (int i = 0; i < message.getNumberOfAttachments(); i++) {
                PSTAttachment att = message.getAttachment(i);
                String name = att.getLongFilename();
                if (name == null || name.isBlank()) name = att.getFilename();
                if (name == null || name.isBlank()) continue;

                if (named == index) {
                    final String filename = name;
                    final InputStream stream = att.getFileInputStream();
                    final PSTFile toClose = pstFile;

                    StreamingResponseBody body = out -> {
                        try {
                            byte[] buf = new byte[8192];
                            int n;
                            while ((n = stream.read(buf)) != -1) out.write(buf, 0, n);
                        } finally {
                            try { stream.close(); } catch (Exception ignored) {}
                            try { toClose.getFileHandle().close(); } catch (Exception ignored) {}
                        }
                    };

                    String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                            .replace("+", "%20");

                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename*=UTF-8''" + encoded)
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .body(body);
                }
                named++;
            }
        } catch (Exception e) {
            try { pstFile.getFileHandle().close(); } catch (Exception ignored) {}
            throw e;
        }

        pstFile.getFileHandle().close();
        return ResponseEntity.notFound().build();
    }

    /** 메일의 모든 첨부파일을 ZIP으로 일괄 다운로드 */
    @GetMapping("/{mailId}/attachments/zip")
    public ResponseEntity<StreamingResponseBody> downloadZip(
            @PathVariable Long mailId) throws Exception {

        MailAttachmentInfo info = attachmentService.getInfo(mailId);

        PSTFile pstFile = new PSTFile(info.pstFilePath());
        PSTMessage message;
        try {
            message = (PSTMessage) PSTObject.detectAndLoadPSTObject(pstFile, info.pstDescriptorId());
        } catch (Exception e) {
            try { pstFile.getFileHandle().close(); } catch (Exception ignored) {}
            throw e;
        }

        StreamingResponseBody body = out -> {
            try (ZipOutputStream zip = new ZipOutputStream(out)) {
                for (int i = 0; i < message.getNumberOfAttachments(); i++) {
                    PSTAttachment att;
                    try { att = message.getAttachment(i); } catch (Exception e) { throw new java.io.IOException(e); }
                    String name = att.getLongFilename();
                    if (name == null || name.isBlank()) name = att.getFilename();
                    if (name == null || name.isBlank()) continue;

                    zip.putNextEntry(new ZipEntry(name));
                    InputStream stream;
                    try { stream = att.getFileInputStream(); } catch (Exception e) { throw new java.io.IOException(e); }
                    try {
                        byte[] buf = new byte[8192];
                        int n;
                        while ((n = stream.read(buf)) != -1) zip.write(buf, 0, n);
                    } finally {
                        try { stream.close(); } catch (Exception ignored) {}
                    }
                    zip.closeEntry();
                }
            } finally {
                try { pstFile.getFileHandle().close(); } catch (Exception ignored) {}
            }
        };

        String zipName = URLEncoder.encode("attachments_" + mailId + ".zip", StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + zipName)
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(body);
    }

    /**
     * HTML 본문을 PST에서 직접 읽어 반환.
     * 인라인 이미지(cid: 참조)를 base64 data URI로 교체하여 브라우저에서 바로 표시.
     * HTML이 없으면 plain text를 pre 태그로 감싸 반환.
     */
    @GetMapping("/{mailId}/html-body")
    public ResponseEntity<byte[]> htmlBody(@PathVariable Long mailId) throws Exception {

        MailAttachmentInfo info = attachmentService.getInfo(mailId);

        PSTFile pstFile = new PSTFile(info.pstFilePath());
        String html;
        try {
            PSTMessage message = (PSTMessage) PSTObject.detectAndLoadPSTObject(pstFile, info.pstDescriptorId());
            html = message.getBodyHTML();
            if (html != null && !html.isBlank()) {
                html = resolveCidImages(html, message);
            } else {
                String plain = message.getBody();
                if (plain == null) plain = "";
                html = "<!DOCTYPE html><html><body><pre style=\"font-family:sans-serif;font-size:13px;"
                     + "white-space:pre-wrap;word-break:break-word;margin:8px\">"
                     + escapeHtml(plain) + "</pre></body></html>";
            }
        } finally {
            try { pstFile.getFileHandle().close(); } catch (Exception ignored) {}
        }

        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/html; charset=UTF-8"))
                .body(bytes);
    }

    /**
     * HTML 내 cid: 참조를 PST 인라인 첨부파일의 base64 data URI로 교체.
     */
    private static String resolveCidImages(String html, PSTMessage message) {
        Map<String, String> cidMap = new HashMap<>();

        try {
            int count = message.getNumberOfAttachments();
            for (int i = 0; i < count; i++) {
                try {
                    PSTAttachment att = message.getAttachment(i);

                    // Content-ID 추출 (PR_ATTACH_CONTENT_ID)
                    String cid = null;
                    try { cid = att.getContentId(); } catch (Exception ignored) {}
                    if (cid == null || cid.isBlank()) continue;

                    // 꺾쇠 제거: <image001.jpg@...> → image001.jpg@...
                    cid = cid.trim().replaceAll("^<|>$", "");

                    // MIME 타입 결정
                    String mime = null;
                    try { mime = att.getMimeTag(); } catch (Exception ignored) {}
                    if (mime == null || mime.isBlank()) {
                        String fname = att.getLongFilename();
                        if (fname == null || fname.isBlank()) fname = att.getFilename();
                        mime = guessMime(fname);
                    }
                    if (mime == null || mime.isBlank()) mime = "image/png";

                    // 데이터 읽어서 base64 인코딩
                    byte[] data = att.getFileInputStream().readAllBytes();
                    String dataUri = "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(data);
                    cidMap.put(cid.toLowerCase(), dataUri);

                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}

        if (cidMap.isEmpty()) return html;

        // cid:xxx 패턴을 data URI로 교체 (대소문자 무시)
        Pattern pattern = Pattern.compile("cid:([^\"'\\s>]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String cidRef = matcher.group(1).toLowerCase();
            String replacement = cidMap.getOrDefault(cidRef, matcher.group(0));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String guessMime(String filename) {
        if (filename == null) return null;
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif"))  return "image/gif";
        if (lower.endsWith(".bmp"))  return "image/bmp";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".svg"))  return "image/svg+xml";
        if (lower.endsWith(".tif") || lower.endsWith(".tiff")) return "image/tiff";
        return null;
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
