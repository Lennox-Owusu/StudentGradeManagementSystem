
package com.amalitech.menu.actions;

import com.amalitech.menu.AppContext;
import com.amalitech.menu.MenuAction;
import com.amalitech.model.Student;

import java.util.Scanner;

public class ExportGradeReportAction implements MenuAction {
    @Override public String label() { return "Export Grade Report"; }
    @Override public void execute(Scanner scanner, AppContext ctx) {
        System.out.println("\nEXPORT GRADE REPORT");
        System.out.println("─".repeat(50));
        System.out.print("Enter Student ID: ");
        String studentId = scanner.nextLine().trim().toUpperCase();
        Student student = ctx.studentManager.findStudent(studentId);
        if (student == null) { System.out.println("Student not found!"); System.out.print("\nPress Enter to continue..."); scanner.nextLine(); return; }

        System.out.println("\nExport options:");
        System.out.println("1. Summary Report");
        System.out.println("2. Detailed Report");
        System.out.println("3. Both");
        System.out.print("\nSelect option (1-3): ");
        int exportChoice = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("\nEnter filename (without extension): ");
        String baseName = scanner.nextLine().trim();
        if (baseName.isEmpty()) {
            baseName = student.getName().toLowerCase().replaceAll("\\s+", "_") + "_report";
        }

        String content = ctx.reportService.buildReport(student, exportChoice);
        var result = ctx.exportService.exportReport(content, baseName);

        System.out.println();
        if (result.isSuccess()) {
            System.out.println("✓ Report exported successfully!");
            System.out.println(" File: " + result.getFilePath().getFileName());
            System.out.println(" Location: ./reports/");
            System.out.println(" Size: " + result.getSizeText());
        } else {
            System.out.println("✗ Export failed: " + result.getMessage());
        }
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
}
