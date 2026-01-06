
package com.amalitech.monitor;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.LongAdder;

//Captures grade-add events + processing time snapshots for the dashboard.
public final class GradeEventTracker {
    private static final ConcurrentLinkedQueue<Instant> EVENTS = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<Long> TIMES_MS = new ConcurrentLinkedQueue<>();
    private static final LongAdder COUNT = new LongAdder();

    private GradeEventTracker() {}

    public static void recordEventNow() {
        EVENTS.add(Instant.now());
        COUNT.increment();
        trimOld();
    }

    public static void recordProcessingTime(long ms) {
        TIMES_MS.add(ms);
        // limit memory
        if (TIMES_MS.size() > 2000) TIMES_MS.poll();
    }

    public static int countLast(Duration window) {
        Instant cutoff = Instant.now().minus(window);
        int c = 0;
        for (Instant t : EVENTS) if (!t.isBefore(cutoff)) c++;
        return c;
    }

    public static long averageProcessingMs() {
        if (TIMES_MS.isEmpty()) return 0L;
        long sum = 0L; int n = 0;
        for (Long v : TIMES_MS) { sum += v; n++; }
        return (n == 0) ? 0L : sum / n;
    }

    private static void trimOld() {
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(10));
        while (true) {
            Instant head = EVENTS.peek();
            if (head == null || !head.isBefore(cutoff)) break;
            EVENTS.poll();
        }
    }
}
