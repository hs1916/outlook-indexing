package com.pstsearch.controller;

import com.pstsearch.dto.PstFileDto;
import com.pstsearch.service.PstFileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pst-files")
public class PstFileController {

    private final PstFileService pstFileService;

    public PstFileController(PstFileService pstFileService) {
        this.pstFileService = pstFileService;
    }

    @GetMapping
    public ResponseEntity<List<PstFileDto>> list() {
        return ResponseEntity.ok(pstFileService.findAll());
    }

    @PostMapping
    public ResponseEntity<PstFileDto> register(@RequestBody Map<String, String> body) {
        String filePath = body.get("filePath");
        if (filePath == null || filePath.isBlank()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(pstFileService.register(filePath.trim()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pstFileService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
