
package com.amalitech.menu.actions;

import com.amalitech.app.AppContext;
import com.amalitech.io.ImportCoordinator;
import com.amalitech.menu.MenuAction;
import com.amalitech.reporting.StudentReport;
import com.amalitech.util.ErrorHandler;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ImportDataAction implements MenuAction {
    private final AppContext ctx;
    public ImportDataAction(AppContext ctx) { this.ctx = ctx; }

    @Override public String label() { return "Import Data (CSV/JSON/Binary)"; }

    @Override public void execute() {
        try {
            System.out.println("Select format: 1=CSV  2=JSON  3=Binary  4=Auto");
            int f = parseIntSafe(ctx.scanner.nextLine(), 4);
            ImportCoordinator.Format fmt = switch (f) {
                case 1 -> ImportCoordinator.Format.CSV;
                case 2 -> ImportCoordinator.Format.JSON;
                case 3 -> ImportCoordinator.Format.BINARY;
                default -> ImportCoordinator.Format.AUTO;
            };

            System.out.print("Enter filename in ./imports: ");
            Path p = Paths.get("./imports").resolve(ctx.scanner.nextLine().trim());

            StudentReport report = ctx.importer.importReport(p, fmt);
            ctx.importer.merge(report);
            System.out.println("✓ Import completed for " + report.getStudentId() + " : " + report.getTotalGrades() + " grades");
        } catch (Exception ex) {
            ErrorHandler.handle("Import Data", ex);
        }
    }
    private int parseIntSafe(String s,int def){ try{ return Integer.parseInt(s.trim()); } catch(Exception e){ return def; } }
}
