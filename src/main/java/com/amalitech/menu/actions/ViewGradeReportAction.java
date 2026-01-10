
package com.amalitech.menu.actions;

import com.amalitech.app.AppContext;
import com.amalitech.base.Grade;
import com.amalitech.base.Student;
import com.amalitech.menu.MenuAction;
import com.amalitech.reporting.ReportGenerator;

public class ViewGradeReportAction implements MenuAction {
    private final AppContext ctx;
    private final ReportGenerator rg = new ReportGenerator();
    public ViewGradeReportAction(AppContext ctx) { this.ctx = ctx; }

    @Override public String label() { return "View Grade Report"; }

    @Override public void execute() {
        System.out.print("Enter Student ID: ");
        String sid = ctx.scanner.nextLine().trim().toUpperCase();
        Student s = ctx.students.find(sid);
        if (s == null) { System.out.println("Student not found."); return; }

        System.out.println("\n" + rg.generateStudentReport(s));
        int total = 0;
        for (int i = 0; i < ctx.gradeManager.getGradeCount(); i++) {
            Grade g = ctx.gradeManager.getGradeAt(i);
            if (g != null && g.getStudentId().equalsIgnoreCase(sid)) total++;
        }
        if (total == 0) { System.out.println("(No grades yet)"); return; }

        System.out.printf("%-8s | %-10s | %-20s | %-8s | %-7s%n", "GRD ID", "DATE", "SUBJECT", "TYPE", "GRADE");
        System.out.println("--------------------------------------------------------------------------");
        for (int i = ctx.gradeManager.getGradeCount()-1; i >= 0; i--) {
            Grade g = ctx.gradeManager.getGradeAt(i);
            if (g != null && g.getStudentId().equalsIgnoreCase(sid)) {
                System.out.printf("%-8s | %-10s | %-20s | %-8s | %-7.2f%n",
                        g.getGradeId(), g.getDate(),
                        g.getSubject().getSubjectName(),
                        g.getSubject().getSubjectType(),
                        g.getGrade());
            }
        }
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf("Core Avg: %.2f  Elective Avg: %.2f  Overall Avg: %.2f%n",
                ctx.gradeManager.calculateCoreAverage(sid),
                ctx.gradeManager.calculateElectiveAverage(sid),
                ctx.gradeManager.calculateOverallAverage(sid));
    }
}
