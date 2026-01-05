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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Main {


    // --- Advanced Edition v3.0 concurrency ---
    private static ExecutorService fixedPool;
    private static ExecutorService cachedPool;
    private static ScheduledExecutorService scheduler;
    private static volatile boolean RUNNING = true;


    private static final StatisticsCalculator STATS = new StatisticsCalculator();
    //
    private static String safeTrim(String s) {
        return (s == null) ? "" : s.trim();
    }


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

                case 5 -> { /* Export CSV/JSON/Binary — will implement next steps */
                    System.out.println("Export (multi-format) — coming up in next step.");
                }
                case 6 -> { /* Enhanced multi-format import */
                    System.out.println("Import (multi-format) — coming up in next step.");
                }
                case 7 -> bulkImportGrades(scanner, studentManager, gradeManager);

                case 8 -> calculateStudentGPA(scanner, studentManager, gradeManager);
                case 9 -> viewClassStatistics(scanner, studentManager, gradeManager);
                case 10 -> { /* Real-time dashboard (NEW) */
                    System.out.println("Real-Time Statistics Dashboard — to be implemented.");
                }
                case 11 -> { /* Batch reports (NEW) */
                    System.out.println("Generate Batch Reports — to be implemented.");
                }

                case 12 -> { /* Advanced search (ENHANCED) */
                    searchStudents(scanner, studentManager, gradeManager);
                }
                case 13 -> { /* Pattern-based search (NEW) */
                    System.out.println("Pattern-Based Search — to be implemented.");
                }
                case 14 -> { /* Query grade history (NEW) */
                    System.out.println("Query Grade History — to be implemented.");
                }

                case 15 -> { /* Scheduled automated tasks (NEW) */
                    System.out.println("Schedule Automated Tasks — to be implemented.");
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
        studentManager.addStudent(new RegularStudent("Alice Johnson", 16, "alice@school.edu", "0241108345"));
        studentManager.addStudent(new HonorsStudent("Bob Smith", 17, "bob@school.edu", "0256521345"));
        studentManager.addStudent(new RegularStudent("Carol Martinez", 15, "carol@school.edu", "0545678345"));
        studentManager.addStudent(new HonorsStudent("David Chen", 18, "david@school.edu", "0536789435"));
        studentManager.addStudent(new RegularStudent("Emma Wilson", 16, "emma@school.edu", "0237896072"));
    }

    private static void addStudent(Scanner scanner, StudentManager studentManager) {
        System.out.println("\nADD STUDENT");
        System.out.println("─".repeat(40));

        String name;
        while (true) {
            System.out.print("Enter student name: ");
            String input = scanner.nextLine();
            try {
                name = Validators.requireNotBlank("Student name", input);
                break;
            } catch (IllegalArgumentException iae) {
                ErrorHandler.handle("Add Student > name", iae);
            }
        }

        System.out.print("Enter student age: ");
        int age = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter student email: ");
        String email = scanner.nextLine();
        System.out.print("Enter student phone: ");
        String phone = scanner.nextLine();
        System.out.println("\nStudent type:");
        System.out.println("1. Regular Student (Passing grade: 50%): ");
        System.out.println("2. Honors Student (Passing grade: 60%, honors recognition): ");
        System.out.print("Select type (1-2):");
        int type = Integer.parseInt(scanner.nextLine());
        Student student;
        if (type == 1) {
            student = new RegularStudent(name, age, email, phone);
        } else if (type == 2) {
            student = new HonorsStudent(name, age, email, phone);
        } else {
            System.out.println("Invalid type selected.");
            return;
        }
        studentManager.addStudent(student);
        System.out.println("\nStudent added successfully!");
        System.out.println("Student ID: " + student.getStudentId());
        System.out.println("Name: " + student.getName());
        System.out.println("Type: " + student.getStudentType());
        System.out.println("Age: " + student.getAge());
        System.out.println("Email: " + student.getEmail());
        System.out.printf("Passing Grade: %.0f%%%n", student.getPassingGrade());
        if (student instanceof HonorsStudent) {
            boolean eligible = ((HonorsStudent) student).checkHonorsEligibility();
            System.out.println("Honors Eligible: " + (eligible ? "Yes" : "No"));
        }
        System.out.println("Status: Active");
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
        // 1) Real-time stats updater (periodic)
        scheduler.scheduleAtFixedRate(() -> {
            BackgroundTaskTracker.incrementActive();
            BackgroundTaskTracker.setStatsUpdating(true);
            try {
                // Compute something lightweight to simulate live stats refresh
                double classAvg = studentManager.getAverageClassGrade();
                com.amalitech.util.AppLogger.info(String.format("Stats refresh: classAvg=%.2f", classAvg));
            } catch (Exception e) {
                com.amalitech.util.AppLogger.error("Stats updater failed", e);
            } finally {
                BackgroundTaskTracker.setStatsUpdating(false);
                BackgroundTaskTracker.decrementActive();
            }
        }, 0, 3, TimeUnit.SECONDS);

        // 2) Long-running background worker (simulates audit writer)
        cachedPool.submit(() -> {
            BackgroundTaskTracker.incrementActive();
            try {
                while (RUNNING) {
                    // Simulate periodic work
                    Thread.sleep(4000);
                }
            } catch (InterruptedException ignored) {
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
            gradeManager.addGrade(grade);
            boolean ok = student.recordGrade(gradeValue);
            if (!ok) {
                System.out.println("Grade could not be recorded (invalid or full).");
            } else {
                student.enrollSubject(subject);
                System.out.println("\n✓ Grade recorded successfully!");
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


    private static void exportGradeReport(Scanner scanner, StudentManager studentManager, GradeManager gradeManager) {
        System.out.println("\nEXPORT GRADE REPORT");
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

        //Gather grades for the student
        java.util.ArrayList<Grade> list = new java.util.ArrayList<>();
        for (int i = 0; i < gradeManager.getGradeCount(); i++) {
            Grade g = gradeManager.getGradeAt(i);
            if (g != null && g.getStudentId().equalsIgnoreCase(studentId)) {
                list.add(g);
            }
        }

        System.out.println();
        System.out.println("Student: " + student.getStudentId() + " - " + student.getName());
        System.out.println("Type: " + student.getStudentType());
        System.out.println("Total Grades: " + list.size());
        System.out.println();

        if (list.isEmpty()) {
            System.out.println("NOTE: No grades recorded for this student yet.");
            System.out.println();
        }

        System.out.println("Export options:");
        System.out.println("1. Summary Report (overview only)");
        System.out.println("2. Detailed Report (all grades)");
        System.out.println("3. Both");
        System.out.print("\nSelect option (1-3): ");
        int exportChoice;
        try {
            exportChoice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid choice.");
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        if (exportChoice < 1 || exportChoice > 3) {
            System.out.println("Invalid choice.");
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        System.out.print("\nEnter filename (without extension): ");
        String baseName = scanner.nextLine().trim();
        if (baseName.isEmpty()) baseName = student.getName().toLowerCase().replaceAll("\\s+", "_") + "_report";

        Path reportsDir = Paths.get("./reports");
        try {
            if (!Files.exists(reportsDir)) Files.createDirectories(reportsDir);
        } catch (IOException e) {
            System.out.println("Failed to create reports directory: " + e.getMessage());
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        String filename = baseName + ".txt";
        Path target = reportsDir.resolve(filename);

        // 7) Compute aggregates
        double coreAvg = gradeManager.calculateCoreAverage(studentId);
        double elecAvg = gradeManager.calculateElectiveAverage(studentId);
        double overallAvg = gradeManager.calculateOverallAverage(studentId);
        boolean passing = student.isPassing();
        boolean honorsEligible = (student instanceof HonorsStudent) && ((HonorsStudent) student).checkHonorsEligibility();


        StringBuilder out = new StringBuilder();

// Header
        out.append("╔").append("═".repeat(50)).append("╗").append(System.lineSeparator());
        out.append("║ EXPORT GRADE REPORT").append(System.lineSeparator());
        out.append("╚").append("═".repeat(50)).append("╝").append(System.lineSeparator()).append(System.lineSeparator());

// Summary generated by ReportGenerator (includes Average/GPA/Passing)
        out.append(REPORT_GENERATOR.generateStudentReport(student)).append(System.lineSeparator());

// Your additional summary metrics
        out.append("SUMMARY METRICS").append(System.lineSeparator());
        out.append(String.format("Total Grades: %d%n", list.size()));
        out.append(String.format("Core Subjects Average: %.2f%%%n", coreAvg));
        out.append(String.format("Elective Subjects Average: %.2f%%%n", elecAvg));
        out.append(String.format("Overall Average: %.2f%%%n", overallAvg));
        out.append("─".repeat(70)).append(System.lineSeparator());

// Detailed table if chosen
        if (exportChoice == 2 || exportChoice == 3) {
            out.append("DETAILED REPORT (All Grades)").append(System.lineSeparator());
            out.append(String.format("%-8s │ %-10s │ %-20s │ %-8s │ %-7s%n",
                    "GRD ID", "DATE", "SUBJECT", "TYPE", "GRADE"));
            out.append("─".repeat(70)).append(System.lineSeparator());
            for (int i = list.size() - 1; i >= 0; i--) {
                Grade g = list.get(i);
                out.append(String.format("%-8s │ %-10s │ %-20s │ %-8s │ %-7.2f%n",
                        g.getGradeId(), g.getDate(), g.getSubject().getSubjectName(),
                        g.getSubject().getSubjectType(), g.getGrade()));
            }
            out.append("─".repeat(70)).append(System.lineSeparator());
            out.append(String.format("Total Grades: %d%n", list.size()));
        }

// Export via FileExporter

        try {
            FILE_EXPORTER.exportToFile(out.toString(), target);
        } catch (com.amalitech.exceptions.ExportFailedException e) {
            System.out.println("\n✗ ERROR: ExportFailedException");
            System.out.println("  " + e.getMessage());
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }



        //show number of grades recorded
        long bytes = 0L;
        try {
            bytes = Files.size(target);
        } catch (IOException ignored) {
        }

        String sizeText = String.format("%.1f KB", bytes / 1024.0);
        int gradeCount = list.size();

        System.out.println("\n✓ Report exported successfully!");
        System.out.println("  File: " + target.getFileName());
        System.out.println("  Location: ./reports/");
        System.out.println("  Size: " + sizeText);

        if (exportChoice == 1) {
            System.out.println("  Contains: overview only, averages, performance summary");
        } else {
            System.out.println("  Contains: " + gradeCount + " grades, averages, performance summary");
        }

        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();

        //log normal operation for successful exported report
        AppLogger.info("Exported grade report for student " + studentId);
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
