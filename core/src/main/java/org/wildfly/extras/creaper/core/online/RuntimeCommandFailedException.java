package org.wildfly.extras.creaper.core.online;

import org.wildfly.extras.creaper.core.CommandFailedException;

final class RuntimeCommandFailedException extends RuntimeException {
    final CommandFailedException original;

    RuntimeCommandFailedException(CommandFailedException original) {
        this.original = original;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        // no need to pay for it, this exception only serves as a container for the original CommandFailedException
        // that will be thrown at the right point in time
        return this;
    }
}
