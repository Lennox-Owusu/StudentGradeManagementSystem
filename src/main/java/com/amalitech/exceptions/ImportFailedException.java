
package com.amalitech.exceptions;

public class ImportFailedException extends DomainException {
    public ImportFailedException(String message) { super(message); }
    public ImportFailedException(String message, Throwable cause) { super(message, cause); }
}
