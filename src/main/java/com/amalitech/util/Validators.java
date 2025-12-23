
package com.amalitech.util;

import com.amalitech.exceptions.InvalidGradeException;

public final class Validators {
    private Validators() {}

    /** Ensures grade is within [0..100]; otherwise throws. */
    public static void requireGradeInRange(double grade) throws InvalidGradeException {
        if (grade < 0 || grade > 100) {
            throw new InvalidGradeException(grade);
        }
    }

    /** Ensures a required string is nonempty; throws IllegalArgumentException if not. */
    public static void requireNonEmpty(String label, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be empty.");
        }
    }
}
