
package com.amalitech.menu;

import java.util.Scanner;

public interface MenuAction {
    String label();
    void execute(Scanner scanner, AppContext ctx);
}
