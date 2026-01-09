
package com.amalitech.menu.actions;

import com.amalitech.app.AppContext;
import com.amalitech.menu.MenuAction;

import java.util.List;

public class AuditTrailAction implements MenuAction {
    private final AppContext ctx;
    private List<String> lastView = List.of();

    public AuditTrailAction(AppContext ctx){ this.ctx = ctx; }

    @Override public String label() { return "Audit Trail Viewer"; }

    @Override public void execute() {
        while (true) {
            System.out.println("\nAudit Trail");
            System.out.println(" 1. Tail last N lines");
            System.out.println(" 2. Filter by level (INFO/ERROR)");
            System.out.println(" 3. Search by keyword");
            System.out.println(" 4. Export current view");
            System.out.println(" 5. Archive and clear log");
            System.out.println(" 6. Back");
            System.out.print("Select: ");
            int a = parseIntSafe(ctx.scanner.nextLine(),6);
            if (a == 6) return;

            switch (a) {
                case 1 -> { System.out.print("N: "); lastView = ctx.audit.tailLast(parseIntSafe(ctx.scanner.nextLine(), 100)); print(lastView); }
                case 2 -> { System.out.print("Level: "); lastView = ctx.audit.filterByLevel(ctx.scanner.nextLine().trim()); print(lastView); }
                case 3 -> { System.out.print("Keyword: "); lastView = ctx.audit.searchByKeyword(ctx.scanner.nextLine().trim()); print(lastView); }
                case 4 -> { if (lastView==null || lastView.isEmpty()) System.out.println("(no current view)"); else { ctx.audit.exportView(lastView); System.out.println("✓ Exported."); } }
                case 5 -> { ctx.audit.archiveAndTruncate(); System.out.println("✓ Archived and cleared."); }
                default -> System.out.println("Invalid.");
            }
        }
    }

    private void print(List<String> lines){
        System.out.println();
        if (lines.isEmpty()) System.out.println("(no results)");
        else lines.forEach(System.out::println);
    }
    private static int parseIntSafe(String s,int def){ try{ return Integer.parseInt(s.trim()); } catch(Exception e){ return def; } }
}
