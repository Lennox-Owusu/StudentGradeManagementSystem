
package com.amalitech.exceptions;

public class JsonFormatException extends DomainException {
    public JsonFormatException(String message) { super(message); }
    public JsonFormatException(String message, Throwable cause) { super(message, cause); }
}
