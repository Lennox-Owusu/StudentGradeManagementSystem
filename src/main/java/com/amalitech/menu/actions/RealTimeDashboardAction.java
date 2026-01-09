
package com.amalitech.menu.actions;

import com.amalitech.app.AppContext;
import com.amalitech.menu.MenuAction;

public class RealTimeDashboardAction implements MenuAction {
    private final AppContext ctx;
    public RealTimeDashboardAction(AppContext ctx){ this.ctx = ctx; }

    @Override public String label() { return "Real-Time Statistics Dashboard"; }

    @Override public void execute() { ctx.dashboard.run(); }
}
