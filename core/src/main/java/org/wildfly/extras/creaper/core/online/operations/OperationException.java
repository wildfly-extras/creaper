package org.wildfly.extras.creaper.core.online.operations;

/**
 * A general exception for problems related to the {@link Operations} API: failed operation etc.
 */
public class OperationException extends Exception {
    public OperationException(String message) {
        super(message);
    }

    public OperationException(Throwable cause) {
        super(cause);
    }
}
