
package com.amalitech.dashboard;

import com.amalitech.base.Grade;
import com.amalitech.base.Student;
import com.amalitech.cache.CacheService;
import com.amalitech.concurrent.TaskProgressRegistry;
import com.amalitech.manager.GradeManager;
import com.amalitech.manager.StudentManager;
import com.amalitech.monitor.GradeEventTracker;
import com.amalitech.monitor.PoolMetrics;
import com.amalitech.calculation.StatisticsCalculator;
import com.amalitech.reporting.GPACalculator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public final class RealTimeDashboard {
    private final StatisticsCalculator stats = new StatisticsCalculator();
    private final GPACalculator gpa = new GPACalculator();
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void runInteractive(Scanner scanner,
                               StudentManager sm,
                               GradeManager gm,
                               ExecutorService fixedPool,
                               ExecutorService cachedPool,
                               ScheduledThreadPoolExecutor scheduler,
                               CacheService<String, Object> cacheService) {

        boolean running = true;
        boolean auto = true;
        int refreshSecs = 5;

        while (running) {
            renderOnce(sm, gm, fixedPool, cachedPool, scheduler, cacheService, refreshSecs, auto);

            System.out.print("\nCommand: "); // Q=quit, R=refresh, P=pause/resume
            String cmd = scanner.nextLine().trim().toUpperCase();
            switch (cmd) {
                case "", "R" -> {
                    // refresh immediately
                    continue;
                    // refresh immediately
                }
                case "Q" -> running = false;
                case "P" -> auto = !auto;
            }

            if (auto) {
                try { Thread.sleep(refreshSecs * 1000L); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void renderOnce(StudentManager sm,
                            GradeManager gm,
                            ExecutorService fixedPool,
                            ExecutorService cachedPool,
                            ScheduledThreadPoolExecutor scheduler,
                            CacheService<String, Object> cacheService,
                            int refreshSecs,
                            boolean autoEnabled) {

        // Snapshot time
        String now = LocalDateTime.now().format(TS);

        // --- Gather numbers ---
        int totalStudents = sm.getStudentCount();
        int totalGrades = gm.getGradeCount();
        int activeThreads = PoolMetrics.activeThreads();
        String mem = PoolMetrics.memoryStatus();
        String cacheHit = cacheService == null ? "n/a" : cacheService.hitRateText();

        // Collect grade values
        List<Double> values = new ArrayList<>(totalGrades);
        for (int i = 0; i < gm.getGradeCount(); i++) {
            Grade g = gm.getGradeAt(i);
            if (g != null) values.add(g.getGrade());
        }
        int[] bands = stats.gradeBandsCounts(values);

        double mean   = stats.mean(values);
        double median = stats.median(values);
        double std    = stats.stdDevPopulation(values);

        // Top performers by GPA (live rankings)
        List<Student> students = Arrays.asList(sm.getStudents());
        students.sort((a, b) -> {
            double ga = gpa.computeGPA(a.getGrades());
            double gb = gpa.computeGPA(b.getGrades());
            return Double.compare(gb, ga); // descending
        });

        // Formatting helpers
        String autoText = String.format("Auto-refresh: %s (%d sec) | Thread: %s",
                autoEnabled ? "Enabled" : "Paused", refreshSecs, "RUNNING");
        String lastUpdated = "Last Updated: " + now;

        // --- Render ---
        System.out.println();
        System.out.println("REAL-TIME STATISTICS DASHBOARD");
        System.out.println("────────────────────────────────────────────");
        System.out.println(autoText);
        System.out.println("Press 'Q' to quit | 'R' to refresh now | 'P' to pause");
        System.out.println();
        System.out.println(lastUpdated);
        System.out.println();

        System.out.println("SYSTEM STATUS");
        System.out.println("────────────────────────────────────────────");
        System.out.println("Total Students: " + totalStudents);
        System.out.println("Active Threads: " + activeThreads);
        System.out.println("Cache Hit Rate: " + cacheHit);
        System.out.println(mem);
        System.out.println();

        System.out.println("LIVE STATISTICS");
        System.out.println("────────────────────────────────────────────");
        System.out.println("Total Grades: " + totalGrades);
        System.out.println("Grades Added (last 5 min): " + GradeEventTracker.countLast(java.time.Duration.ofMinutes(5)));
        System.out.println("Average Processing Time: " + GradeEventTracker.averageProcessingMs() + "ms");
        System.out.println();

        // Grade Distribution
        System.out.println("Grade Distribution (Live):");
        printBand("90–100% (A):", bands[0], totalGrades);
        printBand("80–89% (B):",  bands[1], totalGrades);
        printBand("70–79% (C):",  bands[2], totalGrades);
        printBand("60–69% (D):",  bands[3], totalGrades);
        printBand("0–59% (F):",   bands[4], totalGrades);
        System.out.println();

        System.out.println("Current Statistics:");
        System.out.printf("Mean:   %5.1f%%%n", mean);
        System.out.printf("Median: %5.1f%%%n", median);
        System.out.printf("Std Dev:%5.1f%%%n", std);
        System.out.println();

        System.out.println("Top Performers (Live Rankings):");
        int top = Math.min(3, students.size());
        for (int i = 0; i < top; i++) {
            Student s = students.get(i);
            double gpaVal = gpa.computeGPA(s.getGrades());
            System.out.printf("%d. %s - %s  GPA: %.2f%n", i + 1, s.getStudentId(), s.getName(), gpaVal);
        }
        if (top == 0) System.out.println("(no students)");

        System.out.println();
        System.out.println("CONCURRENT OPERATIONS IN PROGRESS");
        System.out.println("────────────────────────────────────────────");
        Map<String, TaskProgressRegistry.TaskInfo> ops = TaskProgressRegistry.snapshot();
        if (ops.isEmpty()) {
            System.out.println("(no active operations)");
        } else {
            for (var e : ops.values()) {
                String bar = progressBar(e.percent());
                String status = e.completed() ? "COMPLETED" : String.format("%d%%", e.percent());
                System.out.printf("%s %s - %s - %d threads%n", bar, e.name(), status, e.threads());
            }
        }
        System.out.println();

        System.out.println("Thread Pool Status:");
        System.out.println(PoolMetrics.fixedStatus(fixedPool));
        System.out.println(PoolMetrics.cachedStatus(cachedPool));
        System.out.println(PoolMetrics.scheduledStatus(scheduler));
        System.out.println();
        System.out.println("Auto-refresh in: " + refreshSecs + " seconds...");
        System.out.println();
    }

    // visual blocks like in your class statistics view
    private static void printBand(String label, int count, int total) {
        double pct = total == 0 ? 0.0 : (count * 100.0 / total);
        String bar = buildBar(count, total);
        System.out.printf("%-13s %s %4.1f%% (%d grades)%n", label, bar, pct, count);
    }

    private static String buildBar(int count, int total) {
        int filled = (total == 0) ? 0 : (int) Math.round((count * 1.0 / total) * 28);
        if (filled < 0) filled = 0; if (filled > 28) filled = 28;
        String filledPart = "█".repeat(filled);
        String emptyPart  = "░".repeat(28 - filled);
        return filledPart + emptyPart;
    }

    private static String progressBar(int percent) {
        int width = 20;
        int filled = Math.max(0, Math.min(width, (int) Math.round(percent / 5.0)));
        return "[" + "█".repeat(filled) + "░".repeat(width - filled) + "]";
    }
}
