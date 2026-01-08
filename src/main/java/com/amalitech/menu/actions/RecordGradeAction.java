
package com.amalitech.menu.actions;

import com.amalitech.menu.AppContext;
import com.amalitech.menu.MenuAction;
import com.amalitech.ui.GradeUI;

import java.util.Scanner;

public class RecordGradeAction implements MenuAction {
    @Override public String label() { return "Record Grade"; }
    @Override public void execute(Scanner scanner, AppContext ctx) {
        GradeUI.recordGrade(scanner, ctx.studentManager, ctx.gradeManager);
    }
}
