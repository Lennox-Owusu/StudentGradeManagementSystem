
package com.amalitech.menu.actions;

import com.amalitech.menu.AppContext;
import com.amalitech.menu.MenuAction;
import com.amalitech.ui.StudentUI;

import java.util.Scanner;

public class ViewStudentsAction implements MenuAction {
    @Override public String label() { return "View Students"; }
    @Override public void execute(Scanner scanner, AppContext ctx) {
        StudentUI.viewStudents(ctx.studentManager);
    }
}
