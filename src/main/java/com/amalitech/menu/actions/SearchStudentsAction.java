
package com.amalitech.menu.actions;

import com.amalitech.menu.AppContext;
import com.amalitech.menu.MenuAction;
import com.amalitech.ui.SearchUI;
import java.util.Scanner;

public class SearchStudentsAction implements MenuAction {
    @Override public String label() { return "Search Students"; }
    @Override public void execute(Scanner scanner, AppContext ctx) {
        SearchUI.searchStudents(scanner, ctx.studentManager, ctx.gradeManager, ctx.reportService, ctx.exportService);
    }
}
