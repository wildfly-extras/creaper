package org.wildfly.extras.creaper.commands.logging;

public enum ConsoleTarget {
    CONSOLE("console"),
    STDOUT("System.out"),
    STDERR("System.err");

    private final String value;

    ConsoleTarget(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
