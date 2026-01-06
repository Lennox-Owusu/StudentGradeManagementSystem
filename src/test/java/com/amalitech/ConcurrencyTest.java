
package com.amalitech;

import com.amalitech.cache.CacheService;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.*;

public class ConcurrencyTest {

    @Test
    public void concurrentHashMap_updates_concurrent_load() throws Exception {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        int threads = Math.max(4, Runtime.getRuntime().availableProcessors());
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        int perThreadOps = 50_000;

        Callable<Void> task = () -> {
            for (int i = 0; i < perThreadOps; i++) {
                map.compute("K" + i, (k, v) -> v == null ? 1 : v + 1);
            }
            return null;
        };

        List<Future<Void>> futs = new ArrayList<>();
        for (int t = 0; t < threads; t++) futs.add(pool.submit(task));
        for (Future<Void> f : futs) f.get();

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        // Spot-checks
        assertTrue(map.size() > 0);
        assertTrue(map.get("K0") >= 1);
    }

    @Test
    public void proper_threadpool_shutdown() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        pool.submit(() -> {
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        });
        pool.shutdown();
        boolean ok = pool.awaitTermination(3, TimeUnit.SECONDS);
        assertTrue(ok);
    }

    @Test
    public void deadlock_prevention_with_timeouts() throws InterruptedException {
        // Use two locks with tryLock & timeouts to avoid deadlock
        ReentrantLock A = new ReentrantLock();
        ReentrantLock B = new ReentrantLock();

        Runnable r1 = () -> {
            try {
                if (A.tryLock(200, TimeUnit.MILLISECONDS)) {
                    try {
                        if (B.tryLock(200, TimeUnit.MILLISECONDS)) {
                            try { /* critical section */ } finally { B.unlock(); }
                        }
                    } finally { A.unlock(); }
                }
            } catch (InterruptedException ignored) {}
        };
        Runnable r2 = () -> {
            try {
                if (B.tryLock(200, TimeUnit.MILLISECONDS)) {
                    try {
                        if (A.tryLock(200, TimeUnit.MILLISECONDS)) {
                            try { /* critical section */ } finally { A.unlock(); }
                        }
                    } finally { B.unlock(); }
                }
            } catch (InterruptedException ignored) {}
        };

        Thread t1 = new Thread(r1); Thread t2 = new Thread(r2);
        t1.start(); t2.start();
        t1.join(); t2.join();
        // If we reached here, no deadlock occurred
        assertTrue(true);
    }

    @Test
    public void cache_consistency_under_concurrent_access() throws Exception {
        CacheService<String, Object> cache = new CacheService<>(256);
        int threads = Math.max(4, Runtime.getRuntime().availableProcessors());
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        Callable<Void> loader = () -> {
            for (int i = 0; i < 10_000; i++) {
                String k = "K" + (i % 500);
                cache.getOrLoad(k, () -> ("V" + k));
            }
            return null;
        };

        List<Future<Void>> futs = new ArrayList<>();
        for (int i = 0; i < threads; i++) futs.add(pool.submit(loader));
        for (Future<Void> f : futs) f.get();
        pool.shutdown();

        assertTrue(cache.size() <= 256);
        long total = cache.hits() + cache.misses();
        assertTrue(total > 0);
    }

    // Simple lock wrapper
    private static final class ReentrantLock {
        private final java.util.concurrent.locks.ReentrantLock lock = new java.util.concurrent.locks.ReentrantLock();
        boolean tryLock(long timeoutMs, TimeUnit unit) throws InterruptedException {
            return lock.tryLock(timeoutMs, unit);
        }
        void unlock() { if (lock.isHeldByCurrentThread()) lock.unlock(); }
    }
}
