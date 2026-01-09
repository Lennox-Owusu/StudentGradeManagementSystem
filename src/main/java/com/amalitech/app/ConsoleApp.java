
package com.amalitech.app;

import com.amalitech.menu.MenuAction;

import java.util.ArrayList;
import java.util.List;

public class ConsoleApp {
    private final AppContext ctx;
    private final List<MenuAction> actions = new ArrayList<>();

    public ConsoleApp(AppContext ctx) { this.ctx = ctx; }

    public ConsoleApp register(MenuAction action) { actions.add(action); return this; }

    public void run() {
        boolean exit = false;
        while (!exit) {
            printMenu();
            System.out.print("\nEnter choice: ");
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

    private void printMenu() {
        System.out.println();
        System.out.println("┌──────────────────────────────────────────────────┐");
        System.out.println("│ STUDENT GRADE MANAGEMENT — Advanced Edition v3.0 │");
        System.out.println("└──────────────────────────────────────────────────┘\n");
        for (int i = 0; i < actions.size(); i++) {
            System.out.printf("%2d. %s%n", i + 1, actions.get(i).label());
        }
        System.out.printf("%2d. Exit%n", actions.size() + 1);
    }

    private int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }
}
