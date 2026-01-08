
package com.amalitech.menu.actions;

import com.amalitech.menu.AppContext;
import com.amalitech.menu.MenuAction;
import com.amalitech.ui.StatsUI;
import java.util.Scanner;

public class ViewClassStatisticsAction implements MenuAction {
    @Override public String label() { return "View Class Statistics"; }
    @Override public void execute(Scanner scanner, AppContext ctx) {
        StatsUI.viewClassStatistics(scanner, ctx.statisticsService);
    }
}
