
package com.amalitech.menu.actions;

import com.amalitech.menu.AppContext;
import com.amalitech.menu.MenuAction;
import com.amalitech.ui.GradeUI;

import java.util.Scanner;

public class ViewGradeReportAction implements MenuAction {
    @Override public String label() { return "View Grade Report"; }
    @Override public void execute(Scanner scanner, AppContext ctx) {
        GradeUI.viewGradeReport(scanner, ctx.studentManager, ctx.gradeManager, ctx.reportService);
    }
}
