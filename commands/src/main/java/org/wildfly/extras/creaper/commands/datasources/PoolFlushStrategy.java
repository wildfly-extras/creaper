package org.wildfly.extras.creaper.commands.datasources;

public enum PoolFlushStrategy {
    FAILING_CONNECTION_ONLY("FailingConnectionOnly"),
    IDLE_CONNECTIONS("IdleConnections"),
    ENTIRE_POOL("EntirePool");

    private final String value;

    PoolFlushStrategy(final String value) {
        this.value = value;
    }

    String value() {
        return value;
    }
}
