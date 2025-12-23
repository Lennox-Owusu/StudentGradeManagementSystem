
package com.amalitech;

import com.amalitech.exceptions.InvalidGradeException;
import com.amalitech.util.Validators;
import org.junit.Test;

public class ValidatorsTest {

    @Test(expected = InvalidGradeException.class)
    public void grade_below_zero_throws() throws InvalidGradeException {
        Validators.requireGradeInRange(-1);
    }

    @Test(expected = InvalidGradeException.class)
    public void grade_above_hundred_throws() throws InvalidGradeException {
        Validators.requireGradeInRange(101);
    }

    @Test
    public void grade_in_range_ok() throws InvalidGradeException {
        Validators.requireGradeInRange(50.0); // no exception
    }
}
