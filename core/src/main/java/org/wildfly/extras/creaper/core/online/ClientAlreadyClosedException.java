package org.wildfly.extras.creaper.core.online;

public final class ClientAlreadyClosedException extends RuntimeException {
    public ClientAlreadyClosedException(ThisIsWhereTheClientWasClosed closedAt) {
        super("The client is already closed. Creaper captured the moment when it was closed, "
                + "see the cause of this exception.", closedAt);
    }
}
