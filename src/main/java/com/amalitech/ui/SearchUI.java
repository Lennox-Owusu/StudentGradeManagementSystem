
package com.amalitech.ui;

import com.amalitech.manager.GradeManager;
import com.amalitech.manager.StudentManager;
import com.amalitech.model.Student;
import com.amalitech.service.api.IReportService;
import com.amalitech.service.api.IExportService;
import com.amalitech.service.ExportResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SearchUI {

    public static void searchStudents(Scanner scanner,
                                      StudentManager studentManager,
                                      GradeManager gradeManager,
                                      IReportService reportService,
                                      IExportService exportService) {

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
            try { selectOption = Integer.parseInt(scanner.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Invalid choice. Please enter 1-4."); continue; }
            if (selectOption < 1 || selectOption > 4) { System.out.println("Invalid choice. Please enter 1-4."); continue; }

            List<Student> results = new ArrayList<>();
            Student[] all = studentManager.getStudents();
            switch (selectOption) {
                case 1 -> {
                    System.out.print("\nEnter ID (partial or full): ");
                    String q = scanner.nextLine().trim().toUpperCase();
                    for (Student s : all) if (s.getStudentId().toUpperCase().contains(q)) results.add(s);
                }
                case 2 -> {
                    System.out.print("\nEnter name (partial or full): ");
                    String q = scanner.nextLine().trim().toLowerCase();
                    for (Student s : all) if (s.getName().toLowerCase().contains(q)) results.add(s);
                }
                case 3 -> {
                    double min, max;
                    try {
                        System.out.print("\nEnter minimum average (0-100): ");
                        min = Double.parseDouble(scanner.nextLine().trim());
                        System.out.print("Enter maximum average (0-100): ");
                        max = Double.parseDouble(scanner.nextLine().trim());
                    } catch (NumberFormatException e) { System.out.println("Invalid number. Try again."); continue; }
                    if (min > max) { double t = min; min = max; max = t; }
                    for (Student s : all) {
                        double avg = s.calculateAverageGrade();
                        if (avg >= min && avg <= max) results.add(s);
                    }
                }
                case 4 -> {
                    System.out.print("\nEnter type (Regular/Honors): ");
                    String q = scanner.nextLine().trim();
                    String label = q.equalsIgnoreCase("Regular") ? "Regular Student" :
                            q.equalsIgnoreCase("Honors") ? "Honors Student" : q;
                    for (Student s : all) if (s.getStudentType().equalsIgnoreCase(label)) results.add(s);
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
                try { action = Integer.parseInt(scanner.nextLine().trim()); }
                catch (NumberFormatException e) { System.out.println("Invalid choice. Enter 1-4."); continue; }

                if (action == 1) {
                    GradeUI.viewGradeReport(scanner, studentManager, gradeManager, reportService);
                } else if (action == 2) {
                    String content = reportService.buildSearchResultsReport(results);
                    String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                    ExportResult result = exportService.exportReport(content, "search_results_" + ts);
                    if (result.isSuccess()) {
                        System.out.println("\n✓ Search results exported!");
                        System.out.println(" File: " + result.getFilePath().getFileName());
                        System.out.println(" Location: ./reports/");
                        System.out.println(" Size: " + result.getSizeText());
                    } else {
                        System.out.println("\n✗ Export failed: " + result.getMessage());
                    }
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

    private static void printSearchResults(List<Student> results) {
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
            String name = s.getName().length() > 18 ? s.getName().substring(0, 15) + "..." : s.getName();
            System.out.printf("%-8s │ %-18s │ %-8s │ %5.1f%%%n",
                    s.getStudentId(), name, s.getStudentType(), s.calculateAverageGrade());
        }
        System.out.println("─".repeat(50));
    }
}
