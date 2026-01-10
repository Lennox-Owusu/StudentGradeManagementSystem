
package com.amalitech.service.api;

/** Per-format result row shown in the console after export. */
public class FormatResult {
    private final String format;      // "CSV", "JSON", "Binary"
    private final String fileName;    // e.g., alice_johnson_detailed.csv
    private final String location;    // e.g., ./reports/csv/
    private final long sizeBytes;
    private final double timeMs;
    private final String description; // e.g., "Rows: 12 grades + header"

    public FormatResult(String format, String fileName, String location,
                        long sizeBytes, double timeMs, String description) {
        this.format = format; this.fileName = fileName; this.location = location;
        this.sizeBytes = sizeBytes; this.timeMs = timeMs; this.description = description;
    }
    public String getFormat() { return format; }
    public String getFileName() { return fileName; }
    public String getLocation() { return location; }
    public long getSizeBytes() { return sizeBytes; }
    public double getTimeMs() { return timeMs; }
    public String getDescription() { return description; }
}
