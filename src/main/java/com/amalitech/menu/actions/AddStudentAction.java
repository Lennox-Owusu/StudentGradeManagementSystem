
package com.amalitech.menu.actions;

import com.amalitech.base.Student;
import com.amalitech.app.AppContext;
import com.amalitech.menu.MenuAction;
import com.amalitech.util.ErrorHandler;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class AddStudentAction implements MenuAction {
    private final AppContext ctx;

    public AddStudentAction(AppContext ctx) { this.ctx = ctx; }

    @Override
    public String label() { return "Add Student"; }

    @Override
    public void execute() {
        try {
            System.out.println();
            System.out.println("ADD STUDENT (with validation)");
            System.out.println("--------------------------------");

            // 1) Student ID: STU###
            String id = promptStudentId();

            // 2) Name: letters + spaces/hyphens/apostrophes
            String name = promptName();

            // 3) Email: username@domain.extension
            String email = promptEmail();

            // 4) Phone:
            String phone = promptPhone();

            // 5) Type: 1=Regular, 2=Honors
            boolean isHonors = promptType();

            // 6) Enrollment Date: YYYY-MM-DD
            String enrollDate = promptEnrollmentDate();

            // Persist student
            Student s = ctx.students.addStudent(id, name, email, phone, isHonors);

            // Final success block (exact wording & order)
            System.out.println();
            System.out.println("✓ Student added successfully!");
            System.out.println("All inputs validated with regex patterns");
            System.out.println(" Student ID: " + s.getStudentId());
            System.out.println(" Name: " + s.getName());
            System.out.println(" Email: " + s.getEmail());
            System.out.println(" Phone: " + s.getPhone());
            System.out.println(" Type: " + (isHonors ? "Honors Student" : "Regular Student"));
            System.out.println(" Enrolled: " + enrollDate);


        } catch (Exception ex) {
            ErrorHandler.handle("Add Student", ex);
        }
    }

    //Prompts with exact validation messages

    private String promptStudentId() {
        Pattern p = Pattern.compile("^STU\\d{3}$");
        while (true) {
            System.out.print("\nEnter Student ID: ");
            String input = ctx.scanner.nextLine().trim();
            if (p.matcher(input).matches()) {
                System.out.println("✓ Valid Student ID");
                return input;
            }
            System.out.println("X VALIDATION ERROR: Invalid Student ID format");
            System.out.println("  Pattern required: STU### (STU followed by exactly 3 digits)");
            System.out.println("  Examples: STU001, STU042, STU999");
            System.out.println("  Your input: " + input);
        }
    }

    private String promptName() {
        // Only letters + spaces/hyphens/apostrophes between words (e.g., Mary-Jane O'Connor)
        Pattern p = Pattern.compile("^[A-Za-z]+([ \\-'][A-Za-z]+)*$");
        while (true) {
            System.out.print("\nEnter Student Name: ");
            String input = ctx.scanner.nextLine().trim();
            if (p.matcher(input).matches()) {
                System.out.println("✓ Valid Student Name");
                return input;
            }
            String reason = containsDigit(input) ? " (contains digits)" : "";
            System.out.println("X VALIDATION ERROR: Invalid name format");
            System.out.println("  Pattern required: Only letters, spaces, hyphens, and apostrophes");
            System.out.println("  Examples: John Smith, Mary-Jane O'Connor");
            System.out.println("  Your input: " + input + reason);
        }
    }

    private String promptEmail() {
        Pattern p = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        while (true) {
            System.out.print("\nEnter Email Address: ");
            String input = ctx.scanner.nextLine().trim();
            if (p.matcher(input).matches()) {
                System.out.println("✓ Valid Email Address");
                return input;
            }
            String reason = "";
            if (!input.contains("@")) reason = " (missing '@')";
            else if (!input.contains(".")) reason = " (missing valid extension)";
            System.out.println("X VALIDATION ERROR: Invalid email format");
            System.out.println("  Pattern required: username@domain.extension");
            System.out.println("  Examples: john.smith@university.edu, jsmith@college.org");
            System.out.println("  Your input: " + input + reason);
        }
    }

    private String promptPhone() {
        // Accepted patterns
        // (123) 456-7890 | 123-456-7890 | +1-123-456-7890 | 1234567890
        Pattern a = Pattern.compile("^\\(\\d{3}\\) \\d{3}-\\d{4}$");       // (123) 456-7890
        Pattern b = Pattern.compile("^\\d{3}-\\d{3}-\\d{4}$");             // 123-456-7890
        Pattern c = Pattern.compile("^\\+\\d+-\\d{3}-\\d{3}-\\d{4}$");     // +1-123-456-7890
        Pattern d = Pattern.compile("^\\d{10}$");                          // 1234567890
        while (true) {
            System.out.print("\nEnter Phone Number: ");
            String input = ctx.scanner.nextLine().trim();
            if (a.matcher(input).matches() || b.matcher(input).matches()
                    || c.matcher(input).matches() || d.matcher(input).matches()) {
                System.out.println("✓ Valid Phone Number");
                return input;
            }
            String reason = inferPhoneReason(input);
            System.out.println("X VALIDATION ERROR: Invalid phone format");
            System.out.println("  Accepted patterns:");
            System.out.println("  - (123) 456-7890");
            System.out.println("  - 123-456-7890");
            System.out.println("  - +1-123-456-7890");
            System.out.println("  - 1234567890");
            System.out.println("  Your input: " + input + reason);
        }
    }

    private boolean promptType() {
        System.out.println("\nStudent Type:");
        System.out.println("1. Regular Student");
        System.out.println("2. Honors Student");
        while (true) {
            System.out.print("\nSelect type (1-2): ");
            int t = parseIntSafe(ctx.scanner.nextLine(), 1);
            if (t == 1 || t == 2) return (t == 2);
            System.out.println("Invalid choice. Enter 1 or 2.");
        }
    }

    private String promptEnrollmentDate() {
        Pattern p = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$"); // YYYY-MM-DD only
        while (true) {
            System.out.print("\nEnter Enrollment Date (YYYY-MM-DD): ");
            String input = ctx.scanner.nextLine().trim();
            if (p.matcher(input).matches() && isValidDate(input)) {
                System.out.println("\n✓ Valid Enrollment Date");
                return input;
            }
            String reason = input.contains("/") ? " (wrong separators)" : "";
            System.out.println("\nX VALIDATION ERROR: Invalid date format");
            System.out.println("  Pattern required: YYYY-MM-DD");
            System.out.println("  Example: 2024-11-03");
            System.out.println("  Your input: " + input + reason);
        }
    }

    // Helpers -

    private static boolean containsDigit(String s) {
        for (int i = 0; i < s.length(); i++) if (Character.isDigit(s.charAt(i))) return true;
        return false;
    }

    private static String inferPhoneReason(String input) {
        // Example friendly hint for "555-0123"
        if (input.matches("^\\d{3}-\\d{4}$")) return " (missing area code)";
        if (input.contains("(") || input.contains(")")) {
            if (!input.matches("^\\(\\d{3}\\) \\d{3}-\\d{4}$")) return " (check spaces and dashes)";
        }
        return "";
    }

    private static boolean isValidDate(String yyyyMmDd) {
        try { LocalDate.parse(yyyyMmDd); return true; }
        catch (DateTimeParseException e) { return false; }
    }

    private static int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }
}
