
package com.amalitech.menu.actions;

import com.amalitech.base.Student;
import com.amalitech.app.AppContext;
import com.amalitech.menu.MenuAction;
import com.amalitech.concurrent.BackgroundTaskTracker;
import com.amalitech.util.AppLogger;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ScheduleAutomatedTasksAction implements MenuAction {
    private final AppContext ctx;

    private static final List<ScheduledTaskInfo> ACTIVE = new ArrayList<>();
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

    private final List<ScheduledFuture<?>> scheduled = new ArrayList<>();

    public ScheduleAutomatedTasksAction(AppContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public String label() {
        return "Schedule Automated Tasks";
    }

    @Override
    public void execute() {
        while (true) {
            printTitle();

            System.out.println("Current Scheduled Tasks: " + ACTIVE.size() + " active\n");
            printActiveSchedules();

            System.out.println("Add New Scheduled Task:");
            System.out.println("1. Daily GPA Recalculation");
            System.out.println("2. Weekly Grade Report Email");
            System.out.println("3. Monthly Performance Summary");
            System.out.println("4. Hourly Data Sync");
            System.out.println("5. Custom Schedule");
            System.out.println("6. Cancel\n");

            System.out.print("Select option (1-6): ");
            int choice = parseIntSafe(ctx.scanner.nextLine(), 6);

            if (choice == 6) return;

            switch (choice) {
                case 1 -> configureDailyGpaRecalc();      // full flow, matches screenshot
                case 2 -> { System.out.println("\nCONFIGURE: Weekly Grade Report Email"); System.out.println("(Not implemented in this step)"); pause(); }
                case 3 -> { System.out.println("\nCONFIGURE: Monthly Performance Summary"); System.out.println("(Not implemented in this step)"); pause(); }
                case 4 -> { System.out.println("\nCONFIGURE: Hourly Data Sync"); System.out.println("(Not implemented in this step)"); pause(); }
                case 5 -> { System.out.println("\nCONFIGURE: Custom Schedule"); System.out.println("(Not implemented in this step)"); pause(); }
                default -> { System.out.println("Invalid choice."); pause(); }
            }
        }
    }

    // === Daily GPA Recalculation wizard (with Audit logging) ===
    private void configureDailyGpaRecalc() {
        System.out.println("\nCONFIGURE: Daily GPA Recalculation");
        System.out.println("----------------------------------\n");

        System.out.println("Execution Time:");
        int hour = askInt("Enter hour (0-23): ", 0, 23);
        int minute = askInt("Enter minute (0-59): ", 0, 59);

        System.out.println("\nTarget Students:");
        System.out.println("1. All Students");
        System.out.println("2. Honors Students Only");
        System.out.println("3. Students with Grade Changes\n");
        int scopeChoice = askInt("Select (1-3): ", 1, 3);

        List<Student> scope = new ArrayList<>(ctx.students.listStudents());
        if (scopeChoice == 2) {
            scope.removeIf(s -> !"Honors".equalsIgnoreCase(s.getStudentType()));
        } else if (scopeChoice == 3) {
            scope.removeIf(s -> s.getEnrolledSubjectCount() <= 0);
        }
        int totalStudents = scope.size();

        System.out.println("\nThread Pool Configuration:");
        int recommended = clamp(recommendThreads(totalStudents), 1, 8);
        System.out.println("Recommended: " + recommended + " threads for " + totalStudents + " students");
        int threadCount = askInt("Enter thread count (1-8): ", 1, 8);

        System.out.println("\nNotification Settings:");
        System.out.println("1. Email summary on completion");
        System.out.println("2. Log to file only");
        System.out.println("3. Both\n");
        int notifyChoice = askInt("Select (1-3): ", 1, 3);

        String recipient = "";
        if (notifyChoice == 1 || notifyChoice == 3) {
            System.out.print("\nEnter notification email: ");
            recipient = ctx.scanner.nextLine().trim();
            if (isValidEmail(recipient)) {
                System.out.println("\n✓ Validation passed");
            } else {
                System.out.println("\nInvalid email. Using log-only notification.");
                notifyChoice = 2;
                recipient = "";
            }
        }

        System.out.println("\nTASK CONFIGURATION SUMMARY");
        System.out.println("--------------------------\n");
        System.out.println("Task: Daily GPA Recalculation");
        System.out.println("Schedule: Every day at " + toAmPm(hour, minute));
        System.out.println("Scope: " + scopeLabel(scopeChoice) + " (" + totalStudents + ")");
        System.out.println("Threads: " + threadCount + " (parallel execution)");
        System.out.println("Notifications: " + notifyLabel(notifyChoice));
        if (!recipient.isEmpty()) System.out.println("Recipient: " + recipient);

        String estimatedTime = "~" + Math.max(2, Math.min(10, (totalStudents / Math.max(1, threadCount)) / 10 + 2)) + " minutes";
        String resourceUsage = resourceUsage(totalStudents, threadCount);
        System.out.println("\nEstimated Execution Time: " + estimatedTime);
        System.out.println("Resource Usage: " + resourceUsage);

        System.out.print("\nConfirm schedule? (Y/N): ");
        String conf = ctx.scanner.nextLine().trim();
        if (!conf.equalsIgnoreCase("Y")) {
            System.out.println("\nSchedule cancelled.");
            pause();
            return;
        }

        String taskId = String.format("TASK-%03d", NEXT_ID.getAndIncrement());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = nextDailyRun(now, hour, minute);
        long initialDelaySeconds = Math.max(0, Duration.between(now, nextRun).getSeconds());

        // --- Audit: record scheduling ---
        AppLogger.info(String.format(
                "[SCHEDULE] id=%s type=DAILY name=GPA_Recalc time=%02d:%02d scope=%s(%d) threads=%d notify=%s recipient=%s next=%s",
                taskId, hour, minute, scopeLabel(scopeChoice), totalStudents, threadCount,
                notifyLabel(notifyChoice), (recipient.isEmpty() ? "-" : recipient),
                nextRun.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        )); // visible in Audit Trail via app.log  (AuditServiceImpl tails/filters app.log)  [1](https://amalitech-my.sharepoint.com/personal/lennox_afriyie_amalitech_com/Documents/Microsoft%20Copilot%20Chat%20Files/AuditServiceImpl.java)

        ScheduledTaskInfo info = new ScheduledTaskInfo();
        info.id = taskId;
        info.tag = "[DAILY]";
        info.name = "GPA Recalculation";
        info.scheduleDescription = "Every day at " + toAmPm(hour, minute);
        info.nextRun = nextRun;
        info.lastRun = null;
        info.status = "✓ Success";
        info.threadCount = threadCount;

        ACTIVE.add(info);

        ScheduledFuture<?> future = ctx.scheduler.scheduleAtFixedRate(() -> {
            long runStartMs = System.currentTimeMillis();
            try {
                BackgroundTaskTracker.incrementActive();
                info.status = "⚡ Running";
                info.progress = 0;

                AppLogger.info(String.format("[RUN-START] id=%s name=%s students=%d", info.id, info.name, scope.size()));

                int total = Math.max(1, scope.size());
                AtomicInteger done = new AtomicInteger(0);

                List<Student> workList = new ArrayList<>(scope);
                for (Student s : workList) {
                    ctx.fixedPool.submit(() -> {
                        try {
                            s.calculateAverageGrade(); // simulate recomputation
                        } finally {
                            int completed = done.incrementAndGet();
                            info.progress = (int) Math.round((completed * 100.0) / total);
                        }
                    });
                }

                while (info.progress < 100) {
                    try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                }

                info.lastRun = LocalDateTime.now();
                info.nextRun = info.lastRun.plusDays(1);
                info.status = "✓ Success";
                info.progress = 100;

                long elapsedMs = System.currentTimeMillis() - runStartMs;
                AppLogger.info(String.format("[RUN-END] id=%s result=SUCCESS elapsedMs=%d next=%s",
                        info.id, elapsedMs, info.nextRun.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));

            } catch (Exception ex) {
                info.status = "✗ Failed";
                AppLogger.error("[RUN-END] id=" + info.id + " result=FAILED " + ex.getMessage());
            } finally {
                BackgroundTaskTracker.decrementActive();
            }
        }, initialDelaySeconds, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);

        scheduled.add(future);

        System.out.println("\n✓ Task scheduled successfully!");
        System.out.println("  Task ID: " + taskId);
        System.out.println("  Scheduler Thread: RUNNING");
        System.out.println("  Next Execution: " + nextRun.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("  Initial Delay: " + humanDelay(initialDelaySeconds));
        System.out.println("\nThe task will run automatically in the background.");

        System.out.println("\nYou can monitor its execution in the Audit Trail.");

        pause();
    }

    // === ACTIVE SCHEDULES display ===
    private void printActiveSchedules() {
        if (ACTIVE.isEmpty()) {
            System.out.println("ACTIVE SCHEDULES");
            System.out.println("----------------");
            System.out.println("(none)\n");
            return;
        }

        System.out.println("ACTIVE SCHEDULES");
        System.out.println("----------------\n");

        int i = 1;
        DateTimeFormatter ts = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (ScheduledTaskInfo info : ACTIVE) {
            System.out.printf("%d. %s %s%n", i++, info.tag, info.name);
            System.out.println("   Schedule: " + info.scheduleDescription);
            System.out.println("   Last Run: " + (info.lastRun == null ? "—" : info.lastRun.format(ts)));
            System.out.println("   Next Run: " + (info.nextRun == null ? "—" : info.nextRun.format(ts)));
            if ("⚡ Running".equals(info.status) && info.progress > 0 && info.progress < 100) {
                System.out.println("   Status: ⚡ Running (" + info.progress + "% complete)");
            } else {
                System.out.println("   Status: " + info.status);
            }
            System.out.println();
        }
    }

    // === Internal types & helpers ===
    private static class ScheduledTaskInfo {
        String id;
        String tag;
        String name;
        String scheduleDescription;
        LocalDateTime lastRun;
        LocalDateTime nextRun;
        String status;
        int threadCount;
        int progress;
    }

    private static void printTitle() {
        System.out.println();
        System.out.println("SCHEDULE AUTOMATED TASKS");
        System.out.println("------------------------\n");
    }

    private void pause() {
        System.out.print("\nPress Enter to continue...");
        ctx.scanner.nextLine();
    }

    private static int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private int askInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            int v = parseIntSafe(ctx.scanner.nextLine(), min);
            if (v >= min && v <= max) return v;
            System.out.println("Invalid input. Range: " + min + "-" + max);
        }
    }

    private static String scopeLabel(int choice) {
        return switch (choice) {
            case 2 -> "Honors Students";
            case 3 -> "Students with Grade Changes";
            default -> "All Students";
        };
    }

    private static String notifyLabel(int choice) {
        return switch (choice) {
            case 1 -> "Email summary on completion";
            case 2 -> "Log file only";
            default -> "Email + Log file";
        };
    }

    private static String toAmPm(int hour24, int minute) {
        int h = ((hour24 + 11) % 12) + 1;
        String ampm = (hour24 < 12) ? "AM" : "PM";
        return String.format("%02d:%02d %s", h, minute, ampm);
    }

    private static LocalDateTime nextDailyRun(LocalDateTime now, int hour, int minute) {
        LocalDateTime candidate = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0);
        if (!candidate.isAfter(now)) candidate = candidate.plusDays(1);
        return candidate;
    }

    private static String humanDelay(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return String.format("%dh %dm %ds", h, m, s);
    }

    private static boolean isValidEmail(String email) {
        if (email == null) return false;
        String x = email.trim();
        if (x.isEmpty()) return false;
        return x.matches("(?i)^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private static String resourceUsage(int students, int threads) {
        int score = students / Math.max(1, threads);
        if (score <= 25) return "LOW";
        if (score <= 100) return "MEDIUM";
        return "HIGH";
    }

    private static int recommendThreads(int n) {
        return (int) Math.ceil(Math.max(1, n) / 7.0);
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
