
package com.amalitech;

import com.amalitech.calculation.StatisticsCalculator;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class StatisticsCalculatorTest {

    private final StatisticsCalculator stats = new StatisticsCalculator();

    @Test
    public void mean_median_mode_std_min_max_work() {
        var data = Arrays.asList(90.0, 80.0, 70.0, 60.0, 90.0);
        assertEquals(78.0, stats.mean(data), 0.0001);
        assertEquals(80.0, stats.median(data), 0.0001);
        assertEquals(90.0, stats.modeRounded(data), 0.0001);
        // Population std dev: sqrt(((12^2 + 2^2 + 8^2 + 18^2 + 12^2)/5)) = sqrt( (144+4+64+324+144)/5 ) = sqrt(680/5)= sqrt(136)=11.66
        assertEquals(11.66, stats.stdDevPopulation(data), 0.05);
        assertEquals(60.0, stats.min(data), 0.0001);
        assertEquals(90.0, stats.max(data), 0.0001);
    }

    @Test
    public void empty_inputs_return_zero() {
        assertEquals(0.0, stats.mean(Collections.emptyList()), 0.0);
        assertEquals(0.0, stats.median(Collections.emptyList()), 0.0);
        assertEquals(0.0, stats.modeRounded(Collections.emptyList()), 0.0);
        assertEquals(0.0, stats.stdDevPopulation(Collections.emptyList()), 0.0);
        assertEquals(0.0, stats.min(Collections.emptyList()), 0.0);
        assertEquals(0.0, stats.max(Collections.emptyList()), 0.0);
    }
}
