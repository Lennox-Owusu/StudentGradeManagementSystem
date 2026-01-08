
package com.amalitech.service;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class ImportResult {
    private final Path csvPath;
    private final Path logPath;
    private final int totalRows;
    private final int successCount;
    private final int failureCount;
    private final List<String> failures;
    private final boolean fileFound;

    public ImportResult(Path csvPath, Path logPath, int totalRows, int successCount,
                        int failureCount, List<String> failures, boolean fileFound) {
        this.csvPath = csvPath;
        this.logPath = logPath;
        this.totalRows = totalRows;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.failures = failures == null ? Collections.emptyList() : Collections.unmodifiableList(failures);
        this.fileFound = fileFound;
    }

    public Path getCsvPath() { return csvPath; }
    public Path getLogPath() { return logPath; }
    public int getTotalRows() { return totalRows; }
    public int getSuccessCount() { return successCount; }
    public int getFailureCount() { return failureCount; }
    public List<String> getFailures() { return failures; }
    public boolean isFileFound() { return fileFound; }
}
