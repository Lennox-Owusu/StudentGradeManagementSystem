
package com.amalitech.calculation;

import com.amalitech.interfaces.Calculable;

import java.util.*;

//Performs statistical calculations on numeric data.
public class StatisticsCalculator implements Calculable {

    //Arithmetic mean. Returns 0.0 for null/empty input.
    public double mean(List<Double> data) {
        if (data == null || data.isEmpty()) return 0.0;
        double sum = 0.0;
        for (double d : data) sum += d;
        return sum / data.size();
    }

    //Median. Returns 0.0 for null/empty input.
    public double median(List<Double> data) {
        if (data == null || data.isEmpty()) return 0.0;
        List<Double> copy = new ArrayList<>(data);
        Collections.sort(copy);
        int n = copy.size();
        if (n % 2 == 1) return copy.get(n / 2);
        return (copy.get(n / 2 - 1) + copy.get(n / 2)) / 2.0;
    }


     //Mode (returns a single mode; if multimodal, returns the highest

    public double modeRounded(List<Double> data) {
        if (data == null || data.isEmpty()) return 0.0;
        Map<Integer, Integer> freq = new HashMap<>();
        for (double v : data) {
            int r = (int) Math.round(v);
            freq.put(r, freq.getOrDefault(r, 0) + 1);
        }
        int bestVal = -1, bestFreq = -1;
        for (Map.Entry<Integer, Integer> e : freq.entrySet()) {
            int val = e.getKey(), cnt = e.getValue();
            if (cnt > bestFreq || (cnt == bestFreq && val > bestVal)) {
                bestFreq = cnt; bestVal = val;
            }
        }
        return bestVal < 0 ? 0.0 : (double) bestVal;
    }

    //Population standard deviation (σ). Returns 0.0 for null/empty.
    public double stdDevPopulation(List<Double> data) {
        if (data == null || data.isEmpty()) return 0.0;
        double m = mean(data);
        double sq = 0.0;
        for (double d : data) sq += Math.pow(d - m, 2);
        return Math.sqrt(sq / data.size());
    }

    //Minimum value. Returns 0.0 for null/empty.
    public double min(List<Double> data) {
        if (data == null || data.isEmpty()) return 0.0;
        return Collections.min(data);
    }

    //Maximum value. Returns 0.0 for null/empty.
    public double max(List<Double> data) {
        if (data == null || data.isEmpty()) return 0.0;
        return Collections.max(data);
    }

    //Helper to compute histogram counts for grade bands.
    public int[] gradeBandsCounts(List<Double> data) {
        // bands: A:90–100, B:80–89, C:70–79, D:60–69, F:0–59
        int[] buckets = new int[5];
        if (data == null) return buckets;
        for (double v : data) {
            if (v >= 90) buckets[0]++;
            else if (v >= 80) buckets[1]++;
            else if (v >= 70) buckets[2]++;
            else if (v >= 60) buckets[3]++;
            else buckets[4]++;
        }
        return buckets;
    }

}
