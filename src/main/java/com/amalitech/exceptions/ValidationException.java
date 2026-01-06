
package com.amalitech.exceptions;

//Domain-specific exception for regex validation failures.
public class ValidationException extends DomainException {
    public ValidationException(String message) { super(message); }
    public ValidationException(String message, Throwable cause) { super(message, cause); }
}
