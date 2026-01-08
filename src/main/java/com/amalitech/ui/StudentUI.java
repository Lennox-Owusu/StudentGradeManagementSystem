
package com.amalitech.ui;

import com.amalitech.manager.StudentManager;
import com.amalitech.model.HonorsStudent;
import com.amalitech.model.RegularStudent;
import com.amalitech.model.Student;
import com.amalitech.util.ErrorHandler;
import com.amalitech.util.Validators;
import java.util.Scanner;

public class StudentUI {
    public static void addStudent(Scanner scanner, StudentManager studentManager) {
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
        if (type == 1) student = new RegularStudent(name, age, email, phone);
        else if (type == 2) student = new HonorsStudent(name, age, email, phone);
        else { System.out.println("Invalid type selected."); return; }

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

    public static void viewStudents(StudentManager studentManager) {
        if (studentManager.getStudentCount() == 0) {
            System.out.println("No students to display yet.");
            return;
        }
        System.out.println("\nSTUDENT LISTING");
        System.out.println("─".repeat(80));
        System.out.printf("%-8s \n %-18s \n %-8s \n %-10s \n %-8s%n",
                "STU ID", "NAME", "TYPE", "AVG GRADE", "STATUS");
        System.out.println("─".repeat(80));
        for (Student s : studentManager.getStudents()) {
            System.out.printf("%-8s │ %-18s │ %-8s │ %-10.2f │ %-8s%n",
                    s.getStudentId(), truncateName(s.getName()), s.getStudentType(),
                    s.calculateAverageGrade(), (s.isPassing() ? "Passing" : "Failing"));
            if ("Honors Student".equals(s.getStudentType())) {
                boolean eligible = (s instanceof HonorsStudent) && ((HonorsStudent) s).checkHonorsEligibility();
                System.out.printf(" Enrolled Subjects: %d \n Passing Grade: %.0f%% \n Honors Eligible: %s%n",
                        s.getEnrolledSubjectCount(), s.getPassingGrade(), (eligible ? "Yes" : "No"));
            } else {
                System.out.printf(" Enrolled Subjects: %d \n Passing Grade: %.0f%%%n",
                        s.getEnrolledSubjectCount(), s.getPassingGrade());
            }
            System.out.println("─".repeat(80));
        }
        System.out.println("Total Students: " + studentManager.getStudentCount());
        System.out.printf("Average Class Grade: %.2f%n", studentManager.getAverageClassGrade());
        System.out.print("\nPress Enter to continue...");
        new Scanner(System.in).nextLine();
    }

    private static String truncateName(String name) {
        return name.length() > 18 ? name.substring(0, 18 - 3) + "..." : name;
    }
}
