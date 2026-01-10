
package com.amalitech.service.api;

import java.util.ArrayList;
import java.util.List;

/** Aggregated metrics for a single export command. */
public class ExportSummary {
    private double totalTimeMs;
    private long totalSizeBytes;
    private Double compressionRatio; // binary vs JSON (optional)
    private int parallelWrites;
    private final List<FormatResult> results = new ArrayList<>();

    public double getTotalTimeMs() { return totalTimeMs; }
    public void setTotalTimeMs(double totalTimeMs) { this.totalTimeMs = totalTimeMs; }

    public long getTotalSizeBytes() { return totalSizeBytes; }
    public void setTotalSizeBytes(long totalSizeBytes) { this.totalSizeBytes = totalSizeBytes; }

    public Double getCompressionRatio() { return compressionRatio; }
    public void setCompressionRatio(Double compressionRatio) { this.compressionRatio = compressionRatio; }

    public int getParallelWrites() { return parallelWrites; }
    public void setParallelWrites(int parallelWrites) { this.parallelWrites = parallelWrites; }

    public List<FormatResult> getResults() { return results; }
    public void addResult(FormatResult r) { if (r != null) results.add(r); }
}
