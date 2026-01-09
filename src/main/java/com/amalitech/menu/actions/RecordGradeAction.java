
package com.amalitech.menu.actions;

import com.amalitech.*;
import com.amalitech.app.AppContext;
import com.amalitech.menu.MenuAction;
import com.amalitech.util.ErrorHandler;

public class RecordGradeAction implements MenuAction {
    private final AppContext ctx;
    public RecordGradeAction(AppContext ctx) { this.ctx = ctx; }

    @Override public String label() { return "Record Grade"; }

    @Override public void execute() {
        try {
            System.out.print("Enter Student ID: ");
            String sid = ctx.scanner.nextLine().trim().toUpperCase();
            Student student = ctx.students.find(sid);
            if (student == null) { System.out.println("Student not found."); return; }

            System.out.println("\nSubject type:\n 1. Core (Mathematics, English, Science)\n 2. Elective (Music, Art, Physical Education)");
            int st = parseIntSafe(ctx.scanner.nextLine(), 1);

            String[] core = {"Mathematics", "English", "Science"};
            String[] elec = {"Music", "Art", "Physical Education"};
            Subject subject;

            if (st == 1) {
                System.out.print("Select Core (1-3): ");
                int i = parseIntSafe(ctx.scanner.nextLine(), 1);
                if (i < 1 || i > 3) { System.out.println("Invalid choice."); return; }
                subject = new CoreSubject(core[i-1], "C"+(int)(Math.random()*1000));
            } else {
                System.out.print("Select Elective (1-3): ");
                int i = parseIntSafe(ctx.scanner.nextLine(), 1);
                if (i < 1 || i > 3) { System.out.println("Invalid choice."); return; }
                subject = new ElectiveSubject(elec[i-1], "E"+(int)(Math.random()*1000));
            }

            System.out.print("Enter grade (0-100): ");
            double g = Double.parseDouble(ctx.scanner.nextLine().trim());

            Grade saved = ctx.grades.recordGrade(student, subject, g);
            System.out.println("\n✓ Grade recorded: " + saved.getGradeId() + " / " + saved.getGrade() + "%");
        } catch (Exception ex) {
            ErrorHandler.handle("Record Grade", ex);
        }
    }
    private int parseIntSafe(String s,int def){ try{ return Integer.parseInt(s.trim()); } catch(Exception e){ return def; } }
}
