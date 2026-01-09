
package com.amalitech.menu.actions;

import com.amalitech.Student;
import com.amalitech.app.AppContext;
import com.amalitech.menu.MenuAction;
import com.amalitech.util.ErrorHandler;

public class AddStudentAction implements MenuAction {
    private final AppContext ctx;
    public AddStudentAction(AppContext ctx) { this.ctx = ctx; }

    @Override public String label() { return "Add Student (validated)"; }

    @Override public void execute() {
        try {
            System.out.print("Enter Student ID (e.g., STU001): ");
            String id = ctx.scanner.nextLine();
            System.out.print("Enter Name: ");
            String name = ctx.scanner.nextLine();
            System.out.print("Enter Email: ");
            String email = ctx.scanner.nextLine();
            System.out.print("Enter Phone: ");
            String phone = ctx.scanner.nextLine();
            System.out.print("Type (1=Regular, 2=Honors): ");
            int t = parseIntSafe(ctx.scanner.nextLine(), 1);

            Student s = ctx.students.addStudent(id, name, email, phone, t == 2);
            System.out.println("\n✓ Student added: " + s.getStudentId() + " - " + s.getName());
        } catch (Exception ex) {
            ErrorHandler.handle("Add Student", ex);
        }
    }
    private int parseIntSafe(String s,int def){ try{ return Integer.parseInt(s.trim()); } catch(Exception e){ return def; } }
}
