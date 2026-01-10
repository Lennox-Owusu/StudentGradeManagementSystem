
package com.amalitech.menu.actions;

import com.amalitech.app.AppContext;
import com.amalitech.menu.MenuAction;

public class CacheManagementAction implements MenuAction {
    private final AppContext ctx;
    public CacheManagementAction(AppContext ctx){ this.ctx = ctx; }

    @Override public String label() { return "Cache Management"; }

    @Override public void execute() {
        while (true) {
            System.out.println("\nCache Status: " + ctx.cacheAdmin.status());
            System.out.println(" 1. Warm cache");
            System.out.println(" 2. Benchmark lookups");
            System.out.println(" 3. Clear all");
            System.out.println(" 4. Reset counters");
            System.out.println(" 5. Back");
            System.out.print("Select: ");
            int act = parseIntSafe(ctx.scanner.nextLine(), 5);
            if (act == 5) return;

            long t0 = System.nanoTime();
            switch (act) {
                case 1 -> ctx.cacheAdmin.warm();
                case 2 -> ctx.cacheAdmin.benchmark();
                case 3 -> ctx.cacheAdmin.clear();
                case 4 -> ctx.cacheAdmin.resetCounters();
                default -> System.out.println("Invalid choice.");
            }
            double ms = (System.nanoTime() - t0) / 1_000_000.0;
            System.out.printf("✓ Done in %.1fms%n", ms);
        }
    }
    private static int parseIntSafe(String s,int def){ try{ return Integer.parseInt(s.trim()); } catch(Exception e){ return def; } }
}
