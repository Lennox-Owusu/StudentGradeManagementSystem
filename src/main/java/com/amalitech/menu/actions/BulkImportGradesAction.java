
package com.amalitech.menu.actions;

import com.amalitech.menu.AppContext;
import com.amalitech.menu.MenuAction;
import com.amalitech.ui.ImportUI;
import java.util.Scanner;

public class BulkImportGradesAction implements MenuAction {
    @Override public String label() { return "Bulk Import Grades"; }
    @Override public void execute(Scanner scanner, AppContext ctx) {
        ImportUI.bulkImportGrades(scanner, ctx.importService);
    }
}
