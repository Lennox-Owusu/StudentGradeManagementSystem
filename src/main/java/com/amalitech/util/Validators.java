
package com.amalitech.util;

import com.amalitech.exceptions.InvalidGradeException;

public final class Validators {
    private Validators() {}

    //Guards: 0 <= grade <= 100; throws InvalidGradeException otherwise.
    public static void requireGradeInRange(double grade) throws InvalidGradeException {
        if (Double.isNaN(grade) || grade < 0.0 || grade > 100.0) {
            throw new InvalidGradeException(grade);
        }
    }

    //Trims value; ensures non-empty after trim.
    public static String requireNotBlank(String label, String value) throws IllegalArgumentException {
        String v = (value == null) ? "" : value.trim();
        if (v.isEmpty()) {
            throw new IllegalArgumentException(label + " must not be empty.");
        }
        return v;
    }

}
