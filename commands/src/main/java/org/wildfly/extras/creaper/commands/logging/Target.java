package org.wildfly.extras.creaper.commands.logging;

public enum Target {

    CONSOLE("console"),
    STDOUT("System.out"),
    STDERR("System.err");

    private final String val;

    private Target(String val) {
        this.val = val;
    }

    public String value() {
        return val;
    }
}
