
package com.amalitech.reporting;

import java.util.List;

/**
 * Computes GPA from percentage grades using a 4.0 scale mapping.
 * SRP: Only GPA-related logic.
 */
public class GPACalculator {


    public String toLetter(double pct) {
        if (pct >= 93) return "A";
        if (pct >= 90) return "A-";
        if (pct >= 87) return "B+";
        if (pct >= 83) return "B";
        if (pct >= 80) return "B-";
        if (pct >= 77) return "C+";
        if (pct >= 73) return "C";
        if (pct >= 70) return "C-";
        if (pct >= 67) return "D+";
        if (pct >= 60) return "D";
        return "F";
    }

    /**
     * Compute GPA (0.0 - 4.0) by converting each percentage to points, then averaging.
     * Returns 0.0 if there are no grades.
     */
    public double computeGPA(List<Double> percentages) {
        if (percentages == null || percentages.isEmpty()) return 0.0;

        double totalPoints = 0.0;
        for (double pct : percentages) {
            totalPoints += toFourPointScale(pct);
        }
        return totalPoints / percentages.size();
    }

    /**
     * Example conversion from % to 4.0 scale (adjust as needed for your institution).
     * You can later swap this logic via a Strategy to support different scales.
     */
    public double toFourPointScale(double pct) {
        if (pct >= 85) return 4.0;
        if (pct >= 80) return 3.7;
        if (pct >= 75) return 3.3;
        if (pct >= 70) return 3.0;
        if (pct >= 65) return 2.7;
        if (pct >= 60) return 2.3;
        if (pct >= 55) return 2.0;
        if (pct >= 50) return 1.7;
        return 0.0;

    }
}
