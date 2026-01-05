
package com.amalitech.io;

public final class ExportResult {
    private final long bytes;
    private final long millis;
    private final String fileName;
    private final String location;

    public ExportResult(long bytes, long millis, String fileName, String location) {
        this.bytes = bytes;
        this.millis = millis;
        this.fileName = fileName;
        this.location = location;
    }

    public long getBytes()    { return bytes; }
    public long getMillis()   { return millis; }
    public String getFileName(){ return fileName; }
    public String getLocation(){ return location; }
}
