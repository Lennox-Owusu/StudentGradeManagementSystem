
package com.amalitech.menu.actions;

import com.amalitech.app.AppContext;
import com.amalitech.base.Grade;
import com.amalitech.base.Student;
import com.amalitech.menu.MenuAction;
import com.amalitech.reporting.GPACalculator;

import java.util.ArrayList;
import java.util.List;

public class CalculateGpaAction implements MenuAction {
    private final AppContext ctx;
    private final GPACalculator calc = new GPACalculator();

    public CalculateGpaAction(AppContext ctx) { this.ctx = ctx; }

    @Override public String label() { return "Calculate Student GPA (4.0 scale)"; }

    @Override public void execute() {
        System.out.print("Enter Student ID: ");
        String sid = ctx.scanner.nextLine().trim().toUpperCase();
        Student s = ctx.students.find(sid);
        if (s == null) { System.out.println("Student not found."); return; }

        List<Grade> gs = new ArrayList<>();
        for (int i=0;i<ctx.gradeManager.getGradeCount();i++){
            Grade g = ctx.gradeManager.getGradeAt(i);
            if (g!=null && g.getStudentId().equalsIgnoreCase(sid)) gs.add(g);
        }
        if (gs.isEmpty()) { System.out.println("No grades recorded."); return; }

        double sum=0;
        System.out.printf("%-12s | %-6s | %-10s%n","Subject","Grade","GPA Points");
        System.out.println("--------------------------------------");
        for (Grade g : gs) {
            double pts = calc.toFourPointScale(g.getGrade());
            String letter = calc.toLetter(g.getGrade());
            System.out.printf("%-12s | %5.0f%% | %.1f (%s)%n", g.getSubject().getSubjectName(), g.getGrade(), pts, letter);
            sum += pts;
        }
        System.out.printf("%nCumulative GPA: %.2f / 4.0%n", sum / gs.size());
    }
}
