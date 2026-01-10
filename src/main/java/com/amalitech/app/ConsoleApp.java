
package com.amalitech.app;

import com.amalitech.menu.MenuAction;
import com.amalitech.menu.actions.*;
import com.amalitech.concurrent.BackgroundTaskTracker;


import java.util.ArrayList;
import java.util.List;

public class ConsoleApp {
    private final AppContext ctx;
    private final List<MenuAction> actions = new ArrayList<>();

    public ConsoleApp(AppContext ctx) {
        this.ctx = ctx;
    }

    //manual registration supported
    public ConsoleApp register(MenuAction action) {
        actions.add(action);
        return this;
    }



    /**
     * Registers the standard 18 actions used in main menu.
     * 1–4 Student Mgmt, 5–7 File Ops, 8–11 Analytics, 12–14 Search, 15–18 Advanced.
     */
    public ConsoleApp registerDefaultActions() {
        return this
                // STUDENT MANAGEMENT (1–4)
                .register(new AddStudentAction(ctx))               // 1
                .register(new ViewStudentsAction(ctx))             // 2
                .register(new RecordGradeAction(ctx))              // 3
                .register(new ViewGradeReportAction(ctx))          // 4

                // FILE OPERATIONS (5–7)
                .register(new ExportReportAction(ctx))             // 5
                .register(new ImportDataAction(ctx))               // 6
                .register(new BulkImportGradesAction(ctx))         // 7

                // ANALYTICS & REPORTING (8–11)
                .register(new CalculateGpaAction(ctx))             // 8
                .register(new ViewClassStatisticsAction(ctx))      // 9
                .register(new RealTimeDashboardAction(ctx))        // 10
                .register(new GenerateBatchReportsAction(ctx))     // 11

                // SEARCH & QUERY (12–14)
                .register(new SearchStudentsAction(ctx))           // 12
                .register(new PatternBasedSearchAction(ctx))       // 13
                .register(new QueryGradeHistoryAction(ctx))        // 14

                // ADVANCED FEATURES (15–18)
                .register(new ScheduleAutomatedTasksAction(ctx))   // 15
                .register(new PerformanceMonitorAction(ctx))       // 16
                .register(new CacheManagementAction(ctx))          // 17
                .register(new AuditTrailAction(ctx));              // 18
    }

    /** Main loop. */
    public void run() {
        boolean exit = false;
        while (!exit) {
            printMenu();

            // >>> Show the background status line
            System.out.println();
            System.out.println(BackgroundTaskTracker.statusLine());
            // <<<

            System.out.print("\nEnter choice: "); // keep prompt position the same
            int c = parseIntSafe(ctx.scanner.nextLine(), actions.size() + 1);
            if (c == actions.size() + 1) {
                exit = true;
            } else if (c >= 1 && c <= actions.size()) {
                try {
                    actions.get(c - 1).execute();
                } catch (Exception ex) {
                    com.amalitech.util.ErrorHandler.handle("Menu action", ex);
                }
                System.out.print("\nPress Enter to continue...");
                ctx.scanner.nextLine();
            }
        }
    }


    /** Prints the grouped menu */
    private void printMenu() {
        System.out.println();
        System.out.println("┌───────────────────────────────────────────────────────────┐");
        System.out.println("│ STUDENT GRADE MANAGEMENT - MAIN MENU                      │");
        System.out.println("│ [Advanced Edition v3.0]                                   │");
        System.out.println("└───────────────────────────────────────────────────────────┘");
        System.out.println();

        int total = actions.size();
        for (int i = 0; i < total; i++) {
            int displayIndex = i + 1;
            if (displayIndex == 1) {
                System.out.println("STUDENT MANAGEMENT");
            } else if (displayIndex == 5) {
                System.out.println("\nFILE OPERATIONS");
            } else if (displayIndex == 8) {
                System.out.println("\nANALYTICS & REPORTING");
            } else if (displayIndex == 12) {
                System.out.println("\nSEARCH & QUERY");
            } else if (displayIndex == 15) {
                System.out.println("\nADVANCED FEATURES");
            }
            System.out.printf("%2d. %s%n", displayIndex, actions.get(i).label());
        }

        // Exit always appears last (index size + 1)
        System.out.printf("%n%2d. Exit%n", actions.size() + 1);
    }

    private int parseIntSafe(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }
}

