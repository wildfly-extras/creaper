package org.wildfly.extras.creaper.commands.logging;

public enum LogLevel {
    ALL("ALL"),
    FINEST("FINEST"),
    FINER("FINER"), TRACE("TRACE"),
    FINE("FINE"), DEBUG("DEBUG"),
    CONFIG("CONFIG"),
    INFO("INFO"),
    WARNING("WARNING"), WARN("WARN"),
    SEVERE("SEVERE"), ERROR("ERROR"),
    FATAL("FATAL"),
    OFF("OFF");

    private final String value;

    LogLevel(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
