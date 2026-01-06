
package com.amalitech;

import org.junit.Assume;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class CollectionPerformanceTest {

    @Test
    public void hashmap_lookup_vs_arraylist_search() {
        int n = 100_000;
        Map<String, Integer> map = new HashMap<>(n);
        List<String> list = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            String key = "K" + i;
            map.put(key, i);
            list.add(key);
        }
        String target = "K" + (n - 1); // worst-case for list

        long t0 = System.nanoTime();
        Integer mv = map.get(target);
        long mapNs = System.nanoTime() - t0;

        t0 = System.nanoTime();
        boolean found = list.contains(target);
        long listNs = System.nanoTime() - t0;

        assertNotNull(mv);
        assertTrue(found);

        // empirical O(n) vs O(1) indication: list should be notably slower than map
        assertTrue("listNs=" + listNs + " vs mapNs=" + mapNs, listNs > mapNs * 10);
    }

    @Test
    public void treemap_sorting_performance_and_order() {
        TreeMap<Integer, String> tm = new TreeMap<>();
        tm.put(5, "e"); tm.put(1, "a"); tm.put(4, "d"); tm.put(2, "b"); tm.put(3, "c");

        assertEquals(Integer.valueOf(1), tm.firstKey());
        assertEquals(Integer.valueOf(5), tm.lastKey());
        assertEquals(Map.entry(4, "d"), tm.floorEntry(4));
        assertEquals(Map.entry(2, "b"), tm.ceilingEntry(2));

        long t0 = System.nanoTime();
        for (int i = 0; i < 10000; i++) tm.floorEntry(7);
        long ns = System.nanoTime() - t0;
        assertTrue(ns > 0); // operation performed
    }

    @Test
    public void hashset_uniqueness_guarantee() {
        Set<String> hs = new HashSet<>();
        hs.add("A"); hs.add("A");
        hs.add("B"); hs.add("C"); hs.add("C");
        assertEquals(3, hs.size());
    }

    @Test
    public void empirical_growth_list_search_scales_with_n() {
        // measure list.contains over increasing sizes; time should grow
        int[] sizes = {10_000, 50_000, 100_000};
        long lastTime = 0;
        for (int n : sizes) {
            List<String> list = IntStream.range(0, n).mapToObj(i -> "K" + i).collect(Collectors.toList());
            String target = "K" + (n - 1);
            long t0 = System.nanoTime();
            boolean f = list.contains(target);
            long ns = System.nanoTime() - t0;
            assertTrue(f);
            if (lastTime > 0) assertTrue("n=" + n + " ns=" + ns + " last=" + lastTime, ns >= lastTime * 0.8);
            lastTime = ns;
        }
    }

    @Test
    public void parallel_stream_vs_sequential_summary() {
        int cores = Runtime.getRuntime().availableProcessors();
        Assume.assumeTrue(cores >= 2);

        List<Double> data = IntStream.range(0, 300_000).mapToDouble(i -> Math.sin(i)).boxed().collect(Collectors.toList());
        double seqSum = data.stream().mapToDouble(Double::doubleValue).sum();
        double parSum = data.parallelStream().mapToDouble(Double::doubleValue).sum();
        assertEquals(seqSum, parSum, 1e-9);
    }
}
