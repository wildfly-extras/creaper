package org.wildfly.extras.creaper.commands.logging;

public enum LogHandlerType {
    CONSOLE("console-handler"),
    PERIODIC_ROTATING_FILE("periodic-rotating-file-handler");

    private final String value;

    LogHandlerType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
