package org.wildfly.extras.creaper.core;

/**
 * An exception that signifies a failure when performing a management command. Management commands can be (and often
 * are) comprised of multiple atomic management operations and if one of them fails, the entire command fails. Problem
 * is that management commands can't be performed in a transactional fashion, so if the command fails, the server is
 * in an unknown state and all bets are off. When you catch this exception, abort everything and report a major fail.
 */
public class CommandFailedException extends Exception {
    public CommandFailedException(String message) {
        super(message);
    }

    public CommandFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandFailedException(Throwable cause) {
        super(cause);
    }
}
