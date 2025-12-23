
package com.amalitech.exceptions;

/** Base type for domain-level failures (checked). */
public abstract class DomainException extends Exception {
    public DomainException(String message) { super(message); }
    public DomainException(String message, Throwable cause) { super(message, cause); }
}
