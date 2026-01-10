
package com.amalitech.menu.actions;

import com.amalitech.app.AppContext;
import com.amalitech.concurrent.BackgroundTaskTracker;  // <-- add
import com.amalitech.menu.MenuAction;

public class PerformanceMonitorAction implements MenuAction {
    private final AppContext ctx;
    public PerformanceMonitorAction(AppContext ctx){ this.ctx = ctx; }
    @Override public String label() { return "System Performance Monitor"; }
    @Override public void execute() {
        // Mark stats updating while the monitor is active
        BackgroundTaskTracker.setStatsUpdating(true);
        try {
            ctx.monitor.run();    // your existing call
        } finally {
            BackgroundTaskTracker.setStatsUpdating(false);
        }
    }
}
