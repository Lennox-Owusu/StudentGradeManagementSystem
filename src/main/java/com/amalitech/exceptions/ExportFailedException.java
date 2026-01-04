
package com.amalitech.exceptions;

public class ExportFailedException extends DomainException {
    public ExportFailedException(String path, Throwable cause) {
        super("Failed to export file to: " + path, cause);
    }
}
