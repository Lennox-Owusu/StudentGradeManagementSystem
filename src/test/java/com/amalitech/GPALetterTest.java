
package com.amalitech;

import com.amalitech.reporting.GPACalculator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GPALetterTest {
    @Test
    public void letter_mapping_matches_thresholds() {
        GPACalculator calc = new GPACalculator();
        assertEquals("A",  calc.toLetter(93));
        assertEquals("A-", calc.toLetter(90));
        assertEquals("B+", calc.toLetter(87));
        assertEquals("F",  calc.toLetter(40));
    }
}
