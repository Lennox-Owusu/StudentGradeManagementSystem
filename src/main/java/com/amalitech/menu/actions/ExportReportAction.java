
package com.amalitech.menu.actions;

import com.amalitech.base.Student;
import com.amalitech.app.AppContext;
import com.amalitech.menu.MenuAction;
import com.amalitech.reporting.ReportType;
import com.amalitech.service.api.ExportSummary;
import com.amalitech.service.api.FormatResult;
import com.amalitech.util.ErrorHandler;

import java.util.Locale;

public class ExportReportAction implements MenuAction {
    private final AppContext ctx;

    public ExportReportAction(AppContext ctx) { this.ctx = ctx; }

    @Override public String label() { return "Export Grade Report (CSV/JSON/Binary)"; }

    @Override public void execute() {
        try {
            System.out.println();
            System.out.println("EXPORT GRADE REPORT (Multi-Format)");
            System.out.println("----------------------------------");
            System.out.println();

            System.out.print("Enter Student ID: ");
            String sid = ctx.scanner.nextLine().trim().toUpperCase();

            // Try service first (if available), then manager as fallback.
            Student s = null;
            try { s = ctx.students.find(sid); } catch (Exception ignore) {}
            if (s == null) s = ctx.studentManager.findStudent(sid);
            if (s == null) { System.out.println("Student not found."); return; }

            int totalGrades = countGradesFor(sid);
            System.out.printf("Student: %s - %s (%s)%n", s.getStudentId(), s.getName(), s.getEmail());
            System.out.printf("Type: %s | Phone: %s%n", s.getStudentType(), s.getPhone());
            System.out.printf("Total Grades: %d%n%n", totalGrades);

            System.out.println("Export Format:");
            System.out.println("1. CSV (Comma-Separated Values)");
            System.out.println("2. JSON (JavaScript Object Notation)");
            System.out.println("3. Binary (Serialized Java Object)");
            System.out.println("4. All formats");
            System.out.print("\nSelect format (1-4): ");
            int fmt = parseIntSafe(ctx.scanner.nextLine(), 4);

            System.out.println("\nReport Type:");
            System.out.println("1. Summary Report");
            System.out.println("2. Detailed Report");
            System.out.println("3. Transcript Format");
            System.out.println("4. Performance Analytics");
            System.out.print("\nSelect type (1-4): ");
            int t = parseIntSafe(ctx.scanner.nextLine(), 2);

            ReportType type = switch (t) {
                case 1 -> ReportType.SUMMARY;
                case 3 -> ReportType.TRANSCRIPT;
                case 4 -> ReportType.ANALYTICS;
                default -> ReportType.DETAILED;
            };

            System.out.println("\nProcessing with NIO.2 Streaming...");

            ExportSummary summary = ctx.exporter.exportReport(s, fmt, type);

            // Per-format result blocks
            for (FormatResult r : summary.getResults()) {
                System.out.println();
                System.out.println("✓ " + r.getFormat() + " Export completed");
                System.out.println("File: " + r.getFileName());
                System.out.println("Location: " + r.getLocation());
                System.out.printf(Locale.US, "Size: %.1f KB%n", r.getSizeBytes() / 1024.0);
                System.out.println(r.getDescription()); // e.g., Rows/Structure/Format
                System.out.printf(Locale.US, "Time: %.0fms%n", r.getTimeMs());
            }

            // Summary footer
            System.out.println();
            System.out.println("📊 Export Performance Summary:");
            System.out.printf(Locale.US, "Total Time: %.0fms%n", summary.getTotalTimeMs());
            System.out.printf(Locale.US, "Total Size: %.1f KB%n", summary.getTotalSizeBytes() / 1024.0);
            if (summary.getCompressionRatio() != null) {
                System.out.printf(Locale.US, "Compression Ratio: %.1f:1 (binary vs JSON)%n", summary.getCompressionRatio());
            }
            System.out.println("I/O Operations: " + summary.getParallelWrites() + " parallel writes");

            System.out.println();
            System.out.print("Press Enter to continue...");
            ctx.scanner.nextLine();

        } catch (Exception ex) {
            ErrorHandler.handle("Export Report", ex);
        }
    }

    private int countGradesFor(String sid) {
        int total = 0;
        for (int i = 0; i < ctx.gradeManager.getGradeCount(); i++) {
            var g = ctx.gradeManager.getGradeAt(i);
            if (g != null && g.getStudentId().equalsIgnoreCase(sid)) total++;
        }
        return total;
    }

    private int parseIntSafe(String s,int def){ try{ return Integer.parseInt(s.trim()); } catch(Exception e){ return def; } }
}
