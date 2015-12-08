package org.wildfly.extras.creaper.commands.logging;

public enum Level {

    ALL("ALL"),
    FINEST("FINEST"), TRACE("TRACE"),
    FINER("FINER"),
    FINE("FINE"), DEBUG("DEBUG"),
    CONFIG("CONFIG"),
    INFO("INFO"),
    WARN("WARN"), WARNING("WARNING"),
    ERROR("ERROR"), SEVERE("SEVERE"),
    FATAL("FATAL"),
    OFF("OFF");

    private final String val;

    private Level(final String val) {
        this.val = val;
    }

    public String value() {
        return val;
    }
}
