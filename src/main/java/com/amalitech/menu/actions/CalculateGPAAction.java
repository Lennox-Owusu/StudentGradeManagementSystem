
package com.amalitech.menu.actions;

import com.amalitech.menu.AppContext;
import com.amalitech.menu.MenuAction;
import com.amalitech.ui.GradeUI;
import java.util.Scanner;

public class CalculateGPAAction implements MenuAction {
    @Override public String label() { return "Calculate Student GPA"; }
    @Override public void execute(Scanner scanner, AppContext ctx) {
        GradeUI.calculateGPA(scanner, ctx.studentManager, ctx.gradeManager, ctx.gpaService);
    }
}
