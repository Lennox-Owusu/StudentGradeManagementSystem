
package com.amalitech.ui;

import com.amalitech.manager.GradeManager;
import com.amalitech.manager.StudentManager;
import com.amalitech.model.*;
import com.amalitech.service.api.IReportService;
import com.amalitech.service.api.IGpaService;
import com.amalitech.service.GPAData;
import com.amalitech.util.ErrorHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GradeUI {

    public static void recordGrade(Scanner scanner, StudentManager studentManager, GradeManager gradeManager) {
        System.out.println("\nRECORD GRADE");
        System.out.println("─".repeat(40));

        Student student;
        Subject subject;

        while (true) {
            System.out.print("\nEnter Student ID: ");
            String studentId = scanner.nextLine().trim().toUpperCase();
            try {
                student = studentManager.findStudent(studentId);
                if (student == null) throw new com.amalitech.exceptions.StudentNotFoundException(studentId);
                System.out.println("\nStudent Details:");
                System.out.println("Name: " + student.getName());
                System.out.println("Type: " + student.getStudentType());
                System.out.printf("Current Average: %.2f%n", student.calculateAverageGrade());
                break;
            } catch (com.amalitech.exceptions.StudentNotFoundException snfe) {
                System.out.println("\n✗ ERROR: StudentNotFoundException");
                System.out.println(" " + snfe.getMessage());
                System.out.println();
                Student[] all = studentManager.getStudents();
                StringBuilder ids = new StringBuilder(" Available student IDs: ");
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
        try { subjectType = Integer.parseInt(scanner.nextLine()); }
        catch (NumberFormatException e) { System.out.println("Invalid subject type."); System.out.print("\nPress Enter to continue..."); scanner.nextLine(); return; }

        String[] coreSubjects = {"Mathematics", "English", "Science"};
        String[] electiveSubjects = {"Music", "Art", "Physical Education"};

        if (subjectType == 1) {
            System.out.println("\nAvailable Core Subjects:");
            for (int i = 0; i < coreSubjects.length; i++) System.out.println((i + 1) + ". " + coreSubjects[i]);
            System.out.print("\nSelect subject (1-3): ");
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice >= 1 && choice <= 3)
                subject = new CoreSubject(coreSubjects[choice - 1], "C" + (int) (Math.random() * 1000));
            else { System.out.println("Invalid choice."); System.out.print("\nPress Enter to continue..."); scanner.nextLine(); return; }
        } else if (subjectType == 2) {
            System.out.println("\nAvailable Elective Subjects:");
            for (int i = 0; i < electiveSubjects.length; i++) System.out.println((i + 1) + ". " + electiveSubjects[i]);
            System.out.print("\nSelect subject (1-3): ");
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice >= 1 && choice <= 3)
                subject = new ElectiveSubject(electiveSubjects[choice - 1], "E" + (int) (Math.random() * 1000));
            else { System.out.println("Invalid choice."); System.out.print("\nPress Enter to continue..."); scanner.nextLine(); return; }
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
                gradeValue = g; break;
            } catch (NumberFormatException nfe) {
                ErrorHandler.handle("Record Grade > parse number", nfe);
                if (!retry(scanner)) return;
            } catch (com.amalitech.exceptions.InvalidGradeException ige) {
                ErrorHandler.handle("Record Grade > range check", ige);
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
            if (!ok) { System.out.println("Grade could not be recorded (invalid or full)."); }
            else { student.enrollSubject(subject); System.out.println("\n✓ Grade recorded successfully!"); }
        } else {
            System.out.println("\nGrade entry canceled.");
        }
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public static void viewGradeReport(Scanner scanner, StudentManager studentManager,
                                       GradeManager gradeManager, IReportService reportService) {
        System.out.println("\nVIEW GRADE REPORT");
        System.out.println("─".repeat(50));
        System.out.print("Enter Student ID: ");
        String studentId = scanner.nextLine().trim().toUpperCase();

        Student student = studentManager.findStudent(studentId);
        if (student == null) {
            System.out.println("Student not found!");
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        String view = reportService.buildViewReport(student);
        System.out.println(view);
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public static void calculateGPA(Scanner scanner, StudentManager studentManager,
                                    GradeManager gradeManager, IGpaService gpaService) {
        System.out.println("\nCALCULATE STUDENT GPA");
        System.out.println("─".repeat(50));
        System.out.print("Enter Student ID: ");
        String studentId = scanner.nextLine().trim().toUpperCase();

        GPAData data = gpaService.computeFor(studentId);
        String output = gpaService.toConsoleString(data);
        System.out.println(output);

        if (data.found && data.hasGrades) {
            boolean honorsEligible = (data.student instanceof HonorsStudent) && ((HonorsStudent) data.student).checkHonorsEligibility();
            double classAvgGpa = computeClassAvgGpa(studentManager, gradeManager);
            System.out.println((data.cumulative >= 3.5 ? "✓" : "•") + " Excellent performance (3.5+ GPA)");
            System.out.println((honorsEligible ? "✓" : "•") + " Honors eligibility maintained");
            System.out.printf((data.cumulative >= classAvgGpa ? "✓" : "•") + " Above class average (%.2f GPA)%n", classAvgGpa);
        }
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static double computeClassAvgGpa(StudentManager studentManager, GradeManager gradeManager) {
        int totalStudents = studentManager.getStudentCount();
        if (totalStudents == 0) return 0.0;
        double sum = 0.0;
        for (Student s : studentManager.getStudents()) {
            List<Double> pct = new ArrayList<>();
            for (int j = 0; j < gradeManager.getGradeCount(); j++) {
                Grade g = gradeManager.getGradeAt(j);
                if (g != null && g.getStudentId().equalsIgnoreCase(s.getStudentId())) {
                    pct.add(g.getGrade());
                }
            }
            sum += pct.isEmpty() ? 0.0 : new com.amalitech.reporting.GPACalculator().computeGPA(pct);
        }
        return sum / totalStudents;
    }

    private static boolean retry(Scanner scanner) {
        System.out.print("\nTry again? (Y/N): ");
        String retry = scanner.nextLine().trim().toUpperCase();
        return "Y".equals(retry);
    }
}
