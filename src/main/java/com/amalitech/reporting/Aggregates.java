
package com.amalitech.reporting;

import java.io.Serializable;

public final class Aggregates implements Serializable {
    private final int totalGrades;
    private final double coreAverage;
    private final double electiveAverage;
    private final double overallAverage;

    public Aggregates(int totalGrades, double coreAverage, double electiveAverage, double overallAverage) {
        this.totalGrades = totalGrades;
        this.coreAverage = coreAverage;
        this.electiveAverage = electiveAverage;
        this.overallAverage = overallAverage;
    }

    public int getTotalGrades() { return totalGrades; }
    public double getCoreAverage() { return coreAverage; }
    public double getElectiveAverage() { return electiveAverage; }
    public double getOverallAverage() { return overallAverage; }
}

