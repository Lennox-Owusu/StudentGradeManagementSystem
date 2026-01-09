
package com.amalitech.menu.actions;

import com.amalitech.Grade;
import com.amalitech.app.AppContext;
import com.amalitech.menu.MenuAction;
import com.amalitech.util.ErrorHandler;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class QueryGradeHistoryAction implements MenuAction {
    private final AppContext ctx;
    public QueryGradeHistoryAction(AppContext ctx){ this.ctx = ctx; }

    @Override public String label() { return "Query Grade History"; }

    @Override public void execute() {
        try {
            System.out.print("Student ID (Enter=All): "); String sid  = safe(ctx.scanner.nextLine());
            System.out.print("Subject contains: ");        String subj = safe(ctx.scanner.nextLine());
            System.out.print("Type (Core/Elective): ");    String type = safe(ctx.scanner.nextLine());
            System.out.print("Date From (YYYY-MM-DD): ");  String from = safe(ctx.scanner.nextLine());
            System.out.print("Date To   (YYYY-MM-DD): ");  String to   = safe(ctx.scanner.nextLine());
            System.out.print("Min Grade (Enter=none): ");  Double min  = parseDoubleOrNull(ctx.scanner.nextLine());
            System.out.print("Max Grade (Enter=none): ");  Double max  = parseDoubleOrNull(ctx.scanner.nextLine());
            System.out.println("Sort: 1=Date desc  2=Date asc  3=Grade desc  4=Grade asc");
            int sort = parseIntSafe(ctx.scanner.nextLine(), 1);

            List<Grade> rows = ctx.search.queryGradeHistory(blankToNull(sid), subj, type, from, to, min, max, sort);

            System.out.printf("%n%-8s | %-10s | %-20s | %-8s | %-7s | %-8s%n",
                    "GRD ID","DATE","SUBJECT","TYPE","GRADE","STU ID");
            System.out.println("--------------------------------------------------------------------------");
            for (Grade g : rows) {
                System.out.printf("%-8s | %-10s | %-20s | %-8s | %7.2f | %-8s%n",
                        g.getGradeId(), g.getDate(), g.getSubject().getSubjectName(),
                        g.getSubject().getSubjectType(), g.getGrade(), g.getStudentId());
            }
            System.out.println("--------------------------------------------------------------------------");
            System.out.println("Total: " + rows.size());

            System.out.print("\nExport results to ./reports/history_*.txt ? (Y/N): ");
            if ("Y".equalsIgnoreCase(ctx.scanner.nextLine().trim())) {
                Path reports = Paths.get("./reports");
                Files.createDirectories(reports);
                String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                Path out = reports.resolve("history_" + (sid.isBlank()? "all" : sid) + "_" + ts + ".txt");

                var sb = new StringBuilder();
                sb.append(String.format("%-8s | %-10s | %-20s | %-8s | %-7s | %-8s%n",
                        "GRD ID","DATE","SUBJECT","TYPE","GRADE","STU ID"));
                sb.append("-".repeat(74)).append(System.lineSeparator());
                for (Grade g : rows) {
                    sb.append(String.format("%-8s | %-10s | %-20s | %-8s | %7.2f | %-8s%n",
                            g.getGradeId(), g.getDate(), g.getSubject().getSubjectName(),
                            g.getSubject().getSubjectType(), g.getGrade(), g.getStudentId()));
                }
                Files.writeString(out, sb.toString(), StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                System.out.println("✓ Exported: " + out.toAbsolutePath());
            }
        } catch (Exception ex) {
            ErrorHandler.handle("Query Grade History", ex);
        }
    }

    private static String safe(String s){ return s==null? "" : s.trim(); }
    private static String blankToNull(String s){ return (s==null || s.isBlank()) ? null : s; }
    private static Double parseDoubleOrNull(String s){ s = safe(s); if (s.isBlank()) return null; try { return Double.parseDouble(s); } catch(Exception e){ return null; } }
    private static int parseIntSafe(String s,int def){ try{ return Integer.parseInt(s.trim()); } catch(Exception e){ return def; } }
}
