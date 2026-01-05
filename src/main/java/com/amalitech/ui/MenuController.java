
package com.amalitech.ui;

/**
 * Central router for the menu selections.
 * For Phase 1, handlers are safe stubs—no external dependencies—so the UI is fully usable.
 * We will wire real services in subsequent phases.
 */
public final class MenuController {

    public boolean handle(int choice) {
        switch (choice) {
            case 1:  banner("Add Student (with validation)"); break;
            case 2:  banner("View Students"); break;
            case 3:  banner("Record Grade"); break;
            case 4:  banner("View Grade Report"); break;

            case 5:  banner("Export Grade Report (CSV/JSON/Binary)"); break;
            case 6:  banner("Import Data (Multi-format support) [ENHANCED]"); break;
            case 7:  banner("Bulk Import Grades"); break;

            case 8:  banner("Calculate Student GPA"); break;
            case 9:  banner("View Class Statistics"); break;
            case 10: banner("Real-Time Statistics Dashboard [NEW]"); break;
            case 11: banner("Generate Batch Reports [NEW]"); break;

            case 12: banner("Search Students (Advanced) [ENHANCED]"); break;
            case 13: banner("Pattern-Based Search [NEW]"); break;
            case 14: banner("Query Grade History [NEW]"); break;

            case 15: banner("Schedule Automated Tasks [NEW]"); break;
            case 16: banner("View System Performance [NEW]"); break;
            case 17: banner("Cache Management [NEW]"); break;
            case 18: banner("Audit Trail Viewer [NEW]"); break;

            case 19:
                System.out.println("Exiting...");
                return false; // signal loop to stop
            default:
                System.out.println("Invalid choice. Please try again.");
        }
        return true; // continue loop
    }

    private static void banner(String title) {
        System.out.println();
        System.out.println("— — — — — — — — — — — — — — — — — — — — —");
        System.out.println("▶ " + title);
        System.out.println("— — — — — — — — — — — — — — — — — — — — —");
        System.out.println();
    }
}
