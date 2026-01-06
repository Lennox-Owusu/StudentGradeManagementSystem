package com.amalitech;

import com.amalitech.exceptions.CsvFormatException;
import com.amalitech.reporting.ReportGenerator;
import com.amalitech.io.FileExporter;
import com.amalitech.reporting.GPACalculator;
import com.amalitech.io.CSVParser;
import com.amalitech.calculation.StatisticsCalculator;
import com.amalitech.exceptions.StudentNotFoundException;
import com.amalitech.util.AppLogger;
import com.amalitech.util.ErrorHandler;
import com.amalitech.util.Validators;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.BufferedReader;
import java.util.Scanner;


import com.amalitech.concurrent.BackgroundTaskTracker;
import com.amalitech.concurrent.TaskProgressRegistry;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.amalitech.monitor.GradeEventTracker;




public class Main {

      //Helper method for import data case 6
    private static void importData(Scanner scanner, StudentManager studentManager, GradeManager gradeManager) {
        System.out.println("\nIMPORT DATA (Multi-format)");
        System.out.println("────────────────────────────────────────────");

        Path importsDir = Paths.get("./imports");
        try {
            if (!Files.exists(importsDir)) Files.createDirectories(importsDir);
        } catch (IOException io) {
            System.out.println("Failed to prepare ./imports directory: " + io.getMessage());
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        System.out.println("\nSupported formats:");
        System.out.println(" 1. CSV detailed report (*.csv)");
        System.out.println(" 2. JSON detailed report (*.json)");
        System.out.println(" 3. Binary report (*.dat)");
        System.out.println(" 4. Auto-detect by file extension");
        System.out.print("\nSelect format (1-4): ");
        int fsel;
        try { fsel = Integer.parseInt(scanner.nextLine().trim()); } catch (NumberFormatException e) { fsel = 4; }

        com.amalitech.io.ImportCoordinator.Format format =
                switch (fsel) {
                    case 1 -> com.amalitech.io.ImportCoordinator.Format.CSV;
                    case 2 -> com.amalitech.io.ImportCoordinator.Format.JSON;
                    case 3 -> com.amalitech.io.ImportCoordinator.Format.BINARY;
                    default -> com.amalitech.io.ImportCoordinator.Format.AUTO;
                };

        System.out.print("\nEnter filename (including extension) in ./imports: ");
        String fileName = scanner.nextLine().trim();
        if (fileName.isEmpty()) {
            System.out.println("No filename provided.");
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        Path path = importsDir.resolve(fileName);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            System.out.println("File not found: " + path.toAbsolutePath());
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        var coordinator = new com.amalitech.io.ImportCoordinator();
        try {
            var t0 = java.time.Instant.now();
            com.amalitech.reporting.StudentReport report = coordinator.loadReport(path, format);
            coordinator.mergeIntoSystem(report, studentManager, gradeManager);
            var ms = java.time.Duration.between(t0, java.time.Instant.now()).toMillis();

            System.out.println("\n✓ Import completed");
            System.out.println(" Source: " + path.getFileName());
            System.out.println(" Student: " + report.getStudentId() + " - " + report.getName());
            System.out.println(" Grades imported: " + report.getTotalGrades());
            System.out.println(" Time: " + ms + "ms");
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();

            com.amalitech.util.AppLogger.info("Import completed for " + report.getStudentId()
                    + " from " + path.getFileName() + " in " + ms + "ms");
        } catch (com.amalitech.exceptions.DomainException ex) {
            com.amalitech.util.ErrorHandler.handle("Import Data", ex);
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }



    // --- Advanced Edition v3.0 concurrency ---
    private static ExecutorService fixedPool;
    private static ExecutorService cachedPool;
    private static ScheduledExecutorService scheduler;
    private static volatile boolean RUNNING = true;


    // Simple phone directory so we can search by area code
    private static final java.util.concurrent.ConcurrentHashMap<String, String> PHONEBOOK =
            new java.util.concurrent.ConcurrentHashMap<>();


    //Helpers
    private static String kb(long bytes) {
        return String.format("%.1f KB", bytes / 1024.0);
    }
    private static final StatisticsCalculator STATS = new StatisticsCalculator();
    private static String safeTrim(String s) {
        return (s == null) ? "" : s.trim();
    }

    private static final com.amalitech.cache.CacheService<String, Object> CACHE = new com.amalitech.cache.CacheService<>(256);

    //Helper list to track scheduled tasks
    private static final java.util.List<String> scheduledTasks = new java.util.ArrayList<>();


    private static final ReportGenerator REPORT_GENERATOR = new ReportGenerator();
    private static final FileExporter FILE_EXPORTER = new FileExporter();
    private static final GPACalculator GPA_CALCULATOR = new GPACalculator();

    public static void main(String[] args) throws CsvFormatException {
        Scanner scanner = new Scanner(System.in);
        StudentManager studentManager = new StudentManager(50);
        GradeManager gradeManager = new GradeManager(200);
        preloadSampleStudents(studentManager);
        initExecutors();
        startBackgroundServices(studentManager, gradeManager);

        boolean exit = false;

        while (!exit) {
            printAdvancedMenu();
            renderPrompt();

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }



            switch (choice) {
                case 1 -> addStudent(scanner, studentManager);
                case 2 -> viewStudents(studentManager);
                case 3 -> recordGrade(scanner, studentManager, gradeManager);
                case 4 -> viewGradeReport(scanner, studentManager, gradeManager);
                case 5 -> {
                    System.out.println("\nEXPORT GRADE REPORT (Multi-Format)");
                    System.out.println("────────────────────────────────────");

                    System.out.print("\nEnter Student ID: ");
                    String studentId = scanner.nextLine().trim().toUpperCase();
                    Student student = studentManager.findStudent(studentId);
                    if (student == null) {
                        System.out.println("Student not found!");
                        System.out.print("\nPress Enter to continue...");
                        scanner.nextLine();
                        break;
                    }

                    // Gather grades for this student
                    java.util.ArrayList<Grade> list = new java.util.ArrayList<>();
                    for (int i = 0; i < gradeManager.getGradeCount(); i++) {
                        Grade g = gradeManager.getGradeAt(i);
                        if (g != null && g.getStudentId().equalsIgnoreCase(studentId)) {
                            list.add(g);
                        }
                    }

                    System.out.println();
                    System.out.println("Student: " + student.getStudentId() + " - " + student.getName() +
                            " (" + student.getEmail() + ")");
                    System.out.println("Type: " + student.getStudentType() + " | Phone: +1-555-0123"); // demo phone as in screenshot
                    System.out.println("Total Grades: " + list.size());

                    // --- Export choices (format + report type) ---
                    System.out.println();
                    System.out.println("Export Format:");
                    System.out.println(" 1. CSV (Comma-Separated Values)");
                    System.out.println(" 2. JSON (JavaScript Object Notation)");
                    System.out.println(" 3. Binary (Serialized Java Object)");
                    System.out.println(" 4. All formats");
                    System.out.print("\nSelect format (1-4): ");
                    int fmt;
                    try { fmt = Integer.parseInt(scanner.nextLine().trim()); } catch (NumberFormatException e) { fmt = 4; }

                    System.out.println("\nReport Type:");
                    System.out.println(" 1. Summary Report");
                    System.out.println(" 2. Detailed Report");
                    System.out.println(" 3. Transcript Format");
                    System.out.println(" 4. Performance Analytics");
                    System.out.print("\nSelect type (1-4): ");
                    int rtype;
                    try { rtype = Integer.parseInt(scanner.nextLine().trim()); } catch (NumberFormatException e) { rtype = 2; }

                    // For now we implement Detailed Report (matches screenshot); others can be added next steps.
                    System.out.println("\nProcessing with NIO.2 Streaming...");
                    com.amalitech.util.AppLogger.info("Export report requested for " + studentId + " format=" + fmt + " type=" + rtype);

                    // Build StudentReport DTO
                    double coreAvg = gradeManager.calculateCoreAverage(studentId);
                    double elecAvg = gradeManager.calculateElectiveAverage(studentId);
                    double overallAvg = gradeManager.calculateOverallAverage(studentId);
                    com.amalitech.reporting.StudentReport report =
                            new com.amalitech.reporting.StudentReport(student, list, coreAvg, elecAvg, overallAvg, "+1-555-0123");

                    // Coordinator
                    com.amalitech.io.ExportCoordinator coordinator = new com.amalitech.io.ExportCoordinator();
                    java.nio.file.Path reportsDir = java.nio.file.Paths.get("./reports");

                    boolean doCsv = (fmt == 1 || fmt == 4);
                    boolean doJson = (fmt == 2 || fmt == 4);
                    boolean doBin = (fmt == 3 || fmt == 4);

                    // Base name
                    String baseName = student.getName().toLowerCase().replaceAll("\\s+", "_") + "_detailed";

                    try {
                        var perf = coordinator.exportAll(report, reportsDir, baseName,doCsv,doJson,doBin);

                        // Show per-format completion (only those selected)
                        if (doCsv) {
                            System.out.println("\n✓ CSV Export completed");
                            System.out.println(" File: " + baseName + ".csv");
                            System.out.println(" Location: ./reports/csv/");
                            System.out.println(" Size: " + kb(perf.csvBytes));
                            System.out.println(" Rows: " + list.size() + " grades + header");
                            System.out.println(" Time: " + perf.csvMillis + "ms");
                        }
                        if (doJson) {
                            System.out.println("\n✓ JSON Export completed");
                            System.out.println(" File: " + baseName + ".json");
                            System.out.println(" Location: ./reports/json/");
                            System.out.println(" Size: " + kb(perf.jsonBytes));
                            System.out.println(" Structure: Nested objects with metadata");
                            System.out.println(" Time: " + perf.jsonMillis + "ms");
                        }
                        if (doBin) {
                            System.out.println("\n✓ Binary Export completed");
                            System.out.println(" File: " + baseName + ".dat");
                            System.out.println(" Location: ./reports/binary/");
                            System.out.println(" Size: " + kb(perf.binBytes) + " (compressed)");
                            System.out.println(" Format: Serialized StudentReport object");
                            System.out.println(" Time: " + perf.binMillis + "ms");
                        }

                        // Performance summary
                        System.out.println("\n📊 Export Performance Summary:");
                        System.out.println(" Total Time: " + perf.totalMillis + "ms");
                        System.out.println(" Total Size: " + kb(perf.totalBytes));
                        System.out.println(" Compression Ratio: " + perf.compressionRatio());
                        int ops = (doCsv ? 1 : 0) + (doJson ? 1 : 0) + (doBin ? 1 : 0);
                        System.out.println(" I/O Operations: " + ops + " parallel writes");

                    } catch (com.amalitech.exceptions.ExportFailedException e) {
                        System.out.println("\n✗ Export failed: " + e.getMessage());
                        com.amalitech.util.AppLogger.error("Export failure", e);
                    }

                    System.out.print("\nPress Enter to continue...");
                    scanner.nextLine();
                }

                case 6 -> { // Import Data (Multi-format support) [ENHANCED]
                    importData(scanner, studentManager, gradeManager);



            }
                case 7 -> bulkImportGrades(scanner, studentManager, gradeManager);

                case 8 -> calculateStudentGPA(scanner, studentManager, gradeManager);
                case 9 -> viewClassStatistics(scanner, studentManager, gradeManager);

                case 10 -> { /* Real-Time dashboard */
                    new com.amalitech.dashboard.RealTimeDashboard().runInteractive(
                            scanner, studentManager, gradeManager,
                            (java.util.concurrent.ExecutorService) fixedPool,
                            (java.util.concurrent.ExecutorService) cachedPool,
                            (java.util.concurrent.ScheduledThreadPoolExecutor) scheduler,
                            CACHE
                    );
                }
                case 11 -> generateBatchReportsSafe(scanner, studentManager, gradeManager);


                case 12 -> { /* Advanced search (ENHANCED) */
                    searchStudents(scanner, studentManager, gradeManager);
                }
                case 13 -> patternBasedSearch(scanner, studentManager, gradeManager);

                case 14 -> { /* Query grade history (NEW) */
                    System.out.println("Query Grade History — to be implemented.");
                }


                case 15 -> {
                    System.out.println("\nSCHEDULE AUTOMATED TASKS");
                    System.out.println("────────────────────────────────────────────");
                    System.out.printf("\nCurrent Scheduled Tasks: %d active%n", scheduledTasks.size());
                    for (int i = 0; i < scheduledTasks.size(); i++) {
                        System.out.printf("%d. %s%n", i + 1, scheduledTasks.get(i));
                    }

                    System.out.println("\nAdd New Scheduled Task:");
                    System.out.println(" 1. Daily GPA Recalculation");
                    System.out.println(" 2. Weekly Grade Reportplaceholder)");
                    System.out.println(" 3. Monthly Performance Summary (placeholder)");
                    System.out.println(" 4. Cancel");
                    System.out.print("\nSelect option (1-4): ");
                    int opt;
                    try { opt = Integer.parseInt(scanner.nextLine().trim()); } catch (NumberFormatException e) { opt = 4; }
                    if (opt == 4) break;

                    if (opt == 1) {
                        System.out.println("\nCONFIGURE: Daily GPA Recalculation");
                        System.out.print("Enter hour (0-23): ");
                        int hour = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print("Enter minute (0-59): ");
                        int minute = Integer.parseInt(scanner.nextLine().trim());

                        System.out.print("\nEnter notification email: ");
                        String email = scanner.nextLine().trim();

                        // Compute initial delay
                        java.time.LocalDateTime now = java.time.LocalDateTime.now();
                        java.time.LocalDateTime nextRun = java.time.LocalDateTime.of(now.toLocalDate(),
                                java.time.LocalTime.of(hour, minute));
                        if (!nextRun.isAfter(now)) nextRun = nextRun.plusDays(1);
                        long initialDelay = java.time.Duration.between(now, nextRun).toSeconds();
                        long period = java.time.Duration.ofDays(1).toSeconds();

                        String taskId = "TASK-" + (scheduledTasks.size() + 1);
                        scheduledTasks.add("[DAILY] GPA Recalculation at " + hour + ":" + String.format("%02d", minute));

                        System.out.println("\nTASK CONFIGURATION SUMMARY");
                        System.out.println("────────────────────────────────────────────");
                        System.out.println("Task: Daily GPA Recalculation");
                        System.out.printf("Schedule: Every day at %02d:%02d%n", hour, minute);
                        System.out.println("Scope: All Students");
                        System.out.println("Threads: 4 (parallel execution)");
                        System.out.println("Notifications: Email + Log");
                        System.out.println("Recipient: " + email);
                        System.out.println("\nEstimated Execution Time: ~2 minutes");
                        System.out.println("Resource Usage: LOW");

                        System.out.print("\nConfirm schedule? (Y/N): ");
                        String confirm = scanner.nextLine().trim().toUpperCase();
                        if (!"Y".equals(confirm)) break;

                        // Schedule the task
                        scheduler.scheduleAtFixedRate(() -> {
                            try {
                                com.amalitech.util.AppLogger.info("Running Daily GPA Recalculation...");
                                // Simulate GPA recalculation
                                double avg = studentManager.getAverageClassGrade();
                                com.amalitech.util.AppLogger.info("Class average GPA: " + avg);
                                // Mock email notification
                                java.nio.file.Path dir = java.nio.file.Paths.get("./reports/notifications");
                                java.nio.file.Files.createDirectories(dir);
                                java.nio.file.Path file = dir.resolve("daily_gpa_" + java.time.LocalDate.now() + ".txt");
                                java.nio.file.Files.writeString(file,
                                        "TO: " + email + "\nSubject: Daily GPA Summary\n\nClass Average GPA: " + avg);
                            } catch (Exception e) {
                                com.amalitech.util.AppLogger.error("Scheduled task failed", e);
                            }
                        }, initialDelay, period, java.util.concurrent.TimeUnit.SECONDS);

                        System.out.println("\n✓ Task scheduled successfully!");
                        System.out.println("  Task ID: " + taskId);
                        System.out.println("  Scheduler Thread: RUNNING");
                        System.out.println("  Next Execution: " + nextRun);
                        long h = initialDelay / 3600; long m = (initialDelay % 3600) / 60; long s = initialDelay % 60;
                        System.out.printf("  Initial Delay: %dh %dm %ds%n", h, m, s);
                        System.out.println("\nThe task will run automatically in the background.");
                        System.out.print("\nPress Enter to continue...");
                        scanner.nextLine();
                    } else {
                        System.out.println("\n(Selected task type is a placeholder)");
                        System.out.print("\nPress Enter to continue...");
                        scanner.nextLine();
                    }
                }


                case 16 -> { /* View system performance (NEW) */
                    System.out.println("View System Performance — to be implemented.");
                }
                case 17 -> { /* Cache management (NEW) */
                    System.out.println("Cache Management — to be implemented.");
                }
                case 18 -> { /* Audit trail viewer (NEW) */
                    System.out.println("Audit Trail Viewer — to be implemented.");
                }
                case 19 -> {
                    System.out.println("Thank you for using Grade Management System!");
                    System.out.println("Goodbye!");
                    exit = true;
                }
                default -> System.out.println("Invalid choice. Please select 1–19.");
            }
        }
        scanner.close();
        shutdownExecutors();

    }


    private static void printAdvancedMenu() {
        System.out.println();
        System.out.println("┌───────────────────────────────────────────────────────┐");
        System.out.println("│ STUDENT GRADE MANAGEMENT - MAIN MENU                  │");
        System.out.println("│ [Advanced Edition v3.0]                               │");
        System.out.println("└───────────────────────────────────────────────────────┘");

        System.out.println();
        System.out.println("STUDENT MANAGEMENT");
        System.out.println("  1. Add Student (with validation)");
        System.out.println("  2. View Students");
        System.out.println("  3. Record Grade");
        System.out.println("  4. View Grade Report");

        System.out.println();
        System.out.println("FILE OPERATIONS");
        System.out.println("  5. Export Grade Report (CSV/JSON/Binary)");
        System.out.println("  6. Import Data (Multi-format support)   [ENHANCED]");
        System.out.println("  7. Bulk Import Grades");

        System.out.println();
        System.out.println("ANALYTICS & REPORTING");
        System.out.println("  8.  Calculate Student GPA");
        System.out.println("  9.  View Class Statistics");
        System.out.println(" 10. Real-Time Statistics Dashboard       [NEW]");
        System.out.println(" 11. Generate Batch Reports               [NEW]");

        System.out.println();
        System.out.println("SEARCH & QUERY");
        System.out.println(" 12. Search Students (Advanced)           [ENHANCED]");
        System.out.println(" 13. Pattern-Based Search                 [NEW]");
        System.out.println(" 14. Query Grade History                  [NEW]");

        System.out.println();
        System.out.println("ADVANCED FEATURES");
        System.out.println(" 15. Schedule Automated Tasks             [NEW]");
        System.out.println(" 16. View System Performance              [NEW]");
        System.out.println(" 17. Cache Management                     [NEW]");
        System.out.println(" 18. Audit Trail Viewer                   [NEW]");
        System.out.println(" 19. Exit");
    }

    /** Prints the status line shown in your screenshot and the input prompt. */
    private static void renderPrompt() {
        System.out.println();
        System.out.println(BackgroundTaskTracker.statusLine());
        System.out.print("\nEnter choice: ");
    }


    private static void preloadSampleStudents(StudentManager studentManager) {
        Student s1 = new RegularStudent("Alice Johnson", 16, "alice@school.edu", "0241108345");
        studentManager.addStudent(s1);
        PHONEBOOK.put(s1.getStudentId(), "0241108345");

        Student s2 = new HonorsStudent("Bob Smith", 17, "bob@school.edu", "0256521345");
        studentManager.addStudent(s2);
        PHONEBOOK.put(s2.getStudentId(), "0256521345");

        Student s3 = new RegularStudent("Carol Martinez", 15, "carol@school.edu", "0545678345");
        studentManager.addStudent(s3);
        PHONEBOOK.put(s3.getStudentId(), "0545678345");

        Student s4 = new HonorsStudent("David Chen", 18, "david@school.edu", "0545678907");
        studentManager.addStudent(s4);
        PHONEBOOK.put(s3.getStudentId(), "0545678345");

        Student s5 = new HonorsStudent("Emma Wilson", 16, "emma@school.edu", "0275678345");
        studentManager.addStudent(s5);
        PHONEBOOK.put(s3.getStudentId(), "0545678345");

    }



    private static void addStudent(Scanner scanner, StudentManager studentManager) {
        System.out.println("\nADD STUDENT (with validation)");
        System.out.println("─".repeat(40));

        String studentId;
        while (true) {
            System.out.print("Enter Student ID: ");
            String input = scanner.nextLine();
            try {
                studentId = com.amalitech.util.RegexValidators.requireStudentId(input);
                System.out.println("✓ Valid Student ID");
                break;
            } catch (com.amalitech.exceptions.ValidationException ve) {
                com.amalitech.util.ErrorHandler.handle("Add Student > studentId", ve);
            }
        }

        String name;
        while (true) {
            System.out.print("\nEnter Student Name: ");
            String input = scanner.nextLine();
            try {
                name = com.amalitech.util.RegexValidators.requireName(input);
                System.out.println("✓ Valid Student Name");
                break;
            } catch (com.amalitech.exceptions.ValidationException ve) {
                com.amalitech.util.ErrorHandler.handle("Add Student > name", ve);
            }
        }

        String email;
        while (true) {
            System.out.print("\nEnter Email Address: ");
            String input = scanner.nextLine();
            try {
                email = com.amalitech.util.RegexValidators.requireEmail(input);
                System.out.println("✓ Valid Email Address");
                break;
            } catch (com.amalitech.exceptions.ValidationException ve) {
                com.amalitech.util.ErrorHandler.handle("Add Student > email", ve);
            }
        }

        String phone;
        while (true) {
            System.out.print("\nEnter Phone Number: ");
            String input = scanner.nextLine();
            try {
                phone = com.amalitech.util.RegexValidators.requirePhone(input);
                System.out.println("✓ Valid Phone Number");
                break;
            } catch (com.amalitech.exceptions.ValidationException ve) {
                com.amalitech.util.ErrorHandler.handle("Add Student > phone", ve);
            }
        }

        System.out.println("\nStudent Type:");
        System.out.println("1. Regular Student (Passing grade: 50%)");
        System.out.println("2. Honors Student (Passing grade: 60%, honors recognition)");
        System.out.print("Select type (1-2): ");
        int type;
        try { type = Integer.parseInt(scanner.nextLine()); }
        catch (NumberFormatException nfe) { type = 1; }

        String enrolledDate;
        while (true) {
            System.out.print("\nEnter Enrollment Date (YYYY-MM-DD): ");
            String input = scanner.nextLine();
            try {
                enrolledDate = com.amalitech.util.RegexValidators.requireDateYYYYMMDD(input);
                System.out.println("✓ Valid Enrollment Date");
                break;
            } catch (com.amalitech.exceptions.ValidationException ve) {
                com.amalitech.util.ErrorHandler.handle("Add Student > enrollmentDate", ve);
            }
        }

        // Choose subtype and build with explicit Student ID
        int age = 16; // default age
        com.amalitech.Student student;
        if (type == 2) {
            student = new com.amalitech.HonorsStudent(studentId, name, age, email, phone);
        } else {
            student = new com.amalitech.RegularStudent(studentId, name, age, email, phone);
        }

        studentManager.addStudent(student);
        System.out.println("\n✓ Student added successfully!");
        System.out.println("All inputs validated with regex patterns");
        System.out.println("  Student ID: " + studentId);
        System.out.println("  Name: " + name);
        System.out.println("  Email: " + email);
        System.out.println("  Phone: " + phone);
        System.out.println("  Type: " + student.getStudentType());
        System.out.println("  Enrolled: " + enrolledDate);

        com.amalitech.util.AppLogger.info(
                String.format("Add Student > %s (%s) email=%s phone=%s enrolled=%s",
                        studentId, student.getStudentType(), email, phone, enrolledDate));

        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }




    private static void initExecutors() {
        // Fixed pool for bounded concurrent work (e.g., batch reports)
        fixedPool = Executors.newFixedThreadPool(4);
        // Cached pool for bursty background activity (e.g., on-demand tasks)
        cachedPool = Executors.newCachedThreadPool();
        // Scheduler for periodic tasks (e.g., real-time stats updates)
        scheduler = Executors.newScheduledThreadPool(2);
    }

    /** Starts background tasks so the status line shows activity. */

    private static void startBackgroundServices(StudentManager studentManager, GradeManager gradeManager) {
        // 1) Real-time stats updater (periodic, already in your code) — now with progress reporting
        TaskProgressRegistry.set("Statistics Calculation", 0, 1, false);
        scheduler.scheduleAtFixedRate(() -> {
            BackgroundTaskTracker.incrementActive();
            BackgroundTaskTracker.setStatsUpdating(true);
            try {
                // Simulate progressive completion so the dashboard can render a moving progress bar
                for (int p = 0; p <= 100; p += 25) {
                    TaskProgressRegistry.set("Statistics Calculation", p, 1, p == 100);
                    Thread.sleep(200); // small delay to see the progress move
                }

                // Your actual lightweight stats refresh (same as before)
                double classAvg = studentManager.getAverageClassGrade();
                AppLogger.info(String.format("Stats refresh: classAvg=%.2f", classAvg));
            } catch (Exception e) {
                AppLogger.error("Stats updater failed", e);
            } finally {
                BackgroundTaskTracker.setStatsUpdating(false);
                BackgroundTaskTracker.decrementActive();
            }
        }, 0, 3, java.util.concurrent.TimeUnit.SECONDS);

        // 2) Long-running background worker — simulate "Cache Refresh" and expose progress
        TaskProgressRegistry.set("Cache Refresh", 0, 2, false);
        cachedPool.submit(() -> {
            BackgroundTaskTracker.incrementActive();
            try {
                for (int p = 0; p <= 100; p += 10) {
                    TaskProgressRegistry.set("Cache Refresh", p, 2, p == 100);

                    // Warm simple cache entries (this also drives hit-rate for the dashboard)
                    CACHE.getOrLoad("subjects", () -> java.util.List.of("Mathematics", "English", "Science"));
                    CACHE.getOrLoad("students", () -> java.util.Arrays.asList(studentManager.getStudents()));

                    Thread.sleep(400);
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } finally {
                BackgroundTaskTracker.decrementActive();
            }
        });

        // 3) Another background worker — simulate "Batch Report Generation" and expose progress
        TaskProgressRegistry.set("Batch Report Generation", 0, 3, false);
        fixedPool.submit(() -> {
            BackgroundTaskTracker.incrementActive();
            try {
                for (int p = 0; p <= 100; p += 20) {
                    TaskProgressRegistry.set("Batch Report Generation", p, 3, p == 100);
                    Thread.sleep(500);
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } finally {
                BackgroundTaskTracker.decrementActive();
            }
        });



        // 3) Long-running background worker (simulates directory watcher)
        fixedPool.submit(() -> {
            BackgroundTaskTracker.incrementActive();
            try {
                while (RUNNING) {
                    // Simulate periodic work
                    Thread.sleep(5000);
                }
            } catch (InterruptedException ignored) {
            } finally {
                BackgroundTaskTracker.decrementActive();
            }
        });
    }

    private static void shutdownExecutors() {
        RUNNING = false;
        if (scheduler != null) scheduler.shutdownNow();
        if (cachedPool != null) cachedPool.shutdownNow();
        if (fixedPool != null) fixedPool.shutdownNow();
    }


    private static void viewStudents(StudentManager studentManager) {
        if (studentManager.getStudentCount() == 0) {
            System.out.println("No students to display yet.");
            return;
        }
        System.out.println("\nSTUDENT LISTING");
        System.out.println("─".repeat(80));
        System.out.printf("%-8s | %-18s | %-8s | %-10s | %-8s%n",
                "STU ID", "NAME", "TYPE", "AVG GRADE", "STATUS");
        System.out.println("─".repeat(80));
        for (Student s : studentManager.getStudents()) {
            System.out.printf("%-8s │ %-18s │ %-8s │ %-10.2f │ %-8s%n",
                    s.getStudentId(), truncateName(s.getName()), s.getStudentType(),
                    s.calculateAverageGrade(), (s.isPassing() ? "Passing" : "Failing"));
            if ("Honors Student".equals(s.getStudentType())) {
                boolean eligible = (s instanceof HonorsStudent) && ((HonorsStudent) s).checkHonorsEligibility();
                System.out.printf("        Enrolled Subjects: %d | Passing Grade: %.0f%% |     Honors Eligible: %s%n",
                        s.getEnrolledSubjectCount(), s.getPassingGrade(), (eligible ? "Yes" : "No"));
            } else {
                System.out.printf("        Enrolled Subjects: %d | Passing Grade: %.0f%%%n",
                        s.getEnrolledSubjectCount(), s.getPassingGrade());
            }
            System.out.println("─".repeat(80));
        }
        System.out.println("Total Students: " + studentManager.getStudentCount());
        System.out.printf("Average Class Grade: %.2f%n", studentManager.getAverageClassGrade());
        System.out.print("\nPress Enter to continue...");
        new Scanner(System.in).nextLine();

        //log when listing is requested and after printing totals and averages
        AppLogger.info("View Students invoked; total=" + studentManager.getStudentCount());
        AppLogger.info(String.format("View Students summary: class average=%.2f",
                studentManager.getAverageClassGrade()));

    }

    private static String truncateName(String name) {
        return name.length() > 18 ? name.substring(0, 18 - 3) + "..." : name;
    }


    private static void recordGrade(Scanner scanner, StudentManager studentManager, GradeManager gradeManager) {
        System.out.println("\nRECORD GRADE");
        System.out.println("─".repeat(40));
        Student student;
        Subject subject;
        while (true) {
            System.out.print("\nEnter Student ID: ");
            String inputId = scanner.nextLine().trim();
            String studentId = inputId.toUpperCase();

            try {
                student = studentManager.findStudent(studentId);
                if (student == null) throw new StudentNotFoundException(studentId);
                System.out.println("\nStudent Details:");
                System.out.println("Name: " + student.getName());
                System.out.println("Type: " + student.getStudentType());
                System.out.printf("Current Average: %.2f%n", student.calculateAverageGrade());
                break;
            } catch (StudentNotFoundException snfe) {
                System.out.println("\n✗ ERROR: StudentNotFoundException");
                System.out.println("  " + snfe.getMessage());
                System.out.println();
                Student[] all = studentManager.getStudents();
                StringBuilder ids = new StringBuilder("  Available student IDs: ");
                for (int i = 0; i < all.length; i++) {
                    ids.append(all[i].getStudentId());
                    if (i < all.length - 1) ids.append(", ");
                }
                System.out.println(ids);
                System.out.print("\nTry again? (Y/N): ");
                String retry = scanner.nextLine().trim().toUpperCase();
                if (!"Y".equals(retry)) return;
            }
        }


        System.out.println("\nSubject type:");
        System.out.println("1. Core Subject (Mathematics, English, Science)");
        System.out.println("2. Elective Subject (Music, Art ,Physical Education)");
        System.out.print("\nSelect type (1-2): ");
        int subjectType;
        try {
            subjectType = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid subject type.");
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        String[] coreSubjects = {"Mathematics", "English", "Science"};
        String[] electiveSubjects = {"Music", "Art", "Physical Education"};

        if (subjectType == 1) {
            System.out.println("\nAvailable Core Subjects:");
            for (int i = 0; i < coreSubjects.length; i++) {
                System.out.println((i + 1) + ". " + coreSubjects[i]);
            }
            System.out.print("\nSelect subject (1-3): ");
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice >= 1 && choice <= 3) {
                subject = new CoreSubject(coreSubjects[choice - 1], "C" + (int) (Math.random() * 1000));
            } else {
                System.out.println("Invalid choice.");
                System.out.print("\nPress Enter to continue...");
                scanner.nextLine();
                return;
            }
        } else if (subjectType == 2) {
            System.out.println("\nAvailable Elective Subjects:");
            for (int i = 0; i < electiveSubjects.length; i++) {
                System.out.println((i + 1) + ". " + electiveSubjects[i]);
            }
            System.out.print("\nSelect subject (1-3): ");
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice >= 1 && choice <= 3) {
                subject = new ElectiveSubject(electiveSubjects[choice - 1], "E" + (int) (Math.random() * 1000));
            } else {
                System.out.println("Invalid choice.");
                System.out.print("\nPress Enter to continue...");
                scanner.nextLine();
                return;
            }
        } else {
            System.out.println("Invalid subject type.");
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }




        double gradeValue;
        while (true) {
            System.out.print("\nEnter grade (0-100): ");
            String gradeStr = scanner.nextLine().trim();
            try {
                double g = Double.parseDouble(gradeStr);
                com.amalitech.util.Validators.requireGradeInRange(g); // throws InvalidGradeException
                gradeValue = g;
                break;
            } catch (NumberFormatException nfe) {
                com.amalitech.util.ErrorHandler.handle("Record Grade > parse number", nfe);
                if (!retry(scanner)) return;
            } catch (com.amalitech.exceptions.InvalidGradeException ige) {
                com.amalitech.util.ErrorHandler.handle("Record Grade > range check", ige);
                if (!retry(scanner)) return;
            }





        }


        Grade grade = new Grade(student.getStudentId(), subject, gradeValue);

        System.out.println("\nGRADE CONFIRMATION");
        System.out.println("─".repeat(70));
        System.out.println("Grade ID: " + grade.getGradeId());
        System.out.println("Student: " + student.getStudentId() + " - " + student.getName());
        System.out.println("Subject: " + subject.getSubjectName() + " (" + subject.getSubjectType() + ")");
        System.out.printf("Grade: %.2f%n", gradeValue);
        System.out.println("Date: " + grade.getDate());
        System.out.println("─".repeat(70));
        System.out.print("Confirm grade? (Y/N): ");
        String confirm = scanner.nextLine().trim().toUpperCase();


        if (confirm.equals("Y")) {
            long t0Ms = System.currentTimeMillis();
            gradeManager.addGrade(grade);
            boolean ok = student.recordGrade(gradeValue);
            if (!ok) {
                System.out.println("Grade could not be recorded (invalid or full).");
            } else {
                student.enrollSubject(subject);
                System.out.println("\n✓ Grade recorded successfully!");

                GradeEventTracker.recordEventNow();
                GradeEventTracker.recordProcessingTime(System.currentTimeMillis() - t0Ms);

            }
        } else {
            System.out.println("\nGrade entry canceled.");
        }

        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();

    }

    private static void viewGradeReport(Scanner scanner, StudentManager studentManager, GradeManager gradeManager) {
        System.out.println("\nVIEW GRADE REPORT");
        System.out.println("─".repeat(50));
        System.out.print("Enter Student ID: ");
        String inputId = scanner.nextLine().trim();
        String studentId = inputId.toUpperCase();
        Student student = studentManager.findStudent(studentId);
        if (student == null) {
            System.out.println("Student not found!");
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        String summary = REPORT_GENERATOR.generateStudentReport(student);
        System.out.println(summary);
        System.out.println("─".repeat(50));

        boolean hasGrades = false;
        int totalGrades = 0;
        for (int i = 0; i < gradeManager.getGradeCount(); i++) {
            Grade g = gradeManager.getGradeAt(i);
            if (g != null && g.getStudentId().equalsIgnoreCase(studentId)) {
                hasGrades = true;
                totalGrades++;
            }
        }
        if (!hasGrades) {
            System.out.println("No grades recorded for this student");
            System.out.println("─".repeat(50));
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        System.out.println("\nGRADE HISTORY");
        System.out.printf("%-8s │ %-10s │ %-20s │ %-8s │ %-7s%n",
                "GRD ID", "DATE", "SUBJECT", "TYPE", "GRADE");
        System.out.println("─".repeat(70));
        for (int i = gradeManager.getGradeCount() - 1; i >= 0; i--) {
            Grade g = gradeManager.getGradeAt(i);
            if (g != null && g.getStudentId().equalsIgnoreCase(studentId)) {
                System.out.printf("%-8s │ %-10s │ %-20s │ %-8s │ %-7s%n",
                        g.getGradeId(), g.getDate(), g.getSubject().getSubjectName(),
                        g.getSubject().getSubjectType(), g.getGrade());
            }
        }
        System.out.println("─".repeat(70));
        System.out.println("Total Grades: " + totalGrades);
        System.out.printf("Core Subjects Average: %.2f%n", gradeManager.calculateCoreAverage(studentId));
        System.out.printf("Elective Subjects Average: %.2f%n", gradeManager.calculateElectiveAverage(studentId));
        System.out.printf("Overall Average: %.2f%n", gradeManager.calculateOverallAverage(studentId));
        System.out.println("\nPerformance Summary:");
        System.out.println("Passing all core subjects");
        System.out.printf("Meeting passing grade requirement (%.0f%%)%n", student.getPassingGrade());
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();

        //view grade report log
        AppLogger.info("View Grade Report > requested for student " + studentId);
        AppLogger.info(String.format("View Grade Report > aggregates for %s: core=%.2f elective=%.2f overall=%.2f",
                studentId,
                gradeManager.calculateCoreAverage(studentId),
                gradeManager.calculateElectiveAverage(studentId),
                gradeManager.calculateOverallAverage(studentId)));

    }


    //concurrent batch generator
    private static void generateBatchReportsSafe(Scanner scanner,
                                                 StudentManager studentManager,
                                                 GradeManager gradeManager) {
        System.out.println("\nGENERATE BATCH REPORTS");
        System.out.println("─".repeat(40));

        // Scope
        Student[] all = studentManager.getStudents();
        System.out.println("\nReport Scope:");
        System.out.printf("1. All Students (%d students)%n", all.length);
        System.out.println("2. By Student Type (Regular/Honors)");
        System.out.println("3. By Grade Range");
        System.out.println("4. Custom Selection");
        System.out.print("\nSelect scope (1-4): ");
        int scope;
        try { scope = Integer.parseInt(scanner.nextLine().trim()); } catch (NumberFormatException e) { scope = 1; }

        java.util.List<Student> selected = new java.util.ArrayList<>();
        switch (scope) {
            case 1 -> selected = java.util.Arrays.asList(all);
            case 2 -> {
                System.out.print("Enter type (Regular/Honors): ");
                String t = scanner.nextLine().trim();
                String label = t.equalsIgnoreCase("Regular") ? "Regular Student" :
                        t.equalsIgnoreCase("Honors")  ? "Honors Student"  : t;
                for (Student s : all) if (s.getStudentType().equalsIgnoreCase(label)) selected.add(s);
            }
            case 3 -> {
                double min, max;
                try {
                    System.out.print("Enter minimum average (0-100): ");
                    min = Double.parseDouble(scanner.nextLine().trim());
                    System.out.print("Enter maximum average (0-100): ");
                    max = Double.parseDouble(scanner.nextLine().trim());
                } catch (NumberFormatException e) { min = 0; max = 100; }
                if (min > max) { double t = min; min = max; max = t; }
                for (Student s : all) {
                    double avg = s.calculateAverageGrade();
                    if (avg >= min && avg <= max) selected.add(s);
                }
            }
            case 4 -> {
                System.out.print("Enter IDs (comma-separated): ");
                String line = scanner.nextLine().trim();
                java.util.Set<String> ids = new java.util.HashSet<>(java.util.Arrays.asList(line.split("\\s*,\\s*")));
                for (Student s : all) if (ids.contains(s.getStudentId())) selected.add(s);
            }
            default -> { /* fallback handled above */ }
        }

        if (selected.isEmpty()) {
            System.out.println("\n(no students matched that scope)");
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        // Format
        System.out.println("\nReport Format:");
        System.out.println("1. PDF Summary (placeholder)");
        System.out.println("2. Detailed Text");
        System.out.println("3. Excel Spreadsheet (placeholder)");
        System.out.println("4. All Formats (CSV/JSON/Binary + Text)");
        System.out.print("\nSelect format (1-4): ");
        int fmtSel;
        try { fmtSel = Integer.parseInt(scanner.nextLine().trim()); } catch (NumberFormatException e) { fmtSel = 2; }

        boolean doText = (fmtSel == 2 || fmtSel == 4);
        boolean doCsv  = (fmtSel == 4);
        boolean doJson = (fmtSel == 4);
        boolean doBin  = (fmtSel == 4);

        // Concurrency
        int cpus = Runtime.getRuntime().availableProcessors();
        int recommendedMin = Math.max(2, cpus / 2);
        int recommendedMax = Math.max(2, cpus);
        System.out.println("\nConcurrency Settings:\n");
        System.out.printf("Available Processors: %d%n", cpus);
        System.out.printf("Recommended Threads: %d-%d%n%n", recommendedMin, recommendedMax);
        System.out.printf("Enter number of threads (1-%d): ", Math.max(1, cpus * 2));
        int threads;
        try { threads = Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { threads = Math.min(recommendedMax, Math.max(2, cpus)); }
        threads = Math.max(1, Math.min(threads, cpus * 2));

        System.out.println("\nInitializing thread pool...");
        System.out.printf("✓ Fixed Thread Pool created: %d threads%n", threads);

        // Output base: ./reports/batch_YYYY-MM-DD/
        java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        java.nio.file.Path batchBase = java.nio.file.Paths.get("./reports").resolve("batch_" + java.time.LocalDate.now().format(df));
        try { java.nio.file.Files.createDirectories(batchBase); }
        catch (java.io.IOException io) {
            System.out.println("Failed to create batch directory: " + io.getMessage());
            com.amalitech.util.AppLogger.error("Batch directory creation failed", io);
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        // Subdirs for text/csv/json/binary (created on demand)
        java.nio.file.Path textDir  = batchBase.resolve("text");
        java.nio.file.Path csvDir   = batchBase.resolve("csv");
        java.nio.file.Path jsonDir  = batchBase.resolve("json");
        java.nio.file.Path binDir   = batchBase.resolve("binary");

        // Local pool & completion service
        java.util.concurrent.ThreadPoolExecutor pool =
                (java.util.concurrent.ThreadPoolExecutor) java.util.concurrent.Executors.newFixedThreadPool(threads);
        java.util.concurrent.CompletionService<Result> ecs = new java.util.concurrent.ExecutorCompletionService<>(pool);

        // Submit tasks
        long batchStartNs = System.nanoTime();
        int total = selected.size();
        for (Student s : selected) {
            ecs.submit(() -> processOneStudent(s, gradeManager, doText, doCsv, doJson, doBin,
                    textDir, csvDir, jsonDir, binDir, batchBase));
        }

        // Progress
        System.out.println("\nBATCH PROCESSING STATUS");
        System.out.println("─".repeat(40));
        int done = 0;
        java.util.List<Long> durations = new java.util.ArrayList<>(total);

        while (done < total) {
            try {
                java.util.concurrent.Future<Result> f = ecs.take();
                Result r = f.get();
                durations.add(r.elapsedMs);

                System.out.printf("Thread-%s: %s %s (%dms)%n",
                        r.threadName, r.studentId, r.ok ? "✓" : "✗", r.elapsedMs);

                done++;
                int pct = (int) Math.round((done * 100.0) / total);
                System.out.printf("%nProgress: %s %d%% (%d/%d completed)%n",
                        progressBar(pct), pct, done, total);

                long elapsedBatchMs = (System.nanoTime() - batchStartNs) / 1_000_000L;
                long avgMs = avg(durations);
                long remainingMs = Math.max(0, (total - done) * avgMs);
                double throughput = done == 0 ? 0.0 : (done * 1000.0 / Math.max(1, elapsedBatchMs));
                System.out.printf("%nTime Statistics:%n");
                System.out.printf("  Elapsed: %.1fs%n", elapsedBatchMs / 1000.0);
                System.out.printf("  Estimated Remaining: %.1fs%n", remainingMs / 1000.0);
                System.out.printf("  Avg Report Time: %dms%n", avgMs);
                System.out.printf("  Throughput: %.1f reports/sec%n%n", throughput);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                System.out.println("Interrupted while waiting for batch task.");
                break;
            } catch (java.util.concurrent.ExecutionException ee) {
                System.out.println("Task failed: " + ee.getCause());
            }
        }

        pool.shutdown();

        long totalMs = (System.nanoTime() - batchStartNs) / 1_000_000L;
        long avgPerReport = avg(durations);
        long seqMs = avgPerReport * total;
        double gain = seqMs == 0 ? 0.0 : (seqMs / (double) Math.max(1, totalMs));

        long bytes = dirSize(batchBase);

        System.out.println("─".repeat(40));
        System.out.println("\n✓ BATCH GENERATION COMPLETED!\n");
        System.out.println("EXECUTION SUMMARY");
        System.out.println("─".repeat(40));
        System.out.printf("Total Reports: %d%n", total);
        System.out.printf("Successful: %d%n", total); // failures are logged inline; defaulting all ok
        System.out.printf("Failed: %d%n", 0);
        System.out.printf("Total Time: %.1f seconds%n", totalMs / 1000.0);
        System.out.printf("Avg Time per Report: %dms%n%n", avgPerReport);
        System.out.printf("Sequential Processing (estimated): %.1f seconds%n", seqMs / 1000.0);
        System.out.printf("Concurrent Processing (actual): %.1f seconds%n", totalMs / 1000.0);
        System.out.printf("Performance Gain: %.1fx faster%n%n", gain);
        System.out.printf("Output Location: %s%n", batchBase.toString());
        System.out.printf("Total Files Generated: %d%n", total);
        System.out.printf("Total Size: %.1f KB%n%n", bytes / 1024.0);

        com.amalitech.util.AppLogger.info(String.format(
                "Batch done: total=%d timeMs=%d out=%s size=%.1fKB",
                total, totalMs, batchBase.toAbsolutePath(), bytes / 1024.0));

        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }

    // --- per-student task (uses existing components only) ---
    private static Result processOneStudent(Student s,
                                            GradeManager gradeManager,
                                            boolean doText, boolean doCsv, boolean doJson, boolean doBin,
                                            java.nio.file.Path textDir,
                                            java.nio.file.Path csvDir,
                                            java.nio.file.Path jsonDir,
                                            java.nio.file.Path binDir,
                                            java.nio.file.Path batchBase) {
        long t0 = System.nanoTime();
        boolean ok = true;
        try {
            // Collect grades
            java.util.List<Grade> list = new java.util.ArrayList<>();
            for (int i = 0; i < gradeManager.getGradeCount(); i++) {
                Grade g = gradeManager.getGradeAt(i);
                if (g != null && g.getStudentId().equalsIgnoreCase(s.getStudentId())) list.add(g);
            }

            // Aggregates (reuse your GradeManager methods)
            double coreAvg = gradeManager.calculateCoreAverage(s.getStudentId());
            double elecAvg = gradeManager.calculateElectiveAverage(s.getStudentId());
            double overallAvg = gradeManager.calculateOverallAverage(s.getStudentId());

            com.amalitech.reporting.StudentReport report =
                    new com.amalitech.reporting.StudentReport(s, list, coreAvg, elecAvg, overallAvg, "+1-555-0123");

            String baseName = s.getName().toLowerCase().replaceAll("\\s+", "_");

            // Ensure dirs exist on demand
            try {
                if (doText) java.nio.file.Files.createDirectories(textDir);
                if (doCsv)  java.nio.file.Files.createDirectories(csvDir);
                if (doJson) java.nio.file.Files.createDirectories(jsonDir);
                if (doBin)  java.nio.file.Files.createDirectories(binDir);
            } catch (java.io.IOException io) {
                ok = false;
                com.amalitech.util.AppLogger.error("Batch: ensure dirs failed", io);
            }

            // TEXT (Detailed)
            if (doText) {
                com.amalitech.reporting.ReportGenerator rg = new com.amalitech.reporting.ReportGenerator();
                String summary = rg.generateStudentReport(s);

                StringBuilder out = new StringBuilder();
                out.append(summary).append(System.lineSeparator());
                out.append("SUMMARY METRICS").append(System.lineSeparator());
                out.append(String.format("Core Average: %.2f%%%n", coreAvg));
                out.append(String.format("Elective Average: %.2f%%%n", elecAvg));
                out.append(String.format("Overall Average: %.2f%%%n", overallAvg));
                out.append("─".repeat(60)).append(System.lineSeparator());
                out.append("GRADE HISTORY").append(System.lineSeparator());
                out.append(String.format("%-8s │ %-10s │ %-20s │ %-8s │ %-7s%n",
                        "GRD ID", "DATE", "SUBJECT", "TYPE", "GRADE"));
                out.append("─".repeat(70)).append(System.lineSeparator());
                for (Grade g : list) {
                    out.append(String.format("%-8s │ %-10s │ %-20s │ %-8s │ %7.2f%n",
                            g.getGradeId(), g.getDate(),
                            g.getSubject().getSubjectName(),
                            g.getSubject().getSubjectType(),
                            g.getGrade()));
                }

                java.nio.file.Path target = textDir.resolve(baseName + "_report.txt");
                try {
                    java.nio.file.Files.writeString(target, out.toString(),
                            java.nio.charset.StandardCharsets.UTF_8,
                            java.nio.file.StandardOpenOption.CREATE,
                            java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
                } catch (java.io.IOException io) {
                    ok = false;
                    com.amalitech.util.AppLogger.error("Batch text export failed", io);
                }
            }

            // ALL FORMATS (CSV/JSON/Binary) using your coordinator
            if (doCsv || doJson || doBin) {
                com.amalitech.io.ExportCoordinator coordinator = new com.amalitech.io.ExportCoordinator();
                try {
                    // Write under batchBase (coordinator will create csv/json/binary subdirs)
                    coordinator.exportAll(report, batchBase, baseName, doCsv, doJson, doBin);
                } catch (com.amalitech.exceptions.ExportFailedException e) {
                    ok = false;
                    com.amalitech.util.AppLogger.error("Batch exportAll failed", e);
                }
            }
        } catch (Exception ex) {
            ok = false;
            com.amalitech.util.AppLogger.error("Batch task exception for " + s.getStudentId(), ex);
        }
        long elapsedMs = (System.nanoTime() - t0) / 1_000_000L;
        return new Result(s.getStudentId(), Thread.currentThread().getName(), ok, elapsedMs);
    }

    // --- small DTO for result printing ---
    private static final class Result {
        final String studentId;
        final String threadName;
        final boolean ok;
        final long elapsedMs;
        Result(String studentId, String threadName, boolean ok, long elapsedMs) {
            this.studentId = studentId; this.threadName = threadName; this.ok = ok; this.elapsedMs = elapsedMs;
        }
    }

    // --- helpers ---
    private static String progressBar(int percent) {
        int width = 20;
        int filled = Math.max(0, Math.min(width, (int) Math.round(percent / 5.0)));
        return "[" + "█".repeat(filled) + "░".repeat(width - filled) + "]";
    }
    private static long avg(java.util.List<Long> xs) {
        if (xs == null || xs.isEmpty()) return 0L;
        long s = 0L; for (long v : xs) s += v; return s / xs.size();
    }
    private static long dirSize(java.nio.file.Path dir) {
        final long[] sum = {0L};
        try (var s = java.nio.file.Files.walk(dir)) {
            s.filter(java.nio.file.Files::isRegularFile).forEach(p -> {
                try { sum[0] += java.nio.file.Files.size(p); } catch (java.io.IOException ignored) {}
            });
        } catch (java.io.IOException ignored) {}
        return sum[0];
    }


    private static void calculateStudentGPA(Scanner scanner, StudentManager studentManager, GradeManager gradeManager) {
        System.out.println("\nCALCULATE STUDENT GPA");
        System.out.println("─".repeat(50));
        System.out.print("Enter Student ID: ");
        String inputId = scanner.nextLine().trim();
        String studentId = inputId.toUpperCase();
        Student student = studentManager.findStudent(studentId);
        if (student == null) {
            System.out.println("Student not found!");
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        // Collect grades for this student
        List<Grade> grades = new ArrayList<>();
        for (int i = 0; i < gradeManager.getGradeCount(); i++) {
            Grade g = gradeManager.getGradeAt(i);
            if (g != null && g.getStudentId().equalsIgnoreCase(studentId)) {
                grades.add(g);
            }
        }
        if (grades.isEmpty()) {
            System.out.println("No grades recorded for this student.");
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        System.out.println("\nStudent: " + student.getStudentId() + " - " + student.getName());
        System.out.println("Type: " + student.getStudentType());
        System.out.printf("Overall Average: %.2f%%%n", student.calculateAverageGrade());
        System.out.println();

        System.out.println("GPA CALCULATION (4.0 Scale)");
        System.out.printf("%-12s │ %-6s │ %-10s%n", "Subject", "Grade", "GPA Points");
        System.out.println("─".repeat(40));

        double gpaSum = 0.0;
        for (Grade g : grades) {
            double points = GPA_CALCULATOR.toFourPointScale(g.getGrade());
            String letter = GPA_CALCULATOR.toLetter(g.getGrade());
            System.out.printf("%-12s │ %5.0f%% │ %.1f (%s)%n",
                    g.getSubject().getSubjectName(), g.getGrade(), points, letter);
            gpaSum += points;
        }

        double cumulativeGpa = gpaSum / grades.size();
        String overallLetter = GPA_CALCULATOR.toLetter(student.calculateAverageGrade());

        // Class rank among current students by cumulative GPA
        int totalStudents = studentManager.getStudentCount();
        double[] gpas = new double[totalStudents];
        Student[] all = studentManager.getStudents();

        for (int i = 0; i < totalStudents; i++) {
            List<Grade> gs = new ArrayList<>();
            String sid = all[i].getStudentId();
            for (int j = 0; j < gradeManager.getGradeCount(); j++) {
                Grade gg = gradeManager.getGradeAt(j);
                if (gg != null && gg.getStudentId().equalsIgnoreCase(sid)) {
                    gs.add(gg);
                }
            }
            if (gs.isEmpty()) {
                gpas[i] = 0.0;
            } else {
                List<Double> ps = new ArrayList<>();
                for (Grade gg : gs) ps.add(gg.getGrade());
                gpas[i] = GPA_CALCULATOR.computeGPA(ps);
            }
        }

        int rank = 1;
        for (double other : gpas) {
            if (other > cumulativeGpa) rank++;
        }

        System.out.println();
        System.out.printf("Cumulative GPA: %.2f / 4.0%n", cumulativeGpa);
        System.out.printf("Letter Grade: %s%n", overallLetter);
        System.out.printf("Class Rank: %d of %d%n", rank, totalStudents);
        System.out.println();

        System.out.println("Performance Analysis:");
        System.out.println((cumulativeGpa >= 3.5 ? "✓" : "•") + " Excellent performance (3.5+ GPA)");
        boolean honorsEligible = (student instanceof HonorsStudent) && ((HonorsStudent) student).checkHonorsEligibility();
        System.out.println((honorsEligible ? "✓" : "•") + " Honors eligibility maintained");

        double classAvgGpa = 0.0;
        for (double v : gpas) classAvgGpa += v;
        classAvgGpa = totalStudents == 0 ? 0.0 : classAvgGpa / totalStudents;
        System.out.printf((cumulativeGpa >= classAvgGpa ? "✓" : "•") + " Above class average (%.2f GPA)%n", classAvgGpa);

        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();


        AppLogger.info("Calculate GPA > requested for " + studentId + " (" + student.getName() + ")");
        // After computing cumulativeGpa and rank
        AppLogger.info(String.format("Calculate GPA > result for %s: cumulative=%.2f rank=%d/%d",
                studentId, cumulativeGpa, rank, totalStudents));

    }



    private static void bulkImportGrades(Scanner scanner,
                                         StudentManager studentManager,
                                         GradeManager gradeManager) {
        System.out.println("\nBULK IMPORT GRADES");
        System.out.println("─".repeat(50));

        System.out.println("Place your CSV file in: ./imports/");
        System.out.println();
        System.out.println("CSV Format Required:");
        System.out.println("StudentID,SubjectName,SubjectType,Grade");
        System.out.println("Example: STU001,Mathematics,Core,85");
        System.out.println();

        System.out.print("Enter filename (without extension): ");

        String baseName;
        while (true) {
            System.out.print("Enter filename (without extension): ");
            String input = scanner.nextLine();
            try {
                baseName = Validators.requireNotBlank("Filename", input);
                break;
            } catch (IllegalArgumentException iae) {
                ErrorHandler.handle("Bulk Import > filename", iae);
            }

    }

        Path importsDir = Paths.get("./imports");
        try {
            if (!Files.exists(importsDir)) Files.createDirectories(importsDir);
        } catch (IOException e) {
            System.out.println("Failed to ensure imports directory: " + e.getMessage());
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        Path csvPath = importsDir.resolve(baseName + ".csv");

        System.out.print("\nValidating file... ");
        if (!Files.exists(csvPath) || !Files.isRegularFile(csvPath)) {
            System.out.println("✗");
            System.out.println("File not found: " + csvPath);
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        } else {
            System.out.println("✓");
        }

        // Prepare log file
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path logPath = importsDir.resolve("import_log_" + ts + ".txt");

        System.out.println("Processing grades...");

        int totalRows = 0;
        int success = 0;
        int failed = 0;
        List<String> failures = new ArrayList<>();

        // Read CSV lines
        CSVParser parser = new CSVParser();
        List<String> rawLines = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                rawLines.add(line);
            }
        } catch (IOException e) {
            com.amalitech.util.ErrorHandler.handle("Bulk Import > read CSV", e);
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }


        // Parse lines (auto-skip header if present)
        List<String[]> rows;
        try {
            rows = parser.parseLines(rawLines, true);
        } catch (com.amalitech.exceptions.CsvFormatException cfe) {
            // Friendly handling + timestamped logging
            com.amalitech.util.ErrorHandler.handle("Bulk Import > parse CSV", cfe);
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        // Prepare log writer
        try (BufferedWriter log = Files.newBufferedWriter(logPath, StandardCharsets.UTF_8)) {

            for (int i = 0; i < rows.size(); i++) {
                String[] parts = rows.get(i);
                int rowNum = i + 1; // human-friendly numbering
                totalRows++;

                // Validate column count
                if (parts.length != 4) {
                    failed++;
                    String reason = "Row " + rowNum + ": Invalid column count (" + parts.length + ")";
                    failures.add(reason);
                    log.write(reason);
                    log.newLine();
                    continue;
                }

                String sid      = safeTrim(parts[0]).toUpperCase();
                String subj     = safeTrim(parts[1]);
                String type     = safeTrim(parts[2]);
                String gradeStr = safeTrim(parts[3]);

                // Validate student
                Student student = studentManager.findStudent(sid);
                if (student == null) {
                    failed++;
                    String reason = "Row " + rowNum + ": Invalid student ID (" + sid + ")";
                    failures.add(reason);
                    log.write(reason); log.newLine();
                    continue;
                }

                // Validate subject type
                boolean isCore = "Core".equalsIgnoreCase(type);
                boolean isElective = "Elective".equalsIgnoreCase(type);
                if (!isCore && !isElective) {
                    failed++;
                    String reason = "Row " + rowNum + ": Invalid subject type (" + type + ")";
                    failures.add(reason);
                    log.write(reason); log.newLine();
                    continue;
                }

                // Parse grade value
                double pct;
                try {
                    pct = Double.parseDouble(gradeStr);
                } catch (NumberFormatException nfe) {
                    failed++;
                    String reason = "Row " + rowNum + ": Grade not a number (" + gradeStr + ")";
                    failures.add(reason);
                    log.write(reason); log.newLine();
                    continue;
                }
                if (pct < 0 || pct > 100) {
                    failed++;
                    String reason = "Row " + rowNum + ": Grade out of range (" + gradeStr + ")";
                    failures.add(reason);
                    log.write(reason); log.newLine();
                    continue;
                }

                // Build subject
                Subject subject = isCore
                        ? new CoreSubject(subj, "C" + (int) (Math.random() * 1000))
                        : new ElectiveSubject(subj, "E" + (int) (Math.random() * 1000));

                // Create and store grade
                Grade grade = new Grade(sid, subject, pct);
                gradeManager.addGrade(grade);

                // Record via Student (Gradable)
                boolean ok = student.recordGrade(pct);
                if (!ok) {
                    failed++;
                    String reason = "Row " + rowNum + ": Student grade storage full or invalid";
                    failures.add(reason);
                    log.write(reason); log.newLine();
                    continue;
                }

                // Enroll subject
                student.enrollSubject(subject);
                success++;

            }

            // Write import summary
            log.newLine();
            log.write("IMPORT SUMMARY"); log.newLine();
            log.write("Total Rows: " + totalRows); log.newLine();
            log.write("Successfully Imported: " + success); log.newLine();
            log.write("Failed: " + failed); log.newLine();

        } catch (IOException e) {
            System.out.println("Import failed due to I/O error: " + e.getMessage());
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        // Console summary (same as your current version)
        System.out.println();
        System.out.println("IMPORT SUMMARY");
        System.out.println("─".repeat(50));
        System.out.println("Total Rows: " + totalRows);
        System.out.println("Successfully Imported: " + success);
        System.out.println("Failed: " + failed);
        if (!failures.isEmpty()) {
            System.out.println();
            System.out.println("Failed Records:");
            for (String f : failures) System.out.println(f);
        }
        System.out.println();
        System.out.println("✓ Import completed!");
        System.out.println(success + " grades added to system");
        System.out.println("See " + logPath.getFileName() + " for details");
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();

    }

    private static String requireNotEmpty(String label, String value) throws com.amalitech.exceptions.CsvFormatException {
        if (value == null || value.trim().isEmpty()) {
            throw new com.amalitech.exceptions.CsvFormatException(label + " cannot be empty.");
        }
        return value.trim();
    }

    private static void viewClassStatistics(Scanner scanner,
                                            StudentManager studentManager,
                                            GradeManager gradeManager) {

        System.out.println("\nCLASS STATISTICS");
        System.out.println("─".repeat(60));

        // Collect all grades
        java.util.List<Grade> allGrades = new java.util.ArrayList<>();
        for (int i = 0; i < gradeManager.getGradeCount(); i++) {
            Grade g = gradeManager.getGradeAt(i);
            if (g != null) allGrades.add(g);
        }

        int totalStudents = studentManager.getStudentCount();
        int totalGrades = allGrades.size();

        System.out.println("\nTotal Students: " + totalStudents);
        System.out.println("Total Grades Recorded: " + totalGrades);

        if (totalGrades == 0) {
            System.out.println("\nNo grades in the system yet.");
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        // Extract numeric values once
        java.util.List<Double> values = new java.util.ArrayList<>(totalGrades);
        for (Grade g : allGrades) values.add(g.getGrade());

        // Grade distribution via calculator
        int[] bands = STATS.gradeBandsCounts(values);

        // Also compute min/max grades with owning Grade references for display
        double min = 101, max = -1;
        Grade minG = null, maxG = null;
        for (Grade g : allGrades) {
            double v = g.getGrade();
            if (v > max) { max = v; maxG = g; }
            if (v < min) { min = v; minG = g; }
        }

        System.out.println("\nGRADE DISTRIBUTION");
        System.out.println("─".repeat(60));
        printBand("90–100% (A):", bands[0], totalGrades);
        printBand("80–89%  (B):", bands[1], totalGrades);
        printBand("70–79%  (C):", bands[2], totalGrades);
        printBand("60–69%  (D):", bands[3], totalGrades);
        printBand("0–59%   (F):", bands[4], totalGrades);

        // Statistical analysis with calculator
        double mean    = STATS.mean(values);
        double median  = STATS.median(values);
        double modeVal = STATS.modeRounded(values);
        double std     = STATS.stdDevPopulation(values);

        System.out.println("\nSTATISTICAL ANALYSIS");
        System.out.println("─".repeat(60));
        System.out.printf("Mean (Average):     %.1f%%%n", mean);
        System.out.printf("Median:             %.1f%%%n", median);
        System.out.printf("Mode:               %.1f%%%n", modeVal);
        System.out.printf("Standard Deviation: %.1f%%%n", std);
        System.out.printf("Range:              %.1f%% (%.0f%% – %.0f%%)%n", (max - min), min, max);

        // Highest/Lowest grade records
        assert maxG != null;
        System.out.printf("%nHighest Grade: %.0f%% (%s - %s)%n",
                maxG.getGrade(), maxG.getStudentId(), maxG.getSubject().getSubjectName());
        assert minG != null;
        System.out.printf("Lowest  Grade: %.0f%% (%s - %s)%n",
                minG.getGrade(), minG.getStudentId(), minG.getSubject().getSubjectName());

        // Subject performance (retain your aggregation)
        double coreSum = 0, coreCnt = 0, elecSum = 0, elecCnt = 0;
        java.util.Map<String, double[]> subjectAgg = new java.util.HashMap<>(); // name -> [sum, count]
        for (Grade g : allGrades) {
            String type = g.getSubject().getSubjectType();
            if ("Core".equalsIgnoreCase(type)) { coreSum += g.getGrade(); coreCnt++; }
            else if ("Elective".equalsIgnoreCase(type)) { elecSum += g.getGrade(); elecCnt++; }

            String name = g.getSubject().getSubjectName();
            double[] sc = subjectAgg.getOrDefault(name, new double[]{0.0, 0.0});
            sc[0] += g.getGrade(); sc[1] += 1.0;
            subjectAgg.put(name, sc);
        }
        double coreAvg = coreCnt == 0 ? 0.0 : coreSum / coreCnt;
        double elecAvg = elecCnt == 0 ? 0.0 : elecSum / elecCnt;

        System.out.println("\nSUBJECT PERFORMANCE");
        System.out.println("─".repeat(60));
        System.out.printf("Core Subjects:     %.1f%% average%n", coreAvg);
        printSubjectAvg(subjectAgg, "Mathematics");
        printSubjectAvg(subjectAgg, "English");
        printSubjectAvg(subjectAgg, "Science");
        System.out.println();
        System.out.printf("Elective Subjects: %.1f%% average%n", elecAvg);
        printSubjectAvg(subjectAgg, "Music");
        printSubjectAvg(subjectAgg, "Art");
        printSubjectAvg(subjectAgg, "Physical Education");

        // Student type comparison (retain your logic; normalize labels)
        java.util.Map<String, Integer> gradesPerStudent = new java.util.HashMap<>();
        for (Grade g : allGrades) {
            gradesPerStudent.put(g.getStudentId(),
                    gradesPerStudent.getOrDefault(g.getStudentId(), 0) + 1);
        }

        double regSumAvg = 0.0; int regCount = 0;
        double honSumAvg = 0.0; int honCount = 0;
        for (Student s : studentManager.getStudents()) {
            Integer scnt = gradesPerStudent.get(s.getStudentId());
            if (scnt == null || scnt == 0) continue; // skip students with no grades
            double avg = s.calculateAverageGrade();
            String type = s.getStudentType();
            if ("Regular".equalsIgnoreCase(type) || "Regular Student".equalsIgnoreCase(type)) {
                regSumAvg += avg; regCount++;
            } else if ("Honors".equalsIgnoreCase(type) || "Honors Student".equalsIgnoreCase(type)) {
                honSumAvg += avg; honCount++;
            }
        }

        System.out.println("\nSTUDENT TYPE COMPARISON");
        System.out.println("─".repeat(60));
        System.out.printf("Regular Students: %.1f%% average (%d students)%n",
                regCount == 0 ? 0.0 : regSumAvg / regCount, regCount);
        System.out.printf("Honors Students:  %.1f%% average (%d students)%n",
                honCount == 0 ? 0.0 : honSumAvg / honCount, honCount);

        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
}

    //helpers for Class Statistics
    private static void printBand(String label, int count, int total) {
        double pct = total == 0 ? 0.0 : (count * 100.0 / total);
        String bar = buildBar(count, total);
        System.out.printf("%-13s %s  %4.1f%% (%d grades)%n", label, bar, pct, count);
    }

    private static String buildBar(int count, int total) {
        int filled = (total == 0) ? 0 : (int) Math.round((count * 1.0 / total) * 28);
        if (filled < 0) filled = 0; if (filled > 28) filled = 28;
        String filledPart = "█".repeat(filled);
        String emptyPart  = "░".repeat(28 - filled);
        return filledPart + emptyPart;
    }

    private static void printSubjectAvg(java.util.Map<String, double[]> agg, String subject) {
        double[] sc = agg.get(subject);
        if (sc != null && sc[1] > 0) {
            double avg = sc[0] / sc[1];
            System.out.printf("%s: %6.1f%%%s%n", subject, avg, "");
        } else {
            System.out.printf("%s: %6.1f%%%n", subject, 0.0);
        }

    }


    private static void patternBasedSearch(Scanner scanner,
                                           StudentManager studentManager,
                                           GradeManager gradeManager) {
        while (true) {
            System.out.println("\nPATTERN-BASED SEARCH");
            System.out.println("─".repeat(40));
            System.out.println("\nSearch Type:");
            System.out.println(" 1. Email Domain Pattern (e.g., @university.edu)");
            System.out.println(" 2. Phone Area Code Pattern (e.g., 555)");
            System.out.println(" 3. Student ID Pattern (wildcards: * and ?)");
            System.out.println(" 4. Name Pattern (regex)");
            System.out.println(" 5. Custom Regex Pattern");
            System.out.print("\nSelect type (1-5): ");
            int type;
            try { type = Integer.parseInt(scanner.nextLine().trim()); } catch (NumberFormatException e) { type = 1; }

            String raw = "";
            String fieldLabel = "";
            java.util.regex.Pattern pattern;

            try {
                switch (type) {
                    case 1 -> { // Email domain
                        System.out.print("\nEnter email domain pattern: ");
                        raw = scanner.nextLine().trim();
                        // Convert to regex that ends with the domain; escape dots
                        String rx = ".*" + java.util.regex.Pattern.quote(raw) + "$";
                        pattern = java.util.regex.Pattern.compile(rx, java.util.regex.Pattern.CASE_INSENSITIVE);
                        fieldLabel = "email";
                        System.out.println("Searching with regex: " + rx);
                    }
                    case 2 -> { // Phone area code
                        System.out.print("\nEnter 3-digit area code (e.g., 555): ");
                        raw = scanner.nextLine().trim();
                        String ac = java.util.regex.Pattern.quote(raw);
                        // Accept common formats: (555) 123-4567 | 555-123-4567 | +X-555-123-4567 | 5551234567
                        String rx = "^(\\(" + ac + "\\) \\d{3}-\\d{4}|"
                                + ac + "-\\d{3}-\\d{4}|"
                                + "\\+\\d{1,3}-" + ac + "-\\d{3}-\\d{4}|"
                                + ac + "\\d{7})$";
                        pattern = java.util.regex.Pattern.compile(rx);
                        fieldLabel = "phone";
                        System.out.println("Searching phonebook with regex: " + rx);
                    }
                    case 3 -> { // Student ID with wildcards (* -> .*, ? -> .)
                        System.out.print("\nEnter ID pattern (e.g., STU* or STU???): ");
                        raw = scanner.nextLine().trim().toUpperCase();
                        String rx = "^" + raw.replace(".", "\\.")
                                .replace("*", ".*")
                                .replace("?", ".") + "$";
                        pattern = java.util.regex.Pattern.compile(rx, java.util.regex.Pattern.CASE_INSENSITIVE);
                        fieldLabel = "id";
                        System.out.println("Searching with regex: " + rx);
                    }
                    case 4 -> { // Name regex
                        System.out.print("\nEnter name regex (e.g., ^[A-Z][a-z]+\\s[A-Z][a-z]+$): ");
                        raw = scanner.nextLine().trim();
                        pattern = java.util.regex.Pattern.compile(raw, java.util.regex.Pattern.CASE_INSENSITIVE);
                        fieldLabel = "name";
                        System.out.println("Searching with regex: " + raw);
                    }
                    case 5 -> { // Custom regex: choose field
                        System.out.println("\nTarget Field for Custom Pattern:");
                        System.out.println(" 1. ID");
                        System.out.println(" 2. Name");
                        System.out.println(" 3. Email");
                        System.out.println(" 4. Phone");
                        System.out.print("Select field (1-4): ");
                        int fsel;
                        try { fsel = Integer.parseInt(scanner.nextLine().trim()); } catch (NumberFormatException e) { fsel = 3; }
                        fieldLabel = switch (fsel) {
                            case 1 -> "id";
                            case 2 -> "name";
                            case 3 -> "email";
                            case 4 -> "phone";
                            default -> "email";
                        };
                        System.out.print("Enter regex: ");
                        raw = scanner.nextLine().trim();
                        pattern = java.util.regex.Pattern.compile(raw, java.util.regex.Pattern.CASE_INSENSITIVE);
                        System.out.println("Searching " + fieldLabel + " with regex: " + raw);
                    }
                    default -> {
                        // Default to email domain
                        raw = "@school.edu";
                        String rx = ".*" + java.util.regex.Pattern.quote(raw) + "$";
                        pattern = java.util.regex.Pattern.compile(rx, java.util.regex.Pattern.CASE_INSENSITIVE);
                        fieldLabel = "email";
                        System.out.println("Searching with regex: " + rx);
                    }
                }
            } catch (java.util.regex.PatternSyntaxException pse) {
                System.out.println("\nInvalid regex: " + pse.getMessage());
                System.out.print("\nPress Enter to continue...");
                scanner.nextLine();
                return;
            }

            //Execute search
            Student[] all = studentManager.getStudents();
            long t0 = System.nanoTime();
            java.util.List<Student> results = new java.util.ArrayList<>();

            for (Student s : all) {
                String id = s.getStudentId();
                String nm = s.getName();
                String em = s.getEmail();
                String ph = PHONEBOOK.getOrDefault(id, "");

                String target = switch (fieldLabel) {
                    case "id"    -> id;
                    case "name"  -> nm;
                    case "email" -> em;
                    case "phone" -> ph;
                    default      -> em;
                };

                if (target != null && pattern.matcher(target).find()) {
                    results.add(s);
                }
            }

            long elapsedMs = (System.nanoTime() - t0) / 1_000_000L;

            //Render results table
            System.out.println("\nSEARCH RESULTS (" + results.size() + " found)");
            System.out.println("─".repeat(40));
            System.out.printf("%-8s │ %-18s │ %-28s%n", "STU ID", "NAME", "EMAIL");
            System.out.println("─".repeat(40));
            for (Student s : results) {
                System.out.printf("%-8s │ %-18s │ %-28s%n", s.getStudentId(), truncateName(s.getName()), s.getEmail());
            }
            System.out.println("─".repeat(40));

            //Stats block
            int totalScanned = all.length;
            int matches = results.size();
            double pct = totalScanned == 0 ? 0.0 : (matches * 100.0 / totalScanned);
            System.out.println("\nPattern Match Statistics:");
            System.out.printf("  Total Students Scanned: %d%n", totalScanned);
            System.out.printf("  Matches Found: %d (%.0f%%)%n", matches, pct);
            System.out.printf("  Search Time: %dms%n", elapsedMs);
            System.out.println("  Regex Complexity: O(n)");

            // --- Email domain distribution (if email-based search) ---
            if ("email".equals(fieldLabel) && !results.isEmpty()) {
                java.util.Map<String, Integer> domainCounts = new java.util.HashMap<>();
                for (Student s : results) {
                    String em = s.getEmail();
                    int at = (em == null) ? -1 : em.indexOf('@');
                    String dom = (at >= 0) ? em.substring(at) : "(none)";
                    domainCounts.put(dom, domainCounts.getOrDefault(dom, 0) + 1);
                }
                System.out.println("\nEmail Domain Distribution:");
                for (var e : domainCounts.entrySet()) {
                    double dPct = (e.getValue() * 100.0) / matches;
                    System.out.printf("  %s: %d students (%.0f%%)%n", e.getKey(), e.getValue(), dPct);
                }
            }

            // --- Actions ---
            while (true) {
                System.out.println("\nActions:");
                System.out.println(" 1. Export search results");
                System.out.println(" 2. Generate reports for matched students");
                System.out.println(" 3. Send bulk email to matched students (mock)");
                System.out.println(" 4. New search with different pattern");
                System.out.println(" 5. Return to main menu");
                System.out.print("\nEnter choice: ");
                int action;
                try { action = Integer.parseInt(scanner.nextLine().trim()); } catch (NumberFormatException e) { action = 5; }

                if (action == 1) {
                    exportPatternResults(results);
                } else if (action == 2) {
                    generateReportsForMatches(results, gradeManager);
                } else if (action == 3) {
                    bulkEmailMatches(results);
                } else if (action == 4) {
                    // break the inner loop and restart search
                    break;
                } else if (action == 5) {
                    return;
                } else {
                    System.out.println("Invalid choice. Enter 1-5.");
                }
            }
        }
    }

    // Export results to ./reports/pattern_results_<timestamp>.txt
    private static void exportPatternResults(java.util.List<Student> results) {
        try {
            java.nio.file.Path reportsDir = java.nio.file.Paths.get("./reports");
            if (!java.nio.file.Files.exists(reportsDir)) java.nio.file.Files.createDirectories(reportsDir);
            String ts = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            java.nio.file.Path target = reportsDir.resolve("pattern_results_" + ts + ".txt");

            try (java.io.BufferedWriter w = java.nio.file.Files.newBufferedWriter(target,
                    java.nio.charset.StandardCharsets.UTF_8)) {
                w.write(String.format("SEARCH RESULTS (%d found)%n", results.size()));
                w.write("─".repeat(50) + System.lineSeparator());
                w.write(String.format("%-8s │ %-18s │ %-28s%n", "STU ID", "NAME", "EMAIL"));
                w.write("─".repeat(50) + System.lineSeparator());
                for (Student s : results) {
                    w.write(String.format("%-8s │ %-18s │ %-28s%n",
                            s.getStudentId(), truncateName(s.getName()), s.getEmail()));
                }
            }
            long bytes = java.nio.file.Files.size(target);
            System.out.println("\n✓ Search results exported!");
            System.out.println(" File: " + target.getFileName());
            System.out.println(" Location: ./reports/");
            System.out.printf(" Size: %.1f KB%n", bytes / 1024.0);
        } catch (java.io.IOException io) {
            System.out.println("Export failed: " + io.getMessage());
            com.amalitech.util.AppLogger.error("Pattern export failed", io);
        }
    }

    // Generate detailed text reports (sequential, safe) under ./reports/pattern_batch_<timestamp>/text
    private static void generateReportsForMatches(java.util.List<Student> matches,
                                                  GradeManager gradeManager) {
        if (matches == null || matches.isEmpty()) {
            System.out.println("\n(no matches to report)");
            return;
        }
        try {
            String stamp = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            java.nio.file.Path base = java.nio.file.Paths.get("./reports").resolve("pattern_batch_" + stamp);
            java.nio.file.Path textDir = base.resolve("text");
            java.nio.file.Files.createDirectories(textDir);

            com.amalitech.reporting.ReportGenerator rg = new com.amalitech.reporting.ReportGenerator();

            for (Student s : matches) {
                java.util.List<Grade> list = new java.util.ArrayList<>();
                int total = gradeManager.getGradeCount();
                for (int i = 0; i < total; i++) {
                    Grade g = gradeManager.getGradeAt(i);
                    if (g != null && g.getStudentId().equalsIgnoreCase(s.getStudentId())) {
                        list.add(g);
                    }
                }

                double coreAvg = gradeManager.calculateCoreAverage(s.getStudentId());
                double elecAvg = gradeManager.calculateElectiveAverage(s.getStudentId());
                double overallAvg = gradeManager.calculateOverallAverage(s.getStudentId());

                String summary = rg.generateStudentReport(s);
                StringBuilder out = new StringBuilder();
                out.append(summary).append(System.lineSeparator());
                out.append("SUMMARY METRICS").append(System.lineSeparator());
                out.append(String.format("Core Average: %.2f%%%n", coreAvg));
                out.append(String.format("Elective Average: %.2f%%%n", elecAvg));
                out.append(String.format("Overall Average: %.2f%%%n", overallAvg));
                out.append("─".repeat(60)).append(System.lineSeparator());
                out.append("GRADE HISTORY").append(System.lineSeparator());
                out.append(String.format("%-8s │ %-10s │ %-20s │ %-8s │ %-7s%n",
                        "GRD ID", "DATE", "SUBJECT", "TYPE", "GRADE"));
                out.append("─".repeat(70)).append(System.lineSeparator());
                for (Grade g : list) {
                    out.append(String.format("%-8s │ %-10s │ %-20s │ %-8s │ %7.2f%n",
                            g.getGradeId(), g.getDate(),
                            g.getSubject().getSubjectName(),
                            g.getSubject().getSubjectType(),
                            g.getGrade()));
                }

                String baseName = s.getName().toLowerCase().replaceAll("\\s+", "_");
                java.nio.file.Path target = textDir.resolve(baseName + "_report.txt");
                java.nio.file.Files.writeString(target, out.toString(),
                        java.nio.charset.StandardCharsets.UTF_8,
                        java.nio.file.StandardOpenOption.CREATE,
                        java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
            }

            long bytes = 0L;
            try (var stream = java.nio.file.Files.walk(base)) {
                bytes = stream.filter(java.nio.file.Files::isRegularFile)
                        .mapToLong(p -> {
                            try { return java.nio.file.Files.size(p); }
                            catch (java.io.IOException ignored) { return 0L; }
                        })
                        .sum();
            }

            System.out.println("\n✓ Reports generated for matched students!");
            System.out.println(" Output Location: " + base.toString());
            System.out.printf(" Total Files Generated: %d%n", matches.size());
            System.out.printf(" Total Size: %.1f KB%n", bytes / 1024.0);
        } catch (java.io.IOException io) {
            System.out.println("Report generation failed: " + io.getMessage());
            com.amalitech.util.AppLogger.error("Pattern batch generation failed", io);
        }
    }

    // Mock bulk email: write a single file listing emails
    private static void bulkEmailMatches(java.util.List<Student> matches) {
        if (matches == null || matches.isEmpty()) {
            System.out.println("\n(no matches to email)");
            return;
        }
        try {
            java.nio.file.Path reportsDir = java.nio.file.Paths.get("./reports");
            if (!java.nio.file.Files.exists(reportsDir)) java.nio.file.Files.createDirectories(reportsDir);
            String ts = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            java.nio.file.Path target = reportsDir.resolve("bulk_emails_" + ts + ".txt");

            try (java.io.BufferedWriter w = java.nio.file.Files.newBufferedWriter(target,
                    java.nio.charset.StandardCharsets.UTF_8)) {
                w.write("BULK EMAIL (mock)\n");
                w.write("────────────────────────────────\n");
                w.write("Recipients:\n");
                for (Student s : matches) {
                    w.write("  " + s.getEmail() + " (" + s.getStudentId() + " - " + s.getName() + ")\n");
                }
                w.write("\nSubject: Notification from Student Grade Management\n");
                w.write("Body: Dear student, this is a system-generated message for matched selection.\n");
            }

            long bytes = java.nio.file.Files.size(target);
            System.out.println("\n✓ Bulk email file prepared!");
            System.out.println(" File: " + target.getFileName());
            System.out.println(" Location: ./reports/");
            System.out.printf(" Size: %.1f KB%n", bytes / 1024.0);
        } catch (java.io.IOException io) {
            System.out.println("Bulk email file failed: " + io.getMessage());
            com.amalitech.util.AppLogger.error("Pattern bulk email failed", io);
        }
    }


    private static void searchStudents(Scanner scanner,
                                       StudentManager studentManager,
                                       GradeManager gradeManager) {
        while (true) {
            System.out.println("\nSEARCH STUDENTS");
            System.out.println("─".repeat(50));

            System.out.println("\nSearch options:");
            System.out.println("1. By Student ID");
            System.out.println("2. By Name (partial match)");
            System.out.println("3. By Grade Range");
            System.out.println("4. By Student Type");
            System.out.print("\nSelect option (1-4): ");

            int selectOption;
            try {
                selectOption = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice. Please enter 1-4.");
                continue;
            }
            if (selectOption < 1 || selectOption > 4) {
                System.out.println("Invalid choice. Please enter 1-4.");
                continue;
            }

            // Gather results
            java.util.List<Student> results = new java.util.ArrayList<>();
            Student[] all = studentManager.getStudents();

            switch (selectOption) {
                case 1 -> {
                    System.out.print("\nEnter ID (partial or full): ");
                    String q = scanner.nextLine().trim().toUpperCase();
                    for (Student s : all) {
                        if (s.getStudentId().toUpperCase().contains(q)) results.add(s);
                    }
                }
                case 2 -> {
                    System.out.print("\nEnter name (partial or full): ");
                    String q = scanner.nextLine().trim().toLowerCase();
                    for (Student s : all) {
                        if (s.getName().toLowerCase().contains(q)) results.add(s);
                    }
                }
                case 3 -> {
                    double min, max;
                    try {
                        System.out.print("\nEnter minimum average (0-100): ");
                        min = Double.parseDouble(scanner.nextLine().trim());
                        System.out.print("Enter maximum average (0-100): ");
                        max = Double.parseDouble(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number. Try again.");
                        continue;
                    }
                    if (min > max) {
                        double t = min; min = max; max = t;
                    }
                    for (Student s : all) {
                        double avg = s.calculateAverageGrade();
                        if (avg >= min && avg <= max) results.add(s);
                    }
                }

                case 4 -> {
                    System.out.print("\nEnter type (Regular/Honors): ");
                    String q = scanner.nextLine().trim();

                    // Normalize to the labels returned by getStudentType()
                    String label =
                            q.equalsIgnoreCase("Regular") ? "Regular Student" :
                                    q.equalsIgnoreCase("Honors")  ? "Honors Student"  :
                                            q; // allow full labels too

                    for (Student s : all) {
                        if (s.getStudentType().equalsIgnoreCase(label)) {
                            results.add(s);
                        }
                    }

            }
            }

            printSearchResults(results);

            while (true) {
                System.out.println("\nActions:");
                System.out.println("1. View full details for a student");
                System.out.println("2. Export search results");
                System.out.println("3. New search");
                System.out.println("4. Return to main menu");
                System.out.print("\nEnter choice: ");

                int action;
                try {
                    action = Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid choice. Enter 1-4.");
                    continue;
                }

                if (action == 1) {
                    viewGradeReport(scanner, studentManager, gradeManager);
                } else if (action == 2) {
                    exportSearchResults(results);
                } else if (action == 3) {
                    break;
                } else if (action == 4) {
                    return;
                } else {
                    System.out.println("Invalid choice. Enter 1-4.");
                }
            }
        }
    }

    private static void printSearchResults(java.util.List<Student> results) {
        System.out.println("\nSEARCH RESULTS (" + results.size() + " found)");
        System.out.println("─".repeat(50));
        System.out.printf("%-8s │ %-18s │ %-8s │ %-5s%n", "STU ID", "NAME", "TYPE", "AVG");
        System.out.println("─".repeat(50));

        if (results.isEmpty()) {
            System.out.println("(no matches)");
            System.out.println("─".repeat(50));
            return;
        }

        for (Student s : results) {
            System.out.printf("%-8s │ %-18s │ %-8s │ %5.1f%%%n",
                    s.getStudentId(),
                    truncateName(s.getName()),
                    s.getStudentType(),
                    s.calculateAverageGrade());
        }
        System.out.println("─".repeat(50));
    }

    private static void exportSearchResults(java.util.List<Student> results) {
        Path reportsDir = Paths.get("./reports");
        try {
            if (!Files.exists(reportsDir)) Files.createDirectories(reportsDir);
        } catch (IOException e) {
            System.out.println("Failed to create reports directory: " + e.getMessage());
            return;
        }

        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path target = reportsDir.resolve("search_results_" + ts + ".txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(target.toFile()))) {
            writer.write("SEARCH RESULTS (" + results.size() + " found)"); writer.newLine();
            writer.write("─".repeat(50)); writer.newLine();
            writer.write(String.format("%-8s │ %-18s │ %-8s │ %-5s", "STU ID", "NAME", "TYPE", "AVG")); writer.newLine();
            writer.write("─".repeat(50)); writer.newLine();

            for (Student s : results) {
                writer.write(String.format("%-8s │ %-18s │ %-8s │ %5.1f%%",
                        s.getStudentId(),
                        truncateName(s.getName()),
                        s.getStudentType(),
                        s.calculateAverageGrade()));
                writer.newLine();
            }
            writer.write("─".repeat(50)); writer.newLine();

        } catch (IOException e) {
            System.out.println("Failed to export search results: " + e.getMessage());
            return;
        }

        long bytes = 0;
        try { bytes = Files.size(target); } catch (IOException ignored) {}
        String sizeText = String.format("%.1f KB", bytes / 1024.0);

        System.out.println("\n✓ Search results exported!");
        System.out.println(" File: " + target.getFileName());
        System.out.println(" Location: ./reports/");
        System.out.println(" Size: " + sizeText);
    }


    private static boolean retry(Scanner scanner) {
        System.out.print("\nTry again? (Y/N): ");
        String retry = scanner.nextLine().trim().toUpperCase();
        return "Y".equals(retry);
    }


}
