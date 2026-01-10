
package com.amalitech.menu.actions;

import com.amalitech.base.Student;
import com.amalitech.app.AppContext;
import com.amalitech.menu.MenuAction;
import com.amalitech.reporting.ReportType;
import com.amalitech.service.api.ExportSummary;
import com.amalitech.service.api.FormatResult;
import com.amalitech.util.ErrorHandler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Option 11: Generate Batch Reports (advanced version)
 * - Scope selection (All / By Type / By Grade Range / Custom)
 * - Format selection with styles
 * - Concurrency settings (threads) + live status & progress
 * - Full execution summary (times, performance, pool stats, output size)
 */
public class GenerateBatchReportsAction implements MenuAction {
    private final AppContext ctx;

    public GenerateBatchReportsAction(AppContext ctx) { this.ctx = ctx; }

    @Override public String label() { return "Generate Batch Reports"; }

    @Override
    public void execute() {
        try {
            System.out.println();
            System.out.println("GENERATE BATCH REPORTS");
            System.out.println("--------------------------------");

            // ---------- Scope selection ----------
            List<Student> allStudents = ctx.students.listStudents();
            int totalAll = allStudents.size();

            System.out.println();
            System.out.println("Report Scope:");
            System.out.printf("1. All Students (%d students)%n", totalAll);
            System.out.println("2. By Student Type (Regular/Honors)");
            System.out.println("3. By Grade Range");
            System.out.println("4. Custom Selection");
            System.out.print("\nSelect scope (1–4): ");
            int scope = parseIntSafe(ctx.scanner.nextLine(), 1);

            List<Student> selected = switch (scope) {
                case 2 -> selectByType(allStudents);
                case 3 -> selectByGradeRange(allStudents);
                case 4 -> selectCustom(allStudents);
                default -> allStudents;
            };
            if (selected.isEmpty()) {
                System.out.println("(no students match the selected scope)");
                return;
            }

            // ---------- Format selection ----------
            System.out.println();
            System.out.println("Report Format:");
            System.out.println("1. PDF Summary");
            System.out.println("2. Detailed Text");
            System.out.println("3. Excel Spreadsheet");
            System.out.println("4. All Formats");
            System.out.print("\nSelect format (1–4): ");
            int fmtChoiceScreen = parseIntSafe(ctx.scanner.nextLine(), 2);

            // Map screen formats to existing exporter formatChoice + ReportType
            // formatChoice: 1=CSV, 2=JSON, 3=Binary, 4=All (existing)
            int formatChoice;
            ReportType reportType;
            switch (fmtChoiceScreen) {
                case 1 -> { formatChoice = 2; reportType = ReportType.SUMMARY; }     // "PDF Summary" -> use JSON + SUMMARY
                case 2 -> { formatChoice = 1; reportType = ReportType.DETAILED; }    // "Detailed Text" -> use CSV + DETAILED
                case 3 -> { formatChoice = 1; reportType = ReportType.ANALYTICS; }   // "Excel Spreadsheet" -> use CSV (as spreadsheet) + ANALYTICS
                default -> { formatChoice = 4; reportType = ReportType.DETAILED; }   // "All Formats" -> CSV+JSON+Binary, DETAILED
            }

            // ---------- Concurrency settings ----------
            System.out.println();
            System.out.println("Concurrency Settings:\n");
            final int procs = Runtime.getRuntime().availableProcessors();
            final int recMin = Math.max(1, Math.min(4, procs / 2));  // show 4–8 as common guidance when procs >= 8
            final int recMax = Math.max(1, Math.min(8, procs));
            System.out.printf("Available Processors: %d%n", procs);
            System.out.printf("Recommended Threads: %d-%d%n", recMin, recMax);
            System.out.printf("%nEnter number of threads (1-%d): ", recMax);
            int threads = clamp(parseIntSafe(ctx.scanner.nextLine(), recMin), 1, recMax);

            // ---------- Initialize thread pool ----------
            System.out.println();
            System.out.println("Initializing thread pool...");
            ThreadFactory tf = new NamedThreadFactory("Thread");
            ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads, tf);
            System.out.printf("✓ Fixed Thread Pool created: %d threads%n", threads);

            // ---------- Prepare batch folder (for summary display) ----------
            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Path batchDir = Paths.get("./reports").resolve("batch_" + dateStr);
            try { Files.createDirectories(batchDir); } catch (Exception ignore) {}

            // ---------- Submit tasks & live status ----------
            int total = selected.size();
            System.out.printf("%nProcessing %d student reports...%n", total);
            System.out.println();
            System.out.println("BATCH PROCESSING STATUS");
            System.out.println("--------------------------------");

            AtomicInteger done = new AtomicInteger(0);
            AtomicInteger successful = new AtomicInteger(0);
            AtomicInteger failed = new AtomicInteger(0);
            List<Long> durations = Collections.synchronizedList(new ArrayList<>());
            List<Long> queueWaits = Collections.synchronizedList(new ArrayList<>());
            List<FormatResult> allFiles = Collections.synchronizedList(new ArrayList<>());
            Map<String, Long> submitTime = new ConcurrentHashMap<>();
            Map<String, Long> startTime = new ConcurrentHashMap<>();
            Map<String, Long> finishTime = new ConcurrentHashMap<>();
            Map<String, String> lastThreadLine = new ConcurrentHashMap<>(); // Thread-X: STU001 ✓ (234ms)

            long t0 = System.nanoTime();

            // Schedule monitor (prints progress every 1s)
            ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Monitor"));
            monitor.scheduleAtFixedRate(() -> {
                try {
                    // Print last lines per thread name
                    List<String> threadsNames = pool.getPoolSize() > 0 ? collectThreadNames(pool) : Collections.emptyList();
                    for (String name : threadsNames) {
                        String line = lastThreadLine.get(name);
                        if (line != null) System.out.println(line);
                    }
                    // Progress bar
                    int doneCount = done.get();
                    String bar = progressBar((double) doneCount / Math.max(1, total), 28);
                    System.out.printf("%nProgress: [%s] %d%% (%d/%d completed)%n", bar,
                            (int) Math.round((doneCount * 100.0) / total), doneCount, total);

                    // Time statistics
                    long elapsedMs = (System.nanoTime() - t0) / 1_000_000;
                    double avgMs = durations.isEmpty() ? 0.0 :
                            durations.stream().mapToLong(Long::longValue).average().orElse(0.0);
                    double remMs = (total - doneCount) * avgMs;
                    double throughput = elapsedMs > 0 ? (doneCount * 1000.0 / elapsedMs) : 0.0;

                    System.out.printf("%nTime Statistics:%n");
                    System.out.printf(" Elapsed: %.1fs%n", elapsedMs / 1000.0);
                    System.out.printf(" Estimated Remaining: %.1fs%n", remMs / 1000.0);
                    System.out.printf(" Avg Report Time: %.0fms%n", avgMs);
                    System.out.printf(" Throughput: %.1f reports/sec%n", throughput);
                    System.out.println();
                } catch (Exception ignore) {}
            }, 500, 1000, TimeUnit.MILLISECONDS);

            // Publish progress to dashboard registry (if present)
            com.amalitech.concurrent.TaskProgressRegistry.set("batch_reports", 0, pool.getActiveCount(), false);
            com.amalitech.concurrent.BackgroundTaskTracker.incrementActive();

            // Submit tasks
            List<Future<?>> futures = new ArrayList<>(total);
            for (Student s : selected) {
                String sid = s.getStudentId();
                submitTime.put(sid, System.nanoTime());
                futures.add(pool.submit(() -> {
                    String threadName = Thread.currentThread().getName();
                    startTime.put(sid, System.nanoTime());
                    try {
                        ExportSummary summary = ctx.exporter.exportReport(s, formatChoice, reportType);
                        long durMs = (System.nanoTime() - startTime.get(sid)) / 1_000_000;
                        durations.add(durMs);
                        successful.incrementAndGet();
                        allFiles.addAll(summary.getResults());

                        lastThreadLine.put(threadName,
                                String.format("%s: %s ✓ (%dms)", threadName, sid, durMs));
                    } catch (Exception ex) {
                        failed.incrementAndGet();
                        lastThreadLine.put(threadName,
                                String.format("%s: %s ✗ (failed)", threadName, sid));
                        ErrorHandler.handle("Batch export for " + sid, ex);
                    } finally {
                        finishTime.put(sid, System.nanoTime());
                        long waitMs = (startTime.getOrDefault(sid, System.nanoTime()) - submitTime.getOrDefault(sid, System.nanoTime())) / 1_000_000;
                        queueWaits.add(Math.max(0, waitMs));
                        int nowDone = done.incrementAndGet();
                        com.amalitech.concurrent.TaskProgressRegistry.set("batch_reports",
                                (int) Math.round(nowDone * 100.0 / total),
                                pool.getActiveCount(),
                                nowDone >= total);
                    }
                }));
            }

            // Wait for completion
            for (Future<?> f : futures) { try { f.get(); } catch (Exception ignore) {} }

            long t1 = System.nanoTime();
            monitor.shutdownNow();
            pool.shutdown();
            com.amalitech.concurrent.BackgroundTaskTracker.decrementActive();

            // ---------- Execution summary ----------
            System.out.println();
            System.out.println("✓ BATCH GENERATION COMPLETED!");
            System.out.println();
            System.out.println("EXECUTION SUMMARY");
            System.out.println("--------------------------------");

            int ok = successful.get();
            int fail = failed.get();
            int totalReports = total;
            double totalSeconds = (t1 - t0) / 1_000_000_000.0;
            double avgMsPerReport = durations.isEmpty() ? 0.0 :
                    durations.stream().mapToLong(Long::longValue).average().orElse(0.0);

            double sequentialSeconds = (avgMsPerReport * totalReports) / 1000.0;
            double perfGain = sequentialSeconds > 0 ? sequentialSeconds / totalSeconds : 0.0;

            // Pool stats (peak / tasks)
            long totalTasks = pool.getTaskCount();
            int peakThreads = pool.getLargestPoolSize();
            double avgQueueMs = queueWaits.isEmpty() ? 0.0 :
                    queueWaits.stream().mapToLong(Long::longValue).average().orElse(0.0);
            double utilization = computeThreadUtilization(durations, threads, totalSeconds);

            // Files & size
            long totalFiles = allFiles.size();
            long totalBytes = 0;
            for (FormatResult r : allFiles) totalBytes += r.getSizeBytes();

            System.out.printf("Total Reports: %d%n", totalReports);
            System.out.printf("Successful: %d%n", ok);
            System.out.printf("Failed: %d%n", fail);
            System.out.printf("Total Time: %.1f seconds%n", totalSeconds);
            System.out.printf("Avg Time per Report: %.0fms%n", avgMsPerReport);

            System.out.printf("%nSequential Processing (estimated): %.1f seconds%n", sequentialSeconds);
            System.out.printf("Concurrent Processing (actual): %.1f seconds%n", totalSeconds);
            System.out.printf("Performance Gain: %.1fx faster%n", perfGain);

            System.out.printf("%nThread Pool Statistics:%n");
            System.out.printf("  Peak Thread Count: %d%n", peakThreads);
            System.out.printf("  Total Tasks Executed: %d%n", totalTasks);
            System.out.printf("  Average Queue Time: %.0fms%n", avgQueueMs);
            System.out.printf("  Thread Utilization: %.1f%%%n", utilization);

            System.out.printf("%nOutput Location: %s/%n", batchDir.toString());
            System.out.printf("Total Files Generated: %d%n", totalFiles);
            System.out.printf("Total Size: %.1f KB%n", totalBytes / 1024.0);

            System.out.print("\nPress Enter to continue...");
            ctx.scanner.nextLine();

        } catch (Exception ex) {
            ErrorHandler.handle("Generate Batch Reports", ex);
        }
    }

    // ---------------------- Helpers ----------------------

    private List<Student> selectByType(List<Student> src) {
        System.out.print("Enter type (Regular/Honors): ");
        String t = ctx.scanner.nextLine().trim();
        return src.stream()
                .filter(s -> s.getStudentType().equalsIgnoreCase(t))
                .collect(Collectors.toList());
    }

    private List<Student> selectByGradeRange(List<Student> src) {
        System.out.print("Min average (0–100): ");
        double min = parseDoubleSafe(ctx.scanner.nextLine(), 0);
        System.out.print("Max average (0–100): ");
        double max = parseDoubleSafe(ctx.scanner.nextLine(), 100);
        return src.stream()
                .filter(s -> {
                    double avg = s.calculateAverageGrade();
                    return avg >= min && avg <= max;
                })
                .collect(Collectors.toList());
    }

    private List<Student> selectCustom(List<Student> src) {
        System.out.print("Enter Student IDs (comma-separated): ");
        String line = ctx.scanner.nextLine();
        Set<String> ids = Arrays.stream(line.split(","))
                .map(String::trim).map(String::toUpperCase)
                .filter(s -> !s.isEmpty()).collect(Collectors.toSet());
        return src.stream()
                .filter(s -> ids.contains(s.getStudentId().toUpperCase()))
                .collect(Collectors.toList());
    }

    private static int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }
    private static double parseDoubleSafe(String s, double def) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return def; }
    }
    private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }

    private static String progressBar(double fraction, int width) {
        int filled = (int) Math.round(fraction * width);
        return "█".repeat(Math.max(0, filled)) + "░".repeat(Math.max(0, width - filled));
    }

    private static double computeThreadUtilization(List<Long> durationsMs, int threads, double totalSec) {
        double activeMs = durationsMs.stream().mapToLong(Long::longValue).sum();
        double capacityMs = threads * totalSec * 1000.0;
        if (capacityMs <= 0) return 0.0;
        return Math.min(100.0, (activeMs * 100.0) / capacityMs);
    }

    private static List<String> collectThreadNames(ThreadPoolExecutor pool) {
        //build names Thread-1.Thread-N for display.
        int n = pool.getPoolSize();
        List<String> names = new ArrayList<>(n);
        for (int i = 1; i <= n; i++) names.add("Thread-" + i);
        return names;
    }

    // Named thread factory to print "Thread-1", "Thread-2", ...
    private static class NamedThreadFactory implements ThreadFactory {
        private final String base;
        private final AtomicInteger counter = new AtomicInteger(1);
        NamedThreadFactory(String base) { this.base = base; }
        @Override public Thread newThread(Runnable r) {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setName(base + "-" + counter.getAndIncrement());
            t.setDaemon(false);
            return t;
        }
    }
}
