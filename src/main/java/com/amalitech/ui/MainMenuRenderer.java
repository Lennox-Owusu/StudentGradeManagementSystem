
package com.amalitech.ui;

/**
 * Renders the Advanced Edition v3.0 main menu exactly as per the screenshot.
 * Pure presentation—no business logic.
 */
public final class MainMenuRenderer {

    public void render() {
        System.out.println();
        System.out.println("┌────────────────────────────────────────────────────┐");
        System.out.println("│ STUDENT GRADE MANAGEMENT - MAIN MENU               │");
        System.out.println("│ [Advanced Edition v3.0]                            │");
        System.out.println("└────────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("STUDENT MANAGEMENT");
        System.out.println("  1. Add Student (with validation)");
        System.out.println("  2. View Students");
        System.out.println("  3. Record Grade");
        System.out.println("  4. View Grade Report");
        System.out.println();
        System.out.println("FILE OPERATIONS");
        System.out.println("  5. Export Grade Report (CSV/JSON/Binary)");
        System.out.println("  6. Import Data (Multi-format support)    [ENHANCED]");
        System.out.println("  7. Bulk Import Grades");
        System.out.println();
        System.out.println("ANALYTICS & REPORTING");
        System.out.println("  8. Calculate Student GPA");
        System.out.println("  9. View Class Statistics");
        System.out.println(" 10. Real-Time Statistics Dashboard        [NEW]");
        System.out.println(" 11. Generate Batch Reports                [NEW]");
        System.out.println();
        System.out.println("SEARCH & QUERY");
        System.out.println(" 12. Search Students (Advanced)            [ENHANCED]");
        System.out.println(" 13. Pattern-Based Search                  [NEW]");
        System.out.println(" 14. Query Grade History                   [NEW]");
        System.out.println();
        System.out.println("ADVANCED FEATURES");
        System.out.println(" 15. Schedule Automated Tasks              [NEW]");
        System.out.println(" 16. View System Performance               [NEW]");
        System.out.println(" 17. Cache Management                      [NEW]");
        System.out.println(" 18. Audit Trail Viewer                    [NEW]");
        System.out.println();
        System.out.println(" 19. Exit");
        System.out.println();
    }
}
