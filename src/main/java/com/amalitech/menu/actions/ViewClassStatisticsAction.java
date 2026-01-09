
package com.amalitech.menu.actions;

import com.amalitech.*;
import com.amalitech.app.AppContext;
import com.amalitech.calculation.StatisticsCalculator;
import com.amalitech.menu.MenuAction;

import java.util.*;

public class ViewClassStatisticsAction implements MenuAction {
    private final AppContext ctx;
    private final StatisticsCalculator STATS = new StatisticsCalculator();

    public ViewClassStatisticsAction(AppContext ctx) { this.ctx = ctx; }

    @Override public String label() { return "View Class Statistics"; }

    @Override public void execute() {
        List<Grade> all = new ArrayList<>();
        for (int i=0;i<ctx.gradeManager.getGradeCount();i++){
            Grade g = ctx.gradeManager.getGradeAt(i);
            if (g!=null) all.add(g);
        }
        if (all.isEmpty()) { System.out.println("(no grades yet)"); return; }

        List<Double> vals = new ArrayList<>(all.size());
        for (Grade g : all) vals.add(g.getGrade());

        int[] bands = STATS.gradeBandsCounts(vals);
        System.out.println("Grade distribution:");
        printBand("90–100% (A):", bands[0], vals.size());
        printBand("80–89%  (B):", bands[1], vals.size());
        printBand("70–79%  (C):", bands[2], vals.size());
        printBand("60–69%  (D):", bands[3], vals.size());
        printBand("0–59%   (F):", bands[4], vals.size());

        double mean = STATS.mean(vals), median = STATS.median(vals),
                mode = STATS.modeRounded(vals), std = STATS.stdDevPopulation(vals);

        System.out.printf("%nMean: %.1f%%  Median: %.1f%%  Mode: %.1f%%  StdDev: %.1f%%%n", mean, median, mode, std);
    }

    private void printBand(String label, int count, int total){
        double pct = total==0?0.0:(count*100.0/total);
        String bar = "█".repeat(Math.max(0,(int)Math.round((count*28.0)/Math.max(1,total))))
                + "░".repeat(Math.max(0,28 - (int)Math.round((count*28.0)/Math.max(1,total))));
        System.out.printf("%-13s %s %4.1f%% (%d)%n", label, bar, pct, count);
    }
}
