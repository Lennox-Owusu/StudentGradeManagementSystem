
package com.amalitech.menu.actions;

import com.amalitech.menu.AppContext;
import com.amalitech.menu.MenuAction;
import com.amalitech.ui.StudentUI;
import java.util.Scanner;

public class AddStudentAction implements MenuAction {
    @Override public String label() { return "Add Student"; }
    @Override public void execute(Scanner scanner, AppContext ctx) {
        StudentUI.addStudent(scanner, ctx.studentManager);
    }
}
