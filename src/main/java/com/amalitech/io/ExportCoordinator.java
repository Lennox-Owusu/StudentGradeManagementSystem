
package com.amalitech.io;

import com.amalitech.reporting.StudentReport;
import com.amalitech.util.AppLogger;
import com.amalitech.exceptions.ExportFailedException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.*;

public final class ExportCoordinator {

    public static final class PerformanceSummary {
        public final long totalMillis;
        public final long csvMillis;
        public final long jsonMillis;
        public final long binMillis;
        public final long totalBytes;
        public final long csvBytes;
        public final long jsonBytes;
        public final long binBytes;

        public PerformanceSummary(long totalMillis, long csvMillis, long jsonMillis, long binMillis,
                                  long totalBytes, long csvBytes, long jsonBytes, long binBytes) {
            this.totalMillis = totalMillis;
            this.csvMillis = csvMillis;
            this.jsonMillis = jsonMillis;
            this.binMillis = binMillis;
            this.totalBytes = totalBytes;
            this.csvBytes = csvBytes;
            this.jsonBytes = jsonBytes;
            this.binBytes = binBytes;
        }

        public String compressionRatio() {
            if (binBytes == 0 || jsonBytes == 0) return "n/a";
            double ratio = (double) jsonBytes / (double) binBytes;
            return String.format("%.1f:1 (binary vs JSON)", ratio);
        }
    }

    private final ExecutorService pool = Executors.newFixedThreadPool(3);
    private final CSVReportExporter csv = new CSVReportExporter();
    private final JSONReportExporter json = new JSONReportExporter();
    private final BinaryReportExporter bin = new BinaryReportExporter();

    public PerformanceSummary exportAll(StudentReport report, Path baseDir, String baseName,
                                        boolean doCsv, boolean doJson, boolean doBin) throws ExportFailedException {
        Instant t0 = Instant.now();
        try {
            Path csvDir = baseDir.resolve("csv");
            Path jsonDir = baseDir.resolve("json");
            Path binDir = baseDir.resolve("binary");

            // Submit tasks conditionally
            Map<String, Future<Result>> futures = new ConcurrentHashMap<>();

            if (doCsv) futures.put("csv", pool.submit(() -> timed(() -> csv.exportDetailed(report, csvDir, baseName))));
            if (doJson) futures.put("json", pool.submit(() -> timed(() -> json.exportDetailed(report, jsonDir, baseName))));
            if (doBin) futures.put("bin", pool.submit(() -> timed(() -> bin.exportSerialized(report, binDir, baseName))));

            long csvMs = 0, jsonMs = 0, binMs = 0;
            long csvBytes = 0, jsonBytes = 0, binBytes = 0;

            // Gather results
            for (var e : futures.entrySet()) {
                String k = e.getKey();
                Result r;
                try {
                    r = e.getValue().get(60, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new ExportFailedException("Export interrupted", ex);
                } catch (ExecutionException | TimeoutException ex) {
                    throw new ExportFailedException("Export task failed", ex);
                }

                long size = 0;
                try { size = Files.size(r.path()); } catch (Exception ignore) {}

                switch (k) {
                    case "csv" -> { csvMs = r.millis(); csvBytes = size; }
                    case "json" -> { jsonMs = r.millis(); jsonBytes = size; }
                    case "bin" -> { binMs = r.millis(); binBytes = size; }
                }
            }

            long totalMs = Duration.between(t0, Instant.now()).toMillis();
            long totalBytes = csvBytes + jsonBytes + binBytes;

            AppLogger.info(String.format("Parallel export done: total=%dms csv=%dms json=%dms bin=%dms",
                    totalMs, csvMs, jsonMs, binMs));

            return new PerformanceSummary(totalMs, csvMs, jsonMs, binMs, totalBytes, csvBytes, jsonBytes, binBytes);
        } finally {
            pool.shutdown();
        }
    }

    // ---- helpers ----

    private static Result timed(Callable<Path> work) throws Exception {
        Instant t0 = Instant.now();
        Path p = work.call();
        return new Result(p, Duration.between(t0, Instant.now()).toMillis());
    }

    private record Result(Path path, long millis) { }
}
