
package com.amalitech.menu.actions;

import com.amalitech.base.Student;
import com.amalitech.app.AppContext;
import com.amalitech.menu.MenuAction;

import java.util.List;

public class ViewStudentsAction implements MenuAction {
    private final AppContext ctx;
    public ViewStudentsAction(AppContext ctx) { this.ctx = ctx; }

    @Override public String label() { return "View Students"; }

    @Override public void execute() {
        List<Student> list = ctx.students.listStudents();
        if (list.isEmpty()) { System.out.println("No students."); return; }

        System.out.printf("%-8s | %-18s | %-14s | %-10s | %-7s%n", "STU ID", "NAME", "TYPE", "AVG GRADE", "STATUS");
        System.out.println("---------------------------------------------------------------------");
        for (Student s : list) {
            System.out.printf("%-8s | %-18s | %-14s | %10.2f | %-7s%n",
                    s.getStudentId(),
                    trunc(s.getName(), 18),
                    s.getStudentType(),
                    s.calculateAverageGrade(),
                    s.isPassing() ? "Pass" : "Fail");
        }
    }

    private static String trunc(String x,int n){ return x.length()>n?x.substring(0,n-3)+"...":x; }
}
