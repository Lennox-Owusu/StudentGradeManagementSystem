
package com.amalitech.service;

import com.amalitech.io.FileExporter;
import com.amalitech.util.AppLogger;
import com.amalitech.exceptions.ExportFailedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExportService implements com.amalitech.service.api.IExportService {
    private final FileExporter fileExporter;

    public ExportService(FileExporter fileExporter) {
        this.fileExporter = fileExporter;
    }

    public ExportResult exportReport(String content, String baseName) {
        if (content == null || content.isEmpty()) {
            return new ExportResult(null, 0, false, "Report content is empty.");
        }
        if (baseName == null || baseName.trim().isEmpty()) {
            baseName = "report_" + System.currentTimeMillis();
        }

        Path reportsDir = Paths.get("./reports");
        try {
            if (!Files.exists(reportsDir)) Files.createDirectories(reportsDir);
        } catch (IOException e) {
            return new ExportResult(null, 0, false, "Failed to create reports directory: " + e.getMessage());
        }

        Path target = reportsDir.resolve(baseName + ".txt");
        try {
            fileExporter.exportToFile(content, target);
        } catch (ExportFailedException e) {
            return new ExportResult(target, 0, false, "Export failed: " + e.getMessage());
        }

        long size = 0;
        try {
            size = Files.size(target);
        } catch (IOException ignored) { }

        AppLogger.info("Report exported successfully: " + target.getFileName());
        return new ExportResult(target, size, true, "Report exported successfully.");
    }
}
