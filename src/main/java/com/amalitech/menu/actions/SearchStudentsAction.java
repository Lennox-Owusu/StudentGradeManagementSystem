
package com.amalitech.menu.actions;

import com.amalitech.Student;
import com.amalitech.app.AppContext;
import com.amalitech.menu.MenuAction;

import java.util.List;

public class SearchStudentsAction implements MenuAction {
    private final AppContext ctx;
    public SearchStudentsAction(AppContext ctx){ this.ctx = ctx; }

    @Override public String label() { return "Search Students (ID/Name/Type)"; }

    @Override public void execute() {
        System.out.println("Search: 1=By ID (*, ?)  2=By Name (contains)  3=By Type (Regular/Honors)");
        int opt = parseIntSafe(ctx.scanner.nextLine(),1);
        List<Student> results = switch (opt) {
            case 2 -> { System.out.print("Name contains: "); yield ctx.search.searchStudentsByName(ctx.scanner.nextLine().trim()); }
            case 3 -> { System.out.print("Type: "); yield ctx.search.searchStudentsByType(ctx.scanner.nextLine().trim()); }
            default -> { System.out.print("ID pattern: "); yield ctx.search.searchStudentsById(ctx.scanner.nextLine().trim().toUpperCase()); }
        };

        System.out.printf("%-8s | %-18s | %-14s | %-5s%n","STU ID","NAME","TYPE","AVG");
        System.out.println("----------------------------------------------------------");
        for (Student s : results) {
            System.out.printf("%-8s | %-18s | %-14s | %5.1f%%%n",
                    s.getStudentId(), trunc(s.getName(),18), s.getStudentType(), s.calculateAverageGrade());
        }
        System.out.println("Total: " + results.size());
    }
    private static String trunc(String x,int n){ return x.length()>n?x.substring(0,n-3)+"...":x; }
    private int parseIntSafe(String s,int def){ try{ return Integer.parseInt(s.trim()); } catch(Exception e){ return def; } }
}
