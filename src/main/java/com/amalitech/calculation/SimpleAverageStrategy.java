
package com.amalitech.calculation;

import java.util.List;

public class SimpleAverageStrategy implements GradingStrategy {

    @Override
    public double computeAverage(List<Double> grades) {
        if (grades == null || grades.isEmpty()) return 0.0;
        double sum = 0.0;
        for (double g : grades) sum += g;
        return sum / grades.size();
    }

    @Override
    public boolean isPassing(List<Double> grades, double passingThreshold) {
        return computeAverage(grades) >= passingThreshold;
    }
}
