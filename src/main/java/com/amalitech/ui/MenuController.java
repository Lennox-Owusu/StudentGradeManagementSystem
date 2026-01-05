
package com.amalitech.ui;

public final class MenuController {

    private final CoreMenuHandlers handlers;

    public MenuController(CoreMenuHandlers handlers) {
        this.handlers = handlers;
    }

    public boolean handle(int choice) {
        switch (choice) {
            // ── STUDENT MANAGEMENT ───────────────────────────────
            case 1:  handlers.addStudent();           break;
            case 2:  handlers.viewStudents();         break;
            case 3:  handlers.recordGrade();          break;
            case 4:  handlers.viewGradeReport();      break;

            // ── FILE OPERATIONS ─────────────────────────────────
            case 5:  handlers.exportGradeReportMultiFormat(); break; // new wizard
            case 6:  banner("Import Data (Multi-format support) [ENHANCED]"); break;
            case 7:  handlers.bulkImportGrades();     break;

            // ── ANALYTICS & REPORTING ───────────────────────────
            case 8:  handlers.calculateStudentGpa();  break;
            case 9:  handlers.viewClassStatistics();  break;
            case 10: banner("Real-Time Statistics Dashboard [NEW]"); break;
            case 11: banner("Generate Batch Reports [NEW]");         break;

            // ── SEARCH & QUERY ──────────────────────────────────
            case 12: banner("Search Students (Advanced) [ENHANCED]"); break;
            case 13: banner("Pattern-Based Search [NEW]");            break;
            case 14: handlers.searchStudents();                        break; // optional mapping

            // ── ADVANCED FEATURES ───────────────────────────────
            case 15: banner("Schedule Automated Tasks [NEW]");        break;
            case 16: banner("View System Performance [NEW]");         break;
            case 17: banner("Cache Management [NEW]");                break;
            case 18: banner("Audit Trail Viewer [NEW]");              break;

            // Exit
            case 19:
                System.out.println("Exiting...");
                return false;

            default:
                System.out.println("Invalid choice. Please try again.");
        }
        return true;
    }

    private static void banner(String title) {
        System.out.println();
        System.out.println("— — — — — — — — — — — — — — — — — — — — —");
        System.out.println("▶ " + title);
        System.out.println("— — — — — — — — — — — — — — — — — — — — —");
        System.out.println();
    }
}
