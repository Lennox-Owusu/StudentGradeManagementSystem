
package com.amalitech.service.impl;

import com.amalitech.service.api.IAuditService;
import com.amalitech.util.AppLogger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AuditServiceImpl implements IAuditService {
    private static final Path LOG = Paths.get("./logs/app.log");
    private static final Path REPORTS = Paths.get("./reports");

    @Override public List<String> tailLast(int n) {
        if (!Files.exists(LOG)) return List.of();
        Deque<String> dq = new ArrayDeque<>(n);
        try (var br = Files.newBufferedReader(LOG, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (dq.size() == n) dq.removeFirst();
                dq.addLast(line);
            }
        } catch (IOException io) { AppLogger.error("Audit tail failed"); }
        return new ArrayList<>(dq);
    }

    @Override public List<String> filterByLevel(String level) {
        if (!Files.exists(LOG)) return List.of();
        try (var stream = Files.lines(LOG, StandardCharsets.UTF_8)) {
            return stream.filter(l -> l.contains("[" + level.toUpperCase() + "]")).toList();
        } catch (IOException io) { AppLogger.error("Audit filter failed"); return List.of(); }
    }

    @Override public List<String> searchByKeyword(String keyword) {
        if (!Files.exists(LOG)) return List.of();
        String kw = keyword.toLowerCase();
        try (var stream = Files.lines(LOG, StandardCharsets.UTF_8)) {
            return stream.filter(l -> l.toLowerCase().contains(kw)).toList();
        } catch (IOException io) { AppLogger.error("Audit search failed"); return List.of(); }
    }

    @Override public void exportView(List<String> lines) {
        try {
            Files.createDirectories(REPORTS);
            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path out = REPORTS.resolve("audit_view_" + ts + ".log");
            Files.write(out, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException io) { AppLogger.error("Audit export failed"); }
    }

    @Override public void archiveAndTruncate() {
        try {
            if (!Files.exists(LOG)) return;
            Path dir = LOG.getParent();
            if (dir != null && !Files.exists(dir)) Files.createDirectories(dir);
            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path archive = dir.resolve("app_" + ts + ".log");
            Files.copy(LOG, archive, StandardCopyOption.REPLACE_EXISTING);
            Files.writeString(LOG, "", StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (IOException io) { AppLogger.error("Audit archive failed"); }
    }
}
