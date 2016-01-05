package org.wildfly.extras.creaper.commands.logging;

enum HandlerType {

    CONSOLE("console-handler"),
    PERIODIC_ROTATING_FILE("periodic-rotating-file-handler");
    private final String val;

    private HandlerType(String val) {
        this.val = val;
    }

    public String value() {
        return val;
    }
}
