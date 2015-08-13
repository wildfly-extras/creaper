package org.wildfly.extras.creaper.commands.datasources;

public enum TrackStatementType {
    TRUE("true"),
    FALSE("false"),
    NOWARN("nowarn");

    private final String value;

    TrackStatementType(final String value) {
        this.value = value;
    }

    String value() {
        return value;
    }
}
