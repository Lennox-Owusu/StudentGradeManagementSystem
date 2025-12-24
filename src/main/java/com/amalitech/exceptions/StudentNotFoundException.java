
package com.amalitech.exceptions;

public class StudentNotFoundException extends DomainException {
    public StudentNotFoundException(String id) {
        super("Student with ID '" + id + "' not found in the system.");
    }
}
