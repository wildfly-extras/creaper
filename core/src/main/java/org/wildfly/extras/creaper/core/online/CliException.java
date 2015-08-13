package org.wildfly.extras.creaper.core.online;

/**
 * A general exception for problems related to CLI: syntax error, operation performed through CLI failed etc.
 */
public class CliException extends Exception {
    public CliException(String message) {
        super(message);
    }

    public CliException(Throwable cause) {
        super(cause);
    }
}
