
package com.amalitech.util;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


//Simple file logger that appends timestamped lines to ./logs/app.log
public final class AppLogger {
    private static final Path LOG_DIR = Paths.get("./logs");
    private static final Path LOG_FILE = LOG_DIR.resolve("app.log");
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AppLogger() {}

    public static void info(String message) {
        write("INFO", message, null);
    }

    public static void error(String message) {
        Throwable t = new Throwable();
        write("ERROR", message, t);
    }

    private static void write(String level, String message, Throwable t) {
        String ts = LocalDateTime.now().format(TS);
        StringBuilder line = new StringBuilder()
                .append(ts).append(" [").append(level).append("] ").append(message);
        if (t != null) {
            line.append(System.lineSeparator())
                    .append("   -> ").append(t.getClass().getName()).append(": ").append(t.getMessage());
        }

        try {
            if (!Files.exists(LOG_DIR)) Files.createDirectories(LOG_DIR);
            Files.writeString(LOG_FILE, line + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException io) {
            // Last-resort: print to console.
            System.out.println("⚠ Logging failed: " + io.getMessage());
        }
    }
}
