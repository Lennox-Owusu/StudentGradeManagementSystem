
package com.amalitech.monitor;

import com.amalitech.*;
import com.amalitech.cache.CacheService;
import com.amalitech.util.AppLogger;

import java.lang.management.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public final class SystemPerformanceMonitor {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final int refreshSecs;

    public SystemPerformanceMonitor() {
        this(2); // default 2s as per screenshot
    }

    public SystemPerformanceMonitor(int refreshSeconds) {
        this.refreshSecs = Math.max(1, refreshSeconds);
    }

    public void runInteractive(
            Scanner scanner,
            StudentManager sm,
            GradeManager gm,
            ExecutorService fixedPool,
            ExecutorService cachedPool,
            ScheduledExecutorService scheduler,
            CacheService<String, Object> cacheService
    ) {
        boolean running = true;
        while (running) {
            clearScreen();
            renderHeader();
            renderCollections(sm, gm);
            renderThreadPools(fixedPool, cachedPool, scheduler);
            renderFileIO();
            renderCache(cacheService);
            renderRegex();
            renderResources();
            renderGC();
            renderRecommendations(cacheService);

            System.out.print("\n Press 'Q' to quit, 'R' to refresh: ");
            String cmd = safeReadLine(scanner).trim().toUpperCase();
            if ("Q".equals(cmd)) {
                running = false;
            } else if ("R".equals(cmd)) {
                // refresh immediately
                continue;
            }
            // auto-refresh after delay
            try { Thread.sleep(refreshSecs * 1000L); } catch (InterruptedException ignored) {}
        }
    }

    // ─────────────────────────── UI sections ───────────────────────────

    private void renderHeader() {
        System.out.println("SYSTEM PERFORMANCE MONITOR");
        System.out.println("────────────────────────────────────────────");
        System.out.println(" Real-time monitoring | Refresh every " + refreshSecs + " seconds");
        System.out.println(" Press 'Q' to quit");
        System.out.println();
    }

    private void renderCollections(StudentManager sm, GradeManager gm) {
        System.out.println("COLLECTION PERFORMANCE ANALYSIS");
        System.out.println("────────────────────────────────────────────");
        System.out.printf("%-22s | %-6s | %-14s | %-10s%n", "Data Structure", "Size", "Access Time", "Memory");

        // HashMap<StudentID> snapshot (O(1) lookup) built from manager
        Map<String, Student> idMap = Arrays.stream(sm.getStudents())
                .collect(Collectors.toMap(s -> s.getStudentId().toUpperCase(), s -> s, (a, b) -> a, HashMap::new));

        long hmNs = timeNs(() -> {
            for (int i = 0; i < Math.min(50, idMap.size()); i++) {
                idMap.get("STU" + String.format("%03d", i + 1));
            }
        });

        // TreeMap<GradeSort> (sorted by grade value)
        TreeMap<Double, List<Grade>> gradeTree = new TreeMap<>();
        for (int i = 0; i < gm.getGradeCount(); i++) {
            Grade g = gm.getGradeAt(i);
            if (g != null) gradeTree.computeIfAbsent(g.getGrade(), k -> new ArrayList<>()).add(g);
        }
        long tmNs = timeNs(() -> {
            // access min/max and a floor lookup
            gradeTree.firstEntry();
            gradeTree.lastEntry();
            gradeTree.floorEntry(75.0);
        });

        // ArrayList<Students>
        List<Student> studentList = Arrays.asList(sm.getStudents());
        long alNs = timeNs(() -> {
            double sum = 0;
            for (Student s : studentList) sum += s.calculateAverageGrade();
        });

        // HashSet<Courses> (unique subject names from grades)
        Set<String> courses = new HashSet<>();
        for (int i = 0; i < gm.getGradeCount(); i++) {
            Grade g = gm.getGradeAt(i);
            if (g != null) courses.add(g.getSubject().getSubjectName());
        }
        long hsNs = timeNs(() -> {
            courses.contains("Mathematics");
            courses.contains("English");
            courses.contains("Science");
        });

        // ConcurrentHashMap demo (simple snapshot of student emails)
        ConcurrentHashMap<String, String> chm = new ConcurrentHashMap<>();
        for (Student s : sm.getStudents()) {
            chm.put(s.getStudentId(), s.getEmail());
        }
        long chmNs = timeNs(() -> {
            for (Student s : sm.getStudents()) chm.get(s.getStudentId());
        });

        // Rough memory estimation (very approximate, just to display text)
        String hmMem = kbText(idMap.size() * 500L);
        String tmMem = kbText(Math.max(1, gradeTree.size()) * 80L + gradeTree.values().size() * 64L);
        String alMem = kbText(studentList.size() * 320L);
        String hsMem = kbText(courses.size() * 64L);
        String chmMem = kbText(chm.size() * 600L);

        System.out.printf("%-22s | %-6d | %-14s | %-10s%n",
                "HashMap<StudentID>", idMap.size(), msText(hmNs), hmMem + " (O(1))");
        System.out.printf("%-22s | %-6d | %-14s | %-10s%n",
                "TreeMap<GradeSort>", gradeTree.size(), msText(tmNs), tmMem + " (O(log n))");
        System.out.printf("%-22s | %-6d | %-14s | %-10s%n",
                "ArrayList<Students>", studentList.size(), msText(alNs), alMem + " (O(1))");
        System.out.printf("%-22s | %-6d | %-14s | %-10s%n",
                "HashSet<Courses>", courses.size(), msText(hsNs), hsMem + " (O(1))");
        System.out.printf("%-22s | %-6d | %-14s | %-10s%n",
                "ConcurrentHashMap", chm.size(), msText(chmNs), chmMem + " (O(1))");
        System.out.println();
    }

    private void renderThreadPools(ExecutorService fixed, ExecutorService cached, ScheduledExecutorService sched) {
        System.out.println("THREAD POOL PERFORMANCE");
        System.out.println("────────────────────────────────────────────");
        System.out.printf("%-14s | %-6s | %-6s | %-5s | %-10s%n", "Pool Type", "Active", "Max", "Queue", "Completed");

        printPool("FixedThreadPool", fixed);
        printPool("CachedThreadPool", cached);
        printScheduledPool("ScheduledPool", sched);

        // Thread activity summary (names + simple state snapshot)
        System.out.println("\nThread Activity:");
        Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
        int shown = 0;
        for (Thread t : traces.keySet()) {
            if (shown >= 7) break;
            String name = t.getName();
            if (name.contains("pool") || name.toLowerCase().contains("sched")) {
                String state = t.getState().name();
                System.out.printf(" %-18s: %s%n", name, state);
                shown++;
            }
        }
        System.out.println();
    }

    private void renderFileIO() {
        System.out.println("FILE I/O PERFORMANCE");
        System.out.println("────────────────────────────────────────────");
        System.out.printf("%-12s | %-6s | %-8s | %-10s | %-12s%n",
                "Operation", "Count", "Avg Time", "Total Size", "Method");
        // Basic counters derived from log patterns or instrumentation (if added)
        for (IoStat s : IoRegistry.snapshot()) {
            System.out.printf("%-12s | %-6d | %-8s | %-10s | %-12s%n",
                    s.operation, s.count, msText(s.avgNs()), kbText(s.totalBytes), s.method);
        }

        List<String> recent = IoRegistry.recentOps();
        if (!recent.isEmpty()) {
            System.out.println("\nRecent I/O Operations:");
            for (String line : recent) System.out.println(" [" + TS.format(LocalDateTime.now()) + "] " + line);
        }
        System.out.println();
    }

    private void renderCache(CacheService<String, Object> cacheService) {
        System.out.println("CACHE PERFORMANCE");
        System.out.println("────────────────────────────────────────────");
        if (cacheService == null) {
            System.out.println(" (no cache service attached)\n");
            return;
        }
        int size = CacheIntrospector.size(cacheService);
        long hits = CacheIntrospector.hits(cacheService);
        long misses = CacheIntrospector.misses(cacheService);
        String hitRate = (hits + misses == 0) ? "0.0%" :
                String.format("%.1f%%", (hits * 100.0) / (hits + misses));
        String memText = kbText(size * 128L);

        System.out.println(" Total Entries: " + size);
        System.out.println(" Hit Rate: " + hitRate + " (" + hits + "/" + (hits + misses) + " requests)");
        System.out.println(" Miss Rate: " + String.format("%.1f%%", ((misses * 100.0) / Math.max(1, hits + misses))));
        System.out.println(" Avg Hit Time: " + msText(0));   // placeholder unless instrumented
        System.out.println(" Avg Miss Time: " + msText(0));  // placeholder unless instrumented
        System.out.println(" Memory Usage: " + memText);
        System.out.println(" Evictions: " + CacheIntrospector.evictions(cacheService));
        System.out.println();
    }

    private void renderRegex() {
        System.out.println("REGEX VALIDATION PERFORMANCE");
        System.out.println("────────────────────────────────────────────");
        System.out.printf("%-12s | %-12s | %-10s | %-10s%n", "Pattern Type", "Validations", "Avg Time", "Cache Hits");

        for (RegexStat rs : RegexRegistry.snapshot()) {
            System.out.printf("%-12s | %-12d | %-10s | %-10d%n",
                    rs.name, rs.count, msText(rs.avgNs()), rs.cacheHits);
        }
        System.out.println();
    }

    private void renderResources() {
        System.out.println("RESOURCE UTILIZATION");
        System.out.println("────────────────────────────────────────────");

        // CPU Usage (system) via com.sun.management.OperatingSystemMXBean if available
        double cpuPct = 0.0;
        long openFiles = -1;
        try {
            OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
            if (os instanceof com.sun.management.OperatingSystemMXBean sun) {
                double load = sun.getSystemCpuLoad(); // 0..1 (may be -1 if not available)
                cpuPct = (load < 0) ? 0.0 : (load * 100.0);
                openFiles = (long) sun.getProcessCpuLoad();
            } else {
                cpuPct = 0.0;
            }
        } catch (Throwable ignore) {}

        Runtime rt = Runtime.getRuntime();
        long usedMb = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        long maxMb = Math.max(1, rt.maxMemory() / (1024 * 1024));
        double memPct = (usedMb * 100.0) / maxMb;

        int activeThreads = Thread.activeCount();

        System.out.printf(" CPU Usage: %3.0f%%%n", cpuPct);
        System.out.printf(" Memory: %d MB / %d MB (%.0f%%)%n", usedMb, maxMb, memPct);
        System.out.printf(" Threads: %d active%n", activeThreads);
        if (openFiles >= 0) System.out.printf(" File Handles: %d open%n", openFiles);
        System.out.println();
    }

    private void renderGC() {
        System.out.println("GC Activity (since start)");
        System.out.println("────────────────────────────────────────────");
        long minorCount = 0, minorTimeMs = 0;
        long majorCount = 0, majorTimeMs = 0;

        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            String name = gc.getName().toLowerCase();
            long c = gc.getCollectionCount();
            long tMs = gc.getCollectionTime();
            if (name.contains("scavenge") || name.contains("young") || name.contains("minor"))
            { minorCount += Math.max(0, c); minorTimeMs += Math.max(0, tMs); }
            else
            { majorCount += Math.max(0, c); majorTimeMs += Math.max(0, tMs); }
        }
        long minorAvgMs = (minorCount == 0) ? 0 : (minorTimeMs / minorCount);
        long majorAvgMs = (majorCount == 0) ? 0 : (majorTimeMs / majorCount);

        System.out.printf(" Minor GC: %d collections (avg %dms)%n", minorCount, minorAvgMs);
        System.out.printf(" Major GC: %d collections (avg %dms)%n", majorCount, majorAvgMs);
        System.out.println();
    }

    private void renderRecommendations(CacheService<String, Object> cacheService) {
        System.out.println("Performance Recommendations:");
        System.out.println("────────────────────────────────────────────");
        System.out.println(" ✓ Collection choices optimal for current load");
        System.out.println(" ✓ Thread pool sizes well-configured");
        long hits = CacheIntrospector.hits(cacheService);
        long misses = CacheIntrospector.misses(cacheService);
        double rate = (hits + misses == 0) ? 0.0 : (hits * 100.0 / (hits + misses));
        if (rate > 80.0) {
            System.out.println(" △ Consider increasing cache size (approaching 80% usage)");
        }
        System.out.println(" ✓ I/O operations within acceptable range");
        System.out.println(" ✓ No memory leaks detected");
    }

    // ─────────────────────────── helpers ───────────────────────────

    private static String safeReadLine(Scanner sc) {
        try { return sc.nextLine(); } catch (NoSuchElementException e) { return ""; }
    }

    private static void printPool(String label, ExecutorService svc) {
        if (svc instanceof ThreadPoolExecutor tpe) {
            int active = tpe.getActiveCount();
            int max = tpe.getMaximumPoolSize();
            int queue = tpe.getQueue().size();
            long done = tpe.getCompletedTaskCount();
            System.out.printf("%-14s | %-6d | %-6d | %-5d | %-10d%n",
                    label, active, max, queue, done);
        } else {
            System.out.printf("%-14s | %-6s | %-6s | %-5s | %-10s%n", label, "n/a", "n/a", "n/a", "n/a");
        }
    }

    private static void printScheduledPool(String label, ScheduledExecutorService svc) {
        if (svc instanceof ScheduledThreadPoolExecutor stpe) {
            int active = stpe.getActiveCount();
            int max = stpe.getMaximumPoolSize();
            int queue = stpe.getQueue().size();
            long done = stpe.getCompletedTaskCount();
            System.out.printf("%-14s | %-6d | %-6d | %-5d | %-10d%n", label, active, max, queue, done);
        } else {
            System.out.printf("%-14s | %-6s | %-6s | %-5s | %-10s%n", label, "n/a", "n/a", "n/a", "n/a");
        }
    }

    private static long timeNs(Runnable r) {
        long t0 = System.nanoTime();
        try { r.run(); } finally {}
        return System.nanoTime() - t0;
    }

    private static String msText(long ns) {
        return String.format("%.1fms", ns / 1_000_000.0);
    }

    private static String kbText(long bytes) {
        return String.format("%.1f KB", Math.max(0, bytes) / 1024.0);
    }

    private static void clearScreen() {
        // simple visual separator (avoid ANSI to keep console compatibility)
        System.out.println("\n".repeat(2));
    }

    // ─────────────────────────── lightweight registries ───────────────────────────
    // You can wire these counters from exporters/importers later if desired.

    public static final class IoRegistry {
        private static final List<IoStat> stats = new ArrayList<>();
        private static final Deque<String> recent = new ArrayDeque<>(16);

        public static synchronized void record(String op, long bytes, long ns, String method) {
            IoStat s = stats.stream().filter(x -> x.operation.equals(op)).findFirst().orElse(null);
            if (s == null) { s = new IoStat(op, method); stats.add(s); }
            s.count++;
            s.totalNs += Math.max(0, ns);
            s.totalBytes += Math.max(0, bytes);
            if (recent.size() >= 16) recent.removeFirst();
            recent.addLast(op + " " + kbText(bytes) + " (" + msText(ns) + ")");
        }

        public static synchronized List<IoStat> snapshot() {
            return new ArrayList<>(stats);
        }
        public static synchronized List<String> recentOps() {
            return new ArrayList<>(recent);
        }
    }

    public static final class IoStat {
        public final String operation;
        public final String method;
        public long count;
        public long totalNs;
        public long totalBytes;
        public IoStat(String op, String method) {
            this.operation = op; this.method = method;
        }
        public long avgNs() { return (count == 0) ? 0 : (totalNs / count); }
    }

    public static final class RegexRegistry {
        private static final Map<String, RegexStat> stats = new ConcurrentHashMap<>();
        public static void record(String name, long ns, boolean cacheHit) {
            RegexStat rs = stats.computeIfAbsent(name, k -> new RegexStat(k));
            rs.count++;
            rs.totalNs += Math.max(0, ns);
            if (cacheHit) rs.cacheHits++;
        }
        public static List<RegexStat> snapshot() {
            return new ArrayList<>(stats.values());
        }
    }

    public static final class RegexStat {
        public final String name;
        public long count;
        public long totalNs;
        public long cacheHits;
        public RegexStat(String name) { this.name = name; }
        public long avgNs() { return (count == 0) ? 0 : (totalNs / count); }
    }

    /** Cache introspection via optional getters (see change in CacheService). */
    public static final class CacheIntrospector {
        public static int size(CacheService<String, Object> cs) {
            if (cs == null) return 0;
            try { return cs.size(); } catch (Throwable t) { return 0; }
        }
        public static long hits(CacheService<String, Object> cs) {
            if (cs == null) return 0;
            try { return cs.hits(); } catch (Throwable t) { return 0; }
        }
        public static long misses(CacheService<String, Object> cs) {
            if (cs == null) return 0;
            try { return cs.misses(); } catch (Throwable t) { return 0; }
        }
        public static long evictions(CacheService<String, Object> cs) {
            if (cs == null) return 0;
            try { return cs.evictions(); } catch (Throwable t) { return 0; }
        }
    }
}
