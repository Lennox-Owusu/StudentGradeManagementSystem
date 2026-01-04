
package com.amalitech.calculation;

import java.util.List;

//Abstraction for grading behavior.
//Implementations decide how averages are computed and what "passing" means.

public interface GradingStrategy {
    double computeAverage(List<Double> grades);
    boolean isPassing(List<Double> grades, double passingThreshold);
}
