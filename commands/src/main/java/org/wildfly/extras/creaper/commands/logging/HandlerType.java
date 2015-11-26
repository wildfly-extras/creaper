package org.wildfly.extras.creaper.commands.logging;

/**
 * @author Ivan Straka istraka@redhat.com
 */
public enum HandlerType {

    CONSOLE("console-handler"),
    PERIODIC("periodic-rotating-file-handler");
    private final String val;

    /**
     * @param val
     */
    private HandlerType(final String val) {
        this.val = val;
    }

    public String value() {
        return val;
    }
};
