
package com.amalitech;

import com.amalitech.app.AppContext;
import com.amalitech.app.ConsoleApp;
import com.amalitech.app.DemoDataSeeder;
import com.amalitech.concurrent.BackgroundTaskTracker;
import com.amalitech.concurrent.StatusRefresher;
import com.amalitech.exceptions.ValidationException;

import java.util.concurrent.ThreadPoolExecutor;

public class Main {
    public static void main(String[] args) throws ValidationException {
        AppContext ctx = new AppContext();

        //preload sample data
        new DemoDataSeeder(ctx).seedSampleStudents();

        // Initialize: show Stats updating.
        BackgroundTaskTracker.setStatsUpdating(true);    // optional; monitor will toggle later

        // Build console app with defaults
        ConsoleApp app = new ConsoleApp(ctx).registerDefaultActions();

        // --- Start live status refresher
        ThreadPoolExecutor fixed =
                (ctx.fixedPool instanceof ThreadPoolExecutor) ? (ThreadPoolExecutor) ctx.fixedPool : null;
        ThreadPoolExecutor cached =
                (ctx.cachedPool instanceof ThreadPoolExecutor) ? (ThreadPoolExecutor) ctx.cachedPool : null;
        StatusRefresher refresher = new StatusRefresher(fixed, cached, ctx.scheduler, 2);
        Thread refresherThread = refresher.start();

        try {
            app.run();
        } finally {
            // Stop refresher and shutdown pools
            refresher.stop();
            try { refresherThread.interrupt(); } catch (Exception ignored) {}
            ctx.shutdown();
        }
    }
}
