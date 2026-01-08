
package com.amalitech.exceptions;

public class InvalidGradeException extends DomainException {
    private final double value;
    public InvalidGradeException(double value) {
        super("\nERROR: InvalidGradeException \nGrade must be between 0 and 100. Provided: " + value);
        this.value = value;
    }
    public double getValue() { return value; }
}
