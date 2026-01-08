
package com.amalitech.service;

import java.nio.file.Path;

public class ExportResult {
    private final Path filePath;
    private final long sizeBytes;
    private final boolean success;
    private final String message;

    public ExportResult(Path filePath, long sizeBytes, boolean success, String message) {
        this.filePath = filePath;
        this.sizeBytes = sizeBytes;
        this.success = success;
        this.message = message;
    }

    public Path getFilePath() { return filePath; }
    public long getSizeBytes() { return sizeBytes; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }

    public String getSizeText() {
        return String.format("%.1f KB", sizeBytes / 1024.0);
    }
}
