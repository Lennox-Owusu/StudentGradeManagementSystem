
package com.amalitech.menu.actions;

import com.amalitech.Student;
import com.amalitech.app.AppContext;
import com.amalitech.menu.MenuAction;
import com.amalitech.util.ErrorHandler;

public class ExportReportAction implements MenuAction {
    private final AppContext ctx;
    public ExportReportAction(AppContext ctx) { this.ctx = ctx; }

    @Override public String label() { return "Export Grade Report (CSV/JSON/Binary)"; }

    @Override public void execute() {
        try {
            System.out.print("Enter Student ID: ");
            String sid = ctx.scanner.nextLine().trim().toUpperCase();
            Student s = ctx.students.find(sid);
            if (s == null) { System.out.println("Student not found."); return; }

            System.out.println("Format: 1=CSV  2=JSON  3=Binary  4=All");
            int fmt = parseIntSafe(ctx.scanner.nextLine(), 4);
            ctx.exporter.exportDetailedReport(s, fmt);
            System.out.println("✓ Export completed.");
        } catch (Exception ex) {
            ErrorHandler.handle("Export Report", ex);
        }
    }
    private int parseIntSafe(String s,int def){ try{ return Integer.parseInt(s.trim()); } catch(Exception e){ return def; } }
}
