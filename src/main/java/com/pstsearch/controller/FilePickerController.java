package com.pstsearch.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/api/file-picker")
public class FilePickerController {

    @GetMapping
    public ResponseEntity<Map<String, String>> pickFile() throws Exception {
        AtomicReference<String> selected = new AtomicReference<>();

        SwingUtilities.invokeAndWait(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            // 항상 앞에 나타나는 부모 프레임
            JFrame parent = new JFrame();
            parent.setAlwaysOnTop(true);
            parent.setUndecorated(true);
            parent.setVisible(true);
            parent.toFront();
            parent.requestFocus();

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("PST 파일 선택");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileFilter(new FileNameExtensionFilter("PST 파일 (*.pst)", "pst"));
            chooser.setAcceptAllFileFilterUsed(false);

            int result = chooser.showOpenDialog(parent);
            parent.dispose();

            if (result == JFileChooser.APPROVE_OPTION) {
                selected.set(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        if (selected.get() == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(Map.of("filePath", selected.get()));
    }
}
